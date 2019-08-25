package com.silverhetch.artemis

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.SurfaceHolder
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.silverhetch.aura.intent.ChooserIntent
import com.silverhetch.aura.intent.PickerIntent
import com.silverhetch.aura.media.AuraMediaPlayer
import kotlinx.android.synthetic.main.activity_player.*

/**
 * Player
 */
class PlayerActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_VIDEO_PICK = 1000
    }

    private val handler = Handler()
    private lateinit var mediaPlayer: AuraMediaPlayer
    private val lifecycleOwner = this
    private var showingControl = false
    private var pendingPlay = false
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mediaPlayer = (service as MediaPlayerService.Binder).mediaPlayer()
            if (pendingPlay) {
                play(intent?.data.toString())
            }
            attachDisplay()
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
    private val controlVisibility: Runnable = Runnable {
        if (!showingControl) {
            hideControl()
        }
    }

    private fun play(uri: String) {
        mediaPlayer.load(uri)
        mediaPlayer.playback().play()
    }

    private fun attachDisplay() {
        if (mediaPlayer_display.holder.surface.isValid) {
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
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        pendingPlay = savedInstanceState == null && intent?.data != null

        mediaPlayer_display.setOnTouchListener { v, event ->
            showingControl = true
            handler.postDelayed({
                showingControl = false
            },3000)
            showControl()
            false
        }
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
                attachDisplay()
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
            handler.postDelayed(controlVisibility, 3000)
        } else {
            mediaPlayer_play.setImageResource(android.R.drawable.ic_media_play)
            mediaPlayer_play.setOnClickListener {
                mediaPlayer.playback().play()
                mediaPlayer_play.setImageResource(android.R.drawable.ic_media_pause)
            }
            handler.removeCallbacks(controlVisibility)
            showControl()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        try {
            unbindService(connection)
        } catch (ignore: IllegalArgumentException) {
            ignore.printStackTrace()
        }
    }

    private fun openTarget() {
        startActivityForResult(
            ChooserIntent(
                this,
                getString(R.string.open_from),
                PickerIntent("video/*", "audio/*").value(),
                Intent(this, UriInputActivity::class.java)
            ).value(),
            REQUEST_CODE_VIDEO_PICK
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_VIDEO_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                play(it.toString())
                attachDisplay()
            }
        }
    }

    private fun showControl() {
        mediaPlayer_progress.animate().cancel()
        mediaPlayer_progress.animate()
            .alpha(1.0f)
            .start()


        mediaPlayer_controlPanel.animate().cancel()
        mediaPlayer_controlPanel.animate()
            .alpha(1.0f)
            .start()
    }

    private fun hideControl() {
        mediaPlayer_progress.animate().cancel()
        mediaPlayer_progress.animate()
            .alpha(0.0f)
            .start()

        mediaPlayer_controlPanel.animate().cancel()
        mediaPlayer_controlPanel.animate()
            .alpha(0.0f)
            .start()
    }
}
