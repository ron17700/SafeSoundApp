package com.example.safesound.ui.records_list

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.safesound.R
import com.example.safesound.data.records.Chunk
import com.example.safesound.databinding.FragmentChunkDetailBinding
import com.example.safesound.network.NetworkModule.BASE_URL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ChunkDetailFragment : Fragment() {

    private var _binding: FragmentChunkDetailBinding? = null
    private val binding get() = _binding!!
    private var seekerJob: Job? = null

    private val chunk: Chunk by lazy {
        Chunk(
            _id = requireArguments().getString("chunkId") ?: "",
            name = requireArguments().getString("chunkName") ?: "",
            audioFilePath = requireArguments().getString("audioFilePath") ?: "",
            startTime = "",
            endTime = "",
            chunkClass = "",
            summary = requireArguments().getString("summary") ?: ""
        )
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChunkDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        binding.buttonPlayPause.setOnClickListener {
            togglePlayPause()
        }

        binding.buttonStop.setOnClickListener {
            stopAudio()
        }

        binding.mediaSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupUI() {
        binding.textViewChunkName.text = chunk.name
        binding.textViewChunkSummary.text = chunk.summary
        updatePlayPauseButton()
    }

    private fun togglePlayPause() {
        isPlaying = !isPlaying
        if (isPlaying) {
            playAudio(BASE_URL + chunk.audioFilePath)
        } else {
            pauseAudio()
        }
        updatePlayPauseButton()
    }

    private fun playAudio(audioFilePath: String) {
        if (mediaPlayer == null) {
            seekerJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val player = MediaPlayer().apply {
                        setDataSource(audioFilePath)
                        prepare() // Prepare on a background thread
                    }
                    withContext(Dispatchers.Main) {
                        mediaPlayer = player
                        mediaPlayer?.start()
                        binding.mediaSeekBar.max = mediaPlayer?.duration ?: 0
                        updateSeekBar()
                        mediaPlayer?.setOnCompletionListener {
                            isPlaying = false
                            updatePlayPauseButton()
                            releaseMediaPlayer()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChunkDetailFragment", "Error playing audio: ${e.message}")
                    withContext(Dispatchers.Main) {
                        stopAudio()
                    }
                }
            }
        } else {
            mediaPlayer?.start()
            updateSeekBar()
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
    }

    private fun stopAudio() {
        mediaPlayer?.stop()
        binding.mediaSeekBar.progress = 0
        releaseMediaPlayer()
        isPlaying = false
        updatePlayPauseButton()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateSeekBar() {
        mediaPlayer?.let {
            if (!isPlaying) {
                return
            }
            val currentPosition = it.currentPosition
            val duration = it.duration
            binding.mediaSeekBar.progress = currentPosition
            val remainingTime = duration - currentPosition
            if (remainingTime <= 1000) {
                handler.postDelayed({ updateSeekBar() }, remainingTime.toLong())
            } else {
                handler.postDelayed({ updateSeekBar() }, 1000)
            }
        }
    }

    private fun updatePlayPauseButton() {
        binding.buttonPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseMediaPlayer()
        seekerJob?.cancel()
        _binding = null
    }
}