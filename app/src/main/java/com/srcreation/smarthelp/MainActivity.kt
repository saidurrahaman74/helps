package com.srcreation.smarthelp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.lifecycleScope
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : AppCompatActivity() {
    private var keepSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            splashScreen.setKeepOnScreenCondition { keepSplashScreen }
            lifecycleScope.launch {
                delay(2000.milliseconds)
                keepSplashScreen = false
            }
        } else {
            keepSplashScreen = false
        }

        enableEdgeToEdge()
        val prefManager = PreferenceManager(this)
        AppCompatDelegate.setDefaultNightMode(prefManager.getThemeMode())

        checkAndShowOwnershipDialogIfNeeded()

        setContent {
            HelpsTheme {
                var isAuthenticated by rememberSaveable { mutableStateOf(false) }
                var showDisclaimer by rememberSaveable { mutableStateOf(!prefManager.isDisclaimerAccepted()) }
                val hasLockSet = remember { prefManager.getAppLock() != null }

                LaunchedEffect(Unit) {
                    if (isAuthenticated) return@LaunchedEffect
                    
                    if (!hasLockSet) {
                        isAuthenticated = true
                    } else if (prefManager.isBiometricLockEnabled()) {
                        val biometricHelper = BiometricHelper(this@MainActivity)
                        biometricHelper.showBiometricPrompt(
                            onSuccess = { isAuthenticated = true },
                            onError = { /* PIN fallback */ }
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    if (showDisclaimer) {
                        MedicalDisclaimerDialog(
                            onAccept = {
                                prefManager.setDisclaimerAccepted(true)
                                showDisclaimer = false
                            }
                        )
                    } else if (isAuthenticated) {
                        StartScreen(
                            onPatientClick = { startActivity(Intent(this@MainActivity, HomeActivity::class.java)) },
                            onNurseClick = { startActivity(Intent(this@MainActivity, NurseHubActivity::class.java)) },
                            onWomenCareClick = { startActivity(Intent(this@MainActivity, WomenCareActivity::class.java)) },
                            greetingResId = getGreeting(),
                        )
                    } else {
                        AppLockScreen(
                            prefManager = prefManager,
                            onUnlocked = { isAuthenticated = true }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MedicalDisclaimerDialog(onAccept: () -> Unit) {
        val haptic = LocalHapticFeedback.current
        val isDark = isSystemInDarkTheme()
        Dialog(onDismissRequest = { /* Must Accept */ }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = if (isDark) Color(0xFF0F172A) else Color.White,
                border = BorderStroke(2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.disclaimer_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color.White else Color(0xFF1A237E)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(id = R.string.disclaimer_msg),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAccept()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF0284C7) else Color(0xFF1A237E))
                    ) {
                        Text(
                            text = stringResource(id = R.string.accept_agree),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }


    private fun changeLanguage(localeCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(localeCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    private var isConflictDialogShowing = false

    override fun onResume() {
        super.onResume()
        checkAndShowOwnershipDialogIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkAndShowOwnershipDialogIfNeeded()
    }

    private fun checkAndShowOwnershipDialogIfNeeded() {
        if (isConflictDialogShowing) return
        val prefManager = PreferenceManager(this)
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ownerUid = prefManager.getLocalDataOwnerUid()

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val hasData = LocalDataOwnership.hasUserHealthData(this@MainActivity)
            val decision = LocalDataOwnership.evaluateGateDecision(ownerUid, currentUid, hasData)

            if (decision == GateDecision.REDIRECT_TO_MAIN) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (isConflictDialogShowing) return@withContext
                    isConflictDialogShowing = true
                    if (ownerUid.isNullOrBlank()) {
                        val count = LocalDataOwnership.countUserHealthRecords(this@MainActivity)
                        val newestDate = LocalDataOwnership.getNewestRecordDate(this@MainActivity)
                        showUnsetOwnerConflictDialog(prefManager, currentUid, count, newestDate)
                    } else {
                        showDataOwnerConflictDialog()
                    }
                }
            } else if (ownerUid.isNullOrBlank() && !hasData) {
                LocalDataOwnership.claimIfFresh(this@MainActivity, currentUid)
            }
        }
    }

    private val ALLOWED_TARGET_ACTIVITIES = setOf(
        HomeActivity::class.java.name,
        NurseHubActivity::class.java.name,
        WomenCareActivity::class.java.name
    )

    private fun onOwnershipResolved() {
        isConflictDialogShowing = false
        val targetClass = intent.getStringExtra("TARGET_ACTIVITY")
        intent.removeExtra("TARGET_ACTIVITY")

        if (!targetClass.isNullOrBlank() && ALLOWED_TARGET_ACTIVITIES.contains(targetClass)) {
            try {
                val clazz = Class.forName(targetClass)
                startActivity(Intent(this, clazz))
            } catch (e: Exception) {
                Log.e("MainActivity", "Error launching target activity $targetClass", e)
            }
        } else {
            recreate()
        }
    }

    private fun showDataOwnerConflictDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.owner_mismatch_title))
            .setMessage(getString(R.string.owner_mismatch_msg))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.btn_keep_previous)) { _, _ ->
                intent.removeExtra("TARGET_ACTIVITY")
                AuthRepository().logout()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .show()
    }

    private fun showUnsetOwnerConflictDialog(prefManager: PreferenceManager, currentUid: String, count: Int, newestDate: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.owner_unset_title))
            .setMessage(getString(R.string.owner_unset_msg, count, newestDate))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.btn_data_is_mine)) { _, _ ->
                LocalDataOwnership.claimByUserChoice(this, currentUid)
                SyncWorker.enqueue(this)
                onOwnershipResolved()
            }
            .setNegativeButton(getString(R.string.btn_belongs_to_other)) { _, _ ->
                intent.removeExtra("TARGET_ACTIVITY")
                BackgroundCheckScheduler.cancelWorkers(this)
                AuthRepository().logout()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .show()
    }

    private fun getGreeting(): Int {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        if ((month == Calendar.JANUARY) && (day == 1)) return R.string.special_day_new_year
        return when (hour) {
            in 5..11 -> R.string.good_morning
            in 12..14 -> R.string.good_noon
            in 15..17 -> R.string.good_afternoon
            in 18..20 -> R.string.good_evening
            else -> R.string.good_night
        }
    }
}

