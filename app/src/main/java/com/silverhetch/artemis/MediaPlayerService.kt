package com.silverhetch.artemis

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.silverhetch.aura.media.AuraMediaPlayer
import com.silverhetch.aura.media.AuraMediaPlayerImpl

/**
 * Media player service for general purpose media playback.
 */
class MediaPlayerService : Service() {
    private val binder = Binder()
    private lateinit var mediaPlayer: AuraMediaPlayer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            mediaPlayer.load(it.data.toString())
            mediaPlayer.playback().play()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = AuraMediaPlayerImpl(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class Binder : android.os.Binder() {
        fun mediaPlayer(): AuraMediaPlayer {
            return mediaPlayer
        }
    }
}

