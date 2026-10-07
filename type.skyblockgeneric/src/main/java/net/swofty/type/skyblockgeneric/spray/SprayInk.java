package net.swofty.type.skyblockgeneric.spray;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** The 16 vanilla dyes a Spray Can stores as ink, in the Ink Storage's order (wiki Spray Can/UI). */
@Getter
public enum SprayInk {
    BONE_MEAL("Bone Meal", Material.BONE_MEAL, ItemType.BONE_MEAL),
    ORANGE_DYE("Orange Dye", Material.ORANGE_DYE, ItemType.ORANGE_DYE),
    MAGENTA_DYE("Magenta Dye", Material.MAGENTA_DYE, ItemType.MAGENTA_DYE),
    LIGHT_BLUE_DYE("Light Blue Dye", Material.LIGHT_BLUE_DYE, ItemType.LIGHT_BLUE_DYE),
    DANDELION_YELLOW("Dandelion Yellow", Material.YELLOW_DYE, ItemType.YELLOW_DYE),
    LIME_DYE("Lime Dye", Material.LIME_DYE, ItemType.LIME_DYE),
    PINK_DYE("Pink Dye", Material.PINK_DYE, ItemType.PINK_DYE),
    GRAY_DYE("Gray Dye", Material.GRAY_DYE, ItemType.GRAY_DYE),
    LIGHT_GRAY_DYE("Light Gray Dye", Material.LIGHT_GRAY_DYE, ItemType.LIGHT_GRAY_DYE),
    CYAN_DYE("Cyan Dye", Material.CYAN_DYE, ItemType.CYAN_DYE),
    PURPLE_DYE("Purple Dye", Material.PURPLE_DYE, ItemType.PURPLE_DYE),
    LAPIS_LAZULI("Lapis Lazuli", Material.LAPIS_LAZULI, ItemType.LAPIS_LAZULI),
    COCOA_BEANS("Cocoa Beans", Material.COCOA_BEANS, ItemType.COCOA_BEANS),
    CACTUS_GREEN("Cactus Green", Material.GREEN_DYE, ItemType.CACTUS_GREEN, ItemType.GREEN_DYE),
    ROSE_RED("Rose Red", Material.RED_DYE, ItemType.RED_DYE),
    INK_SAC("Ink Sac", Material.INK_SAC, ItemType.INK_SAC);

    public static final int MAX_PERCENT = 100;

    private final String displayName;
    private final Material icon;
    private final List<ItemType> items;

    SprayInk(String displayName, Material icon, ItemType... items) {
        this.displayName = displayName;
        this.icon = icon;
        this.items = List.of(items);
    }

    /** One "Dye: N%" lore line, green when there is enough of it and red when there isn't. */
    public Text loreLine(int percent, boolean enough) {
        return Text.of(enough ? "  <7>{}: <a>{}%" : "  <7>{}: <c>{}%", displayName, percent);
    }

    public static Optional<SprayInk> fromItem(@Nullable ItemType type) {
        if (type == null) return Optional.empty();
        return Arrays.stream(values()).filter(ink -> ink.items.contains(type)).findFirst();
    }
}
