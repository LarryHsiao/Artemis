package com.silverhetch.artemis

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.silverhetch.aura.media.MediaPlayerService

/**
 *
 */
class MainActivity : AppCompatActivity(), ServiceConnection {
    override fun onServiceDisconnected(name: ComponentName?) {

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        (service as MediaPlayerService.Binder).mediaPlayer().run {
            load("https://d2qguwbxlx1sbt.cloudfront.net/TextInMotion-VideoSample-1080p.mp4")
            this.attachDisplay(findViewById<SurfaceView>(R.id.main_surfaceView).holder)
            Handler().postDelayed({play()}, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindService(
            Intent(this, MediaPlayerService::class.java),
            this,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }
}
