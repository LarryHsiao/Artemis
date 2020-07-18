package com.silverhetch.artemis

import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.SurfaceHolder
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.silverhetch.aura.view.activity.Fullscreen
import com.silverhetch.aura.view.activity.brightness.InAppBrightness
import kotlinx.android.synthetic.main.page_video_player.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlin.math.abs

/**
 * Entry Activity of Artemis.
 */
class VideoPlayerActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main + SupervisorJob() + errorHandler) {
    companion object {
        val errorHandler = CoroutineExceptionHandler { _, error ->
            error.printStackTrace()
        }
    }

    private var player: MediaPlayer = MediaPlayer()
    private val uri by lazy { intent.data }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_video_player)
        Fullscreen(this).value()
        playerView()
        attachDisplay()
        touchEvent()
        statusPolling()
        launch {
            uri?.let { play(it) }
        }
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
        videoPlayer_root.setOnTouchListener(object : View.OnTouchListener {
            private var pX = 0
            private var pY = 0
            private var dY = 0f
            private var dX = 0f
            private var moving = false
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                dY = pY - (event?.y ?: 0f)
                dX = pX - (event?.x ?: 0f)
                when (event?.actionMasked) {
                    ACTION_DOWN -> {
                        moving = false
                    }
                    ACTION_MOVE -> {
                        if (abs(dY) > 10) {
                            if (event.x > (videoPlayer_root.width / 2f)) {
                                volumeAdjustment(dY)
                            } else {
                                brightnessAdjustment(dY)
                            }
                            moving = true
                        }
                        if (abs(dX) > 10) {
                            seekToNextBlock(dX)
                            moving = true
                        }
                    }
                    ACTION_UP -> {
                        if (!moving) {
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
                pX = event?.x?.toInt() ?: 0
                pY = event?.y?.toInt() ?: 0
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
        videoPlayer_progress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    player.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        videoPlayer_backBtn.setOnClickListener { onBackPressed() }
    }

    private fun panelToggle(show: Boolean) {
        if (videoPlayer_overlayTop.alpha != 1f && videoPlayer_overlayTop.alpha != 0f) {
            return
        }
        videoPlayer_overlayTop.animate().apply {
            alpha(if (show) 1f else 0f)
        }
        videoPlayer_overlayBottom.animate().apply {
            alpha(if (show) 1f else 0f)
        }
    }

    private fun statusPolling() = launch {
        var time: IntArray
        var overlayShownMillis = 0
        while (isActive) {
            if (player.isPlaying || player.currentPosition > 0) {
                videoPlayer_progress.max = player.duration
                videoPlayer_progress.progress = player.currentPosition
                time = splitTime(player.currentPosition)
                videoPlayer_progressText.text =
                    "${String.format(
                        "%02d",
                        time[0]
                    )}:${String.format(
                        "%02d",
                        time[1]
                    )}"
                time = splitTime(player.duration)
                videoPlayer_durationText.text =
                    "${String.format(
                        "%02d",
                        time[0]
                    )}:${String.format(
                        "%02d",
                        time[1]
                    )}"
            }
            videoPlayer_playbackBtn.setImageResource(
                if (player.isPlaying) {
                    android.R.drawable.ic_media_pause
                } else {
                    android.R.drawable.ic_media_play
                }
            )
            if (player.isPlaying) {
                overlayShownMillis += 200
            } else {
                overlayShownMillis = 0
            }
            panelToggle(overlayShownMillis < 1500)
            delay(200)
        }
    }

    private fun splitTime(ms: Int): IntArray {
        var ss = ms / 1000
        val mm = ss / 60
        ss -= mm * 60
        return intArrayOf(mm, ss)
    }

    private suspend fun play(uri: Uri) = withContext(IO) {
        player.reset()
        player.setDataSource(this@VideoPlayerActivity, uri)
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