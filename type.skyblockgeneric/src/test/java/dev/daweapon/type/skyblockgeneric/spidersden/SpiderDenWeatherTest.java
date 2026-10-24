package dev.daweapon.type.skyblockgeneric.spidersden;

import net.minestom.server.instance.Weather;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpiderDenWeatherTest {
    private static final long MINUTE = 60_000L;

    private static long at(long cycle, long minutes) {
        return cycle * SpiderDenWeather.PERIOD_MILLIS + minutes * MINUTE;
    }

    @Test
    void rainsForTwentyMinutesEveryFortyMinutes() {
        assertTrue(SpiderDenWeather.raining(at(0, 0)));
        assertTrue(SpiderDenWeather.raining(at(0, 19)));
        assertFalse(SpiderDenWeather.raining(at(0, 20)));
        assertFalse(SpiderDenWeather.raining(at(0, 39)));
        assertTrue(SpiderDenWeather.raining(at(1, 0)));
    }

    @Test
    void everyThirdRainIsAThunderstorm() {
        assertEquals(Weather.RAIN, SpiderDenWeather.forecast(at(0, 5)));
        assertEquals(Weather.RAIN, SpiderDenWeather.forecast(at(1, 5)));
        assertEquals(Weather.THUNDER, SpiderDenWeather.forecast(at(2, 5)));
        assertEquals(Weather.RAIN, SpiderDenWeather.forecast(at(3, 5)));
        assertEquals(Weather.THUNDER, SpiderDenWeather.forecast(at(5, 5)));
        assertEquals(Weather.CLEAR, SpiderDenWeather.forecast(at(2, 25)));
    }

    @Test
    void rainGivesTwentyFiveFishingSpeed() {
        assertEquals(25, SpiderDenWeather.RAIN_FISHING_SPEED);
    }
}