@Composable
fun RoleEntryCard(
    title: String,
    subtitle: String,
    roleType: String, // "patient" or "nurse"
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")
    val rotateX by animateFloatAsState(if (isPressed) -10f else 0f, label = "rotateX")
    
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationX = rotateX
                cameraDistance = 12f * density
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp, Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f))
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f)) else listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stylish Floating Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(28.dp)) {
                        when (roleType) {
                            "patient" -> PatientIconDrawing(color)
                            "nurse" -> NurseIconDrawing(color)
                            "women" -> WomenCareIconDrawing(color)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 15.sp, // Reduced for a more refined look
                            color = if (isDark) Color.White else Color(0xFF1A237E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (roleType == "nurse") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = color.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF1A237E).copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    onPatientClick: () -> Unit,
    onNurseClick: () -> Unit,
    onWomenCareClick: () -> Unit,
    greetingResId: Int
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var showExitDialog by remember { mutableStateOf(value = false) }
    val animatableAlpha = remember { Animatable(0f) }
    val animatableScale = remember { Animatable(0.8f) }
    val animatableSlide = remember { Animatable(50f) }

    // Floating animation for orbs and 3D effect
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val orbOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "offset"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floating"
    )
    
    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        Dialog(onDismissRequest = { showExitDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(32.dp),
                color = if (isDark) Color(0xFF0F172A) else Color.White,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color(0xFFEF4444), modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(id = R.string.exit_app), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(id = R.string.exit_desc), textAlign = TextAlign.Center, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showExitDialog = false
                        }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(id = R.string.stay), color = if (isDark) Color.White else Color(0xFF1A237E))
                        }
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                (context as? Activity)?.finishAffinity()
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF0284C7) else Color(0xFF1A237E))
                        ) {
                            Text(stringResource(id = R.string.exit_now), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        launch { animatableAlpha.animateTo(1f, tween(1500)) }
        launch { animatableScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        launch { animatableSlide.animateTo(0f, tween(1200, easing = EaseOutExpo)) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    LanguageToggleButton()
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = AppGradientBrush)
                .padding(innerPadding)
        ) {
            
            // Decorative Aura Orbs
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.size(380.dp).offset(x = (-120).dp, y = (50 + orbOffset).dp).blur(100.dp),
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFFBAE6FD).copy(alpha = 0.4f)
                ) {}
                Surface(
                    modifier = Modifier.size(320.dp).align(Alignment.BottomEnd).offset(x = (120).dp, y = (-120 - orbOffset).dp).blur(80.dp),
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF818CF8).copy(alpha = 0.12f) else Color(0xFFCFFAFE).copy(alpha = 0.3f)
                ) {}
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(id = greetingResId),
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF1A237E).copy(alpha = 0.5f), letterSpacing = 1.sp),
                        modifier = Modifier.graphicsLayer(alpha = animatableAlpha.value)
                    )
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = floatingOffset
                                val s = 1f + (floatingOffset / -200f)
                                scaleX = animatableScale.value * s
                                scaleY = animatableScale.value * s
                                alpha = animatableAlpha.value
                            }
                    ) {
                        // Logo with Glow
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                modifier = Modifier
                                    .size(110.dp)
                                    .blur(20.dp),
                                shape = CircleShape,
                                color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF3B82F6)).copy(alpha = 0.2f)
                            ) {}
                            AppLogo(size = 72.dp, showBackground = false, cornerRadius = 20.dp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Futuristic Animated Gradient Text
                        val infiniteTransition2 = rememberInfiniteTransition(label = "gradient")
                        val offset by infiniteTransition2.animateFloat(
                            initialValue = 0f,
                            targetValue = 1000f,
                            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
                            label = "offset"
                        )

                        Box(contentAlignment = Alignment.Center) {
                            // Text Glow layer
                            Text(
                                text = "SmartHelp",
                                style = TextStyle(
                                    fontSize = 24.sp, 
                                    fontWeight = FontWeight.Black,
                                    color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.15f),
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.blur(4.dp).offset(y = 1.dp)
                            )
                            
                            // Main Animated Professional Text
                            Text(
                                text = "SmartHelp",
                                style = TextStyle(
                                    brush = Brush.linearGradient(
                                        colors = if (isDark) listOf(Color.White, Color(0xFF38BDF8), Color.White) else listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF1A237E)),
                                        start = Offset(offset, offset),
                                        end = Offset(offset + 350f, offset + 350f)
                                    ),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }

                // Middle Section (Interactive Hub)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 60.dp)
                        .graphicsLayer(alpha = animatableAlpha.value, translationY = animatableSlide.value),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    var isRoleSelectionVisible by remember { mutableStateOf(false) }

                    AnimatedContent(
                        targetState = isRoleSelectionVisible,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f))
                                .togetherWith(fadeOut(animationSpec = tween(300)))
                        },
                        label = "hub_content"
                    ) { showRoles ->
                        if (!showRoles) {
                            // Modernized Get Started Button
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isRoleSelectionVisible = true
                                    },
                                    modifier = Modifier
                                        .width(172.dp)
                                        .height(46.dp)
                                        .shadow(12.dp, RoundedCornerShape(23.dp), spotColor = (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(23.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = if (isDark) listOf(Color(0xFF0284C7), Color(0xFF38BDF8)) else listOf(Color(0xFF1A237E), Color(0xFF3949AB))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.get_started).uppercase(),
                                                style = TextStyle(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    letterSpacing = 1.2.sp
                                                )
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .graphicsLayer {
                                                        translationX = (floatingOffset / -3f)
                                                    }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(id = R.string.tap_to_choose),
                                    fontSize = 11.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        } else {
                            // Revealed Role Selection
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    stringResource(id = R.string.choose_workspace), 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Black, 
                                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.6f), 
                                    letterSpacing = 2.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))

                                RoleEntryCard(
                                    title = stringResource(id = R.string.patient),
                                    subtitle = stringResource(id = R.string.patient_subtitle),
                                    roleType = "patient",
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.fillMaxWidth(0.75f).height(60.dp),
                                    onClick = onPatientClick
                                )
                                
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(16.dp)) {
                                    HorizontalDivider(modifier = Modifier.fillMaxWidth(0.3f), thickness = 1.dp, color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f))
                                    Surface(
                                        modifier = Modifier.size(6.dp),
                                        shape = CircleShape,
                                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                        border = BorderStroke(1.dp, Color.White)
                                    ) {}
                                }
                                
                                RoleEntryCard(
                                    title = stringResource(id = R.string.nurse),
                                    subtitle = stringResource(id = R.string.nurse_subtitle),
                                    roleType = "nurse",
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier.fillMaxWidth(0.75f).height(60.dp),
                                    onClick = onNurseClick
                                )

                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(16.dp)) {
                                    HorizontalDivider(modifier = Modifier.fillMaxWidth(0.3f), thickness = 1.dp, color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f))
                                    Surface(
                                        modifier = Modifier.size(6.dp),
                                        shape = CircleShape,
                                        color = Color(0xFFEC4899),
                                        border = BorderStroke(1.dp, Color.White)
                                    ) {}
                                }

                                RoleEntryCard(
                                    title = stringResource(id = R.string.women_care),
                                    subtitle = stringResource(id = R.string.women_care_subtitle),
                                    roleType = "women",
                                    color = Color(0xFFEC4899),
                                    modifier = Modifier.fillMaxWidth(0.75f).height(60.dp),
                                    onClick = onWomenCareClick
                                )
                            }
                        }
                    }
                }
                
                // Developer Credit at the very bottom
                Text(
                    text = stringResource(id = R.string.developed_by),
                    style = TextStyle(
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Black, 
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1A237E).copy(alpha = 0.6f), 
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp)
                        .graphicsLayer(alpha = animatableAlpha.value)
                )
            }
        }
    }
}
