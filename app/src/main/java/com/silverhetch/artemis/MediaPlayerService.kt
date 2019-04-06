package com.silverhetch.artemis

import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.silverhetch.aura.media.AuraMediaPlayer
import com.silverhetch.aura.media.AuraMediaPlayerImpl

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
        mediaPlayer = AuraMediaPlayerImpl(this)
        mediaPlayer.duration().observe(this, Observer {

        })
        NotificationCompat.Builder(this, ID_CHANNEL)
            .build()
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

