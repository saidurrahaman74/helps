package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.srcreation.smarthelp.ui.theme.HelpsTheme

class HospitalDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val hospitalId = intent.getStringExtra("HOSPITAL_ID") ?: "evercare"
        val hospitalData = getHospitalData(hospitalId)

        setContent {
            HelpsTheme {
                HospitalDetailScreen(data = hospitalData) { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDetailScreen(data: HospitalData, onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val accentColor = Color(0xFFFF5252) // Hospital Theme Red

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(data.name, fontSize = 16.sp, color = Color(0xFF1A237E), fontWeight = FontWeight.Black, maxLines = 1) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A237E))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Quick Actions (Directly under top bar)
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item { QuickActionsRow(data, accentColor) }

                // Emergency Section
                item { EmergencySection(data, accentColor) }

                // Address Section
                item { AddressSection(data) }

                // OPD Section
                if (data.opdItems.isNotEmpty()) {
                    item { SectionHeader("Outdoor / OPD", Icons.Default.MedicalInformation) }
                    items(data.opdItems) { opd ->
                        OpdItemCard(opd)
                    }
                }

                // Departments Section
                if (data.departments.isNotEmpty()) {
                    item { SectionHeader("Main Departments", Icons.Default.Category) }
                    item { DepartmentsGrid(data.departments) }
                }

                // Floor Guide (New for Square)
                if (data.floorGuide.isNotEmpty()) {
                    item { SectionHeader("Floor Guide", Icons.Default.Layers) }
                    item { FloorGuideSection(data.floorGuide) }
                }

                // Facilities Section
                if (data.facilities.isNotEmpty()) {
                    item { SectionHeader("Main Facilities", Icons.Default.Star) }
                    item { FacilitiesSection(data.facilities) }
                }

                // Contact Section
                item { SectionHeader("Contact Info", Icons.Default.ContactPhone) }
                item { ContactInfoSection(data, accentColor) }

                // Doctors Section
                if (data.doctorSections.isNotEmpty()) {
                    item { SectionHeader("Notable Doctors", Icons.Default.People) }
                    items(data.doctorSections) { section ->
                        DoctorSectionCard(section)
                    }
                }
                
                // Footer
                item { FooterNote() }
            }
        }
    }
}

@Composable
fun HospitalHeader(data: HospitalData) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoChip(data.type, Color(0xFF4CAF50))
            InfoChip(data.category, Color(0xFF2196F3))
            if (data.beds.isNotEmpty()) {
                InfoChip("${data.beds} Beds", Color(0xFFFF9800))
            }
        }
    }
}

@Composable
fun InfoChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun QuickActionsRow(data: HospitalData, accentColor: Color) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            "Emergency", 
            Icons.Default.Call, 
            accentColor, 
            Modifier.weight(1f)
        ) {
            context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${data.emergencyHotline}".toUri()))
        }
        QuickActionButton(
            "Ambulance", 
            Icons.Default.LocalShipping, 
            Color(0xFF4CAF50), 
            Modifier.weight(1f)
        ) {
            context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${data.ambulance}".toUri()))
        }
        QuickActionButton(
            "Location", 
            Icons.Default.Map, 
            Color(0xFF2196F3), 
            Modifier.weight(1f)
        ) {
            val gmmIntentUri = "geo:0,0?q=${data.name}".toUri()
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = modifier.height(90.dp).clickable {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
        }
    }
}

@Composable
fun EmergencySection(data: HospitalData, accentColor: Color) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Emergency, contentDescription = null, tint = accentColor)
                Spacer(modifier = Modifier.width(12.dp))
                Text("24-Hour Emergency", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "${data.name} Emergency Room provides 24/7 emergency medical treatment with specialized specialist access.",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hotline: ${data.emergencyHotline}", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${data.emergencyHotline}".toUri()))
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Now", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddressSection(data: HospitalData) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF1A237E))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Address", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                Text(
                    data.address,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF1A237E).copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A237E))
    }
}

@Composable
fun OpdItemCard(opd: OpdInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(opd.name, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E), fontSize = 16.sp)
                Text(opd.services, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            }
            Surface(
                color = Color(0xFF1A237E).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Level ${opd.level}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
            }
        }
    }
}

