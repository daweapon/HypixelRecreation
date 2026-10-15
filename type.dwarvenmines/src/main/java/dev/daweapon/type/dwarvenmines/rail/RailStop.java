package dev.daweapon.type.dwarvenmines.rail;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.Material;

@Getter
public enum RailStop {
    DWARVEN_VILLAGE("Dwarven Village", Material.OAK_DOOR, new Pos(38.5, 201, -85.5), false),
    UPPER_MINES("Upper Mines", Material.COAL_ORE, new Pos(-134.5, 173, -62.5), false),
    FORGE_BASIN("Forge Basin", Material.FURNACE, new Pos(0.5, 145, 23.5), false),
    HANGING_COURT("Hanging Court", Material.QUARTZ_BLOCK, new Pos(86.5, 187, 114.5), false),
    GREAT_ICE_WALL("Great Ice Wall", Material.PACKED_ICE, new Pos(46.5, 131, 173.5), false),
    FAR_RESERVE("Far Reserve", Material.IRON_ORE, new Pos(-96.5, 142, 111.5), false),
    ROYAL_MINES("Royal Mines", Material.GOLD_ORE, new Pos(84.5, 114, 134.5), false),
    ABANDONED_QUARRY("Abandoned Quarry", Material.COBBLESTONE, new Pos(-146.5, 149, -30.5), false),
    GLACITE_TUNNELS("Glacite Tunnels", Material.BLUE_ICE, new Pos(23.5, 128, 207.5), true);

    private final String displayName;
    private final Material icon;
    private final Pos position;
    private final boolean requiresPass;

    RailStop(String displayName, Material icon, Pos position, boolean requiresPass) {
        this.displayName = displayName;
        this.icon = icon;
        this.position = position;
        this.requiresPass = requiresPass;
    }

    public long fareFrom(RailStop origin) {
        return (long) Math.ceil(origin.position.distance(position) * 2);
    }
}
