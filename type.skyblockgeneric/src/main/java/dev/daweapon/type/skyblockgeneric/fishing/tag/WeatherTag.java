package dev.daweapon.type.skyblockgeneric.fishing.tag;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Weather;
import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;

// Fishing tag that gates loot to the current instance weather (raining, clear, thunderstorm).
public record WeatherTag(String id, Condition condition) implements FishingTag {

    public static final WeatherTag RAINING = new WeatherTag("RAINING", Condition.RAINING);
    public static final WeatherTag CLEAR = new WeatherTag("CLEAR", Condition.CLEAR);
    public static final WeatherTag THUNDERSTORM = new WeatherTag("THUNDERSTORM", Condition.THUNDERSTORM);

    @Override
    public boolean isAvailable(FishingContext context) {
        Instance instance = context.player().getInstance();
        if (instance == null) {
            return condition == Condition.CLEAR;
        }
        Weather weather = instance.getWeather();
        return switch (condition) {
            case RAINING -> weather.rainLevel() > 0.0f;
            case CLEAR -> weather.rainLevel() == 0.0f;
            case THUNDERSTORM -> weather.thunderLevel() > 0.0f;
        };
    }

    public enum Condition {
        RAINING, CLEAR, THUNDERSTORM
    }
}
