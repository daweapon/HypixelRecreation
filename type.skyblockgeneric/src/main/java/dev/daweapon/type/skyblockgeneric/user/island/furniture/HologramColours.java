package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import net.minestom.server.color.Color;

final class HologramColours {
    private static final int[] RGB = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };
    private static final String[] NAMES = {
            "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple", "Gold", "Gray",
            "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple", "Yellow", "White"
    };

    private HologramColours() {
    }

    static Color of(char code) {
        return new Color(RGB[HologramSettings.PARTICLE_COLOURS.indexOf(code)]);
    }

    static String nameOf(char code) {
        return NAMES[HologramSettings.PARTICLE_COLOURS.indexOf(code)];
    }
}
