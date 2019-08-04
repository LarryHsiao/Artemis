package com.silverhetch.artemis

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Point
import android.os.Bundle
import android.os.IBinder
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.silverhetch.aura.media.AuraMediaPlayer
import kotlinx.android.synthetic.main.activity_player.*

/**
 * Player
 */
class PlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: AuraMediaPlayer
    private val lifecycleOwner = this
    private var pendingPlay = false
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mediaPlayer = (service as MediaPlayerService.Binder).mediaPlayer()
            if (pendingPlay) {
                mediaPlayer.load(intent?.data.toString())
                mediaPlayer.playback().play()
            }
            mediaPlayer.attachDisplay(mediaPlayer_display.holder)
            mediaPlayer.videoSize().observe(lifecycleOwner, Observer {
                mediaPlayer_display.layoutParams = mediaPlayer_display.layoutParams.apply {
                    width = Point().run {
                        windowManager.defaultDisplay.getSize(this)
                        x
                    }
                    height = ((it.y.toFloat() / it.x.toFloat()) * width).toInt()
                }
            })
            mediaPlayer.state().observe(lifecycleOwner, Observer {
                mediaPlayer_progress.secondaryProgress = ((it.buffered() / 100f) * mediaPlayer_progress.max).toInt()
                mediaPlayer_progress.progress = it.progress()
                mediaPlayer_progress.max = it.duration()
                updateButton(it.isPlaying())
                if (it.completed()) {
                    mediaPlayer.playback().seekTo(0)
                    mediaPlayer.playback().pause()
                }

            })
            mediaPlayer_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.playback().seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })

            mediaPlayer_play.setOnClickListener { mediaPlayer.playback().play() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        pendingPlay = savedInstanceState == null && intent?.data != null

        mediaPlayer_display.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                startService(Intent(mediaPlayer_display.context, MediaPlayerService::class.java))
                bindService(
                    Intent(mediaPlayer_display.context, MediaPlayerService::class.java),
                    connection,
                    Context.BIND_AUTO_CREATE
                )
            }
        })

        mediaPlayer_open.setOnClickListener { openTarget() }
    }

    private fun updateButton(playing: Boolean) {
        if (playing) {
            mediaPlayer_play.setImageResource(android.R.drawable.ic_media_pause)
            mediaPlayer_play.setOnClickListener {
                mediaPlayer.playback().pause()
                mediaPlayer_play.setImageResource(android.R.drawable.ic_media_play)
            }
        } else {
            mediaPlayer_play.setImageResource(android.R.drawable.ic_media_play)
            mediaPlayer_play.setOnClickListener {
                mediaPlayer.playback().play()
                mediaPlayer_play.setImageResource(android.R.drawable.ic_media_pause)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        unbindService(connection)
    }

    private fun openTarget() {
        val inputView = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(this)
            .setView(inputView)
            .setPositiveButton(R.string.app_confirm) { _, _ ->
                mediaPlayer.load(inputView.text.toString())
                mediaPlayer.attachDisplay(mediaPlayer_display.holder)
                mediaPlayer.playback().play()
            }
            .setNegativeButton(R.string.app_cancel) { _, _ -> }
            .show()
    }

}
