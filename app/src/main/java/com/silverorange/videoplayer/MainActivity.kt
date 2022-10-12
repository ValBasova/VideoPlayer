package com.silverorange.videoplayer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import com.silverorange.videoplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val KEY_PLAY_WHEN_READY = "play_when_ready_key"
const val KEY_CURRENT_ITEM = "current_item_key"
const val KEY_PLAYBACK_POSITION = "playback_position_key"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var videoList: List<Video>

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    private var player: ExoPlayer? = null

    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        videoList = listOf()

        uiScope.launch {
            fetchVideos()
        }

        if (savedInstanceState != null) {
            // Get all the state information from the bundle, set it
            playWhenReady = savedInstanceState.getBoolean(KEY_PLAYBACK_POSITION)
            currentItem = savedInstanceState.getInt(KEY_CURRENT_ITEM)
            playbackPosition = savedInstanceState.getLong(KEY_PLAYBACK_POSITION)
        }
    }

    private suspend fun fetchVideos() {
        try {
            videoList = VideoApi.retrofitMoshiService.getAllVideos()
            initializePlayer()
            binding.video = videoList[0]
            binding.invalidateAll()
        } catch (e: Exception) {
            Log.e("GetVideos API error", e.message.toString())
            videoList = listOf()
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(applicationContext).build()
        binding.playerView.player = player

        videoList.let {
            for (videoItem in it) {
                val mediaItem = MediaItem.fromUri(videoItem.url)
                player?.addMediaItem(mediaItem)
            }
        }

        player?.playWhenReady = playWhenReady
        player?.seekTo(currentItem, playbackPosition)
        player?.prepare()
        player?.play()

        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.e("Player error", error.message.toString())

                Toast.makeText(
                    applicationContext,
                    "Access to the requested resource is forbidden",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_PLAY_WHEN_READY, playWhenReady)
        outState.putInt(KEY_CURRENT_ITEM, currentItem)
        outState.putLong(KEY_PLAYBACK_POSITION, playbackPosition)
        super.onSaveInstanceState(outState)
    }
}