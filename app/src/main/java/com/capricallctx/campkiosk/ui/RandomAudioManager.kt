package com.capricallctx.campkiosk.ui

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun rememberRandomAudioManager(): RandomAudioManager {
    val context = LocalContext.current
    return remember { RandomAudioManager(context) }
}

class RandomAudioManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying
    
    private var _audioLevel = mutableStateOf(0f)
    val audioLevel: State<Float> = _audioLevel
    
    private var _isContinuousMode = mutableStateOf(false)
    val isContinuousMode: State<Boolean> = _isContinuousMode
    
    private var _currentFileName = mutableStateOf("")
    val currentFileName: State<String> = _currentFileName
    
    private var audioLevelUpdater: Job? = null
    private var continuousPlaybackJob: Job? = null
    private var audioFiles: List<String> = emptyList()
    
    init {
        loadAudioFileList()
    }
    
    private fun loadAudioFileList() {
        try {
            audioFiles = context.assets.list("")?.filter { it.endsWith(".wav") } ?: emptyList()
            android.util.Log.d("RandomAudioManager", "Found ${audioFiles.size} audio files")
        } catch (e: Exception) {
            android.util.Log.e("RandomAudioManager", "Error loading audio files", e)
        }
    }
    
    fun playRandomAudio() {
        if (audioFiles.isEmpty()) {
            android.util.Log.w("RandomAudioManager", "No audio files available")
            return
        }
        
        if (_isPlaying.value) {
            android.util.Log.d("RandomAudioManager", "Audio already playing, ignoring request")
            return
        }
        
        val randomFile = audioFiles[Random.nextInt(audioFiles.size)]
        android.util.Log.d("RandomAudioManager", "Playing random audio: $randomFile")
        
        // Set current filename (remove .wav extension for matching with JSON)
        _currentFileName.value = randomFile.removeSuffix(".wav")
        
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                val afd: AssetFileDescriptor = context.assets.openFd(randomFile)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    startAudioLevelSimulation()
                }
                
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentFileName.value = ""
                    stopAudioLevelSimulation()
                    _audioLevel.value = 0f
                    android.util.Log.d("RandomAudioManager", "Audio playback completed")
                }
                
                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("RandomAudioManager", "MediaPlayer error: what=$what extra=$extra")
                    _isPlaying.value = false
                    _currentFileName.value = ""
                    stopAudioLevelSimulation()
                    _audioLevel.value = 0f
                    false
                }
                
                prepareAsync()
            }
        } catch (e: Exception) {
            android.util.Log.e("RandomAudioManager", "Error playing audio", e)
            _isPlaying.value = false
        }
    }
    
    fun stopPlaying() {
        android.util.Log.d("RandomAudioManager", "Stopping audio playback")
        mediaPlayer?.stop()
        _isPlaying.value = false
        _currentFileName.value = ""
        stopAudioLevelSimulation()
        _audioLevel.value = 0f
    }
    
    fun startContinuousPlayback() {
        if (_isContinuousMode.value) {
            android.util.Log.d("RandomAudioManager", "Continuous mode already active")
            return
        }
        
        android.util.Log.d("RandomAudioManager", "Starting continuous playback mode")
        _isContinuousMode.value = true
        
        continuousPlaybackJob = CoroutineScope(Dispatchers.Main).launch {
            // Play first audio immediately
            playRandomAudio()
            
            while (_isContinuousMode.value) {
                // Wait for current audio to finish or timeout
                var waitTime = 0L
                while (_isPlaying.value && waitTime < 60000L) { // Max 60 seconds per audio
                    delay(1000)
                    waitTime += 1000
                }
                
                if (_isContinuousMode.value) {
                    // Wait 30 seconds between audio files
                    android.util.Log.d("RandomAudioManager", "Waiting 30 seconds before next audio")
                    delay(30000)
                    
                    if (_isContinuousMode.value) {
                        playRandomAudio()
                    }
                }
            }
        }
    }
    
    fun stopContinuousPlayback() {
        android.util.Log.d("RandomAudioManager", "Stopping continuous playback mode")
        _isContinuousMode.value = false
        continuousPlaybackJob?.cancel()
        continuousPlaybackJob = null
        stopPlaying()
    }
    
    fun release() {
        android.util.Log.d("RandomAudioManager", "Releasing audio resources")
        stopContinuousPlayback()
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