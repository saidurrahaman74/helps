package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.NotificationEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                NotificationScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(viewModel: NotificationViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val notifications by viewModel.allNotifications.collectAsState(initial = emptyList())
    var selectedIds = remember { mutableStateListOf<Int>() }
    var isSelectionMode by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isSelectionMode) "${selectedIds.size} Selected" else "Notifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color.White else Color(0xFF1A237E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedIds.clear()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(if (isSelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            viewModel.deleteMultiple(selectedIds.toList())
                            isSelectionMode = false
                            selectedIds.clear()
                        }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    } else if (notifications.isNotEmpty()) {
                        IconButton(onClick = { isSelectionMode = true }) {
                            Icon(Icons.Default.Edit, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                        }
                    }
                    LanguageToggleButton()
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = if (isDark) Color.White.copy(alpha = 0.5f) else Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No notifications yet.", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { item ->
                        NotificationCard(
                            item = item,
                            isSelected = selectedIds.contains(item.id),
                            isSelectionMode = isSelectionMode,
                            isDark = isDark,
                            onToggleSelect = {
                                if (selectedIds.contains(item.id)) selectedIds.remove(item.id)
                                else selectedIds.add(item.id)
                            },
                            onCheck = {
                                viewModel.deleteNotification(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    item: NotificationEntity, 
    isSelected: Boolean, 
    isSelectionMode: Boolean,
    isDark: Boolean,
    onToggleSelect: () -> Unit,
    onCheck: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (isSelectionMode) onToggleSelect()
                else onCheck()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.15f) else (if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f))
        ),
        border = if (isSelected) BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else if (isDark) BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)) else null
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSelectionMode) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect() })
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(0.15f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Notifications, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                Text(item.message, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray, lineHeight = 18.sp)
            }
            
            if (!isSelectionMode) {
                IconButton(onClick = onCheck) {
                    Icon(Icons.Default.Done, "Check", tint = Color(0xFF4CAF50))
                }
            }
        }
    }
}
