package com.silverhetch.artemis

import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.silverhetch.aura.view.activity.Fullscreen
import com.silverhetch.aura.view.activity.brightness.InAppBrightness
import kotlinx.android.synthetic.main.page_video_player.*
import kotlinx.coroutines.*
import kotlin.math.abs

/**
 * Entry Activity of Artemis.
 */
class VideoPlayerActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob() + errorHandler) {
    companion object {
        private const val URI_DEFAULT =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
        val errorHandler = CoroutineExceptionHandler { _, error ->
            error.printStackTrace()
        }
    }

    private var player: MediaPlayer = MediaPlayer()
    private val uri by lazy {
        intent?.getStringExtra("uri_media") ?: URI_DEFAULT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_video_player)
        Fullscreen(this).value()
        playerView()
        attachDisplay()
        touchEvent()
        statusPolling()
        launch { play() }
    }

    private fun attachDisplay() {
        videoPlayer_display.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {}

            override fun surfaceCreated(holder: SurfaceHolder?) {
                player.setDisplay(videoPlayer_display.holder)
                player.setOnVideoSizeChangedListener { _, width, height ->
                    videoPlayer_display.layoutParams =
                        (videoPlayer_display.layoutParams as ConstraintLayout.LayoutParams).apply {
                            dimensionRatio = "${width}:${height}"
                        }
                }
            }
        })
    }

    private fun touchEvent() {
        videoPlayer_display.setOnTouchListener(object : View.OnTouchListener {
            private var pX = 0
            private var pY = 0
            private var dY = 0f
            private var dX = 0f
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.actionMasked) {
                    ACTION_DOWN -> {
                        pX = event.x.toInt()
                        pY = event.y.toInt()
                    }
                    ACTION_MOVE -> {
                        dY = pY - event.y
                        dX = pX - event.x
                        if (abs(dY) > 10) {
                            if (event.x > (videoPlayer_display.width / 2f)) {
                                volumeAdjustment(dY)
                            } else {
                                brightnessAdjustment(dY)
                            }
                        }
                        if (abs(dX) > 10) {
                            seekToNextBlock(dX)
                        }
                        pX = event.x.toInt()
                        pY = event.y.toInt()
                    }
                    ACTION_UP -> {
                        if (pX - event.x < 10 && pY - event.y < 10) {
                            if (player.isPlaying) {
                                player.pause()
                            } else {
                                player.start()
                            }
                            v?.performClick()
                        }
                    }
                    else -> {
                    }
                }
                return true
            }
        })
    }

    private fun seekToNextBlock(fl: Float) {
        if (abs(fl) < 10) {
            return
        }
        player.seekTo(
            player.currentPosition + if (fl > 0) {
                -1000
            } else {
                1000
            }
        )
    }

    private fun volumeAdjustment(fl: Float) {
        val mgr =
            applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        if (fl > 0) {
            mgr.adjustVolume(ADJUST_RAISE, FLAG_PLAY_SOUND)
        } else {
            mgr.adjustVolume(ADJUST_LOWER, FLAG_PLAY_SOUND)
        }
    }

    private fun brightnessAdjustment(fl: Float) {
        InAppBrightness(this@VideoPlayerActivity).apply {
            if (fl > 0) {
                set(value() + 0.2f)
            } else {
                set(value() - 0.2f)
            }
        }
    }

    private fun playerView() {
        player.setOnBufferingUpdateListener { _, percent ->
            videoPlayer_progress.secondaryProgress =
                (videoPlayer_progress.max * (percent / 100f)).toInt()
        }
    }

    private fun statusPolling() = launch {
        while (isActive) {
            if (player.isPlaying || player.currentPosition > 0) {
                videoPlayer_progress.max = player.duration
                videoPlayer_progress.progress = player.currentPosition
            }

            println("Is playing " + player.isPlaying)
            delay(200)
        }
    }

    private suspend fun play() = withContext(Dispatchers.IO) {
        player.reset()
        player.setDataSource(uri)
        player.prepare()
        player.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        player.stop()
        player.reset()
        player.release()
        cancel()
    }

}