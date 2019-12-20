package com.silverhetch.artemis

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.silverhetch.artemis.media.ConstMedia
import com.silverhetch.artemis.media.Media
import com.silverhetch.artemis.media.MediaByUri
import com.silverhetch.artemis.media.QueriedMedia
import com.silverhetch.artemis.player.PlayerWrapper
import com.silverhetch.aura.media.AuraMediaPlayer
import com.silverhetch.aura.media.AuraMediaPlayerImpl
import com.silverhetch.aura.media.State


/**
 * Media player service for general purpose media playback.
 */
class MediaPlayerService : LifecycleService() {
    companion object {
        private const val REQUEST_CODE_PLAYBACK = 1000
        private const val REQUEST_CODE_CLOSE = 1001
        private const val NOTIFICATION_ID = 1000;
        private const val CHANNEL_ID_PLAYER = "ArtemisPlayer"
        private const val ARG_CONTROL = "ARG_CONTROL"
        private const val CONTROL_PLAYBACK = "CONTROL_PLAYBACK"
        private const val CONTROL_CLOSE = "CONTROL_CLOSE"
    }

    private val binder = Binder()
    private lateinit var mediaPlayer: AuraMediaPlayer
    private var currentMedia: Media = ConstMedia("", "");

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        intent?.let { intent ->
            intent.data?.let {
                mediaPlayer.load(it.toString())
                mediaPlayer.playback().play()
            }
            if (intent.hasExtra(ARG_CONTROL)) {
                when (intent.getStringExtra(ARG_CONTROL)) {
                    CONTROL_CLOSE -> {
                        shutdown()
                        return Service.START_NOT_STICKY
                    }
                    CONTROL_PLAYBACK -> {
                        val state = mediaPlayer.state().value
                        if (state?.isPlaying() == true) {
                            mediaPlayer.playback().pause()
                        } else {
                            mediaPlayer.playback().play()
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = object : PlayerWrapper(AuraMediaPlayerImpl(this)) {
            override fun load(uri: String) {
                super.load(uri)
                if (uri != currentMedia.uri()) {
                    currentMedia = QueriedMedia(
                        MediaByUri(
                            this@MediaPlayerService,
                            Uri.parse(uri)
                        )
                    ).value()
                }
            }
        }
        mediaPlayer.state().observe(this, Observer {
            if (it.completed()){
                mediaPlayer.playback().pause()
            }
            val manager = NotificationManagerCompat.from(this)
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, CHANNEL_ID_PLAYER)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(false)
                    .setCustomContentView(playerView(it))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            if (SDK_INT >= O) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID_PLAYER,
                        CHANNEL_ID_PLAYER,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
            manager.notify(NOTIFICATION_ID, builder.build())
        })
    }

    private fun playerView(state: State): RemoteViews {
        return RemoteViews(
            packageName,
            R.layout.notification_player
        ).also {
            it.setTextViewText(
                R.id.notificationPlayer_title,
                currentMedia.title()
            )
            it.setOnClickPendingIntent(
                R.id.notificationPlayer_close,
                PendingIntent.getService(
                    this,
                    REQUEST_CODE_CLOSE,
                    Intent(
                        this,
                        MediaPlayerService::class.java
                    ).apply { putExtra(ARG_CONTROL, CONTROL_CLOSE) },
                    FLAG_CANCEL_CURRENT
                )
            )
            it.setOnClickPendingIntent(
                R.id.notificationPlayer_playback,
                PendingIntent.getService(
                    this,
                    REQUEST_CODE_PLAYBACK,
                    Intent(
                        this,
                        MediaPlayerService::class.java
                    ).apply { putExtra(ARG_CONTROL, CONTROL_PLAYBACK) },
                    FLAG_CANCEL_CURRENT
                )
            )
            it.setImageViewResource(
                R.id.notificationPlayer_playback,
                if (state.isPlaying()) {
                    android.R.drawable.ic_media_pause
                } else {
                    android.R.drawable.ic_media_play
                }
            )
        }
    }

    private fun shutdown() {
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return binder
    }

    inner class Binder : android.os.Binder() {
        fun mediaPlayer(): AuraMediaPlayer {
            return mediaPlayer
        }
    }
}