@Composable
fun DepartmentsGrid(departments: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        departments.chunked(3).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { dept ->
                    Surface(
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            dept,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A237E)
                        )
                    }
                }
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun FloorGuideSection(floors: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            floors.forEach { (floor, services) ->
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = floor,
                        modifier = Modifier.width(80.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        fontSize = 14.sp
                    )
                    Text(
                        text = services,
                        modifier = Modifier.weight(1f),
                        color = Color.DarkGray,
                        fontSize = 13.sp
                    )
                }
                if (floor != floors.last().first) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun FacilitiesSection(facilities: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        facilities.forEach { facility ->
            Surface(
                color = Color(0xFF1A237E).copy(alpha = 0.05f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFF1A237E).copy(alpha = 0.1f))
            ) {
                Text(
                    facility,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
            }
        }
    }
}

@Composable
fun ContactInfoSection(data: HospitalData, accentColor: Color) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ContactRow("Emergency", data.emergencyHotline, accentColor) {
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${data.emergencyHotline}".toUri()))
            }
            if (data.emergencyMobile.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.5f))
                ContactRow("Mobile", data.emergencyMobile, accentColor) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${data.emergencyMobile}".toUri()))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.5f))
            ContactRow("Ambulance", data.ambulance, Color(0xFF4CAF50)) {
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${data.ambulance}".toUri()))
            }
            if (data.icuHotline.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.5f))
                ContactRow("ICU Hotline", data.icuHotline, Color(0xFF1A237E)) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${data.icuHotline}".toUri()))
                }
            }
            if (data.infoDesk.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.5f))
                ContactRow("Information", data.infoDesk, Color(0xFF2196F3)) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${data.infoDesk}".toUri()))
                }
            }
            data.landlines.forEach { landline ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.5f))
                ContactRow("Landline", landline, Color(0xFF1A237E)) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$landline".toUri()))
                }
            }
        }
    }
}

@Composable
fun DoctorSectionCard(section: DoctorSection) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.department,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A237E),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            section.doctors.forEach { doctor ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = doctor, fontSize = 14.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun ContactRow(label: String, value: String, color: Color, onCallClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = Color.Gray, fontSize = 14.sp)
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCallClick()
            },
            modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Default.Call, contentDescription = "Call", tint = color, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun FooterNote() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Note: Doctor schedules and floor levels may change. Please verify before visiting.",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Source: Official Hospital Information (Sept 2026)",
            fontSize = 10.sp,
            color = Color.Gray.copy(alpha = 0.7f)
        )
    }
}

// Simple FlowRow implementation as Material3 doesn't have it built-in yet in standard way without experimental
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeholders = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val layoutWidth = constraints.maxWidth
        val lines = mutableListOf<List<Placeable>>()
        var currentLine = mutableListOf<Placeable>()
        var currentLineWidth = 0
        
        placeholders.forEach { placeable ->
            if ((currentLineWidth + placeable.width + mainAxisSpacing.toPx()) > layoutWidth) {
                lines.add(currentLine)
                currentLine = mutableListOf(placeable)
                currentLineWidth = placeable.width
            } else {
                currentLine.add(placeable)
                currentLineWidth += placeable.width + mainAxisSpacing.toPx().toInt()
            }
        }
        lines.add(currentLine)
        
        val totalHeight = lines.sumOf { it.maxOfOrNull { p -> p.height } ?: 0 } + (lines.size - 1) * crossAxisSpacing.toPx().toInt()
        
        layout(layoutWidth, totalHeight) {
            var y = 0
            lines.forEach { line ->
                var x = 0
                val lineHeight = line.maxOfOrNull { it.height } ?: 0
                line.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + mainAxisSpacing.toPx().toInt()
                }
                y += lineHeight + crossAxisSpacing.toPx().toInt()
            }
        }
    }
}

