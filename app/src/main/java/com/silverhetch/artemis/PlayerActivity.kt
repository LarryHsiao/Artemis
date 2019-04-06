package com.silverhetch.artemis

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Point
import android.os.Bundle
import android.os.IBinder
import android.view.ContextMenu
import android.view.SurfaceHolder
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.silverhetch.aura.media.AuraMediaPlayer
import kotlinx.android.synthetic.main.activity_main.*

class PlayerActivity : AppCompatActivity(), ServiceConnection, SurfaceHolder.Callback {
    private lateinit var mediaPlayer: AuraMediaPlayer;
    private var pendingPlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pendingPlay = savedInstanceState == null && intent?.data != null

        mediaPlayer_display.holder.addCallback(this)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
        unbindService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        startService(Intent(this@PlayerActivity, MediaPlayerService::class.java))
        bindService(
            Intent(this@PlayerActivity, MediaPlayerService::class.java),
            this@PlayerActivity,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mediaPlayer = (service as MediaPlayerService.Binder).mediaPlayer()
        if (pendingPlay) {
            pendingPlay = false
            mediaPlayer.load(intent?.data.toString())
            mediaPlayer.play()
        }
        mediaPlayer.attachDisplay(mediaPlayer_display.holder)
        mediaPlayer.videoSize().observe(this, Observer {
            mediaPlayer_display.layoutParams = mediaPlayer_display.layoutParams.apply {
                width = Point().run {
                    windowManager.defaultDisplay.getSize(this)
                    x
                }
                height = ((it.y.toFloat() / it.x.toFloat()) * width).toInt()
            }
        })
        mediaPlayer.buffered().observe(this, Observer {
            mediaPlayer_progress.secondaryProgress = ((it / 100f) * mediaPlayer_progress.max).toInt()
        })
        mediaPlayer.progress().observe(this, Observer {
            runOnUiThread {
                mediaPlayer_progress.progress = it
            }
        })
        mediaPlayer.duration().observe(this, Observer {
            mediaPlayer_progress.max = it
        })
        mediaPlayer_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        mediaPlayer_play.setOnClickListener { mediaPlayer.play() }
        mediaPlayer_pause.setOnClickListener { mediaPlayer.pause() }
    }
}
