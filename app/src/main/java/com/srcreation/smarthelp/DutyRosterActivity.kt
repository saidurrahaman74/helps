package com.srcreation.smarthelp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*

class DutyRosterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                DutyRosterScreen(onBack = { finish() })
            }
        }
    }
}

data class ShiftData(
    val date: String, // Format: dd MMM yyyy
    val shiftType: String, // Morning, Evening, Night, OFF
    val wardName: String = "",
    val coNurse: String = "",
    val note: String = "",
    val isAlarmSet: Boolean = false
)

object DutyRosterStorage {
    private const val PREF_KEY_ROSTER = "nurse_duty_roster_json"

    fun saveRosterMap(context: Context, rosterMap: Map<String, ShiftData>) {
        val prefs = context.getSharedPreferences("smart_help_prefs", Context.MODE_PRIVATE)
        val json = Gson().toJson(rosterMap)
        prefs.edit().putString(PREF_KEY_ROSTER, json).apply()
    }

    fun loadRosterMap(context: Context): MutableMap<String, ShiftData> {
        val prefs = context.getSharedPreferences("smart_help_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString(PREF_KEY_ROSTER, null) ?: return mutableMapOf()
        return try {
            val type = object : TypeToken<MutableMap<String, ShiftData>>() {}.type
            Gson().fromJson(json, type) ?: mutableMapOf()
        } catch (_: Exception) {
            mutableMapOf()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyRosterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val currentLocale = LocalConfiguration.current.locales[0]
    val isBn = remember { Locale.getDefault().language == "bn" }

    var rosterMap by remember { mutableStateOf(DutyRosterStorage.loadRosterMap(context)) }
    
    // Date Selection State (Default: Today)
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val selectedDateStr = remember(selectedCalendar, currentLocale) {
        SimpleDateFormat("dd MMM yyyy", currentLocale).format(selectedCalendar.time)
    }

    val currentShift = rosterMap[selectedDateStr] ?: ShiftData(date = selectedDateStr, shiftType = "Morning")

    var shiftType by remember(selectedDateStr) { mutableStateOf(currentShift.shiftType) }
    var wardName by remember(selectedDateStr) { mutableStateOf(currentShift.wardName) }
    var coNurse by remember(selectedDateStr) { mutableStateOf(currentShift.coNurse) }
    var note by remember(selectedDateStr) { mutableStateOf(currentShift.note) }
    var isAlarmSet by remember(selectedDateStr) { mutableStateOf(currentShift.isAlarmSet) }

    // Summary Stats
    val morningCount = rosterMap.values.count { it.shiftType == "Morning" }
    val eveningCount = rosterMap.values.count { it.shiftType == "Evening" }
    val nightCount = rosterMap.values.count { it.shiftType == "Night" }
    val offCount = rosterMap.values.count { it.shiftType == "OFF" }

    fun saveCurrentShift(updatedAlarm: Boolean = isAlarmSet) {
        val updatedShift = ShiftData(
            date = selectedDateStr,
            shiftType = shiftType,
            wardName = wardName,
            coNurse = coNurse,
            note = note,
            isAlarmSet = updatedAlarm
        )
        val newMap = rosterMap.toMutableMap()
        newMap[selectedDateStr] = updatedShift
        rosterMap = newMap
        DutyRosterStorage.saveRosterMap(context, newMap)
    }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isBn) "ডিউটি রোস্টার ও শিফট প্ল্যানার" else "Nurse Duty Roster",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = { LanguageToggleButton() },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Shift Monthly Summary Card
                    item {
                        DutySummaryCard(
                            morning = morningCount,
                            evening = eveningCount,
                            night = nightCount,
                            off = offCount,
                            isBn = isBn
                        )
                    }

                    // 2. Interactive Calendar Date Selector
                    item {
                        Text(
                            text = if (isBn) "তারিখ ও ডিউটি নির্বাচন করুন:" else "Select Date & Duty Shift:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items((-2..14).toList()) { offset ->
                                val dateCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                                val dayName = SimpleDateFormat("EEE", currentLocale).format(dateCal.time)
                                val dayNum = SimpleDateFormat("dd", currentLocale).format(dateCal.time)
                                val dateKey = SimpleDateFormat("dd MMM yyyy", currentLocale).format(dateCal.time)
                                
                                val isSelected = dateKey == selectedDateStr
                                val savedShift = rosterMap[dateKey]

                                Card(
                                    modifier = Modifier
                                        .width(64.dp)
                                        .height(80.dp)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedCalendar = dateCal
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = dayName.uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = dayNum,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        // Status Pill Indicator
                                        val indicatorColor = when (savedShift?.shiftType) {
                                            "Morning" -> Color(0xFF0284C7)
                                            "Evening" -> Color(0xFFFF9800)
                                            "Night" -> Color(0xFF7C4DFF)
                                            "OFF" -> Color(0xFF10B981)
                                            else -> Color.Transparent
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(indicatorColor, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Shift Type Selector & Detail Form
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedDateStr,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Surface(
                                        color = when (shiftType) {
                                            "Morning" -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                            "Evening" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                            "Night" -> Color(0xFF7C4DFF).copy(alpha = 0.15f)
                                            else -> Color(0xFF10B981).copy(alpha = 0.15f)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = when (shiftType) {
                                                "Morning" -> if (isBn) "মর্নিং (০৮:০০ - ১৪:৩০)" else "Morning (08:00 - 14:30)"
                                                "Evening" -> if (isBn) "ইভনিং (১৪:০০ - ২১:০০)" else "Evening (14:00 - 21:00)"
                                                "Night" -> if (isBn) "নাইট (২০:৩০ - ০৮:৩০)" else "Night (20:30 - 08:30)"
                                                else -> if (isBn) "অফ-ডে (ছুটি)" else "OFF DAY"
                                            },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (shiftType) {
                                                "Morning" -> Color(0xFF0284C7)
                                                "Evening" -> Color(0xFFFF9800)
                                                "Night" -> Color(0xFF7C4DFF)
                                                else -> Color(0xFF10B981)
                                            }
                                        )
                                    }
                                }

                                Text(
                                    text = if (isBn) "শিফট পছন্দ করুন:" else "Shift Type:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val shifts = listOf(
                                        "Morning" to (if (isBn) "মর্নিং" else "Morning"),
                                        "Evening" to (if (isBn) "ইভনিং" else "Evening"),
                                        "Night" to (if (isBn) "নাইট" else "Night"),
                                        "OFF" to (if (isBn) "অফ-ডে" else "OFF")
                                    )
                                    shifts.forEach { (type, label) ->
                                        FilterChip(
                                            modifier = Modifier.weight(1f, fill = false),
                                            selected = shiftType == type,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                shiftType = type
                                            },
                                            label = { Text(label, fontSize = 11.sp, textAlign = TextAlign.Center) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = when (type) {
                                                    "Morning" -> Color(0xFF0284C7)
                                                    "Evening" -> Color(0xFFFF9800)
                                                    "Night" -> Color(0xFF7C4DFF)
                                                    else -> Color(0xFF10B981)
                                                },
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                if (shiftType != "OFF") {
                                    OutlinedTextField(
                                        value = wardName,
                                        onValueChange = { wardName = it },
                                        label = { Text(if (isBn) "ওয়ার্ড / ইউনিটের নাম (যেমন: ICU, CCU)" else "Ward / Unit Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = coNurse,
                                        onValueChange = { coNurse = it },
                                        label = { Text(if (isBn) "সহকর্মী নার্সের নাম (Co-Nurse)" else "Co-Nurse on Duty") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    label = { Text(if (isBn) "শিফট নোট বা স্মরণীয় তথ্য" else "Shift Notes / Reminders") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // 1. Primary Save Button
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        saveCurrentShift()
                                        val savedMsg = if (isBn) "ডিউটি রোস্টার সেভ হয়েছে!" else "Duty shift saved successfully!"
                                        Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (isBn) "সেভ করুন (Save Shift)" else "Save Shift", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2. Alarm and Delete Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val newAlarm = !isAlarmSet
                                            isAlarmSet = newAlarm
                                            saveCurrentShift(newAlarm)
                                            if (newAlarm) {
                                                scheduleShiftAlarm(context, selectedCalendar, shiftType)
                                                val alarmMsg = if (isBn) "শিফট শুরুর ১ ঘণ্টা আগে রিমাইন্ডার সেট হয়েছে!" else "Shift alarm set for 1h before start!"
                                                Toast.makeText(context, alarmMsg, Toast.LENGTH_SHORT).show()
                                            } else {
                                                val alarmOffMsg = if (isBn) "শিফট রিমাইন্ডার বন্ধ করা হয়েছে।" else "Shift alarm cancelled."
                                                Toast.makeText(context, alarmOffMsg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.2.dp, if (isAlarmSet) Color(0xFF10B981) else MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = if (isAlarmSet) Icons.Default.AlarmOn else Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = if (isAlarmSet) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = if (isAlarmSet) (if (isBn) "অ্যালার্ম অন" else "Alarm ON") else (if (isBn) "অ্যালার্ম দিন" else "Set Alarm"),
                                            color = if (isAlarmSet) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            rosterMap.remove(selectedDateStr)
                                            wardName = ""
                                            coNurse = ""
                                            note = ""
                                            shiftType = "Morning"
                                            isAlarmSet = false
                                            saveCurrentShift()
                                            val delMsg = if (isBn) "শিফট রেকর্ড ডিলিট করা হয়েছে!" else "Shift record deleted!"
                                            Toast.makeText(context, delMsg, Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                        border = BorderStroke(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isBn) "ডিলিট" else "Delete", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }
}

@Composable
fun DutySummaryCard(morning: Int, evening: Int, night: Int, off: Int, isBn: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (isBn) "📊 এই মাসের ডিউটি হিসাব (Monthly Summary)" else "📊 Monthly Shift Summary",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DutyStatPill(if (isBn) "মর্নিং" else "Morning", morning.toString())
                DutyStatPill(if (isBn) "ইভনিং" else "Evening", evening.toString())
                DutyStatPill(if (isBn) "নাইট" else "Night", night.toString())
                DutyStatPill(if (isBn) "অফ-ডে" else "OFF", off.toString())
            }
        }
    }
}

@Composable
fun DutyStatPill(label: String, count: String) {
    Surface(
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(72.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

private fun scheduleShiftAlarm(context: Context, dateCal: Calendar, shiftType: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val targetCal = Calendar.getInstance().apply {
        timeInMillis = dateCal.timeInMillis
        when (shiftType) {
            "Morning" -> { set(Calendar.HOUR_OF_DAY, 7); set(Calendar.MINUTE, 0) } // 7:00 AM (1h before 8 AM)
            "Evening" -> { set(Calendar.HOUR_OF_DAY, 13); set(Calendar.MINUTE, 0) } // 1:00 PM (1h before 2 PM)
            "Night" -> { set(Calendar.HOUR_OF_DAY, 19); set(Calendar.MINUTE, 30) } // 7:30 PM (1h before 8:30 PM)
            else -> { set(Calendar.HOUR_OF_DAY, 7); set(Calendar.MINUTE, 0) }
        }
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    if (targetCal.timeInMillis <= System.currentTimeMillis()) return

    val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
        putExtra("MED_NAME", "Nurse Duty Shift: $shiftType")
        putExtra("MED_CAT", "Shift Alarm")
        putExtra("MED_DOSAGE", "Time to prepare for $shiftType shift!")
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        ("duty_shift_${targetCal.timeInMillis}").hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
        }
    } catch (e: SecurityException) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
    }
}
