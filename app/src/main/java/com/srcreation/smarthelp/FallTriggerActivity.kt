package com.srcreation.smarthelp

import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.srcreation.smarthelp.ui.theme.HelpsTheme

class FallTriggerActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Play Alarm Sound
        val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(this, alert)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        setContent {
            HelpsTheme {
                FallTriggerScreen(
                    onCancel = {
                        stopAlert()
                        finish()
                    },
                    onTimeout = {
                        stopAlert()
                        sendEmergencySos()
                        finish()
                    }
                )
            }
        }
    }

    private fun stopAlert() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        timer?.cancel()
    }

    private fun sendEmergencySos() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlert()
    }
}

@Composable
fun FallTriggerScreen(onCancel: () -> Unit, onTimeout: () -> Unit) {
    var timeLeft by remember { mutableIntStateOf(20) }

    LaunchedEffect(Unit) {
        val timer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
            }
            override fun onFinish() {
                onTimeout()
            }
        }
        timer.start()
    }

    Scaffold(containerColor = Color(0xFFEF4444)) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(64.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
            
            Text("Fall Detected!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text("Sending SOS in...", color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp)
            
            Spacer(Modifier.height(16.dp))
            
            Text("$timeLeft", color = Color.White, fontWeight = FontWeight.Black, fontSize = 80.sp)
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("I am OK, Cancel SOS", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
