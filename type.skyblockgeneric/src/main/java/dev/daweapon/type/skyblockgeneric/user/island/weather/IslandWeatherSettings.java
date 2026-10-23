package dev.daweapon.type.skyblockgeneric.user.island.weather;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;

public final class IslandWeatherSettings {
    public static final String UNLOCKED_KEY = "weather_node";
    public static final String WEATHER_KEY = "weather";

    private IslandWeatherSettings() {
    }

    public static boolean unlocked(SkyBlockIsland island) {
        return island.getDatabase().has(UNLOCKED_KEY) && Boolean.TRUE.equals(island.getDatabase().get(UNLOCKED_KEY, Boolean.class));
    }

    public static IslandWeather current(SkyBlockIsland island) {
        return island.getDatabase().has(WEATHER_KEY)
                ? IslandWeather.parse(String.valueOf(island.getDatabase().get(WEATHER_KEY, String.class)))
                : IslandWeather.SUNNY;
    }

    public static void apply(SkyBlockIsland island) {
        if (island.getIslandInstance() == null || !unlocked(island)) return;
        island.getIslandInstance().setWeather(current(island).weather());
    }

    public static boolean consumeNode(SkyBlockPlayer player, SkyBlockIsland island) {
        if (unlocked(island)) {
            player.sendMessage("<c>You can only place 1 of these on your island at a time!");
            return false;
        }
        if (player.countItem(ItemType.WEATHER_NODE) <= 0) {
            player.sendMessage("<c>You need a Weather Node to unlock island weather control!");
            return false;
        }
        player.takeItem(ItemType.WEATHER_NODE, 1);
        island.getDatabase().insertOrUpdate(UNLOCKED_KEY, true);
        player.sendMessage("<a>You unlocked the <b>Weather Node<a>! Use it from the Island Properties menu to control your island weather!");
        return true;
    }

    public static void set(SkyBlockPlayer player, SkyBlockIsland island, IslandWeather weather) {
        island.getDatabase().insertOrUpdate(WEATHER_KEY, weather.name());
        apply(island);
        player.sendMessage("<e>You have changed the weather to: <b>{}<e>.", weather.label());
        player.sendMessage("<e>Weather has been set in your <a>Weather Node<e>.");
    }
}
