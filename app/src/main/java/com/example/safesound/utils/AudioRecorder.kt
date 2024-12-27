package com.example.safesound.utils

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor() {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFilePath: String? = null
    private var recordId: String? = null
    private var chunkCount = 0
    private var isRecording = false

    fun start(recordId: String, context: Context) {
        this.recordId = recordId
        chunkCount = 0
        startNewRecording(context)
    }

    fun stop(): File? {
        if (isRecording) {
            try {
                mediaRecorder?.stop()
                Log.d("AudioRecorder", "Recording stopped: $outputFilePath")
            } catch (e: RuntimeException) {
                Log.e("AudioRecorder", "Error stopping recorder: ", e)
            } finally {
                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false
            }
        }
        return outputFilePath?.let { File(it) }
    }

    fun createChunk(context: Context): File? {
        if (!isRecording) {
            Log.w("AudioRecorder", "Cannot create chunk. Recorder is not running.")
            return null
        }

        val stoppedFile = stop() // Stop current recording
        return if (stoppedFile != null) {
            startNewRecording(context) // Start new chunk recording
        } else {
            Log.e("AudioRecorder", "Failed to create chunk. Stop returned null.")
            null
        }
    }

    private fun startNewRecording(context: Context): File? {
        val recordDir = File(context.getExternalFilesDir(null), "SafeSound/Records")
        if (!recordDir.exists() && !recordDir.mkdirs()) {
            Log.e("AudioRecorder", "Failed to create directory: ${recordDir.absolutePath}")
            return null
        }

        val outputFile = File(recordDir, "record_${recordId}_chunk${++chunkCount}.mp3")
        outputFilePath = outputFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFilePath)
            try {
                prepare()
                start()
                isRecording = true
                Log.d("AudioRecorder", "Recording started: $outputFilePath")
            } catch (e: IOException) {
                Log.e("AudioRecorder", "Error starting recording: ", e)
                isRecording = false
                return null
            }
        }

        return outputFile
    }
}
