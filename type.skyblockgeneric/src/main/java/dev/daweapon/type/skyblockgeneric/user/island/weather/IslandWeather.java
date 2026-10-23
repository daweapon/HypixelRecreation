package dev.daweapon.type.skyblockgeneric.user.island.weather;

import net.minestom.server.instance.Weather;
import net.minestom.server.item.Material;

public enum IslandWeather {
    SUNNY("Sunny", Material.SUNFLOWER, Weather.CLEAR),
    RAINING("Raining", Material.WATER_BUCKET, Weather.RAIN),
    THUNDERING("Thundering", Material.LIGHTNING_ROD, Weather.THUNDER);

    private final String label;
    private final Material icon;
    private final Weather weather;

    IslandWeather(String label, Material icon, Weather weather) {
        this.label = label;
        this.icon = icon;
        this.weather = weather;
    }

    public String label() {
        return label;
    }

    public Material icon() {
        return icon;
    }

    public Weather weather() {
        return weather;
    }

    public static IslandWeather parse(String raw) {
        for (IslandWeather value : values()) {
            if (value.name().equalsIgnoreCase(raw)) return value;
        }
        return SUNNY;
    }
}
