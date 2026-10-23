package dev.daweapon.type.skyblockgeneric.user.island.weather;

import net.minestom.server.instance.Weather;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IslandWeatherTest {
    @Test
    void parsesStoredNamesAndFallsBackToSunny() {
        assertEquals(IslandWeather.RAINING, IslandWeather.parse("RAINING"));
        assertEquals(IslandWeather.THUNDERING, IslandWeather.parse("thundering"));
        assertEquals(IslandWeather.SUNNY, IslandWeather.parse("nonsense"));
    }

    @Test
    void mapsOntoTheThreeVanillaWeathers() {
        assertEquals(Weather.CLEAR, IslandWeather.SUNNY.weather());
        assertEquals(Weather.RAIN, IslandWeather.RAINING.weather());
        assertEquals(Weather.THUNDER, IslandWeather.THUNDERING.weather());
    }
}
