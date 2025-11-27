package com.example.eggtimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.rotate
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

@Composable
fun EggTimerApp() {
    var selectedLevel by remember { mutableStateOf(EggLevel.SOFT) }
    var selectedMethod by remember { mutableStateOf(CookingMethod.BOILING_WATER) }
    var currentStep by remember { mutableStateOf(0) } // 0: seviye seçimi, 1: yöntem seçimi, 2: zamanlayıcı
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Yumurta ikonu ve başlık
        Text(
            text = "🥚",
            fontSize = 80.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Yumurta Zamanlayıcısı",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8D6E63),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        when (currentStep) {
            0 -> {
                Text(
                    text = "Yumurtanızı nasıl pişirmek istiyorsunuz?",
                    fontSize = 18.sp,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Yumurta seviyeleri
                EggLevelSelector(
                    selectedLevel = selectedLevel,
                    onLevelSelected = { selectedLevel = it }
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
                        text = "Devam Et",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            1 -> {
                Text(
                    text = "Pişirme yönteminizi seçin",
                    fontSize = 18.sp,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Text(
                    text = "Seçilen: ${selectedLevel.displayName}",
                    fontSize = 14.sp,
                    color = Color(0xFF8D6E63),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Pişirme yöntemleri
                CookingMethodSelector(
                    selectedMethod = selectedMethod,
                    onMethodSelected = { selectedMethod = it }
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
                            text = "Geri",
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
                            text = "Başla",
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
                    onBack = { currentStep = 1 }
                )
            }
        }
    }
}

@Composable
fun EggLevelSelector(
    selectedLevel: EggLevel,
    onLevelSelected: (EggLevel) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EggLevel.values().forEach { level ->
            EggLevelCard(
                level = level,
                isSelected = selectedLevel == level,
                onClick = { onLevelSelected(level) }
            )
        }
    }
}

@Composable
fun EggLevelCard(
    level: EggLevel,
    isSelected: Boolean,
    onClick: () -> Unit
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
                    text = level.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = level.description,
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
    onMethodSelected: (CookingMethod) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CookingMethod.values().forEach { method ->
            CookingMethodCard(
                method = method,
                isSelected = selectedMethod == method,
                onClick = { onMethodSelected(method) }
            )
        }
    }
}

@Composable
fun CookingMethodCard(
    method: CookingMethod,
    isSelected: Boolean,
    onClick: () -> Unit
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
                    text = method.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = method.description,
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
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Yönteme ve seviyeye göre süre hesaplama
    val totalSeconds = remember(level, method) {
        when (method) {
            CookingMethod.BOILING_WATER -> {
                when (level) {
                    EggLevel.SOFT -> 5 * 60  // 5 dakika
                    EggLevel.MEDIUM -> 7 * 60  // 7 dakika
                    EggLevel.HARD -> 10 * 60  // 10 dakika
                }
            }
            CookingMethod.COLD_WATER -> {
                when (level) {
                    EggLevel.SOFT -> 9 * 60  // 9 dakika
                    EggLevel.MEDIUM -> 12 * 60  // 12 dakika
                    EggLevel.HARD -> 15 * 60  // 15 dakika
                }
            }
        }
    }
    
    var timeLeft by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var alarmTriggered by remember { mutableStateOf(false) }
    
    // Süre resetlendiğinde güncelle
    LaunchedEffect(totalSeconds) {
        if (!isRunning) {
            timeLeft = totalSeconds
            alarmTriggered = false
        }
    }
    
    // Geri sayım ve alarm
    LaunchedEffect(isRunning, timeLeft) {
        while (isRunning && timeLeft > 0) {
            kotlinx.coroutines.delay(1000)
            timeLeft--
        }

        if (isRunning && timeLeft == 0) {
            isRunning = false
            alarmTriggered = true

            // Titreşim
            try {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.let { v ->
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), 0))
                    } else {
                        v.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), 0)
                    }
                }
            } catch (e: Exception) {
                // Vibrator bulunamadıysa sessizce geç
            }

            // Beep sesi (sistem sesi)
            try {
                val toneGenerator = android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_NOTIFICATION,
                    100
                )
                // Melodi çal
                toneGenerator.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
                kotlinx.coroutines.delay(300)
                toneGenerator.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
            } catch (e: Exception) {
                // Ses sistemi yoksa sessizce geç
            }
        }
    }
    
    // Sallanan yumurta animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "egg_shake")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "egg_rotation"
    )
    
    // Alarm tetiklendiğinde animasyonu başlat
    val shouldShake = alarmTriggered && timeLeft == 0
    val rotationAngle = if (shouldShake) rotation else 0f
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍳",
            fontSize = 100.sp,
            modifier = Modifier
                .rotate(rotationAngle)
                .padding(bottom = 16.dp)
        )
        
        Text(
            text = "${level.displayName} Yumurta",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5D4037),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = method.displayName,
            fontSize = 16.sp,
            color = Color(0xFF8D6E63),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Süre bilgisi
        val minutes = totalSeconds / 60
        Text(
            text = "Toplam süre: $minutes dakika",
            fontSize = 14.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Alarm durumu
        if (alarmTriggered && timeLeft == 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 48.sp
                    )
                    Text(
                        text = "Yumurta hazır!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "Afiyet olsun! 🍳",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(if (alarmTriggered) 8.dp else 16.dp))
        
        // Geri sayım göstergesi
        Card(
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (timeLeft == 0) Color(0xFF4CAF50) else Color(0xFFFFCC80)
            ),
            shape = RoundedCornerShape(100.dp),
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
                        text = if (timeLeft == 0) "🎉" else "⏰",
                        fontSize = 40.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = if (timeLeft == 0) "Hazır!" else "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                }
            }
        }
        
        // Kontrol butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "Geri",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
            
            Button(
                onClick = { 
                    isRunning = !isRunning
                    if (timeLeft == 0) {
                        timeLeft = totalSeconds
                    }
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
                    text = if (isRunning) "Duraklat" else if (timeLeft == 0) "Tekrar" else "Başla",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

enum class EggLevel(
    val displayName: String,
    val description: String,
    val emoji: String,
    val timeInMinutes: Int
) {
    SOFT("Rafadan", "Akışkan sarı", "🍳", 0),  // Süre yönteme göre değişiyor
    MEDIUM("Kayısı", "Kremamsı sarı", "🥚", 0),  // Süre yönteme göre değişiyor
    HARD("Sert", "Tam pişmiş", "🥞", 0)  // Süre yönteme göre değişiyor
}

enum class CookingMethod(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    BOILING_WATER("Kaynar Suya At", "Su kaynadıktan sonra yumurtayı atın", "🔥"),
    COLD_WATER("Suyla Beraber Kaynat", "Soğuk suyla beraber başlayın", "💧")
}

@Preview(showBackground = true)
@Composable
fun EggTimerAppPreview() {
    EggTimerTheme {
        EggTimerApp()
    }
}