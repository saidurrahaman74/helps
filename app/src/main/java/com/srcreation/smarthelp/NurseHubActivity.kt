package com.srcreation.smarthelp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay

class NurseHubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val pref = PreferenceManager(this)
        pref.setActiveProfile(2) // Ensure Nurse mode is active
        
        setContent {
            HelpsTheme {
                NurseHubScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurseHubScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isBn = remember { Locale.getDefault().language == "bn" }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = spokenResults?.firstOrNull()
            if (!text.isNullOrBlank()) {
                searchQuery = text
                Toast.makeText(context, "🎤 \"$text\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "কথা বলুন... / Speak now...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ভয়েস অনুসন্ধান পাওয়া যায়নি (Voice search unavailable)", Toast.LENGTH_SHORT).show()
        }
    }
    
    val clinicalTools = listOf(
        NurseToolData("smart_diet", if (isBn) "স্মার্ট ডায়েট ও ক্যালরি ট্র্যাকার" else "Smart Diet & Calorie Tracker", if (isBn) "এআই ফুড স্ক্যানার, ক্যালরি ও নিউট্রিশন ডায়েরি" else "AI Food Scanner, Calories & Nutrition Diary", Icons.Default.Restaurant, Color(0xFF22C55E)),
        NurseToolData("drug_interact", if (isBn) "ওষুধের মিথস্ক্রিয়া চেকার" else "Drug Interaction Checker", if (isBn) "একাধিক ওষুধের পার্শ্বপ্রতিক্রিয়া ও কনট্রাডিকশন পরীক্ষা" else "Check adverse drug-drug interactions", Icons.Default.Medication, Color(0xFF0284C7)),
        NurseToolData("emergency_triage", if (isBn) "গোল্ডেন আওয়ার ইমার্জেন্সি গাইড" else "Golden Hour Emergency Guide", if (isBn) "সিপিআর, হার্ট অ্যাটাক ও রক্তক্ষরণ ফার্স্ট এইড" else "CPR, bleeding & emergency triage checklist", Icons.Default.Warning, Color(0xFFEF4444)),
        NurseToolData("icu", if (isBn) "ICU & Critical Care Suite" else "ICU & Critical Care Suite", if (isBn) "ABG, P/F Ratio, MAP, RASS, qSOFA, Parkland, CPP, ETT ও নিওনেটাল টুলস" else "ABG, P/F Ratio, MAP, RASS, qSOFA, Parkland, CPP, ETT & NICU Suite", Icons.Default.HealthAndSafety, Color(0xFFEF4444)),
        NurseToolData("dose", stringResource(id = R.string.tool_dose_calc), stringResource(id = R.string.tool_desc_dose), Icons.Default.Calculate, Color(0xFF0284C7)),
        NurseToolData("iv", stringResource(id = R.string.tool_iv_fluid), stringResource(id = R.string.tool_desc_iv), Icons.Default.WaterDrop, Color(0xFF22C55E)),
        NurseToolData("gcs", stringResource(id = R.string.tool_gcs_calc), stringResource(id = R.string.tool_desc_gcs), Icons.Default.Psychology, Color(0xFFA855F7)),
        NurseToolData("braden", stringResource(id = R.string.tool_braden), stringResource(id = R.string.tool_desc_braden), Icons.Default.AccessibilityNew, Color(0xFF0284C7)),
        NurseToolData("pain", stringResource(id = R.string.tool_pain_scale), stringResource(id = R.string.tool_desc_pain), Icons.Default.SentimentDissatisfied, Color(0xFFFF9800)),
        NurseToolData("ecg", stringResource(id = R.string.tool_ecg_guide), stringResource(id = R.string.tool_desc_ecg), Icons.Default.MonitorHeart, Color(0xFFEF4444)),
        NurseToolData("apgar", stringResource(id = R.string.tool_apgar), stringResource(id = R.string.tool_desc_apgar), Icons.Default.ChildCare, Color(0xFFEC4899)),
        NurseToolData("unit_converter", if (isBn) "মেডিকেল ইউনিট কনভার্টার" else "Medical Unit Converter", if (isBn) "রক্তের গ্লুকোজ, ক্রিয়েটিনিন, তাপমাত্রা ও অন্যান্য আন্তর্জাতিক ইউনিট রূপান্তর" else "SI & Conventional lab & clinical unit converter", Icons.Default.SwapHoriz, Color(0xFF6366F1)),
        NurseToolData("bsa", stringResource(id = R.string.tool_bsa_calc), stringResource(id = R.string.tool_desc_bsa), Icons.Default.Straighten, Color(0xFF0284C7)),
        NurseToolData("dilution", stringResource(id = R.string.tool_drug_dilution), stringResource(id = R.string.tool_desc_dilution), Icons.Default.Bloodtype, Color(0xFFA855F7)),
        NurseToolData("io", stringResource(id = R.string.tool_io_chart), stringResource(id = R.string.tool_desc_io), Icons.Default.WaterDrop, Color(0xFF22C55E)),
        NurseToolData("burn", stringResource(id = R.string.tool_burn_calc), stringResource(id = R.string.tool_desc_burn), Icons.Default.Whatshot, Color(0xFFEF4444)),
        NurseToolData("oxygen", stringResource(id = R.string.tool_oxygen), stringResource(id = R.string.tool_desc_oxygen), Icons.Default.Air, Color(0xFF22C55E))
    )

    val vitalTools = listOf(
        NurseToolData("bp", stringResource(id = R.string.tool_bp_tracker), stringResource(id = R.string.tool_desc_bp), Icons.Default.MonitorHeart, Color(0xFFEF4444)),
        NurseToolData("pulse", "Pulse Tracker", "Heart rate & BPM monitor", Icons.Default.Favorite, Color(0xFFEF4444)),
        NurseToolData("spo2", if (isBn) "অক্সিজেন স্যাচুরেশন (SpO2)" else "SpO2 Tracker", if (isBn) "ব্লাড অক্সিজেন মনিটর" else "Blood oxygen monitor", Icons.Default.Air, Color(0xFF0284C7)),
        NurseToolData("temp", stringResource(id = R.string.card_temp), stringResource(id = R.string.card_temp_desc), Icons.Default.Thermostat, Color(0xFFF59E0B)),
        NurseToolData("insulin", stringResource(id = R.string.tool_insulin), stringResource(id = R.string.tool_desc_insulin), Icons.Default.Medication, Color(0xFF0284C7)),
        NurseToolData("insulin_calc", stringResource(id = R.string.tool_insulin_calc), stringResource(id = R.string.tool_desc_insulin_calc), Icons.Default.Calculate, Color(0xFF0284C7)),
        NurseToolData("lab", stringResource(id = R.string.tool_lab_values), stringResource(id = R.string.tool_desc_lab), Icons.Default.Science, Color(0xFFEF4444)),
        NurseToolData("dictionary", stringResource(id = R.string.tool_med_dictionary), stringResource(id = R.string.tool_desc_dictionary), Icons.AutoMirrored.Filled.MenuBook, Color(0xFF6366F1)),
        NurseToolData("abbr", stringResource(id = R.string.tool_medical_abbr), stringResource(id = R.string.tool_desc_abbr), Icons.Default.Abc, Color(0xFF6366F1)),
        NurseToolData("imaging", stringResource(id = R.string.tool_imaging), stringResource(id = R.string.tool_desc_imaging), Icons.Default.Visibility, Color(0xFFEC4899))
    )

    val patientUtility = listOf(
        NurseToolData("med", stringResource(id = R.string.med_reminder_title), stringResource(id = R.string.tool_desc_med_rem), Icons.Default.Medication, Color(0xFF7C4DFF)),
        NurseToolData("step", stringResource(id = R.string.step_counter_title), if (isBn) "দৈনিক হাঁটা ও স্টেপস ট্র্যাকার" else "Daily walking & step tracker", Icons.AutoMirrored.Filled.DirectionsWalk, Color(0xFF22C55E)),
        NurseToolData("bmi", stringResource(id = R.string.tool_bmi_calc), stringResource(id = R.string.tool_desc_bmi), Icons.Default.Calculate, Color(0xFFA855F7)),
        NurseToolData("encyclopedia", stringResource(id = R.string.tool_encyclopedia), stringResource(id = R.string.tool_desc_encyclopedia), Icons.AutoMirrored.Filled.MenuBook, Color(0xFF6366F1)),
        NurseToolData("weight", stringResource(id = R.string.tool_weight), stringResource(id = R.string.tool_desc_weight), Icons.Default.BarChart, Color(0xFFEC4899))
    )

    val filterClinical = clinicalTools.filter { it.title.contains(searchQuery, true) || it.desc.contains(searchQuery, true) }
    val filterVital = vitalTools.filter { it.title.contains(searchQuery, true) || it.desc.contains(searchQuery, true) }
    val filterUtility = patientUtility.filter { it.title.contains(searchQuery, true) || it.desc.contains(searchQuery, true) }

    val comingSoonTemplate = stringResource(id = R.string.coming_soon_msg)

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val isDark = isSystemInDarkTheme()

    var showQuickActionSheet by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = Color(0xFF0284C7).copy(alpha = 0.1f)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(14.dp)) {
                            NurseIconDrawing(Color(0xFF0284C7))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(id = R.string.nav_nurse_toolkit), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
                    Text(text = stringResource(id = R.string.nurse_hub_subtitle), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                val menuItems = listOf(
                    Triple(stringResource(id = R.string.nav_nurse_id), Icons.Default.Badge, Intent(context, NurseIdActivity::class.java)),
                    Triple(stringResource(id = R.string.tool_vault), Icons.Default.Inventory, Intent(context, MedicalVaultActivity::class.java).apply { putExtra("PROFILE_ID", 2) }),
                    Triple(stringResource(id = R.string.duty_roster), Icons.Default.CalendarMonth, Intent(context, DutyRosterActivity::class.java)),
                    Triple(stringResource(id = R.string.handover_notes), Icons.AutoMirrored.Filled.Assignment, Intent(context, HandoverActivity::class.java).apply { putExtra("PROFILE_ID", 2) }),
                    Triple(stringResource(id = R.string.shift_reminder), Icons.Default.Alarm, Intent(context, DutyRosterActivity::class.java)),
                    Triple(stringResource(id = R.string.guidelines), Icons.AutoMirrored.Filled.MenuBook, Intent(context, NurseDictionaryActivity::class.java))
                )

                menuItems.forEach { (label, icon, intent) ->
                    NavigationDrawerItem(
                        label = { Text(text = label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDark) Color.White else Color.Black) },
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { drawerState.close() }
                            if (intent != null) context.startActivity(intent)
                            else Toast.makeText(context, comingSoonTemplate.replace("%s", label), Toast.LENGTH_SHORT).show()
                        },
                        icon = { Icon(imageVector = icon, contentDescription = label, tint = if (isDark) Color.White else Color.Black) },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                QuickModeSwitcher(
                    currentMode = "nurse",
                    onModeClick = { mode ->
                        scope.launch { drawerState.close() }
                        val intent = when(mode) {
                            "patient" -> Intent(context, HomeActivity::class.java)
                            "women" -> Intent(context, WomenCareActivity::class.java)
                            else -> null
                        }
                        intent?.let { context.startActivity(it.apply { flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT }) }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                    CenterAlignedTopAppBar(
                        title = { Text(text = stringResource(id = R.string.nurse_help_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
                        navigationIcon = {
                            IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                            }
                        },
                        actions = {
                            IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); context.startActivity(Intent(context, NotificationActivity::class.java).apply { putExtra("PROFILE_ID", 2) }) }) {
                                Icon(Icons.Default.NotificationsNone, "Notifications", tint = if (isDark) Color.White else Color(0xFF1A237E))
                            }
                            LanguageToggleButton()
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            bottomBar = { AppBottomBar(context = context, currentTab = 2, isNurseMode = true, backgroundBrush = AppGradientBrush) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showQuickActionSheet = true
                    },
                    containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                    contentColor = if (isDark) Color.Black else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Quick Nurse Hub", modifier = Modifier.size(24.dp))
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
                if (showQuickActionSheet) {
                    NurseQuickActionBottomSheet(onDismiss = { showQuickActionSheet = false })
                }
                var isLoading by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(800) // Brief shimmer for UX consistency
                    isLoading = false
                }

                ScreenEntranceAnimation {
                    if (isLoading) {
                        NurseHubSkeleton()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                        ) {
                            item(span = { GridItemSpan(2) }) {
                                Column {
                                    GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Search, null, tint = Color(0xFF0284C7))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            TextField(
                                                value = searchQuery, onValueChange = { searchQuery = it },
                                                placeholder = { Text("Search clinical tools...", color = Color.Gray, fontSize = 14.sp) },
                                                modifier = Modifier.weight(1f),
                                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                                                maxLines = 1
                                            )
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = "" }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                                }
                                            }
                                            IconButton(onClick = { 
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                launchVoiceSearch()
                                            }) {
                                                Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color(0xFF0284C7))
                                            }
                                        }
                                    }
                                    if (searchQuery.isBlank()) {
                                        Text(text = stringResource(id = getGreetingText()), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1A237E))
                                        Text(text = stringResource(R.string.nurse_toolkit_ready), fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Quick Shift Handover & Duty Roster Banner Card
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                context.startActivity(Intent(context, HandoverActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
                                            },
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.1f)),
                                            border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Surface(
                                                        modifier = Modifier.size(44.dp),
                                                        shape = CircleShape,
                                                        color = Color(0xFF0284C7).copy(alpha = 0.2f)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(Icons.AutoMirrored.Filled.Assignment, null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                                                        }
                                                    }
                                                    Column {
                                                        Text(
                                                            text = if (isBn) "এসবিএআর শিফট হ্যান্ডওভার (SBAR)" else "SBAR Shift Handover",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF0284C7)
                                                        )
                                                        Text(
                                                            text = if (isBn) "শিফট পরিবর্তনের সময় পেশেন্ট রিপোর্ট শেয়ার করুন" else "Professional clinical handover report",
                                                            fontSize = 11.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                }
                                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))
                                    }
                                }
                            }

                            if (filterClinical.isNotEmpty()) {
                                item(span = { GridItemSpan(2) }) { CategoryHeader(stringResource(id = R.string.clinical_calculators), Color(0xFFEF4444)) }
                                items(filterClinical) { tool -> NurseToolCard(tool) { handleToolClick(context, tool.id) } }
                            }

                            if (filterVital.isNotEmpty()) {
                                item(span = { GridItemSpan(2) }) { CategoryHeader(stringResource(id = R.string.vital_trackers_lab), Color(0xFF1A237E)) }
                                items(filterVital) { tool -> NurseToolCard(tool) { handleToolClick(context, tool.id) } }
                            }

                            if (filterUtility.isNotEmpty()) {
                                item(span = { GridItemSpan(2) }) { CategoryHeader(stringResource(id = R.string.patient_utilities), Color(0xFF22C55E)) }
                                items(filterUtility) { tool -> NurseToolCard(tool) { handleToolClick(context, tool.id) } }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NurseHubSkeleton() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        userScrollEnabled = false
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SkeletonItem(modifier = Modifier.fillMaxWidth(), height = 56.dp, cornerRadius = 24.dp)
                SkeletonItem(width = 200.dp, height = 20.dp)
                SkeletonItem(width = 150.dp, height = 12.dp)
            }
        }
        
        item(span = { GridItemSpan(2) }) { SkeletonItem(width = 150.dp, height = 18.dp) }
        
        items(6) {
            SkeletonItem(modifier = Modifier.fillMaxWidth(), height = 125.dp, cornerRadius = 28.dp)
        }
    }
}

