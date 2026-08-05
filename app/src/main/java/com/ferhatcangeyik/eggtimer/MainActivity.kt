package com.ferhatcangeyik.eggtimer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ferhatcangeyik.eggtimer.ui.theme.EggTimerTheme
import com.ferhatcangeyik.eggtimer.ui.theme.LocalEggColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ensureAlarmChannel(this, localizedStrings(storedLanguage(this)))
        setContent {
            EggTimerTheme {
                EggTimerApp()
            }
        }
    }
}

private const val PREF_SELECTED_LANGUAGE = "selected_language"

private const val STEP_LEVEL = 0
private const val STEP_METHOD = 1
private const val STEP_DETAILS = 2
private const val STEP_TIMER = 3
private const val STEP_COUNT = 4

/** Uygulama içi alarmın kendi kendine susmadan önce çalacağı süre. */
private const val ALARM_MAX_DURATION_MS = 60_000L

/** Hata ayıklamadaki test turunun süresi: uygulamayı kapatmaya yetecek kadar. */
private const val DEBUG_TEST_SECONDS = 30

@Composable
fun LanguageMenu(
    language: AppLanguage,
    strings: LocalizedStrings,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEggColors.current
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = strings.languageLabel,
            color = colors.body,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = language.label,
            color = colors.muted
        )

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = strings.settingsContentDescription,
                    tint = colors.body
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AppLanguage.entries.forEach { option ->
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
    val detailsQuestion: String,
    val sizeLabel: String,
    val startTempLabel: String,
    val selectedLabel: String,
    val backLabel: String,
    val startLabel: String,
    val pauseLabel: String,
    val restartLabel: String,
    val testLabel: String,
    val dismissLabel: String,
    val timerReadyTitle: String,
    val timerReadySubtitle: String,
    val iceBathTip: String,
    val readyLabel: String,
    val enterFullscreenLabel: String,
    val exitFullscreenLabel: String,
    val totalTimeLabel: (Int) -> String,
    val eggLabel: String,
    val notificationChannelName: String,
    val notificationChannelDescription: String
)

fun localizedStrings(language: AppLanguage): LocalizedStrings = when (language) {
    AppLanguage.TURKISH -> LocalizedStrings(
        languageLabel = "Dil",
        settingsContentDescription = "Dil ayarları",
        appTitle = "Yumurta Zamanlayıcısı",
        levelQuestion = "Yumurtanızı nasıl pişirmek istiyorsunuz?",
        continueLabel = "Devam Et",
        methodQuestion = "Pişirme yönteminizi seçin",
        detailsQuestion = "Yumurtanız hakkında",
        sizeLabel = "Boy",
        startTempLabel = "Başlangıç",
        selectedLabel = "Seçilen",
        backLabel = "Geri",
        startLabel = "Başla",
        pauseLabel = "Duraklat",
        restartLabel = "Tekrar",
        testLabel = "Test (30 sn)",
        dismissLabel = "Sustur",
        timerReadyTitle = "Yumurta hazır!",
        timerReadySubtitle = "Afiyet olsun!",
        iceBathTip = "Pişmeyi durdurup kabuğu kolay soymak için soğuk suya alın",
        readyLabel = "Hazır!",
        enterFullscreenLabel = "Tam ekran",
        exitFullscreenLabel = "Tam ekrandan çık",
        totalTimeLabel = { minutes -> "Toplam süre: $minutes dakika" },
        eggLabel = "Yumurta",
        notificationChannelName = "Yumurta alarmı",
        notificationChannelDescription = "Zamanlayıcı bittiğinde çalar"
    )

    AppLanguage.ENGLISH -> LocalizedStrings(
        languageLabel = "Language",
        settingsContentDescription = "Language settings",
        appTitle = "Egg Timer",
        levelQuestion = "How would you like your egg cooked?",
        continueLabel = "Continue",
        methodQuestion = "Choose your cooking method",
        detailsQuestion = "About your egg",
        sizeLabel = "Size",
        startTempLabel = "Starting from",
        selectedLabel = "Selected",
        backLabel = "Back",
        startLabel = "Start",
        pauseLabel = "Pause",
        restartLabel = "Restart",
        testLabel = "Test (30s)",
        dismissLabel = "Dismiss",
        timerReadyTitle = "Egg is ready!",
        timerReadySubtitle = "Enjoy your meal!",
        iceBathTip = "Move it to cold water to stop cooking and peel it easily",
        readyLabel = "Ready!",
        enterFullscreenLabel = "Enter fullscreen",
        exitFullscreenLabel = "Exit fullscreen",
        totalTimeLabel = { minutes -> "Total time: $minutes min" },
        eggLabel = "Egg",
        notificationChannelName = "Egg alarm",
        notificationChannelDescription = "Rings when the timer finishes"
    )
}

