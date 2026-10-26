package com.ferhatcangeyik.eggtimer

enum class EggLevel(
    val turkishName: String,
    val englishName: String,
    val turkishDescription: String,
    val englishDescription: String
) {
    SOFT("Rafadan", "Soft-boiled", "Akışkan sarı", "Runny yolk"),
    MEDIUM("Kayısı", "Medium", "Kremamsı sarı", "Jammy yolk"),
    HARD("Sert", "Hard-boiled", "Tam pişmiş", "Fully cooked")
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

enum class EggSize(
    val turkishName: String,
    val englishName: String,
    val turkishDescription: String,
    val englishDescription: String
) {
    SMALL("Küçük", "Small", "S · 50 gr", "S · 50 g"),
    MEDIUM("Orta", "Medium", "M · 60 gr", "M · 60 g"),
    LARGE("Büyük", "Large", "L · 70 gr", "L · 70 g")
}

enum class EggStartTemp(
    val turkishName: String,
    val englishName: String,
    val turkishDescription: String,
    val englishDescription: String
) {
    FRIDGE("Buzdolabı", "From fridge", "Soğuk yumurta", "Chilled egg"),
    ROOM("Oda sıcaklığı", "Room temp", "Dışarıda beklemiş", "Left out a while")
}

/**
 * Seçilen pişirme koşulları için toplam süre (saniye).
 *
 * Temel süreler orta boy, buzdolabından çıkmış yumurtaya göredir; boy ve
 * başlangıç sıcaklığı bunun üzerinden düzeltilir. Varsayılan seçimde
 * (orta boy + buzdolabı) süre uygulamanın önceki sürümüyle aynı kalır.
 */
fun cookingSeconds(
    level: EggLevel,
    method: CookingMethod,
    size: EggSize,
    startTemp: EggStartTemp
): Int {
    val base = when (method) {
        CookingMethod.BOILING_WATER -> when (level) {
            EggLevel.SOFT -> 5 * 60
            EggLevel.MEDIUM -> 7 * 60
            EggLevel.HARD -> 10 * 60
        }

        CookingMethod.COLD_WATER -> when (level) {
            EggLevel.SOFT -> 9 * 60
            EggLevel.MEDIUM -> 12 * 60
            EggLevel.HARD -> 15 * 60
        }
    }

    // Küçük yumurta daha çabuk, büyük daha geç pişer
    val sizeAdjust = when (size) {
        EggSize.SMALL -> -60
        EggSize.MEDIUM -> 0
        EggSize.LARGE -> 60
    }

    // Temel süre buzdolabından çıkmış yumurtaya göre olduğu için indirim
    // oda sıcaklığındakine uygulanır: ılık yumurta kaynar suda daha çabuk
    // pişer. Soğuk suyla birlikte ısıtmada yumurta ve su aynı anda ısındığı
    // için başlangıç sıcaklığının anlamlı bir etkisi kalmaz.
    val tempAdjust = if (
        startTemp == EggStartTemp.ROOM && method == CookingMethod.BOILING_WATER
    ) -60 else 0

    return base + sizeAdjust + tempAdjust
}

fun EggLevel.displayName(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishName else englishName

fun EggLevel.description(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishDescription else englishDescription

fun CookingMethod.displayName(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishName else englishName

fun CookingMethod.description(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishDescription else englishDescription

fun EggSize.displayName(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishName else englishName

fun EggSize.description(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishDescription else englishDescription

fun EggStartTemp.displayName(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishName else englishName

fun EggStartTemp.description(language: AppLanguage): String =
    if (language == AppLanguage.TURKISH) turkishDescription else englishDescription
