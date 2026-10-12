package com.srcreation.smarthelp

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

class MedicineAlarmActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var textToSpeech: TextToSpeech? = null
    
    private val medName = mutableStateOf("Medicine")
    private val category = mutableStateOf("None")
    private val medId = mutableStateOf("")
    private val medDosage = mutableStateOf("")
    private val slotExtra = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure full screen and show over lockscreen
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()
        handleIntent(intent)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Lock screen alarm: require user to tap "Taken" button
            }
        })

        val pref = PreferenceManager(this)

        // Initialize Text To Speech Voice Reminder
        if (pref.isVoiceReminderEnabled()) {
            textToSpeech = TextToSpeech(this, this)
        }

        // Play alarm sound automatically with default fallback
        try {
            val toneUriString = pref.getMedAlarmTone()
            var toneUri = toneUriString?.let { Uri.parse(it) }
            if (toneUri == null) {
                toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, toneUri!!)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                val vol = pref.getMedVolume().coerceIn(0.2f, 1.0f)
                setVolume(vol, vol)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Robust fallback to default alarm/notification ringtone
            try {
                mediaPlayer?.release()
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                mediaPlayer = MediaPlayer.create(this, fallbackUri)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }

        // Vibrate if enabled
        if (pref.isMedVibrationEnabled()) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 500), 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 1000, 500), 0)
            }
        }

        val bgColorHex = pref.getMedAlarmBackground()
        val bgColor = try { Color(android.graphics.Color.parseColor(bgColorHex)) } catch (e: Exception) { Color(0xFF1A237E) }

        setContent {
            HelpsTheme {
                AlarmScreen(
                    name = medName.value,
                    category = category.value,
                    strength = medDosage.value,
                    bgColor = bgColor,
                    onRepeatVoice = { speakVoiceAnnouncement() },
                    onSnooze = {
                        stopAlarm()
                        MedicineAlarmScheduler.scheduleSnoozeAlarm(
                            context = this,
                            medName = medName.value,
                            medId = medId.value,
                            category = category.value,
                            dosage = medDosage.value,
                            profileId = 1,
                            delayMinutes = 10
                        )
                        // 🌐 Sync Snooze to Caregiver
                        CaregiverSyncRepository.syncMedicineTaken(
                            context = this,
                            medicineName = medName.value,
                            dosage = medDosage.value,
                            timeCategory = category.value,
                            status = "SNOOZED"
                        )
                        Toast.makeText(this, getString(R.string.msg_snoozed), Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onTaken = {
                        stopAlarm()
                        markAsTaken(medId.value)
                        finish()
                    }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            speakVoiceAnnouncement()
        }
    }

    private fun speakVoiceAnnouncement() {
        val pref = PreferenceManager(this)
        val patientName = pref.getPatientName().ifEmpty { "Patient" }

        val locale = if (Locale.getDefault().language == "bn") Locale.forLanguageTag("bn-BD") else Locale.US
        val result = textToSpeech?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            textToSpeech?.setLanguage(Locale.US)
        }

        val announcement = getString(
            R.string.voice_reminder_template,
            patientName,
            medName.value,
            medDosage.value
        )

        // Lower ringtone volume so voice announcement is clear
        mediaPlayer?.setVolume(0.2f, 0.2f)
        textToSpeech?.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, "MedVoiceAnnouncement")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        medName.value = intent.getStringExtra("MED_NAME") ?: "Medicine"
        medId.value = intent.getStringExtra("MED_ID") ?: ""
        category.value = intent.getStringExtra("MED_CAT") ?: "None"
        medDosage.value = intent.getStringExtra("MED_DOSAGE") ?: ""
        slotExtra.value = intent.getStringExtra("EXTRA_SLOT")
    }

    private fun stopAlarm() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
    }

    private fun markAsTaken(medId: String) {
        if (medId.isBlank()) return
        
        // Cancel the notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(medId.hashCode())

        GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            MedicineRepository(applicationContext).markDoseTaken(medId, slotExtra.value)
            val db = AppDatabase.getDatabase(applicationContext)
            val med = db.medicineDao().getFullMedicineList().find { it.id == medId }
            med?.let {
                // 🌐 Caregiver Sync
                CaregiverSyncRepository.syncMedicineTaken(
                    context = applicationContext,
                    medicineName = it.name,
                    dosage = "${it.dosageValue} ${it.dosageUnit}".trim(),
                    timeCategory = slotExtra.value ?: it.selectedTimes,
                    status = "TAKEN"
                )

                // ⚠️ Refill Alert Check
                if (it.currentStock <= 3) {
                    val title = getString(R.string.med_refill_alert_title)
                    val body = getString(R.string.med_refill_alert_body, it.currentStock, it.dosageUnit, it.name)
                    NotificationHelper.showNotification(applicationContext, title, body)

                    CaregiverSyncRepository.syncVitals(
                        context = applicationContext,
                        type = "REFILL",
                        title = "স্টক রিফিল নোটিশ (${it.name})",
                        valueText = "মাত্র ${it.currentStock}টি বাকি আছে",
                        statusTag = "Refill Soon"
                    )
                }
            }
            MedicineWidgetProvider.updateAllWidgets(this@MedicineAlarmActivity)
        }
        Toast.makeText(this, "Medicine marked as taken!", Toast.LENGTH_SHORT).show()
        finish()
    }
}

@Composable
fun AlarmScreen(
    name: String,
    category: String,
    strength: String,
    bgColor: Color,
    onRepeatVoice: () -> Unit,
    onSnooze: () -> Unit,
    onTaken: () -> Unit
) {
    val isPill = category == "Tablet" || category == "Capsule"
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgColor, bgColor.copy(alpha = 0.8f), Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(id = if (isPill) R.string.notif_take_tab else R.string.notif_use_med),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = name,
                color = Color.Cyan,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            
            if (strength.isNotBlank()) {
                Text(
                    text = strength,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            // Repeat Voice Announcement Button
            OutlinedButton(
                onClick = onRepeatVoice,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Voice Announcement", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("পুনরায় নাম শুনুন (Replay Voice)", fontSize = 14.sp)
            }

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Snooze, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_snooze_10_mins), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onTaken,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.med_taken_btn),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