private fun SharedPreferences.loadLanguage(): AppLanguage {
    val saved = getString(PREF_SELECTED_LANGUAGE, AppLanguage.TURKISH.name)
    return AppLanguage.entries.firstOrNull { it.name == saved } ?: AppLanguage.TURKISH
}

/** Compose dışından (alarm alıcısı, bildirim kanalı) seçili dile erişmek için. */
fun storedLanguage(context: Context): AppLanguage =
    context.getSharedPreferences("EggTimerPrefs", Context.MODE_PRIVATE).loadLanguage()

/**
 * Compose'un [Context]'i bir sarmalayıcı olabilir; yaşam döngüsüne erişmek için
 * zinciri gerçek activity'ye kadar takip ederiz.
 */
private fun Context.findComponentActivity(): ComponentActivity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun EggTimerApp() {
    val context = LocalContext.current
    val colors = LocalEggColors.current
    val preferences = remember {
        context.getSharedPreferences("EggTimerPrefs", Context.MODE_PRIVATE)
    }
    val store = remember { TimerStore(context) }

    // Çalışan bir zamanlayıcı varsa doğrudan onun ekranıyla açılırız; yoksa
    // son kullanılan seçim hazır gelir, kullanıcı hızlıca geçebilir.
    val savedSession = remember { store.load() }
    val initialChoice = remember { savedSession?.choice ?: store.lastChoice() }

    var selectedLevel by remember { mutableStateOf(initialChoice.level) }
    var selectedMethod by remember { mutableStateOf(initialChoice.method) }
    var selectedSize by remember { mutableStateOf(initialChoice.size) }
    var selectedStartTemp by remember { mutableStateOf(initialChoice.startTemp) }
    var currentStep by remember {
        mutableStateOf(if (savedSession != null) STEP_TIMER else STEP_LEVEL)
    }
    var language by remember { mutableStateOf(preferences.loadLanguage()) }

    val onLanguageSelected: (AppLanguage) -> Unit = { selected ->
        language = selected
        preferences.edit().putString(PREF_SELECTED_LANGUAGE, selected.name).apply()
    }
    val strings = localizedStrings(language)
    val choice = CookingChoice(selectedLevel, selectedMethod, selectedSize, selectedStartTemp)

    FullscreenEffect(isFullscreen = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
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
                color = colors.title,
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
                        STEP_LEVEL -> {
                            Text(
                                text = strings.levelQuestion,
                                fontSize = 18.sp,
                                color = colors.body,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                EggLevel.entries.forEach { level ->
                                    EggLevelCard(
                                        level = level,
                                        isSelected = selectedLevel == level,
                                        onClick = { selectedLevel = level },
                                        language = language
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            PrimaryButton(
                                text = strings.continueLabel,
                                onClick = { currentStep = STEP_METHOD },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        STEP_METHOD -> {
                            Text(
                                text = strings.methodQuestion,
                                fontSize = 18.sp,
                                color = colors.body,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            Text(
                                text = "${strings.selectedLabel}: ${selectedLevel.displayName(language)}",
                                fontSize = 14.sp,
                                color = colors.muted,
                                modifier = Modifier.padding(bottom = 14.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CookingMethod.entries.forEach { method ->
                                    CookingMethodCard(
                                        method = method,
                                        isSelected = selectedMethod == method,
                                        onClick = { selectedMethod = method },
                                        language = language
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                NeutralButton(
                                    text = strings.backLabel,
                                    onClick = { currentStep = STEP_LEVEL },
                                    modifier = Modifier.weight(1f)
                                )
                                PrimaryButton(
                                    text = strings.continueLabel,
                                    onClick = { currentStep = STEP_DETAILS },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        STEP_DETAILS -> {
                            Text(
                                text = strings.detailsQuestion,
                                fontSize = 18.sp,
                                color = colors.body,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Seçim değiştikçe süre anında güncellensin: kullanıcı
                            // boyun ve sıcaklığın etkisini burada görür.
                            Text(
                                text = strings.totalTimeLabel(choice.totalSeconds / 60),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accent,
                                modifier = Modifier.padding(bottom = 18.dp)
                            )

                            OptionGroupLabel(strings.sizeLabel)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                EggSize.entries.forEach { size ->
                                    CompactChoiceCard(
                                        title = size.displayName(language),
                                        subtitle = size.description(language),
                                        isSelected = selectedSize == size,
                                        onClick = { selectedSize = size },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OptionGroupLabel(strings.startTempLabel)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                EggStartTemp.entries.forEach { temp ->
                                    CompactChoiceCard(
                                        title = temp.displayName(language),
                                        subtitle = temp.description(language),
                                        isSelected = selectedStartTemp == temp,
                                        onClick = { selectedStartTemp = temp },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                NeutralButton(
                                    text = strings.backLabel,
                                    onClick = { currentStep = STEP_METHOD },
                                    modifier = Modifier.weight(1f)
                                )
                                PrimaryButton(
                                    text = strings.startLabel,
                                    onClick = { currentStep = STEP_TIMER },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        STEP_TIMER -> {
                            TimerScreen(
                                choice = choice,
                                onBack = { currentStep = STEP_DETAILS },
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
    stepCount: Int = STEP_COUNT
) {
    val colors = LocalEggColors.current

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
                    .background(if (isActive) colors.accent else colors.ringTrack)
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEggColors.current
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onAccent
        )
    }
}

@Composable
private fun NeutralButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEggColors.current
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.neutralButton),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

@Composable
private fun OptionGroupLabel(text: String) {
    val colors = LocalEggColors.current
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.muted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun CompactChoiceCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalEggColors.current
    Card(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.surfaceSelected else colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.bodyStrong,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = colors.muted,
                textAlign = TextAlign.Center
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
    val colors = LocalEggColors.current
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.surfaceSelected else colors.surface
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
                    color = colors.bodyStrong
                )
                Text(
                    text = level.description(language),
                    fontSize = 14.sp,
                    color = colors.muted
                )
            }
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
    val colors = LocalEggColors.current
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.surfaceSelected else colors.surface
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.displayName(language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.bodyStrong
                )
                Text(
                    text = method.description(language),
                    fontSize = 12.sp,
                    color = colors.muted,
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
    choice: CookingChoice,
    onBack: () -> Unit,
    strings: LocalizedStrings,
    language: AppLanguage
) {
    val context = LocalContext.current
    val colors = LocalEggColors.current
    val store = remember { TimerStore(context) }

    val baseTotalSeconds = choice.totalSeconds

    // Kayıtlı oturum yalnızca aynı seçim içinse geri yüklenir; kullanıcı
    // seçimini değiştirdiyse zamanlayıcı sıfırdan başlar.
    val saved = remember(choice) {
        store.load()?.takeIf { it.choice == choice }
    }
    val restoredRemaining = remember(choice) {
        when {
            saved == null -> baseTotalSeconds
            saved.isRunning -> secondsUntil(saved.endAt)
            else -> saved.remainingSeconds
        }
    }

    // Zamanlayıcı bir sayaç değil, bir bitiş anı: kalan süre her zaman
    // saatten hesaplanır, böylece arka planda da doğru işler.
    var endAt by remember(choice) {
        mutableStateOf(if (saved?.isRunning == true) saved.endAt else 0L)
    }
    var timeLeft by remember(choice) { mutableStateOf(restoredRemaining) }
    var isRunning by remember(choice) {
        mutableStateOf(saved?.isRunning == true && restoredRemaining > 0)
    }
    // Süresi dolmuş bir oturum geri yüklendiyse alarm yeniden çalar: kullanıcı
    // telefonun başına döndüğünde yumurtanın beklediğini görmeli.
    var alarmTriggered by remember(choice) {
        mutableStateOf(saved != null && restoredRemaining == 0)
    }
    var activeVibrator by remember { mutableStateOf<Vibrator?>(null) }

    // Çalışan turun toplam süresi: ilerleme halkası bunun üzerinden dolar.
    // Normalde pişirme süresidir, hata ayıklamadaki kısa test turunda onunki.
    var runTotalSeconds by remember(choice) { mutableStateOf(baseTotalSeconds) }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Reddedilirse arka plan bildirimi çıkmaz; zamanlayıcı yine çalışır. */ }

    val ensureNotificationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeVibrator?.cancel()
        }
    }

    // Uygulama arka plana alınınca alarmı kur, geri dönülünce iptal edip
    // kalan süreyi saatten tazele. Böylece iki uyarı üst üste binmez.
    val activity = remember(context) { context.findComponentActivity() }
    DisposableEffect(activity, isRunning, endAt) {
        val lifecycle = activity?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP ->
                    if (isRunning && endAt > System.currentTimeMillis()) {
                        EggAlarm.schedule(context, endAt)
                    }

                Lifecycle.Event.ON_START -> {
                    EggAlarm.cancel(context)
                    if (isRunning) {
                        val remaining = secondsUntil(endAt)
                        timeLeft = remaining
                        if (remaining == 0) {
                            isRunning = false
                            alarmTriggered = true
                        }
                    }
                }

                else -> Unit
            }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    // Zamanlayıcı çalışırken ekranın kararmasını engelle
    val view = LocalView.current
    DisposableEffect(isRunning) {
        view.keepScreenOn = isRunning
        onDispose { view.keepScreenOn = false }
    }

    LaunchedEffect(isRunning, endAt) {
        if (!isRunning) return@LaunchedEffect

        while (true) {
            val remaining = secondsUntil(endAt)
            timeLeft = remaining
            if (remaining == 0) break
            kotlinx.coroutines.delay(200)
        }

        isRunning = false
        alarmTriggered = true
        // Bitiş anını saklamaya devam ederiz: eskiyen oturumlar bu sayede
        // bir süre sonra kendiliğinden temizlenir.
        store.save(TimerSession(choice, endAt, 0, false))
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Sürekli tekrarlayan titreşim: 800ms titreşim, 300ms bekleme
                    val pattern = longArrayOf(0, 800, 300)
                    v.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(ALARM_MAX_DURATION_MS)
                }
            }
        } catch (_: Exception) {
        }

        // Alarm bir dakika sonra kendiliğinden susar; "hazır" görüntüsü kalır.
        // Sonsuza kadar çalan bir alarm hem rahatsız edici hem pil düşmanı.
        try {
            val toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
            val startedAt = System.currentTimeMillis()
            while (
                alarmTriggered &&
                timeLeft == 0 &&
                System.currentTimeMillis() - startedAt < ALARM_MAX_DURATION_MS
            ) {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600)
                kotlinx.coroutines.delay(1000)
            }
            toneGenerator.release()
        } catch (_: Exception) {
        }

        activeVibrator?.cancel()
        activeVibrator = null
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

    // Kalan süre oranı — halka bu değerle dolar, renk turuncudan kırmızıya kayar
    val progress by animateFloatAsState(
        targetValue = if (runTotalSeconds > 0) timeLeft.toFloat() / runTotalSeconds else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "timer_progress"
    )
    val ringColor = if (isDone) colors.success else lerp(colors.danger, colors.accent, progress)
    val ringTrackColor = colors.ringTrack.copy(alpha = 0.35f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = choice.level.displayName(language),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.bodyStrong,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = choice.method.displayName(language),
            fontSize = 16.sp,
            color = colors.muted,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Text(
            text = "${choice.size.displayName(language)} · ${choice.startTemp.displayName(language)}",
            fontSize = 13.sp,
            color = colors.muted,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Text(
            text = strings.totalTimeLabel(baseTotalSeconds / 60),
            fontSize = 14.sp,
            color = colors.muted,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Box(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // Kalan süreyi gösteren ilerleme halkası
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val inset = strokeWidth / 2
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                drawArc(
                    color = ringTrackColor,
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
                        color = if (isDone) colors.successSurface else colors.surface,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            color = colors.bodyStrong,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        Text(
                            text = "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.bodyStrong
                        )

                        Text(
                            text = strings.eggLabel,
                            fontSize = 16.sp,
                            color = colors.muted,
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
                fontWeight = FontWeight.SemiBold,
                color = colors.bodyStrong,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = strings.iceBathTip,
                fontSize = 13.sp,
                color = colors.muted,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NeutralButton(
                text = strings.backLabel,
                onClick = {
                    isRunning = false
                    alarmTriggered = false
                    EggAlarm.cancel(context)
                    store.clearSession()
                    onBack()
                },
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (isRunning) {
                        val remaining = secondsUntil(endAt)
                        isRunning = false
                        timeLeft = remaining
                        endAt = 0L
                        EggAlarm.cancel(context)
                        store.save(TimerSession(choice, 0L, remaining, false))
                    } else {
                        // Süre bittiyse baştan başlarız, duraklatılmışsa kalanı sürdürürüz
                        val restarting = timeLeft <= 0
                        val startFrom = if (restarting) baseTotalSeconds else timeLeft
                        if (restarting) runTotalSeconds = baseTotalSeconds
                        alarmTriggered = false
                        timeLeft = startFrom
                        endAt = System.currentTimeMillis() + startFrom * 1000L
                        isRunning = true
                        store.save(TimerSession(choice, endAt, startFrom, true))
                        store.saveLastChoice(choice)
                        ensureNotificationPermission()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) colors.accentDeep else colors.accent
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
                    color = colors.onAccent
                )
            }

            // Kısa test turu: arka plan alarmını beş dakika beklemeden denemek
            // için. Yalnızca hata ayıklama derlemesinde görünür, Play'e giden
            // sürümde bu blok tamamen derlenmez.
            if (BuildConfig.DEBUG) {
                Button(
                    onClick = {
                        alarmTriggered = false
                        runTotalSeconds = DEBUG_TEST_SECONDS
                        timeLeft = DEBUG_TEST_SECONDS
                        endAt = System.currentTimeMillis() + DEBUG_TEST_SECONDS * 1000L
                        isRunning = true
                        store.save(TimerSession(choice, endAt, DEBUG_TEST_SECONDS, true))
                        ensureNotificationPermission()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.success),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = strings.testLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EggTimerAppPreview() {
    EggTimerTheme {
        EggTimerApp()
    }
}
