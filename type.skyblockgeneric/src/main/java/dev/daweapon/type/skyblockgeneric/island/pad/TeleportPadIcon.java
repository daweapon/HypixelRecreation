package dev.daweapon.type.skyblockgeneric.island.pad;

import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record TeleportPadIcon(Material material, String colour, String name) {
    private static final String[] COLOUR_KEYS = {"red", "yellow", "lime", "blue", "magenta", "orange", "light_gray",
            "white", "green", "light_blue", "purple", "gray", "cyan"};
    private static final String[] COLOUR_CODES = {"<c>", "<e>", "<a>", "<9>", "<d>", "<6>", "<7>", "<f>", "<2>", "<b>",
            "<5>", "<8>", "<3>"};
    private static final String[] COLOUR_NAMES = {"Red", "Yellow", "Lime", "Blue", "Magenta", "Orange", "Gray", "White",
            "Green", "Aqua", "Purple", "Black", "Cyan"};
    private static final String[] DYES = {"red_dye", "yellow_dye", "lime_dye", "lapis_lazuli", "magenta_dye", "orange_dye",
            "light_gray_dye", "bone_meal", "green_dye", "light_blue_dye", "purple_dye", "gray_dye", "cyan_dye"};
    private static final String[] ONES = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
    private static final String[] TENS = {"", "", "Twenty", "Thirty", "Forty"};
    private static final int CARPET_COUNT = 4;
    private static final List<TeleportPadIcon> ALL = build();

    public static List<TeleportPadIcon> all() {
        return ALL;
    }

    public static TeleportPadIcon get(int index) {
        return ALL.get(Math.floorMod(index, ALL.size()));
    }

    public static String numberWord(int number) {
        if (number < 20) return ONES[number];
        int tens = number / 10;
        int ones = number % 10;
        return TENS[tens] + ONES[ones];
    }

    public String label() {
        return colour + name;
    }

    private static List<TeleportPadIcon> build() {
        List<TeleportPadIcon> icons = new ArrayList<>();
        int colours = COLOUR_KEYS.length;
        for (int i = 0; i < colours; i++) {
            icons.add(new TeleportPadIcon(material(COLOUR_KEYS[i] + "_wool"), COLOUR_CODES[i], COLOUR_NAMES[i]));
        }
        int number = 1;
        for (int i = 0; i < colours; i++) {
            icons.add(numbered(COLOUR_KEYS[i] + "_terracotta", i, number++));
        }
        for (int i = 0; i < colours; i++) {
            icons.add(numbered(DYES[i], i, number++));
        }
        for (int i = 0; i < colours; i++) {
            icons.add(numbered(COLOUR_KEYS[i] + "_stained_glass", i, number++));
        }
        for (int i = 0; i < CARPET_COUNT; i++) {
            icons.add(numbered(COLOUR_KEYS[i] + "_carpet", i, number++));
        }
        return List.copyOf(icons);
    }

    private static TeleportPadIcon numbered(String materialKey, int colour, int number) {
        return new TeleportPadIcon(material(materialKey), COLOUR_CODES[colour], numberWord(number));
    }

    private static Material material(String key) {
        return Objects.requireNonNull(Material.fromKey("minecraft:" + key), key);
    }
}
