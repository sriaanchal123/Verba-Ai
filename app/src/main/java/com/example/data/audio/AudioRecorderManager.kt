package com.example.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

enum class RecordingStatus {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

data class RecorderState(
    val status: RecordingStatus = RecordingStatus.IDLE,
    val durationSeconds: Int = 0,
    val currentDecibels: Float = 0f,
    val waveformPoints: List<Float> = emptyList(),
    val outputFile: File? = null,
    val errorMessage: String? = null
)

class AudioRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _recorderState = MutableStateFlow(RecorderState())
    val recorderState: StateFlow<RecorderState> = _recorderState.asStateFlow()

    fun startRecording(): Boolean {
        try {
            stopRecordingInternal(resetState = false)

            val outputDir = File(context.cacheDir, "recordings").apply { mkdirs() }
            val file = File(outputDir, "verba_rec_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _recorderState.value = RecorderState(
                status = RecordingStatus.RECORDING,
                durationSeconds = 0,
                outputFile = file,
                waveformPoints = emptyList()
            )

            startAmplitudePolling()
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Failed to start recording", e)
            _recorderState.value = _recorderState.value.copy(
                status = RecordingStatus.IDLE,
                errorMessage = "Failed to access microphone: ${e.message}"
            )
            return false
        }
    }

    private fun startAmplitudePolling() {
        timerJob?.cancel()
        timerJob = scope.launch {
            var elapsedMs = 0
            val recentPoints = mutableListOf<Float>()

            while (isActive && _recorderState.value.status == RecordingStatus.RECORDING) {
                delay(80)
                elapsedMs += 80

                val maxAmp = try {
                    mediaRecorder?.maxAmplitude ?: 0
                } catch (e: Exception) {
                    0
                }

                // Normalize amplitude to 0.05..1.0
                val normalized = (maxAmp / 32767f).coerceIn(0.05f, 1.0f)
                val decibels = if (maxAmp > 0) 20 * kotlin.math.log10(maxAmp.toDouble()).toFloat() else 0f

                recentPoints.add(normalized)
                if (recentPoints.size > 40) {
                    recentPoints.removeAt(0)
                }

                _recorderState.value = _recorderState.value.copy(
                    durationSeconds = elapsedMs / 1000,
                    currentDecibels = decibels,
                    waveformPoints = recentPoints.toList()
                )
            }
        }
    }

    fun stopRecording(): File? {
        timerJob?.cancel()
        timerJob = null
        val file = currentOutputFile
        stopRecordingInternal(resetState = false)
        _recorderState.value = _recorderState.value.copy(
            status = RecordingStatus.STOPPED,
            outputFile = file
        )
        return file
    }

    fun reset() {
        stopRecordingInternal(resetState = true)
        _recorderState.value = RecorderState()
    }

    private fun stopRecordingInternal(resetState: Boolean) {
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    // Ignore if stop called too fast
                }
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioRecorderManager", "Error releasing media recorder", e)
        } finally {
            mediaRecorder = null
            if (resetState) {
                currentOutputFile = null
            }
        }
    }
}
