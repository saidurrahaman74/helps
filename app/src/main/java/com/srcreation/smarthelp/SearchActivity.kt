package com.srcreation.smarthelp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                SearchScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
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
    
    val searchableItems = remember {
        listOf(
            SearchableItem("BMI Calculator", "Track your body mass index", "bmi_01", "Health Tools"),
            SearchableItem("Pro Tips & Guidance", "1000 Premium Daily Health Tips", "protips_01", "Health Tools"),
            SearchableItem("BP Tracker", "Log and monitor blood pressure", "bp_01", "Health Tools"),
            SearchableItem("Insulin Tracker", "Diabetes glucose management", "insulin_01", "Health Tools"),
            SearchableItem("Medicine Reminder", "Never miss a dose", "med_01", "Health Tools"),
            SearchableItem("Step Counter", "Daily walking goals", "step_01", "Health Tools"),
            SearchableItem("Period Tracker", "Ovulation & cycle log", "period_01", "Health Tools"),
            SearchableItem("Health Vault", "Prescriptions & lab reports", "vault_01", "Health Tools"),
            SearchableItem("Doctor Appointment", "Schedule your next visit", "appo_01", "Health Tools"),
            SearchableItem("Vaccine Record", "Immunization tracking", "vacc_01", "Health Tools"),
            SearchableItem("Fluid Calculator", "Daily hydration tracking", "fluid_01", "Health Tools"),
            SearchableItem("Prescription Scanner", "Digitize your medical slips", "scan_01", "Health Tools"),
            
            SearchableItem("Evercare Hospital", "Bashundhara, Dhaka", "h_evercare", "Hospitals"),
            SearchableItem("Square Hospital", "Panthapath, Dhaka", "h_square", "Hospitals"),
            SearchableItem("United Hospital", "Gulshan, Dhaka", "h_united", "Hospitals"),
            SearchableItem("Labaid Hospital", "Dhanmondi, Dhaka", "h_labaid", "Hospitals"),
            SearchableItem("Ibn Sina", "Dhanmondi, Dhaka", "h_ibnsina", "Hospitals"),
            SearchableItem("BIRDEM General", "Shahbag, Dhaka", "h_birdem", "Hospitals"),
            SearchableItem("Dhaka Medical", "DMCH, Dhaka", "h_dmch", "Hospitals"),
            SearchableItem("CMCH", "Chittagong Medical", "h_cmch", "Hospitals"),
            SearchableItem("Neurosciences", "NINS, Agargaon", "h_nins", "Hospitals"),
            SearchableItem("Mitford Hospital", "SSMC, Old Dhaka", "h_ssmc", "Hospitals"),
            SearchableItem("Maa-O-Shishu", "Agrabad, Chattogram", "h_cmosh", "Hospitals"),

            SearchableItem("Emergency Medicine", "Essential drugs guide", "emed_01", "Emergency Guides"),
            SearchableItem("First Aid Guide", "Emergency medical tips", "aid_01", "Emergency Guides"),
            SearchableItem("Burn Guide", "Treatment for burns", "burn_01", "Emergency Guides"),
            SearchableItem("CPR Instructions", "Life-saving technique", "cpr_01", "Emergency Guides"),
            SearchableItem("Fracture Care", "Bone injury management", "frac_01", "Emergency Guides"),
            SearchableItem("Poisoning Guide", "Toxic exposure help", "pois_01", "Emergency Guides"),
            SearchableItem("Snake Bite", "Emergency bite treatment", "snake_01", "Emergency Guides"),
            SearchableItem("Wound Care", "First aid for cuts & wounds", "wound_01", "Emergency Guides"),
            SearchableItem("Choking Help", "Anti-choking techniques", "chok_01", "Emergency Guides"),
            SearchableItem("Heart Attack", "Chest pain emergency guide", "heart_01", "Emergency Guides"),
            SearchableItem("Emergency SOS", "Instant helpline numbers", "sos_01", "Emergency Guides"),
            
            SearchableItem("Pregnancy Care", "Specialized motherhood tips", "preg_01", "Specialized Care"),
            SearchableItem("Nurse Hub", "Clinical tools for professionals", "nurse_01", "Professional Tools")
        )
    }

    val groupedResults = remember(searchQuery) {
        if (searchQuery.isEmpty()) emptyMap()
        else searchableItems
            .filter { it.title.contains(searchQuery, ignoreCase = true) || it.desc.contains(searchQuery, ignoreCase = true) }
            .groupBy { it.category }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text("Global Search", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A237E))
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF1A237E))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search for hospitals, tools or guides...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF1A237E)) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                                IconButton(onClick = { launchVoiceSearch() }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color(0xFF1A237E))
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(0.7f),
                            focusedBorderColor = Color(0xFF1A237E)
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        bottomBar = {
            AppBottomBar(context = context, currentTab = 5, backgroundBrush = AppGradientBrush)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            if (searchQuery.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp), tint = Color(0xFF1A237E).copy(0.2f))
                    Spacer(Modifier.height(16.dp))
                    Text("Type to explore Health Helpers", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E).copy(0.4f))
                    Text("Hospitals, Emergency Guides, Clinical Tools & more", fontSize = 12.sp, color = Color.Gray)
                }
            } else if (groupedResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results for \"$searchQuery\"", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedResults.forEach { (category, items) ->
                        item {
                            Text(
                                text = category.uppercase(),
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A237E).copy(0.6f),
                                letterSpacing = 1.sp
                            )
                        }
                        items(items) { item ->
                            SearchResultCard(item)
                        }
                    }
                }
            }
        }
    }
}

