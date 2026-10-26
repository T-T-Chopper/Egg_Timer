package com.ferhatcangeyik.eggtimer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CookingTimeTest {

    private fun minutes(
        level: EggLevel,
        method: CookingMethod,
        size: EggSize = EggSize.MEDIUM,
        startTemp: EggStartTemp = EggStartTemp.FRIDGE
    ) = cookingSeconds(level, method, size, startTemp) / 60

    @Test
    fun defaultChoiceKeepsTheOriginalTimes() {
        // Boy ve sıcaklık eklenmeden önceki sürelerle birebir aynı kalmalı:
        // varsayılan seçimle pişiren kullanıcının sonucu değişmesin.
        assertEquals(5, minutes(EggLevel.SOFT, CookingMethod.BOILING_WATER))
        assertEquals(7, minutes(EggLevel.MEDIUM, CookingMethod.BOILING_WATER))
        assertEquals(10, minutes(EggLevel.HARD, CookingMethod.BOILING_WATER))
        assertEquals(9, minutes(EggLevel.SOFT, CookingMethod.COLD_WATER))
        assertEquals(12, minutes(EggLevel.MEDIUM, CookingMethod.COLD_WATER))
        assertEquals(15, minutes(EggLevel.HARD, CookingMethod.COLD_WATER))
    }

    @Test
    fun eggSizeShiftsTheTimeByOneMinute() {
        assertEquals(4, minutes(EggLevel.SOFT, CookingMethod.BOILING_WATER, size = EggSize.SMALL))
        assertEquals(6, minutes(EggLevel.SOFT, CookingMethod.BOILING_WATER, size = EggSize.LARGE))
        assertEquals(14, minutes(EggLevel.HARD, CookingMethod.COLD_WATER, size = EggSize.SMALL))
        assertEquals(16, minutes(EggLevel.HARD, CookingMethod.COLD_WATER, size = EggSize.LARGE))
    }

    @Test
    fun roomTemperatureEggCooksFasterInBoilingWater() {
        assertEquals(
            4,
            minutes(EggLevel.SOFT, CookingMethod.BOILING_WATER, startTemp = EggStartTemp.ROOM)
        )
    }

    @Test
    fun startingTemperatureDoesNotMatterWhenHeatingWithTheWater() {
        for (level in EggLevel.entries) {
            assertEquals(
                minutes(level, CookingMethod.COLD_WATER, startTemp = EggStartTemp.FRIDGE),
                minutes(level, CookingMethod.COLD_WATER, startTemp = EggStartTemp.ROOM)
            )
        }
    }

    @Test
    fun everyCombinationIsAPositiveWholeMinute() {
        for (level in EggLevel.entries) for (method in CookingMethod.entries)
            for (size in EggSize.entries) for (temp in EggStartTemp.entries) {
                val seconds = cookingSeconds(level, method, size, temp)
                assertTrue("$level/$method/$size/$temp = $seconds", seconds > 0)
                assertEquals("$level/$method/$size/$temp = $seconds", 0, seconds % 60)
            }
    }

    @Test
    fun secondsUntilIsZeroOncePassed() {
        assertEquals(0, secondsUntil(System.currentTimeMillis() - 1))
        assertEquals(0, secondsUntil(0L))
    }

    @Test
    fun secondsUntilRoundsUpSoTheDisplayNeverShowsZeroEarly() {
        // 4,5 saniye kala ekran 0:05 göstermeli; aşağı yuvarlasaydık süre
        // bitmeden "0:00" görünürdü.
        val remaining = secondsUntil(System.currentTimeMillis() + 4_500)
        assertEquals(5, remaining)
    }
}
