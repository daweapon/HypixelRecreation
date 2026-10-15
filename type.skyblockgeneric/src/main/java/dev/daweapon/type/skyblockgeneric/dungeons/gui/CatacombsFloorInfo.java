package dev.daweapon.type.skyblockgeneric.dungeons.gui;

import net.minestom.server.item.Material;
import dev.daweapon.dungeons.catacombs.CatacombsDungeonSize;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import org.jetbrains.annotations.Nullable;

/** What the Gate shows for a floor: its slot, icon, boss title, flavour text and requirement line. */
public enum CatacombsFloorInfo {
    ENTRANCE(CatacombsFloor.ENTRANCE, 11, Material.ZOMBIE_HEAD, "Stalker",
            "<7>This strange creature is roaming the\n<7>Catacombs to add powerful adventurers to\n<7>its collection.",
            "<b>Combat Level XV"),
    FLOOR_I(CatacombsFloor.FLOOR_ONE, 12, Material.SKELETON_SKULL, "New Necromancer",
            "<7>Involved in the dark arts due to his\n<7>parents' insistence. Originally worked as\n<7>a Circus clown.",
            "<d>Catacombs Level I"),
    FLOOR_II(CatacombsFloor.FLOOR_TWO, 13, Material.SKELETON_SKULL, "Apprentice Necromancer",
            "<7>First of his class. His teacher said he\n<7>will do \"great things\".",
            "<d>Catacombs Level III"),
    FLOOR_III(CatacombsFloor.FLOOR_THREE, 14, Material.SKELETON_SKULL, "Professor",
            "<7>Despite his great technique, he failed\n<7>the Masters exam three times. Works from 8\n<7>to 5. Cares about his students.",
            "<d>Catacombs Level V"),
    FLOOR_IV(CatacombsFloor.FLOOR_FOUR, 15, Material.SKELETON_SKULL, "Shaman Necromancer",
            "<7>Powerful Necromancer that specializes in\n<7>animals. Calls himself a vegetarian, go\n<7>figure.",
            "<d>Catacombs Level IX"),
    FLOOR_V(CatacombsFloor.FLOOR_FIVE, 21, Material.SKELETON_SKULL, "Master Necromancer",
            "<7>Strongly believes he will become the\n<7>Lord one day. Subject of mockeries, even\n<7>from his disciples.",
            "<d>Catacombs Level XIV"),
    FLOOR_VI(CatacombsFloor.FLOOR_SIX, 22, Material.SKELETON_SKULL, "Necromancer Lord",
            "<7>Necromancy was always strong in his\n<7>family. Says he once beat a Wither in a\n<7>duel. Likes to brag.",
            "<d>Catacombs Level XIX"),
    FLOOR_VII(CatacombsFloor.FLOOR_SEVEN, 23, Material.WITHER_SKELETON_SKULL, "The Wither Lords",
            "<7>Disciples of the Wither King. Inherited\n<7>the Catacombs eons ago. Never defeated,\n<7>feared by anything living AND dead.",
            "<d>Catacombs Level XXIV");

    private final CatacombsFloor floor;
    private final int slot;
    private final Material material;
    private final String bossTitle;
    private final String description;
    private final String requirement;

    CatacombsFloorInfo(CatacombsFloor floor, int slot, Material material, String bossTitle, String description, String requirement) {
        this.floor = floor;
        this.slot = slot;
        this.material = material;
        this.bossTitle = bossTitle;
        this.description = description;
        this.requirement = requirement;
    }

    public CatacombsFloor floor() {
        return floor;
    }

    public int slot() {
        return slot;
    }

    public Material material() {
        return material;
    }

    public String bossTitle() {
        return bossTitle;
    }

    public String description() {
        return description;
    }

    public String requirement() {
        return requirement;
    }

    public static Material material(@Nullable CatacombsFloor floor) {
        if (floor == null) return Material.WITHER_SKELETON_SKULL;
        for (CatacombsFloorInfo info : values()) if (info.floor == floor) return info.material;
        return Material.SKELETON_SKULL;
    }

    public static String size(CatacombsFloor floor) {
        CatacombsDungeonSize size = floor.dungeonSize();
        return size.width() + "x" + size.height();
    }
}