@Composable
fun CategoryHeader(title: String, color: Color) {
    val isDark = isSystemInDarkTheme()
    Text(
        text = title,
        fontSize = if (isDark) 13.sp else 14.sp,
        fontWeight = FontWeight.ExtraBold,
        color = if (isDark) Color.White.copy(alpha = 0.9f) else color,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun NurseToolCard(tool: NurseToolData, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "scale")
    val rotateX by animateFloatAsState(targetValue = if (isPressed) -12f else 0f, label = "rotateX")
    val rotateY by animateFloatAsState(targetValue = if (isPressed) 8f else 0f, label = "rotateY")
    val translationZ by animateFloatAsState(targetValue = if (isPressed) 30f else 0f, label = "z")

    val cardBg = if (isDark) Brush.verticalGradient(colors = listOf(Color(0xFF0F172A), tool.color.copy(alpha = 0.2f))) else Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.9f), tool.color.copy(alpha = 0.05f)))
    val titleColor = if (isDark) Color.White else Color(0xFF1A237E)
    val descColor = if (isDark) Color.White.copy(alpha = 0.75f) else Color.DarkGray
    val borderStroke = if (isDark) BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)) else BorderStroke(1.5.dp, Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent, Color.White.copy(alpha = 0.1f))))

    Card(
        modifier = modifier
            .height(125.dp)
            .graphicsLayer {
                this.scaleX = scale; this.scaleY = scale
                this.rotationX = rotateX; this.rotationY = rotateY
                this.shadowElevation = translationZ
                cameraDistance = 15f * density
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() }),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = borderStroke
    ) {
        Box(modifier = Modifier.fillMaxSize().background(cardBg)) {
            Column(modifier = Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Surface(modifier = Modifier.size(46.dp), shape = RoundedCornerShape(14.dp), color = tool.color.copy(alpha = 0.15f), border = BorderStroke(1.dp, tool.color.copy(alpha = 0.2f))) {
                    Box(contentAlignment = Alignment.Center) {
                        if (tool.id == "step") {
                            Box(modifier = Modifier.size(28.dp)) { WalkingPersonIconDrawing(tool.color) }
                        } else if (tool.id == "med") {
                            Icon(ImageVector.vectorResource(id = R.drawable.ic_medicine_reminder), null, tint = tool.color, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(tool.icon, null, tint = tool.color, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = tool.title, color = titleColor, fontWeight = FontWeight.Black, fontSize = 13.sp, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 16.sp, overflow = TextOverflow.Ellipsis)
                Text(text = tool.desc, color = descColor, fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 11.sp, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

fun handleToolClick(context: Context, id: String) {
    when (id) {
        "med" -> context.startActivity(Intent(context, MedicineReminderActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
        "bp" -> context.startActivity(Intent(context, BpTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
        "pulse" -> context.startActivity(Intent(context, PulseTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
        "spo2" -> context.startActivity(Intent(context, SpO2TrackerActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
        "temp" -> context.startActivity(Intent(context, TempTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
        "step" -> context.startActivity(Intent(context, StepCounterActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
        "icu" -> context.startActivity(Intent(context, NurseIcuActivity::class.java))
        "smart_diet" -> context.startActivity(Intent(context, SmartDietActivity::class.java))
        "drug_interact" -> context.startActivity(Intent(context, MedicationInteractionActivity::class.java))
        "emergency_triage" -> {
            val intent = Intent(context, CategoryActivity::class.java).apply {
                putExtra("CATEGORY_TITLE", "First Aid")
                putExtra("CATEGORY_COLOR", "#EF4444")
                putExtra("PROFILE_ID", 2)
            }
            context.startActivity(intent)
        }
        "dose" -> context.startActivity(Intent(context, NurseDoseActivity::class.java))
        "unit_converter" -> context.startActivity(Intent(context, NurseUnitConverterActivity::class.java))
        "iv" -> context.startActivity(Intent(context, IvFluidCalculatorActivity::class.java))
        "gcs" -> context.startActivity(Intent(context, NurseGcsActivity::class.java))
        "apgar" -> context.startActivity(Intent(context, NurseApgarActivity::class.java))
        "bsa" -> context.startActivity(Intent(context, NurseBsaActivity::class.java))
        "burn" -> context.startActivity(Intent(context, NurseBurnActivity::class.java))
        "braden" -> context.startActivity(Intent(context, NurseBradenActivity::class.java))
        "oxygen" -> context.startActivity(Intent(context, NurseOxygenActivity::class.java))
        "lab" -> context.startActivity(Intent(context, LabValuesActivity::class.java))
        "abbr" -> context.startActivity(Intent(context, MedicalAbbreviationActivity::class.java))
        "dictionary" -> context.startActivity(Intent(context, NurseDictionaryActivity::class.java))
        "ecg" -> context.startActivity(Intent(context, NurseEcgGuideActivity::class.java))
        "pain" -> context.startActivity(Intent(context, NursePainScaleActivity::class.java))
        "dilution" -> context.startActivity(Intent(context, NurseDrugDilutionActivity::class.java))
        "io" -> context.startActivity(Intent(context, NurseIoActivity::class.java))
        "bmi" -> { val intent = Intent(context, CategoryActivity::class.java).apply { putExtra("CATEGORY_TITLE", "BMI Calculator"); putExtra("CATEGORY_COLOR", "#A855F7"); putExtra("PROFILE_ID", 2) }; context.startActivity(intent) }
        "insulin" -> context.startActivity(Intent(context, InsulinTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
        "insulin_calc" -> context.startActivity(Intent(context, InsulinCalculatorActivity::class.java))
        "vault" -> context.startActivity(Intent(context, MedicalVaultActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
        "period" -> context.startActivity(Intent(context, PeriodTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 2) })
        "encyclopedia" -> context.startActivity(Intent(context, MedicalEncyclopediaActivity::class.java))
        "weight" -> { val intent = Intent(context, CategoryActivity::class.java).apply { putExtra("CATEGORY_TITLE", "Weight Chart"); putExtra("CATEGORY_COLOR", "#EC4899"); putExtra("PROFILE_ID", 2) }; context.startActivity(intent) }
        "diet" -> context.startActivity(Intent(context, ProTipsActivity::class.java))
        "aid" -> { val intent = Intent(context, CategoryActivity::class.java).apply { putExtra("CATEGORY_TITLE", "First Aid"); putExtra("CATEGORY_COLOR", "#22C55E"); putExtra("PROFILE_ID", 2) }; context.startActivity(intent) }
        else -> Toast.makeText(context, "Tool coming soon!", Toast.LENGTH_SHORT).show()
    }
}

data class NurseToolData(val id: String, val title: String, val desc: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurseQuickActionBottomSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isBn = remember { Locale.getDefault().language == "bn" }
    val isDark = isSystemInDarkTheme()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isBn) "⚡ কুইক নার্সিং হাব (Quick Nurse Hub)" else "⚡ Quick Nurse Hub",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NurseQuickActionButtonPill(
                    title = if (isBn) "ইউনিট কনভার্টার" else "Unit Converter",
                    icon = Icons.Default.SwapHoriz,
                    color = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                ) {
                    onDismiss()
                    context.startActivity(Intent(context, NurseUnitConverterActivity::class.java))
                }

                NurseQuickActionButtonPill(
                    title = if (isBn) "আইভি ফ্লুইড" else "IV Fluid",
                    icon = Icons.Default.WaterDrop,
                    color = Color(0xFF22C55E),
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                ) {
                    onDismiss()
                    context.startActivity(Intent(context, IvFluidCalculatorActivity::class.java))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NurseQuickActionButtonPill(
                    title = if (isBn) "জিসিএস (GCS) স্কোর" else "GCS Calculator",
                    icon = Icons.Default.Psychology,
                    color = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                ) {
                    onDismiss()
                    context.startActivity(Intent(context, NurseGcsActivity::class.java))
                }

                NurseQuickActionButtonPill(
                    title = if (isBn) "ইসিজি (ECG) গাইড" else "ECG Guide",
                    icon = Icons.Default.MonitorHeart,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                ) {
                    onDismiss()
                    context.startActivity(Intent(context, NurseEcgGuideActivity::class.java))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun NurseQuickActionButtonPill(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(85.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = if (isDark) 0.2f else 0.12f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else color, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}
