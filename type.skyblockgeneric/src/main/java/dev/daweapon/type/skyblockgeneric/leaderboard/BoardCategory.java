package dev.daweapon.type.skyblockgeneric.leaderboard;

import lombok.Getter;
import net.minestom.server.item.Material;

@Getter
public enum BoardCategory {
    GENERAL("General", Material.NETHER_STAR, "<6>"),
    SKILLS("Skills", Material.DIAMOND_PICKAXE, "<a>"),
    SLAYERS("Slayers", Material.ROTTEN_FLESH, "<c>"),
    COLLECTIONS("Collections", Material.PAINTING, "<e>");

    private final String displayName;
    private final Material icon;
    private final String colour;

    BoardCategory(String displayName, Material icon, String colour) {
        this.displayName = displayName;
        this.icon = icon;
        this.colour = colour;
    }
}
