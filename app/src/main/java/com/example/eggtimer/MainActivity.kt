package com.example.eggtimer

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.eggtimer.ui.theme.EggBrown
import com.example.eggtimer.ui.theme.EggBrownDark
import com.example.eggtimer.ui.theme.EggBrownDarkest
import com.example.eggtimer.ui.theme.EggBrownLight
import com.example.eggtimer.ui.theme.EggBrownMedium
import com.example.eggtimer.ui.theme.EggCream
import com.example.eggtimer.ui.theme.EggGray
import com.example.eggtimer.ui.theme.EggGreen
import com.example.eggtimer.ui.theme.EggGreenLight
import com.example.eggtimer.ui.theme.EggOrange
import com.example.eggtimer.ui.theme.EggOrangeDeep
import com.example.eggtimer.ui.theme.EggRed
import com.example.eggtimer.ui.theme.EggSelected
import com.example.eggtimer.ui.theme.EggTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EggTimerTheme {
                EggTimerApp()
            }
        }
    }
}

private const val PREF_SELECTED_LANGUAGE = "selected_language"

@Composable
fun LanguageMenu(
    language: AppLanguage,
    strings: LocalizedStrings,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = strings.languageLabel,
            color = EggBrownDark,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = language.label,
            color = EggBrownLight
        )

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = strings.settingsContentDescription,
                    tint = EggBrownDark
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AppLanguage.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onLanguageSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FullscreenEffect(isFullscreen: Boolean) {
    val view = LocalView.current

    DisposableEffect(isFullscreen) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }

        if (window != null && controller != null) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            if (isFullscreen) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        onDispose {
            if (window != null && controller != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

enum class AppLanguage(val label: String) {
    TURKISH("Türkçe"),
    ENGLISH("English")
}

data class LocalizedStrings(
    val languageLabel: String,
    val settingsContentDescription: String,
    val appTitle: String,
    val levelQuestion: String,
    val continueLabel: String,
    val methodQuestion: String,
    val selectedLabel: String,
    val backLabel: String,
    val startLabel: String,
    val pauseLabel: String,
    val restartLabel: String,
    val testLabel: String,
    val timerReadyTitle: String,
    val timerReadySubtitle: String,
    val readyLabel: String,
    val enterFullscreenLabel: String,
    val exitFullscreenLabel: String,
    val totalTimeLabel: (Int) -> String,
    val eggLabel: String
)

fun localizedStrings(language: AppLanguage): LocalizedStrings = when (language) {
    AppLanguage.TURKISH -> LocalizedStrings(
        languageLabel = "Dil",
        settingsContentDescription = "Dil ayarları",
        appTitle = "Yumurta Zamanlayıcısı",
        levelQuestion = "Yumurtanızı nasıl pişirmek istiyorsunuz?",
        continueLabel = "Devam Et",
        methodQuestion = "Pişirme yönteminizi seçin",
        selectedLabel = "Seçilen",
        backLabel = "Geri",
        startLabel = "Başla",
        pauseLabel = "Duraklat",
        restartLabel = "Tekrar",
        testLabel = "Test (3 sn)",
        timerReadyTitle = "Yumurta hazır!",
        timerReadySubtitle = "Afiyet olsun!",
        readyLabel = "Hazır!",
        enterFullscreenLabel = "Tam ekran",
        exitFullscreenLabel = "Tam ekrandan çık",
        totalTimeLabel = { minutes -> "Toplam süre: $minutes dakika" },
        eggLabel = "Yumurta"
    )

    AppLanguage.ENGLISH -> LocalizedStrings(
        languageLabel = "Language",
        settingsContentDescription = "Language settings",
        appTitle = "Egg Timer",
        levelQuestion = "How would you like your egg cooked?",
        continueLabel = "Continue",
        methodQuestion = "Choose your cooking method",
        selectedLabel = "Selected",
        backLabel = "Back",
        startLabel = "Start",
        pauseLabel = "Pause",
        restartLabel = "Restart",
        testLabel = "Test (3s)",
        timerReadyTitle = "Egg is ready!",
        timerReadySubtitle = "Enjoy your meal!",
        readyLabel = "Ready!",
        enterFullscreenLabel = "Enter fullscreen",
        exitFullscreenLabel = "Exit fullscreen",
        totalTimeLabel = { minutes -> "Total time: $minutes min" },
        eggLabel = "Egg"
    )
}

private fun SharedPreferences.loadLanguage(): AppLanguage {
    val saved = getString(PREF_SELECTED_LANGUAGE, AppLanguage.TURKISH.name)
    return AppLanguage.values().firstOrNull { it.name == saved } ?: AppLanguage.TURKISH
}

@Composable
fun EggTimerApp() {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("EggTimerPrefs", Context.MODE_PRIVATE)
    }

    var selectedLevel by remember { mutableStateOf(EggLevel.SOFT) }
    var selectedMethod by remember { mutableStateOf(CookingMethod.BOILING_WATER) }
    var currentStep by remember { mutableStateOf(0) } // 0: seviye seçimi, 1: yöntem seçimi, 2: zamanlayıcı
    var language by remember {
        mutableStateOf(preferences.loadLanguage())
    }
    val onLanguageSelected: (AppLanguage) -> Unit = { selected ->
        language = selected
        preferences.edit().putString(PREF_SELECTED_LANGUAGE, selected.name).apply()
    }
    val strings = localizedStrings(language)
    FullscreenEffect(isFullscreen = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EggCream)
    ) {
        LanguageMenu(
            language = language,
            strings = strings,
            onLanguageSelected = onLanguageSelected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = strings.appTitle,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = EggBrownDarkest,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            StepIndicator(
                currentStep = currentStep,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    // İleri giderken sağdan, geri dönerken soldan kaydır
                    if (targetState > initialState) {
                        (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { it / 3 } + fadeOut())
                    }
                },
                label = "step_transition"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (step) {
                        0 -> {
                            Text(
                                text = strings.levelQuestion,
                                fontSize = 18.sp,
                                color = EggBrownDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            EggLevelSelector(
                                selectedLevel = selectedLevel,
                                onLevelSelected = { selectedLevel = it },
                                language = language
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { currentStep = 1 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EggOrange
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = strings.continueLabel,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        1 -> {
                            Text(
                                text = strings.methodQuestion,
                                fontSize = 18.sp,
                                color = EggBrownDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            Text(
                                text = "${strings.selectedLabel}: ${selectedLevel.displayName(language)}",
                                fontSize = 14.sp,
                                color = EggBrownMedium,
                                modifier = Modifier.padding(bottom = 14.dp)
                            )

                            CookingMethodSelector(
                                selectedMethod = selectedMethod,
                                onMethodSelected = { selectedMethod = it },
                                language = language
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { currentStep = 0 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EggBrownLight
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        text = strings.backLabel,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EggOrange
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        text = strings.startLabel,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        2 -> {
                            TimerScreen(
                                level = selectedLevel,
                                method = selectedMethod,
                                onBack = { currentStep = 1 },
                                strings = strings,
                                language = language
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier,
    stepCount: Int = 3
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(stepCount) { index ->
            val isActive = index == currentStep
            val dotWidth by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                label = "step_dot_width"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(dotWidth)
                    .clip(CircleShape)
                    .background(if (isActive) EggOrange else EggSelected)
            )
        }
    }
}

@Composable
fun EggLevelSelector(
    selectedLevel: EggLevel,
    onLevelSelected: (EggLevel) -> Unit,
    language: AppLanguage
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EggLevel.values().forEach { level ->
            EggLevelCard(
                level = level,
                isSelected = selectedLevel == level,
                onClick = { onLevelSelected(level) },
                language = language
            )
        }
    }
}

@Composable
fun EggLevelCard(
    level: EggLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    language: AppLanguage
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EggSelected else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = level.displayName(language),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EggBrown
                )
                Text(
                    text = level.description(language),
                    fontSize = 14.sp,
                    color = EggBrownLight
                )
            }
        }
    }
}

@Composable
fun CookingMethodSelector(
    selectedMethod: CookingMethod,
    onMethodSelected: (CookingMethod) -> Unit,
    language: AppLanguage
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CookingMethod.values().forEach { method ->
            CookingMethodCard(
                method = method,
                isSelected = selectedMethod == method,
                onClick = { onMethodSelected(method) },
                language = language
            )
        }
    }
}

@Composable
fun CookingMethodCard(
    method: CookingMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    language: AppLanguage
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EggSelected else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = method.displayName(language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EggBrown
                )
                Text(
                    text = method.description(language),
                    fontSize = 12.sp,
                    color = EggBrownLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Text(
                text = method.emoji,
                fontSize = 40.sp
            )
        }
    }
}

@Composable
fun TimerScreen(
    level: EggLevel,
    method: CookingMethod,
    onBack: () -> Unit,
    strings: LocalizedStrings,
    language: AppLanguage
) {
    val context = LocalContext.current

    val baseTotalSeconds = remember(level, method) {
        when (method) {
            CookingMethod.BOILING_WATER -> {
                when (level) {
                    EggLevel.SOFT -> 5 * 60
                    EggLevel.MEDIUM -> 7 * 60
                    EggLevel.HARD -> 10 * 60
                }
            }

            CookingMethod.COLD_WATER -> {
                when (level) {
                    EggLevel.SOFT -> 9 * 60
                    EggLevel.MEDIUM -> 12 * 60
                    EggLevel.HARD -> 15 * 60
                }
            }
        }
    }

    var timeLeft by remember { mutableStateOf(baseTotalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var alarmTriggered by remember { mutableStateOf(false) }
    var activeVibrator by remember { mutableStateOf<Vibrator?>(null) }
    var isTestMode by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            activeVibrator?.cancel()
        }
    }

    // Zamanlayıcı çalışırken ekranın kararmasını engelle
    val view = LocalView.current
    DisposableEffect(isRunning) {
        view.keepScreenOn = isRunning
        onDispose { view.keepScreenOn = false }
    }

    LaunchedEffect(level, method) {
        isRunning = false
        alarmTriggered = false
        isTestMode = false
        timeLeft = baseTotalSeconds
    }

    LaunchedEffect(isRunning, baseTotalSeconds, isTestMode) {
        if (!isRunning) return@LaunchedEffect

        while (isRunning && timeLeft > 0) {
            kotlinx.coroutines.delay(1000)
            timeLeft -= 1
        }

        if (isRunning && timeLeft == 0) {
            isRunning = false
            alarmTriggered = true
        }
    }

    LaunchedEffect(alarmTriggered) {
        if (!alarmTriggered || timeLeft != 0) {
            activeVibrator?.cancel()
            activeVibrator = null
            return@LaunchedEffect
        }

        // Titreşimi başlat - sürekli tekrarlayan pattern
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            activeVibrator = vibrator
            vibrator?.let { v ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    // Sürekli tekrarlayan titreşim: 800ms titreşim, 300ms bekleme
                    val pattern = longArrayOf(0, 800, 300)
                    v.vibrate(
                        VibrationEffect.createWaveform(
                            pattern,
                            0 // 0 = baştan sürekli tekrar
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    // Eski API için uzun süreli titreşim
                    v.vibrate(10000) // 10 saniye titreşim
                }
            }
        } catch (_: Exception) {
        }

        // Alarm sesini sürekli çal - alarmTriggered false olana kadar
        try {
            val toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
            while (alarmTriggered && timeLeft == 0) {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600)
                kotlinx.coroutines.delay(1000) // Her 1 saniyede bir çal
            }
            toneGenerator.release()
        } catch (_: Exception) {
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "egg_shake")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "egg_rotation"
    )

    val isDone = alarmTriggered && timeLeft == 0
    val rotationAngle = if (isDone) rotation else 0f
    val activeTotalSeconds = if (isTestMode) 3 else baseTotalSeconds

    // Kalan süre oranı — halka bu değerle dolar, renk turuncudan kırmızıya kayar
    val progress by animateFloatAsState(
        targetValue = if (activeTotalSeconds > 0) timeLeft.toFloat() / activeTotalSeconds else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "timer_progress"
    )
    val ringColor = if (isDone) EggGreen else lerp(EggRed, EggOrange, progress)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = level.displayName(language),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = EggBrown,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = method.displayName(language),
            fontSize = 16.sp,
            color = EggBrownLight,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val minutes = activeTotalSeconds / 60
        Text(
            text = strings.totalTimeLabel(minutes),
            fontSize = 14.sp,
            color = EggGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // Kalan süreyi gösteren ilerleme halkası
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val inset = strokeWidth / 2
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                drawArc(
                    color = EggSelected.copy(alpha = 0.35f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = if (isDone) 360f else 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Box(
                modifier = Modifier
                    .size(202.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .background(
                        color = if (isDone) EggGreenLight else Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_classic_egg),
                        contentDescription = strings.eggLabel,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(88.dp)
                            .padding(bottom = 8.dp)
                            .rotate(rotationAngle)
                    )

                    if (timeLeft == 0) {
                        Text(
                            text = strings.timerReadyTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = EggBrown,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        Text(
                            text = "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = EggBrown
                        )

                        Text(
                            text = strings.eggLabel,
                            fontSize = 16.sp,
                            color = EggBrownMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        if (isDone) {
            Text(
                text = strings.timerReadySubtitle,
                fontSize = 16.sp,
                color = EggBrownMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EggBrownLight
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = strings.backLabel,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    isTestMode = false
                    if (timeLeft == 0 || timeLeft == 3) {
                        timeLeft = baseTotalSeconds
                        alarmTriggered = false
                    }
                    isRunning = !isRunning
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) EggOrangeDeep else EggOrange
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when {
                        isRunning -> strings.pauseLabel
                        timeLeft == 0 -> strings.restartLabel
                        else -> strings.startLabel
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Test butonu sadece debug build'de görünür, Play Store sürümünde yoktur
            if (BuildConfig.DEBUG) {
                Button(
                    onClick = {
                        isRunning = false
                        alarmTriggered = false
                        isTestMode = true
                        timeLeft = 3
                        isRunning = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EggGreen
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = strings.testLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

enum class EggLevel(
    val turkishName: String,
    val englishName: String,
    val turkishDescription: String,
    val englishDescription: String,
    val timeInMinutes: Int
) {
    SOFT("Rafadan", "Soft-boiled", "Akışkan sarı", "Runny yolk", 0),
    MEDIUM("Kayısı", "Medium", "Kremamsı sarı", "Jammy yolk", 0),
    HARD("Sert", "Hard-boiled", "Tam pişmiş", "Fully cooked", 0)
}

enum class CookingMethod(
    val emoji: String,
    val turkishName: String,
    val englishName: String,
    val turkishDescription: String,
    val englishDescription: String
) {
    BOILING_WATER(
        "🔥",
        "Kaynar Suya At",
        "Drop into boiling water",
        "Su kaynadıktan sonra yumurtayı atın",
        "Add the egg after the water boils"
    ),
    COLD_WATER(
        "💧",
        "Suyla Beraber Kaynat",
        "Start with cold water",
        "Soğuk suyla beraber başlayın",
        "Begin heating with the egg in cold water"
    )
}

fun EggLevel.displayName(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishName else englishName

fun EggLevel.description(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishDescription else englishDescription

fun CookingMethod.displayName(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishName else englishName

fun CookingMethod.description(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishDescription else englishDescription

@Preview(showBackground = true)
@Composable
fun EggTimerAppPreview() {
    EggTimerTheme {
        EggTimerApp()
    }
}