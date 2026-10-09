package com.srcreation.smarthelp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class ProTipsActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        tts = TextToSpeech(this, this)

        val categoryFilter = intent.getStringExtra("CATEGORY_FILTER") ?: ""

        setContent {
            HelpsTheme {
                ProTipsScreen(
                    initialCategory = categoryFilter,
                    onBack = { finish() },
                    onSpeak = { text, isBangla -> speakText(text, isBangla) }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
        }
    }

    private fun speakText(text: String, isBangla: Boolean) {
        if (isTtsReady && tts != null) {
            val loc = if (isBangla) Locale("bn", "BD") else Locale.US
            tts?.language = loc
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TipTTS")
        } else {
            Toast.makeText(this, "TTS Engine Initializing...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProTipsScreen(
    initialCategory: String,
    onBack: () -> Unit,
    onSpeak: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val sharedPrefs = remember { context.getSharedPreferences("pro_tips_prefs", Context.MODE_PRIVATE) }

    // State for language mode (true = Bangla, false = English)
    var isBangla by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf(if (initialCategory.isNotBlank() && initialCategory != "Daily Tips" && initialCategory != "প্রো টিপস") initialCategory else "") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    // Saved Bookmarked Tip IDs
    var savedBookmarkIds by remember {
        mutableStateOf(
            sharedPrefs.getStringSet("bookmarked_tips", emptySet())
                ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        )
    }

    fun toggleBookmark(id: Int) {
        val newSet = if (savedBookmarkIds.contains(id)) {
            savedBookmarkIds - id
        } else {
            savedBookmarkIds + id
        }
        savedBookmarkIds = newSet
        sharedPrefs.edit().putStringSet("bookmarked_tips", newSet.map { it.toString() }.toSet()).apply()
    }

    val allTips = remember { DailyTipsRepository.getAllTips() }
    val featuredTip = remember { DailyTipsRepository.getTipOfTheDay() }

    val categories = remember(isBangla) {
        if (isBangla) {
            listOf("সব", "পছন্দের টিপস") + DailyTipsRepository.getCategoriesBn().drop(1)
        } else {
            listOf("All", "Bookmarks") + DailyTipsRepository.getCategoriesEn().drop(1)
        }
    }

    val filteredTips = remember(searchQuery, selectedCategoryIndex, isBangla, savedBookmarkIds) {
        allTips.filter { tip ->
            // Category Filter
            val matchesCategory = when (selectedCategoryIndex) {
                0 -> true
                1 -> savedBookmarkIds.contains(tip.id) // Bookmarks
                else -> {
                    val categoryName = categories.getOrNull(selectedCategoryIndex) ?: ""
                    if (isBangla) tip.categoryBn.contains(categoryName, ignoreCase = true)
                    else tip.categoryEn.contains(categoryName, ignoreCase = true)
                }
            }

            // Search Query Filter
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim()
                tip.titleEn.contains(q, ignoreCase = true) ||
                        tip.titleBn.contains(q, ignoreCase = true) ||
                        tip.bodyEn.contains(q, ignoreCase = true) ||
                        tip.bodyBn.contains(q, ignoreCase = true) ||
                        tip.id.toString() == q
            }

            matchesCategory && matchesSearch
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    shadowElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (isBangla) "প্রিমিয়াম স্বাস্থ্য টিপস" else "Premium Health Tips",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Color.White else Color(0xFF1A237E)
                                    )
                                    Text(
                                        text = if (isBangla) "প্রতিদিনের সুস্বাস্থ্য ম্যানুয়াল" else "Daily Wellness Handbook",
                                        fontSize = 11.sp,
                                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onBack()
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                                }
                            },
                            actions = {
                                Surface(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isBangla = !isBangla
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) "English" else "বাংলা",
                                        color = if (isDark) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            placeholder = {
                                Text(
                                    text = if (isBangla) "টিপস খুঁজুন (যেমন: পানি, প্রেশার, ঘুম...)" else "Search tips (e.g. water, sleep, diet...)",
                                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Gray,
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f),
                                focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.7f),
                                unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.7f)
                            )
                        )

                        // Horizontal Category Chips Row
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories.size) { index ->
                                val catName = categories[index]
                                val isSelected = selectedCategoryIndex == index
                                val chipColor = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.8f))
                                val textColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155))

                                Surface(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedCategoryIndex = index
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = chipColor,
                                    border = BorderStroke(1.dp, if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1))),
                                    modifier = Modifier.shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(14.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (index == 1) { // Bookmarks tab icon
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = if (isSelected) (if (isDark) Color.Black else Color.White) else Color(0xFFEF4444),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = catName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                ScreenEntranceAnimation {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tip of the Day Special Featured Section (Only when no search query and tab 0)
                        if (searchQuery.isBlank() && selectedCategoryIndex == 0) {
                            item {
                                FeaturedTipCard(
                                    tip = featuredTip,
                                    isBangla = isBangla,
                                    isBookmarked = savedBookmarkIds.contains(featuredTip.id),
                                    onBookmarkToggle = { toggleBookmark(featuredTip.id) },
                                    onSpeak = onSpeak
                                )
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBangla) "স্বাস্থ্য টিপসের তালিকা (${filteredTips.size})" else "Health Tips List (${filteredTips.size})",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = if (isDark) Color.White else Color(0xFF1A237E)
                                    )
                                    Surface(
                                        color = Color(0xFF22C55E).copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (isBangla) "প্রিমিয়াম কালেকশন" else "Premium Collection",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF22C55E)
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredTips.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (isBangla) "কোনো টিপস পাওয়া যায়নি।" else "No health tips found.",
                                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // List of 1000 Tips
                        items(filteredTips, key = { it.id }) { tip ->
                            DailyTipCard(
                                tip = tip,
                                isBangla = isBangla,
                                isBookmarked = savedBookmarkIds.contains(tip.id),
                                onBookmarkToggle = { toggleBookmark(tip.id) },
                                onSpeak = onSpeak
                            )
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedTipCard(
    tip: DailyTip,
    isBangla: Boolean,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onSpeak: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val title = if (isBangla) tip.titleBn else tip.titleEn
    val body = if (isBangla) tip.bodyBn else tip.bodyEn
    val category = if (isBangla) tip.categoryBn else tip.categoryEn

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E)),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(if (isDark) Color(0xFF38BDF8) else Color.White, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.5f))))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A237E),
                            Color(0xFF283593)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFF59E0B),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBangla) "আজকের সেরা টিপস #${tip.id}" else "Tip of the Day #${tip.id}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSpeak(body, isBangla)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Read Aloud", tint = Color.White)
                        }

                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBookmarkToggle()
                        }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) Color(0xFFF59E0B) else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = body,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        TextButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, "💡 $title\n\n$body\n\n— Shared via Smart Help App")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Tip"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBangla) "শেয়ার করুন" else "Share",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyTipCard(
    tip: DailyTip,
    isBangla: Boolean,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onSpeak: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var isExpanded by remember { mutableStateOf(false) }

    val title = if (isBangla) tip.titleBn else tip.titleEn
    val body = if (isBangla) tip.bodyBn else tip.bodyEn
    val category = if (isBangla) tip.categoryBn else tip.categoryEn

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isExpanded = !isExpanded
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "#${tip.id}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.2f else 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSpeak(body, isBangla)
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Read", tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBookmarkToggle()
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFFEF4444) else (if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else Color(0xFF1E293B),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = body,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569),
                lineHeight = 19.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Health Tip", "$title\n\n$body")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, if (isBangla) "কপি করা হয়েছে!" else "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, "💡 $title\n\n$body\n\n— Smart Help App")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Tip"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
