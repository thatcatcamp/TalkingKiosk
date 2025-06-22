package com.capricallctx.campkiosk.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlin.random.Random

@Composable
fun rememberDialogManager(): DialogManager {
    val context = LocalContext.current
    return remember { DialogManager(context) }
}

class DialogManager(private val context: Context) {
    private var _currentDialog = mutableStateOf("")
    val currentDialog: State<String> = _currentDialog
    
    private var _isShowingDialog = mutableStateOf(false)
    val isShowingDialog: State<Boolean> = _isShowingDialog
    
    private var lastDialogTime = 0L
    private val dialogCooldown = 8000L // 8 seconds between dialogs
    
    private val phrasesMap: Map<String, String> by lazy {
        loadPhrasesFromJson()
    }
    
    private fun loadPhrasesFromJson(): Map<String, String> {
        return try {
            val inputStream = context.assets.open("phrases.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val phrasesObject = jsonObject.getJSONObject("phrases")
            
            val map = mutableMapOf<String, String>()
            val keys = phrasesObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = phrasesObject.getString(key)
            }
            map
        } catch (e: Exception) {
            // Fallback phrases if JSON loading fails
            mapOf(
                "welcome" to "Welcome to our camp, friend!",
                "hello" to "Hey there, visitor!"
            )
        }
    }
    
    fun getTextForAudioFile(filename: String): String? {
        return phrasesMap[filename]
    }
    
    suspend fun triggerGreeting() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDialogTime > dialogCooldown && !_isShowingDialog.value) {
            // Show a simple welcome message for initial greeting
            showDialog("Welcome, visitor!")
            lastDialogTime = currentTime
        }
    }
    
    suspend fun showTextForAudio(filename: String) {
        val text = getTextForAudioFile(filename)
        if (text != null) {
            showDialog(text)
        }
    }
    
    private suspend fun showDialog(text: String) {
        _currentDialog.value = text
        _isShowingDialog.value = true
        
        delay(4000) // Show dialog for 4 seconds
        
        _isShowingDialog.value = false
        _currentDialog.value = ""
    }
    
    fun clearDialog() {
        _isShowingDialog.value = false
        _currentDialog.value = ""
    }
}