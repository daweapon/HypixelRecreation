package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record ToyBoxButton(Material material, String name, String flavor, String description, @Nullable Consumer<SkyBlockPlayer> action) {
    public static ToyBoxButton of(Material material, String name, String flavor, String description, Consumer<SkyBlockPlayer> action) {
        return new ToyBoxButton(material, name, flavor, description, action);
    }

    public static ToyBoxButton unavailable(Material material, String name, String description) {
        return new ToyBoxButton(material, name, "<8>Coming from Centauri's toy box.", description, null);
    }

    public boolean isAvailable() {
        return action != null;
    }
}
