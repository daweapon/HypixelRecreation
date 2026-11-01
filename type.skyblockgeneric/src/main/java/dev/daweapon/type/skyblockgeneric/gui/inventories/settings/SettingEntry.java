package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.type.generic.data.datapoints.DatapointToggles.Toggles.ToggleType;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.item.Material;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public record SettingEntry(int slot, Material material, String name, String description,
                           Predicate<HypixelPlayer> reader, BiConsumer<HypixelPlayer, Boolean> writer) {

    public static SettingEntry stored(int slot, Material material, String name, String description, ToggleType type) {
        return new SettingEntry(slot, material, name, description,
                player -> player.getToggles().get(type),
                (player, value) -> player.getToggles().set(type, value));
    }

    public String plainName() {
        return name.replaceAll("<[^>]*>", "");
    }
}
