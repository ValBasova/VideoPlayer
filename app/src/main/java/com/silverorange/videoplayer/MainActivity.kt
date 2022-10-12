package com.silverorange.videoplayer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.silverorange.videoplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var videoList: List<Video>

    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        videoList = listOf()

        uiScope.launch {
            fetchVideos()
        }
    }

    private suspend fun fetchVideos() {
        try {
            videoList = VideoApi.retrofitMoshiService.getAllVideos()

            binding.video = videoList[0]
            binding.invalidateAll()
        } catch (e: Exception) {
            Log.e("GetVideos API error", e.message.toString())
            videoList = listOf()
        }
    }
}