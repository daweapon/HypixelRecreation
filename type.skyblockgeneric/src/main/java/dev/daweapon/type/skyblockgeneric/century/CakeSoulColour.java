package dev.daweapon.type.skyblockgeneric.century;

import org.jetbrains.annotations.Nullable;

public enum CakeSoulColour {
    PINK("Pink", "<d>"),
    LIME("Lime", "<a>"),
    RED("Red", "<c>"),
    YELLOW("Yellow", "<e>"),
    AQUA("Aqua", "<b>"),
    BLUE("Blue", "<9>"),
    BLACK("Black", "<8>"),
    ORANGE("Orange", "<6>"),
    PURPLE("Purple", "<5>"),
    BROWN("Brown", "<6>"),
    WHITE("White", "<f>"),
    CYAN("Cyan", "<3>"),
    GREEN("Green", "<2>"),
    GRAY("Gray", "<8>"),
    MAGENTA("Magenta", "<d>"),
    LIGHT_GRAY("Light Gray", "<7>");

    private final String displayName;
    private final String tag;

    CakeSoulColour(String displayName, String tag) {
        this.displayName = displayName;
        this.tag = tag;
    }

    public String displayName() {
        return displayName;
    }

    public String tag() {
        return tag;
    }

    public static @Nullable CakeSoulColour parse(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return valueOf(raw);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
