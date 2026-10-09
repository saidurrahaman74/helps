package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.srcreation.smarthelp.ui.theme.HelpsTheme

class ContentDisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val categoryId = intent.getStringExtra("CATEGORY_ID") ?: ""
        val subCategory = intent.getStringExtra("SUB_CATEGORY") ?: ""
        val categoryTitle = intent.getStringExtra("CATEGORY_TITLE") ?: "Details"
        val colorHex = intent.getStringExtra("CATEGORY_COLOR") ?: "#1A237E"

        setContent {
            HelpsTheme {
                ContentDisplayScreen(
                    categoryId = categoryId,
                    subCategory = subCategory,
                    title = "$categoryTitle - $subCategory",
                    colorHex = colorHex,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDisplayScreen(
    categoryId: String,
    subCategory: String,
    title: String,
    colorHex: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val db = remember { FirebaseFirestore.getInstance() }
    var items by remember { mutableStateOf<List<DetailItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val themeColor = remember(colorHex) { colorFromHex(colorHex) }

    DisposableEffect(categoryId, subCategory) {
        val listener = db.collection("items")
            .whereEqualTo("categoryId", categoryId)
            .whereEqualTo("subCategory", subCategory)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    items = value.toObjects(DetailItem::class.java)
                }
                isLoading = false
            }
            
        onDispose {
            listener.remove()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(title, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = { LanguageToggleButton() },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = themeColor)
                    }
                } else if (items.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No information found for \"$subCategory\"", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items) { item ->
                            val icon = getIconByName("Hospital")
                            InfoDetailGridCard(item, themeColor, icon) // Defaulting icon for now
                        }
                    }
                }
            }
        }
    }
}
