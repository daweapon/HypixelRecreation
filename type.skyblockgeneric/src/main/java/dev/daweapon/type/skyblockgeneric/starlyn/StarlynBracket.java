package dev.daweapon.type.skyblockgeneric.starlyn;

import lombok.Getter;
import net.minestom.server.item.Material;

@Getter
public enum StarlynBracket {
    COMMON("<f>", Material.WHITE_STAINED_GLASS_PANE, false),
    UNCOMMON("<a>", Material.LIME_STAINED_GLASS_PANE, false),
    RARE("<9>", Material.LIGHT_BLUE_STAINED_GLASS_PANE, false),
    EPIC("<5>", Material.PURPLE_STAINED_GLASS_PANE, false),
    LEGENDARY("<6>", Material.ORANGE_STAINED_GLASS_PANE, false),
    MYTHIC("<d>", Material.MAGENTA_STAINED_GLASS_PANE, true),
    DIVINE("<b>", Material.CYAN_STAINED_GLASS_PANE, true),
    SPECIAL("<c>", Material.RED_STAINED_GLASS_PANE, true);

    public static final int MAIN_BRACKETS = 5;

    private final String color;
    private final Material pane;
    private final boolean hidden;

    StarlynBracket(String color, Material pane, boolean hidden) {
        this.color = color;
        this.pane = pane;
        this.hidden = hidden;
    }

    public String formatted() {
        return color + "<l>" + name();
    }

    public boolean atLeast(StarlynBracket other) {
        return ordinal() >= other.ordinal();
    }

    public int triggerValue() {
        return ordinal() + 1;
    }
}