data class SearchableItem(val title: String, val desc: String, val id: String, val category: String)

@Composable
fun SearchResultCard(item: SearchableItem) {
    val context = LocalContext.current
    val profileId = (context as? AppCompatActivity)?.intent?.getIntExtra("PROFILE_ID", 1) ?: 1
    
    Card(
        onClick = {
            val intent = when(item.id) {
                "bmi_01" -> Intent(context, BmiToolActivity::class.java).apply { putExtra("TOOL_NAME", "Calculate"); putExtra("PROFILE_ID", profileId) }
                "protips_01" -> Intent(context, ProTipsActivity::class.java)
                "bp_01" -> Intent(context, BpTrackerActivity::class.java).apply { putExtra("PROFILE_ID", profileId) }
                "insulin_01" -> Intent(context, InsulinTrackerActivity::class.java).apply { putExtra("PROFILE_ID", profileId) }
                "med_01" -> Intent(context, MedicineReminderActivity::class.java).apply { putExtra("PROFILE_ID", profileId) }
                "step_01" -> Intent(context, StepCounterActivity::class.java).apply { putExtra("PROFILE_ID", profileId) }
                "period_01" -> Intent(context, PeriodTrackerActivity::class.java).apply { putExtra("PROFILE_ID", profileId) }
                "vault_01" -> Intent(context, MedicalVaultActivity::class.java).apply { putExtra("PROFILE_ID", profileId) }
                "appo_01" -> Intent(context, AppointmentActivity::class.java).apply { putExtra("PROFILE_ID", profileId) }
                "vacc_01" -> Intent(context, VaccineActivity::class.java)
                "emed_01" -> Intent(context, CategoryActivity::class.java).apply {
                    putExtra("CATEGORY_TITLE", "First Aid")
                    putExtra("CATEGORY_COLOR", "#EF4444")
                    putExtra("PROFILE_ID", profileId)
                }
                "preg_01" -> Intent(context, PregnancyActivity::class.java)
                "h_evercare" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "evercare") }
                "h_square" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "square") }
                "h_united" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "united") }
                "h_labaid" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "labaid") }
                "h_ibnsina" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "ibnsina") }
                "h_birdem" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "birdem") }
                "h_dmch" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "dmch") }
                "h_cmch" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "cmch") }
                "h_nins" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "nins") }
                "h_ssmc" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "ssmc") }
                "h_cmosh" -> Intent(context, HospitalDetailActivity::class.java).apply { putExtra("HOSPITAL_ID", "cmosh") }
                "aid_01" -> Intent(context, CategoryActivity::class.java).apply { 
                    putExtra("CATEGORY_ID", "aid_01")
                    putExtra("CATEGORY_TITLE", "First Aid")
                    putExtra("CATEGORY_COLOR", "#4CAF50")
                }
                "burn_01" -> Intent(context, BurnsActivity::class.java)
                "cpr_01" -> Intent(context, CprGuideActivity::class.java)
                "frac_01" -> Intent(context, FracturesActivity::class.java)
                "pois_01" -> Intent(context, PoisoningActivity::class.java)
                "snake_01" -> Intent(context, SnakeBiteActivity::class.java)
                "fluid_01" -> Intent(context, FluidTrackerActivity::class.java)
                "sos_01" -> Intent(context, MedicalQrActivity::class.java)
                "nurse_01" -> Intent(context, NurseHubActivity::class.java)
                "wound_01" -> Intent(context, WoundsActivity::class.java)
                "chok_01" -> Intent(context, ChokingActivity::class.java)
                "heart_01" -> Intent(context, HeartAttackActivity::class.java)
                "scan_01" -> Intent(context, PrescriptionScannerActivity::class.java)
                else -> Intent(context, HomeActivity::class.java)
            }
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color(0xFF1A237E).copy(0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    val icon = getIconByName(item.title)
                    Icon(icon, null, tint = Color(0xFF1A237E), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                Text(item.desc, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
        }
    }
}
