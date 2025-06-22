package com.capricallctx.campkiosk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.capricallctx.campkiosk.ui.AnimatedMouth
import com.capricallctx.campkiosk.ui.DraggableBlinkingEyes
import com.capricallctx.campkiosk.ui.DraggableMouth
import com.capricallctx.campkiosk.ui.rememberAudioManager
import com.capricallctx.campkiosk.ui.rememberDialogManager
import com.capricallctx.campkiosk.ui.rememberEyePositionManager
import com.capricallctx.campkiosk.ui.rememberFaceDetector
import com.capricallctx.campkiosk.ui.rememberMouthPositionManager
import com.capricallctx.campkiosk.ui.rememberRandomAudioManager
import com.capricallctx.campkiosk.ui.RequestCameraPermission
import com.capricallctx.campkiosk.ui.FadingText
import com.capricallctx.campkiosk.ui.theme.CampKioskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampKioskTheme {
                KioskContent()
            }
        }
    }
}

@Composable
fun KioskContent() {
    val randomAudioManager = rememberRandomAudioManager()
    val mouthPositionManager = rememberMouthPositionManager()
    val eyePositionManager = rememberEyePositionManager()
    val faceDetector = rememberFaceDetector()
    val dialogManager = rememberDialogManager()
    
    val isPlaying by randomAudioManager.isPlaying
    val audioLevel by randomAudioManager.audioLevel
    val isContinuousMode by randomAudioManager.isContinuousMode
    val currentFileName by randomAudioManager.currentFileName
    val humanPresent by faceDetector.humanPresent
    val currentDialog by dialogManager.currentDialog
    val isShowingDialog by dialogManager.isShowingDialog
    
    var permissionGranted by remember { mutableStateOf(false) }

    // Request camera permission
    RequestCameraPermission(
        onPermissionGranted = {
            permissionGranted = true
        },
        onPermissionDenied = {
            // Handle permission denial - could show a message
        }
    )

    // Start face detection when permission is granted
    DisposableEffect(permissionGranted) {
        if (permissionGranted) {
            faceDetector.startDetection()
        }
        onDispose {
            randomAudioManager.release()
            faceDetector.stopDetection()
        }
    }
    
    // Handle human presence detection - start/stop continuous audio playback
    LaunchedEffect(humanPresent) {
        if (humanPresent) {
            dialogManager.triggerGreeting()
            if (!isContinuousMode) {
                randomAudioManager.startContinuousPlayback()
            }
        } else {
            // Stop continuous playback when no humans detected
            if (isContinuousMode) {
                randomAudioManager.stopContinuousPlayback()
            }
        }
    }
    
    // Show text for current audio file
    LaunchedEffect(currentFileName) {
        if (currentFileName.isNotEmpty()) {
            dialogManager.showTextForAudio(currentFileName)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.camp_background),
                contentDescription = "Camp background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            DraggableBlinkingEyes(
                positionManager = eyePositionManager,
                modifier = Modifier.fillMaxSize(),
                size = 175f
            )
            DraggableMouth(
                audioLevel = audioLevel,
                isPlaying = isPlaying,
                positionManager = mouthPositionManager,
                modifier = Modifier.fillMaxSize()
            )
            // Show debug info and dialog text
            Text(
                text = "Permission: $permissionGranted | Human: $humanPresent | Continuous: $isContinuousMode | Playing: $isPlaying",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                fontSize = 10.sp,
                color = Color.Yellow
            )
            
            // Show fading text for audio content
            FadingText(
                text = currentDialog,
                isVisible = isShowingDialog,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp),
                color = Color.Black,
                fontSize = 16
            )
            
            // Show greeting when no audio text is shown
            if (!isShowingDialog) {
                Greeting(
                    name = if (humanPresent) "Welcome, Friend!" else "Dirty Hippy",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 60.dp)
                )
            }
            
            // Debug button to manually trigger detection
            Button(
                onClick = {
                    // Manually trigger detection for testing
                    faceDetector.testDetection()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Text("Test Detection")
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CampKioskTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.camp_background),
                contentDescription = "Camp background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            AnimatedMouth(
                modifier = Modifier.align(Alignment.Center),
                audioLevel = 0.7f,
                isPlaying = true
            )
            Greeting(
                "Camp Visitors",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 80.dp)
            )
        }
    }
}
