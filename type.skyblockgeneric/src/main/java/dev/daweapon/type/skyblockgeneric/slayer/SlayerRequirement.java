package dev.daweapon.type.skyblockgeneric.slayer;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

public final class SlayerRequirement {
    private SlayerRequirement() {
    }

    public static int level(SkyBlockPlayer player, SlayerType type) {
        SlayerDefinition definition = SlayerRegistry.get(type);
        return definition == null ? 0 : definition.levelForXp(player.getSlayerData().progress(type).getXp());
    }

    public static boolean isMet(SkyBlockPlayer player, String slayer, int level) {
        SlayerType type = parse(slayer);
        return type == null || level(player, type) >= level;
    }

    public static String display(String slayer, int level) {
        SlayerType type = parse(slayer);
        return (type == null ? slayer : type.categoryName()) + " Slayer " + level;
    }

    public static String lockedLine(SlayerType type, int level) {
        return "<4>☠ <c>Requires <5>" + type.categoryName() + " Slayer " + level + "<c>.";
    }

    private static @Nullable SlayerType parse(String slayer) {
        for (SlayerType type : SlayerType.values()) {
            if (type.name().equalsIgnoreCase(slayer) || type.categoryName().equalsIgnoreCase(slayer)) return type;
        }
        return null;
    }
}
