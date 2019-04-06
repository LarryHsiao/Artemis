package com.silverhetch.artemis

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.silverhetch.aura.media.AuraMediaPlayer
import com.silverhetch.aura.media.AuraMediaPlayerImpl
import androidx.media.app.NotificationCompat as MediaNotificationCompat

/**
 * Media player service for general purpose media playback.
 */
class MediaPlayerService : LifecycleService() {
    companion object {
        private const val ID_CHANNEL = "MediaPlayer"
    }

    private val binder = Binder()
    private lateinit var mediaPlayer: AuraMediaPlayer

    override fun onCreate() {
        super.onCreate()
//        val session = MediaSessionCompat(this, ID_CHANNEL)
//        session.isActive = true
//        session.controller.registerCallback(object: MediaControllerCompat.Callback(){
//
//        })
        mediaPlayer = AuraMediaPlayerImpl(this)



//        NotificationManagerCompat.from(this).notify(
//            0,
//            NotificationCompat.Builder(this, ID_CHANNEL)
//                .setStyle(
//                    MediaNotificationCompat.MediaStyle()
//                        .setMediaSession(session.sessionToken)
//                )
//                .setSmallIcon(android.R.drawable.stat_sys_warning)
//                .setLargeIcon(BitmapFactory.decodeResource(resources, android.R.drawable.stat_sys_warning))
//                .build()
//        )

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

