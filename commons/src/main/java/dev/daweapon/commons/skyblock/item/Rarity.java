package dev.daweapon.commons.skyblock.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Getter
@AllArgsConstructor
// Enumerates SkyBlock item rarity tiers with their display color, tooltip style, and whether they can be reforged.
public enum Rarity {
    COMMON(NamedTextColor.WHITE, "hypixel_skyblock:common"),
    UNCOMMON(NamedTextColor.GREEN, "hypixel_skyblock:uncommon"),
    RARE(NamedTextColor.BLUE, "hypixel_skyblock:rare"),
    EPIC(NamedTextColor.DARK_PURPLE, "hypixel_skyblock:epic"),
    LEGENDARY(NamedTextColor.GOLD, "hypixel_skyblock:legendary"),
    MYTHIC(NamedTextColor.LIGHT_PURPLE, "hypixel_skyblock:mythic"),
    DIVINE(NamedTextColor.AQUA, "hypixel_skyblock:divine"),
    SPECIAL(NamedTextColor.RED, "hypixel_skyblock:special", false),
    VERY_SPECIAL(NamedTextColor.RED, "hypixel_skyblock:very_special", false),
    ADMIN(NamedTextColor.DARK_RED, "hypixel_skyblock:admin", false),
    ;

    private final TextColor color;
    private final String tooltipStyle;
    private final boolean canReforge;

    Rarity(TextColor color, String tooltipStyle) {
        this(color, tooltipStyle, true);
    }

    /**
     * Accessory Power (formerly Magical Power) an accessory of this rarity adds to the Accessory
     * Bag. SkyBlock Levels pay +1 XP per point of it.
     */
    public int getAccessoryPower() {
        return switch (this) {
            case COMMON, SPECIAL -> 3;
            case UNCOMMON, VERY_SPECIAL -> 5;
            case RARE -> 8;
            case EPIC -> 12;
            case LEGENDARY -> 16;
            case MYTHIC, ADMIN -> 22;
            case DIVINE -> 28;
        };
    }

    public boolean canUpgrade() {
        return upgrade() != this;
    }

    /**
     * One step up, as a Recombobulator 3000 applies it: Common through Mythic climb one tier and
     * Special becomes Very Special. Divine, Very Special and Admin are already at the top of their ladder.
     */
    public Rarity upgrade() {
        return switch (this) {
            case COMMON -> UNCOMMON;
            case UNCOMMON -> RARE;
            case RARE -> EPIC;
            case EPIC -> LEGENDARY;
            case LEGENDARY -> MYTHIC;
            case MYTHIC -> DIVINE;
            case SPECIAL -> VERY_SPECIAL;
            case DIVINE, VERY_SPECIAL, ADMIN -> this;
        };
    }

    public Rarity downgrade() {
        if (this.ordinal() - 1 < 0)
            return this;
        return values()[this.ordinal() - 1];
    }

    public boolean isAtLeast(Rarity rarity) {
        return ordinal() >= rarity.ordinal();
    }

    public Component getDisplay() {
        return Component.text(name().replace("_", " "), color, TextDecoration.BOLD);
    }

    public Component getDisplayCapitalized() {
        return Component.text(name().charAt(0) + name().toLowerCase().replace("_", " ").substring(1), color, TextDecoration.BOLD);
    }

    @Deprecated(forRemoval = true)
    public String getLegacyColor() {
        return LegacyComponentSerializer.legacySection().serialize(Component.text("|", color)).replace("|", "");
    }

    @Deprecated(forRemoval = true)
    public String getLegacyDisplay() {
        return LegacyComponentSerializer.legacySection().serialize(getDisplay());
    }

    public static Rarity getRarity(String string) {
        try {
            return Rarity.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
