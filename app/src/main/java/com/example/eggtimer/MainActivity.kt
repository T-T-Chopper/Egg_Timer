package com.example.eggtimer

import android.content.Context
import android.content.SharedPreferences
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onLanguageSelected: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = strings.languageLabel,
            color = Color(0xFF5D4037),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )

        Text(
            text = language.label,
            color = Color(0xFF8D6E63),
            modifier = Modifier.padding(end = 8.dp)
        )

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = strings.settingsContentDescription,
                    tint = Color(0xFF5D4037)
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
    val timerReadyTitle: String,
    val timerReadySubtitle: String,
    val readyLabel: String,
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
        timerReadyTitle = "Yumurta hazır!",
        timerReadySubtitle = "Afiyet olsun!",
        readyLabel = "Hazır!",
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
        timerReadyTitle = "Egg is ready!",
        timerReadySubtitle = "Enjoy your meal!",
        readyLabel = "Ready!",
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LanguageMenu(
            language = language,
            strings = strings,
            onLanguageSelected = onLanguageSelected
        )

        Text(
            text = strings.appTitle,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8D6E63),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        when (currentStep) {
            0 -> {
                Text(
                    text = strings.levelQuestion,
                    fontSize = 18.sp,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Yumurta seviyeleri
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
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(16.dp)
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
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "${strings.selectedLabel}: ${selectedLevel.displayName(language)}",
                    fontSize = 14.sp,
                    color = Color(0xFF8D6E63),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Pişirme yöntemleri
                CookingMethodSelector(
                    selectedMethod = selectedMethod,
                    onMethodSelected = { selectedMethod = it },
                    language = language
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                            containerColor = Color(0xFF757575)
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
                        onClick = { currentStep = 2 },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(16.dp)
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
            containerColor = if (isSelected) Color(0xFFFFCC80) else Color.White
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
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = level.description(language),
                    fontSize = 14.sp,
                    color = Color(0xFF8D6E63)
                )
            }
            
            Text(
                text = level.emoji,
                fontSize = 32.sp
            )
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
            containerColor = if (isSelected) Color(0xFFFFCC80) else Color.White
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
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = method.description(language),
                    fontSize = 12.sp,
                    color = Color(0xFF8D6E63),
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

    val totalSeconds = remember(level, method) {
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

    var timeLeft by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var alarmTriggered by remember { mutableStateOf(false) }
    var activeVibrator by remember { mutableStateOf<Vibrator?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            activeVibrator?.cancel()
        }
    }

    LaunchedEffect(totalSeconds) {
        if (!isRunning) {
            timeLeft = totalSeconds
            alarmTriggered = false
        }
    }

    LaunchedEffect(isRunning, totalSeconds) {
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

        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            activeVibrator = vibrator
            vibrator?.let { v ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    v.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 500, 200, 500, 200, 500),
                            0
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), 0)
                }
            }
        } catch (_: Exception) {
        }

        try {
            val toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_ALARM, 80)
            repeat(3) {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
                kotlinx.coroutines.delay(300)
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

    val rotationAngle = if (alarmTriggered && timeLeft == 0) rotation else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = level.displayName(language),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5D4037),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = method.displayName(language),
            fontSize = 16.sp,
            color = Color(0xFF8D6E63),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val minutes = totalSeconds / 60
        Text(
            text = strings.totalTimeLabel(minutes),
            fontSize = 14.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .size(220.dp)
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (alarmTriggered && timeLeft == 0) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (timeLeft == 0) strings.readyLabel else strings.eggLabel,
                        fontSize = 18.sp,
                        color = Color(0xFF5D4037),
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = if (timeLeft == 0) strings.timerReadyTitle else "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                }
            }
        }

        if (alarmTriggered && timeLeft == 0) {
            Text(
                text = strings.timerReadySubtitle,
                fontSize = 16.sp,
                color = Color(0xFF6D4C41),
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
                    containerColor = Color(0xFF757575)
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
                    if (timeLeft == 0) {
                        timeLeft = totalSeconds
                        alarmTriggered = false
                    }
                    isRunning = !isRunning
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFFF5722) else Color(0xFFFF9800)
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
        }
    }
}

enum class EggLevel(
    val emoji: String,
    val turkishName: String,
    val englishName: String,
    val turkishDescription: String,
    val englishDescription: String,
    val timeInMinutes: Int
) {
    SOFT("🍳", "Rafadan", "Soft-boiled", "Akışkan sarı", "Runny yolk", 0),
    MEDIUM("🥚", "Kayısı", "Medium", "Kremamsı sarı", "Jammy yolk", 0),
    HARD("🥞", "Sert", "Hard-boiled", "Tam pişmiş", "Fully cooked", 0)
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