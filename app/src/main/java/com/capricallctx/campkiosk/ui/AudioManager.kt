package com.capricallctx.campkiosk.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberAudioManager(audioResId: Int): AudioManager {
    val context = LocalContext.current
    return remember { AudioManager(context, audioResId) }
}

class AudioManager(
    private val context: Context,
    private val audioResId: Int
) {
    private var mediaPlayer: MediaPlayer? = null
    private var _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying
    
    private var _audioLevel = mutableStateOf(0f)
    val audioLevel: State<Float> = _audioLevel
    
    private var audioLevelUpdater: Job? = null
    
    fun startPlaying() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, audioResId).apply {
                isLooping = false
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    startAudioLevelSimulation()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    stopAudioLevelSimulation()
                    _audioLevel.value = 0f
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    false
                }
            }
        } else {
            mediaPlayer?.start()
            _isPlaying.value = true
            startAudioLevelSimulation()
        }
    }
    
    fun stopPlaying() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        stopAudioLevelSimulation()
        _audioLevel.value = 0f
    }
    
    fun release() {
        stopAudioLevelSimulation()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _audioLevel.value = 0f
    }
    
    private fun startAudioLevelSimulation() {
        audioLevelUpdater = CoroutineScope(Dispatchers.Main).launch {
            while (_isPlaying.value) {
                // Simulate audio level changes for lip sync
                // In a real implementation, you'd analyze the actual audio
                val time = System.currentTimeMillis()
                val baseLevel = kotlin.math.sin((time % 800) * 0.01) * 0.5 + 0.5
                val variation = kotlin.math.sin((time % 200) * 0.03) * 0.3
                val finalLevel = (baseLevel + variation).coerceIn(0.1, 1.0)
                
                _audioLevel.value = finalLevel.toFloat()
                delay(50) // Update every 50ms
            }
        }
    }
    
    private fun stopAudioLevelSimulation() {
        audioLevelUpdater?.cancel()
        audioLevelUpdater = null
    }
}