fun getHospitalData(id: String): HospitalData {
    return when (id) {
        "evercare" -> HospitalData(
            id = "evercare",
            name = "Evercare Hospital Dhaka",
            banglaName = "এভারকেয়ার হাসপাতাল ঢাকা",
            beds = "425",
            location = "Bashundhara R/A, Dhaka",
            address = "Plot #81, Block-E, Bashundhara Residential Area, Dhaka-1229, Bangladesh",
            emergencyHotline = "10678",
            ambulance = "01714-090000",
            opdItems = listOf(
                OpdInfo("Cardiology", "5", "Heart disease, ECG, Echo, Stress Test"),
                OpdInfo("Neurology", "3", "Stroke, Epilepsy, Headache, Paralysis"),
                OpdInfo("Orthopaedics", "1", "Fracture, Spine, Knee/Hip, Sports injury"),
                OpdInfo("Internal Medicine", "2", "Adult medical conditions"),
                OpdInfo("Respiratory Medicine", "2", "Lung diseases, PFT, Bronchoscopy"),
                OpdInfo("Nephrology", "8", "Kidney disease, Dialysis"),
                OpdInfo("Dermatology", "4", "Skin, Hair, Nail conditions"),
                OpdInfo("Paediatric Cardiology", "5", "Children's heart disease")
            ),
            departments = listOf(
                "Cardiology", "Neurology", "Orthopaedics", "Medicine", "Nephrology", 
                "Paediatrics", "Gynae", "Urology", "Cancer Care", "Ophthalmology",
                "ENT", "Dermatology", "Psychiatry", "Surgery", "Dental"
            ),
            facilities = listOf(
                "ICU", "CCU", "NICU", "PICU", "Cath Lab", "Dialysis", "Pharmacy", "Laboratory", "Blood Bank"
            )
        )
        "square" -> HospitalData(
            id = "square",
            name = "Square Hospital Dhaka",
            banglaName = "স্কয়ার হাসপাতাল ঢাকা",
            category = "Tertiary Care Hospital",
            location = "Panthapath, Dhaka",
            address = "18/F, Bir Uttam Qazi Nuruzzaman Sarak, West Panthapath, Dhaka-1205, Bangladesh",
            emergencyHotline = "10616",
            emergencyMobile = "01713377775",
            ambulance = "01713377775",
            landlines = listOf("02-8144400"),
            opdItems = listOf(
                OpdInfo("Cardiology", "1", "Main Bldg: Heart care"),
                OpdInfo("Neuromedicine", "1", "Main Bldg: Brain & Nerve"),
                OpdInfo("General Surgery", "2", "Main Bldg: Surgical care"),
                OpdInfo("Gastroenterology", "7", "Ext Wing 1: Digestive care"),
                OpdInfo("Oncology", "6", "Ext Wing 1: Cancer care"),
                OpdInfo("Paediatrics", "12", "Ext Wing 2: Child care"),
                OpdInfo("Dermatology", "9", "Ext Wing 2: Skin care")
            ),
            departments = listOf(
                "Cardiology", "Neurology", "Orthopaedics", "Gastroenterology", "Oncology",
                "Nephrology", "Paediatrics", "OBGYN", "Ophthalmology", "ENT", "Urology",
                "Dermatology", "Endocrinology", "Psychiatry", "Internal Medicine", "Dental"
            ),
            facilities = listOf(
                "Air Ambulance", "ICU", "CCU", "Neuro ICU", "Cath Lab", "Operation Theatre",
                "Dialysis", "IVF", "CDC", "24h Pharmacy", "Parking"
            ),
            floorGuide = listOf(
                "7th" to "OBGYN, IVF, NICU, Labour & Delivery",
                "6th" to "Laboratory, Blood Bank, HDU",
                "5th" to "Cath Lab, Operation Theatre",
                "4th" to "ICU, CCU, Neuro ICU",
                "3rd" to "Nephrology, Dialysis, Radiology",
                "2nd" to "General Surgery, Neuro Surgery, Urology",
                "1st" to "Medicine, Neuromedicine, Cardiology",
                "Ground" to "Emergency-1, Information",
                "Basement" to "Emergency-2"
            )
        )
        "birdem" -> HospitalData(
            id = "birdem",
            name = "BIRDEM General Hospital",
            banglaName = "বারডেম জেনারেল হাসপাতাল",
            type = "Specialized / Tertiary Care",
            category = "Specialized (Diabetes & More)",
            location = "Shahbag, Dhaka",
            address = "122 Kazi Nazrul Islam Avenue, Shahbagh, Dhaka-1000, Bangladesh",
            emergencyHotline = "02-41060479",
            ambulance = "02-41060501",
            icuHotline = "02-41060483",
            infoDesk = "02-41060501", // Extension handled via dial
            landlines = listOf("02-41060501"),
            opdItems = listOf(
                OpdInfo("Endocrinology", "111", "Room 111: 7:30 AM - 2:00 PM"),
                OpdInfo("Nephrology", "Counter", "Daily 7:30 AM - 2:00 PM"),
                OpdInfo("Diet & Nutrition", "1st Floor", "Room 247: Counselling center"),
                OpdInfo("Internal Medicine", "Counter", "Various clinical departments"),
                OpdInfo("Cardiology", "Counter", "Specialist consultation")
            ),
            departments = listOf(
                "Endocrinology", "Diabetology", "Nephrology", "Cardiology", "Neurology",
                "Surgery", "Urology", "Orthopaedics", "Ophthalmology", "ENT",
                "Dermatology", "Paediatrics", "Neonatology", "OBGYN", "Psychiatry"
            ),
            facilities = listOf(
                "ICU", "Dialysis", "Kidney Transplant", "Laboratory", "Blood Bank",
                "Radiology", "Pharmacy", "Diabetes Research"
            ),
            doctorSections = listOf(
                DoctorSection("Endocrinology & Diabetology", listOf(
                    "Prof. Md. Faruque Pathan", "Dr. Faria Afsana", "Dr. Kazi Nazmul Hossain", 
                    "Dr. Md. Feroz Amin", "Dr. Rushda Sharmin Binte Rouf"
                )),
                DoctorSection("Cardiology", listOf("Prof. Md. Zahid Alam")),
                DoctorSection("Neurology", listOf("Dr. Rumana Habib", "Prof. Anisul Haque")),
                DoctorSection("Nephrology", listOf(
                    "Dr. Tufayel Ahmed Chowdhury", "Prof. Wasim Md. Mohosin Ul Haque", "Dr. Muhammad Abdur Rahim"
                )),
                DoctorSection("Ophthalmology", listOf(
                    "Prof. Ashraf Sayeed", "Prof. Manash Kumar Goswami", "Dr. Ferdous Akhter Jolly"
                )),
                DoctorSection("Paediatrics", listOf(
                    "Prof. Md. Abid Hossain Mollah", "Prof. Fauzia Mohsin", "Dr. Jebun Nahar"
                ))
            )
        )
        "nidch" -> HospitalData(
            id = "nidch",
            name = "National Institute of Diseases of the Chest & Hospital",
            banglaName = "জাতীয় বক্ষব্যাধি ইনস্টিটিউট ও হাসপাতাল — NIDCH",
            type = "Government Specialized Postgraduate Institute & Hospital (Est. 1955)",
            category = "Respiratory Care & Chest Diseases (National / Tertiary)",
            beds = "870",
            location = "Mohakhali, Dhaka-1212",
            address = "Mohakhali, Dhaka-1212, Bangladesh",
            emergencyHotline = "02-55067149",
            emergencyMobile = "01701-248042",
            ambulance = "02-55067149",
            infoDesk = "01712-534683",
            landlines = listOf("02-55067140", "02-55067146", "02-55067149"),
            opdItems = listOf(
                OpdInfo("Chest Medicine OPD", "Counter", "Pulmonary conditions & respiratory care"),
                OpdInfo("Respiratory Medicine", "Counter", "Asthma, COPD, Pneumonia, ILD"),
                OpdInfo("Tuberculosis (TB) Care", "Counter", "Diagnosis, treatment & DR-TB care"),
                OpdInfo("Thoracic Surgery OPD", "Counter", "Chest & lung-related surgical evaluation"),
                OpdInfo("Diagnostic Services", "Laboratory", "Pathology, X-ray & PFT assessment")
            ),
            departments = listOf(
                "Respiratory Medicine", "Chest Medicine", "Tuberculosis (TB) Care", "Thoracic Surgery",
                "Radiology & Imaging", "Pathology & Laboratory", "Respiratory Diagnostic Services",
                "Emergency 24/7", "IPD & Operation Theatre", "Pharmacy"
            ),
            facilities = listOf(
                "24/7 Emergency", "OPD", "IPD", "Operation Theatre", "Pharmacy",
                "Pathology & Blood Testing", "Ambulance Service", "Chest X-Ray & Radiology", "Respiratory Diagnostics"
            ),
            doctorSections = listOf(
                DoctorSection("Director & Facility Head", listOf(
                    "Golam Sarwar Liaquat Hossain Bhuiyan (Director, NIDCH)"
                )),
                DoctorSection("Respiratory Specialists & Surgeons", listOf(
                    "Senior Consultants in Pulmonology",
                    "Thoracic Surgeons",
                    "Chest Medicine Specialists"
                ))
            )
        )
        "nicvd" -> HospitalData(
            id = "nicvd",
            name = "National Institute of Cardiovascular Diseases",
            banglaName = "জাতীয় হৃদরোগ ইনস্টিটিউট ও হাসপাতাল — NICVD",
            type = "Government Specialized Institute & Hospital (Est. 1978)",
            category = "Cardiovascular Care (National / Tertiary)",
            beds = "1,200",
            location = "Sher-e-Bangla Nagar, Dhaka-1207",
            address = "Sher-e-Bangla Nagar, Agargaon, Dhaka-1207, Bangladesh",
            emergencyHotline = "02-9122560-74",
            emergencyMobile = "01701248041",
            ambulance = "02-9122560-74",
            infoDesk = "01348176897",
            landlines = listOf("02-9122560", "02-9122574"),
            opdItems = listOf(
                OpdInfo("Adult Cardiology", "Counter", "Consultation, ECG, Echo, Stress Test"),
                OpdInfo("Paediatric Cardiology", "Counter", "Specialized children's heart care"),
                OpdInfo("Pacemaker & ICD Clinic", "Cardiology Dept", "Device monitoring & programming"),
                OpdInfo("Hypertension Clinic", "Cardiology Dept", "Blood pressure management"),
                OpdInfo("Rheumatic Fever / RHD Clinic", "Cardiology Dept", "Preventive & curative care"),
                OpdInfo("Cardiac Surgery OPD", "Surgery Dept", "Bypass, Valve replacement pre-op/post-op")
            ),
            departments = listOf(
                "Adult Cardiology", "Paediatric Cardiology", "Adult Cardiac Surgery", "Paediatric Cardiac Surgery",
                "Vascular Surgery", "Cardiac Anaesthesiology", "Radiology & Imaging", "Pathology",
                "Blood Bank", "Physical Medicine & Rehabilitation", "Anatomy", "Physiology", "GASBOS"
            ),
            facilities = listOf(
                "24/7 Cardiac Emergency", "CCU", "ICU", "Cath Lab", "Operation Theatre",
                "Echocardiography", "Treadmill Test (ETT)", "Holter Monitoring", "Stress Echo",
                "Non-invasive Vascular Lab", "Blood Bank", "Pathology", "Radiology & MRI"
            ),
            doctorSections = listOf(
                DoctorSection("Director & Professor", listOf(
                    "Prof. Dr. Abdul Wadud Chowdhury (Director-Cum-Professor)"
                )),
                DoctorSection("Cardiology & Cardiac Surgery Faculty", listOf(
                    "Senior Professors & Consultants",
                    "Associate Professors of Cardiology",
                    "Assistant Professors of Cardiac Surgery"
                ))
            )
        )
        "dmch" -> HospitalData(
            id = "dmch",
            name = "Dhaka Medical College Hospital",
            banglaName = "ঢাকা মেডিকেল কলেজ হাসপাতাল — DMCH",
            type = "Government Medical College",
            category = "Tertiary Care (National)",
            location = "Shahbagh, Dhaka",
            address = "Secretariat Road, Shahbagh, Dhaka-1000, Bangladesh",
            emergencyHotline = "02-55165001",
            emergencyMobile = "01715-016984",
            ambulance = "02-55165001",
            infoDesk = "01701-248097",
            opdItems = listOf(
                OpdInfo("General OPD", "Counter", "Consult Information Desk for specific rooms"),
                OpdInfo("Medicine & Surgery", "Counter", "Daily service available"),
                OpdInfo("Paediatrics", "Counter", "Child emergency care 24/7"),
                OpdInfo("Gynaecology", "Counter", "Specialized women care")
            ),
            departments = listOf(
                "Medicine", "Cardiology", "Neurology", "Nephrology", "Gastroenterology",
                "Burn & Plastic Surgery", "Neurosurgery", "Orthopaedics", "Urology",
                "Paediatrics", "Neonatology", "OBGYN", "Ophthalmology", "ENT", "Psychiatry"
            ),
            facilities = listOf(
                "24/7 Emergency", "Trauma Care", "Burn Unit", "ICU", "Blood Bank",
                "Radiology & MRI", "Pathology", "Forensic Medicine", "Pharmacy"
            )
        )
        "cmch" -> HospitalData(
            id = "cmch",
            name = "Chittagong Medical College Hospital",
            banglaName = "চট্টগ্রাম মেডিকেল কলেজ হাসপাতাল — CMCH",
            type = "Government Medical College",
            category = "Tertiary Care (Regional)",
            beds = "2,335",
            location = "Panchlaish, Chattogram",
            address = "Fazal Quader Road, Panchlaish, Chattogram, Bangladesh",
            emergencyHotline = "02-41356220",
            emergencyMobile = "01701248092",
            ambulance = "02-41356220",
            icuHotline = "02-41356220",
            infoDesk = "01701248092",
            landlines = listOf("02-333360179", "02-333350176"),
            opdItems = listOf(
                OpdInfo("General OPD", "Counter", "Consult Information Counter for schedules"),
                OpdInfo("Medicine & Surgery", "Counter", "Daily service available"),
                OpdInfo("Paediatrics", "Counter", "Regular & Emergency care"),
                OpdInfo("Cardiology", "Counter", "Specialized cardiac evaluation")
            ),
            departments = listOf(
                "Medicine", "Surgery", "Cardiology", "Cardiac Surgery", "Neurology",
                "Neurosurgery", "Nephrology", "Gastroenterology", "Paediatrics",
                "OBGYN", "Orthopaedics", "ENT", "Ophthalmology", "Dermatology", "Dental"
            ),
            facilities = listOf(
                "24/7 Emergency", "ICU (60 Beds)", "CCU (15 Beds)", "Dialysis (13 Beds)",
                "Blood Bank", "Radiology & Imaging", "Pathology", "Pharmacy"
            ),
            floorGuide = listOf(
                "Main Bldg" to "General Hospital Services",
                "Emergency" to "24/7 Trauma & Emergency Care",
                "Cardiac" to "Cardiac Surgery & Evaluation",
                "Outdoor" to "OPD & Ward Building",
                "Dental" to "Specialized Dental Unit"
            )
        )
        "cmosh" -> HospitalData(
            id = "cmosh",
            name = "Chattagram Maa-O-Shishu Hospital",
            banglaName = "চট্টগ্রাম মা ও শিশু হাসপাতাল",
            type = "Non-profit / Non-government",
            category = "Tertiary Care (Mother & Child)",
            beds = "899",
            location = "Agrabad, Chattogram",
            address = "Agrabad, Chattogram-4100, Bangladesh",
            emergencyHotline = "+880-31-2511432",
            emergencyMobile = "+880-31-2525822",
            ambulance = "+880-31-2511432",
            icuHotline = "+880-31-2511457",
            infoDesk = "+880-31-2511432",
            landlines = listOf("+880-31-2511432", "+880-31-2525822", "+880-2-333312365"),
            opdItems = listOf(
                OpdInfo("Paediatrics", "1", "General & Special child care"),
                OpdInfo("Neonatology", "1", "Newborn & NICU follow-up"),
                OpdInfo("OBGYN", "2", "Maternity & Pregnancy care"),
                OpdInfo("Internal Medicine", "3", "Adult medical conditions"),
                OpdInfo("General Surgery", "3", "Surgical consultations"),
                OpdInfo("Nephrology", "4", "Kidney care & Dialysis"),
                OpdInfo("Cancer Care", "New Bldg", "Oncology & Radiotherapy")
            ),
            departments = listOf(
                "Paediatrics", "Neonatology", "Paediatric Surgery", "Paediatric Neurology",
                "Obstetrics & Gynaecology", "Medicine", "Surgery", "Orthopaedics",
                "Ophthalmology", "ENT", "Dermatology", "Gastroenterology", "Nephrology",
                "Urology", "Psychiatry", "Radiology", "Oncology", "Dentistry"
            ),
            facilities = listOf(
                "ICU", "NICU", "PICU", "Cancer Institute", "Autism Institute",
                "Blood Transfusion", "Dialysis", "24/7 Emergency", "Ambulance"
            ),
            floorGuide = listOf(
                "13-Floor" to "Multi-storied specialized building",
                "New Bldg" to "Cancer Institute & Research Center",
                "Institute" to "Autism & Child Development Center"
            )
        )
        "nins" -> HospitalData(
            id = "nins",
            name = "National Institute of Neurosciences & Hospital",
            banglaName = "ন্যাশনাল ইনস্টিটিউট অব নিউরোসায়েন্সেস অ্যান্ড হাসপাতাল",
            type = "Government Postgraduate Institute",
            category = "Tertiary Care (National)",
            beds = "500",
            location = "Agargaon, Dhaka",
            address = "Sher-E-Bangla Nagar, Agargaon, Dhaka-1207, Bangladesh",
            emergencyHotline = "+880-2-9140752",
            emergencyMobile = "+880-2-41024572",
            ambulance = "+880-2-41024584",
            icuHotline = "+880-2-9140752",
            infoDesk = "+880-2-41024584",
            landlines = listOf("+880-2-41024570", "+880-2-41024575", "01729-053327"),
            opdItems = listOf(
                OpdInfo("Stroke Unit", "201", "Sat, Mon, Wed (8 AM - 2 PM)"),
                OpdInfo("Epilepsy Clinic", "204", "Sun, Tue, Thu (8 AM - 2 PM)"),
                OpdInfo("Parkinson's Clinic", "205", "Sat, Tue (8 AM - 2 PM)"),
                OpdInfo("EMG Clinic", "208", "Mon, Thu (8 AM - 2 PM)"),
                OpdInfo("Pediatric Neuro", "1st Floor", "Specialized child care")
            ),
            departments = listOf(
                "Neurology", "Neurosurgery", "Pediatric Neurology", "Neurophysiology",
                "Neuro-Radiology", "Neuro-Intervention", "Neuro-Rehabilitation",
                "Neuropathology", "Stroke Care", "Critical Care Medicine"
            ),
            facilities = listOf(
                "3 Tesla MRI", "128-Slice CT", "100-Bed Stroke Unit", "EEG / EMG / NCV",
                "Neuro ICU", "HDU", "Angiography", "24/7 Neuro Emergency"
            ),
            floorGuide = listOf(
                "8th-10th" to "In-patient Wards & Cabins",
                "4th-5th" to "ICU, HDU & Neurology Wards",
                "2nd-3rd" to "Diagnostics (MRI, CT, EEG)",
                "Ground" to "Emergency & Stroke Unit"
            )
        )
        "ssmc" -> HospitalData(
            id = "ssmc",
            name = "Sir Salimullah Medical College & Mitford Hospital",
            banglaName = "স্যার সলিমুল্লাহ মেডিকেল কলেজ ও মিটফোর্ড হাসপাতাল",
            type = "Government Medical College Hospital",
            category = "Tertiary Care (National)",
            location = "Mitford, Old Dhaka",
            address = "Mitford Road, Dhaka-1206, Bangladesh",
            emergencyHotline = "02-57319935",
            emergencyMobile = "01742-183855", // Casualty
            ambulance = "01322-465805", // Help Desk
            icuHotline = "02-57319935",
            infoDesk = "01322-465805",
            landlines = listOf("02-57319935", "01742183855"),
            opdItems = listOf(
                OpdInfo("General OPD", "Bldg 2", "Medicine, Surgery, Gynae"),
                OpdInfo("Specialized", "Bldg 2", "Breast Clinic, Infertility"),
                OpdInfo("Dialysis", "Bldg 3", "33 Machines available"),
                OpdInfo("Therapy", "Bldg 2", "Physiotherapy & Rehab")
            ),
            departments = listOf(
                "Medicine", "Surgery", "Cardiology", "Cardiac Surgery", "OBGYN",
                "Paediatrics", "Orthopaedics", "ENT", "Ophthalmology", "Dermatology",
                "Haematology", "Nephrology", "Dental", "Physical Medicine"
            ),
            facilities = listOf(
                "24/7 Emergency", "Casualty Block", "CCU", "Dialysis Unit",
                "Blood Bank", "Clinical Pathology", "Chemotherapy", "Pharmacy"
            ),
            floorGuide = listOf(
                "Building 1" to "Admin, Gynae, Paediatrics, Emergency",
                "Building 2" to "Outdoor, Pathology, Cardiology, Surgery",
                "Building 3" to "Medicine & Allied, Haematology, Dialysis"
            )
        )
        else -> HospitalData()
    }
}
