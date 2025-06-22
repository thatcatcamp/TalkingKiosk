package com.capricallctx.campkiosk.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberMouthPositionManager(): MouthPositionManager {
    val context = LocalContext.current
    return remember { MouthPositionManager(context) }
}

class MouthPositionManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("mouth_position", Context.MODE_PRIVATE)
    
    private var _position = mutableStateOf(loadPosition())
    val position: State<Offset> = _position
    
    fun updatePosition(newPosition: Offset) {
        _position.value = newPosition
        savePosition(newPosition)
    }
    
    private fun loadPosition(): Offset {
        val x = prefs.getFloat("mouth_x", 0.5f) // Default to center
        val y = prefs.getFloat("mouth_y", 0.5f) // Default to center
        return Offset(x, y)
    }
    
    private fun savePosition(position: Offset) {
        prefs.edit()
            .putFloat("mouth_x", position.x)
            .putFloat("mouth_y", position.y)
            .apply()
    }
}