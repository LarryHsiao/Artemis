package com.silverhetch.artemis

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.SurfaceHolder
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.silverhetch.aura.view.activity.Fullscreen
import com.silverhetch.aura.view.activity.brightness.InAppBrightness
import kotlinx.android.synthetic.main.page_video_player.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlin.math.abs

/**
 * Entry Activity of Artemis.
 */
class VideoPlayerActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Main + SupervisorJob() + errorHandler),
    SurfaceHolder.Callback {
    companion object {
        const val REQUEST_CODE_PICK_FILE = 1000;
        const val SHOWN_MILLIS = 3000
        val errorHandler = CoroutineExceptionHandler { _, error ->
            error.printStackTrace()
        }
    }

    private var player: MediaPlayer = MediaPlayer()
    private var overlayShownMillis = 0
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = intent.data
        setContentView(R.layout.page_video_player)
        playerView()
        touchEvent()
        statusPolling()
        launch { play() }
    }

    override fun onResume() {
        super.onResume()
        Fullscreen(this).value()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    private fun togglePlayerView(show: Boolean) {
        if (show) {
            videoPlayer_logo.visibility = VISIBLE
            videoPlayer_logo.setOnClickListener { selectVideo() }
            videoPlayer_overlayBottom.visibility = GONE
            videoPlayer_options.visibility = GONE
        } else {
            videoPlayer_logo.visibility = GONE
            videoPlayer_overlayBottom.visibility = VISIBLE
            videoPlayer_options.visibility = VISIBLE
        }
    }

    private fun selectVideo() {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "video/*"
            },
            REQUEST_CODE_PICK_FILE
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            togglePlayerView(false)
            uri = data?.data
            launch { play() }
        }
    }

    private fun touchEvent() {
        videoPlayer_root.setOnTouchListener(object : View.OnTouchListener {
            private var pX = 0
            private var pY = 0
            private var dY = 0f
            private var dX = 0f
            private var moving = false
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                videoPlayer_optionMenu.visibility = GONE
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
                            if (overlayShownMillis > SHOWN_MILLIS) {
                                overlayShownMillis = 0
                            } else {
                                overlayShownMillis = SHOWN_MILLIS
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
        videoPlayer_playbackBtn.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.start()
            }
        }
        videoPlayer_options.setOnClickListener {
            overlayShownMillis = 0
            if (videoPlayer_optionMenu.visibility == VISIBLE) {
                videoPlayer_optionMenu.visibility = GONE
            } else {
                videoPlayer_optionMenu.visibility = VISIBLE
            }
        }
        videoPlayer_optionMenu_open.setOnClickListener {
            videoPlayer_optionMenu.visibility = GONE
            selectVideo()
        }
    }

    private fun panelToggle(show: Boolean) {
        if (videoPlayer_overlayTop.alpha != 1f && videoPlayer_overlayTop.alpha != 0f) {
            return
        }
        val newAlpha = if (show) 1f else 0f
        if (videoPlayer_overlayTop.alpha == newAlpha) {
            if (newAlpha == 0f) {
                videoPlayer_overlayTop.visibility = GONE
                videoPlayer_overlayBottom.visibility = GONE
            }
            return
        }
        if (newAlpha == 1f) {
            videoPlayer_overlayTop.visibility = VISIBLE
            videoPlayer_overlayBottom.visibility = VISIBLE
        }
        videoPlayer_overlayTop.animate().apply {
            alpha(newAlpha)
        }
        videoPlayer_overlayBottom.animate().apply {
            alpha(newAlpha)
        }
    }

    private fun statusPolling() = launch {
        var time: IntArray
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
                    R.drawable.ic_pause
                } else {
                    R.drawable.ic_play
                }
            )
            if (player.isPlaying) {
                overlayShownMillis += 200
            } else {
                overlayShownMillis = 0
            }
            panelToggle(overlayShownMillis < SHOWN_MILLIS)
            delay(200)
        }
    }

    private fun splitTime(ms: Int): IntArray {
        var ss = ms / 1000
        val mm = ss / 60
        ss -= mm * 60
        return intArrayOf(mm, ss)
    }

    private suspend fun play() {
        uri?.let { play(it) } ?: togglePlayerView(true)
    }

    private suspend fun play(uri: Uri) = withContext(IO) {
        player.pause()
        player.stop()
        player.reset()
        player.release()
        player = MediaPlayer()
        player.setDataSource(this@VideoPlayerActivity, uri)
        player.prepare()
        player.start()
        player.setOnVideoSizeChangedListener { _, width, height ->
            videoPlayer_display.layoutParams =
                (videoPlayer_display.layoutParams as ConstraintLayout.LayoutParams).apply {
                    dimensionRatio = "${width}:${height}"
                }
        }
        player.setOnBufferingUpdateListener { _, percent ->
            videoPlayer_progress.secondaryProgress =
                (videoPlayer_progress.max * (percent / 100f)).toInt()
        }
        if (videoPlayer_display.holder.surface.isValid) {
            player.setDisplay(videoPlayer_display.holder)
        }
        videoPlayer_display.holder.addCallback(this@VideoPlayerActivity)
        launch(Main) { updateTitle(uri) }
    }

    private fun updateTitle(uri: Uri) {
        videoPlayer_mediaName.text =
            if (uri.toString().startsWith("content")) {
                "(?!(.*\\/(?=.+\$))).*".toRegex()
                    .find(uri.lastPathSegment ?: "")?.value
            } else {
                uri.toString()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPlayer_display.holder.removeCallback(this)
        player.stop()
        player.reset()
        player.release()
        cancel()
    }

    override fun surfaceChanged(
        holder: SurfaceHolder?,
        format: Int,
        width: Int,
        height: Int
    ) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        player.setDisplay(null)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        player.setDisplay(videoPlayer_display.holder)
    }
}