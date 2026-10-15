package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;

import java.util.Locale;

/** The floor id the orchestrator sends: {@code ENTRANCE} or {@code F1} to {@code F7}, prefixed {@code MASTER_} for Master Mode. */
public record CatacombsFloorId(CatacombsFloor floor, CatacombsMode mode) {
    private static final String MASTER_PREFIX = "MASTER_";

    public CatacombsFloorId {
        if (!floor.supports(mode)) throw new IllegalArgumentException(floor.displayName() + " has no " + mode + " mode");
    }

    public static CatacombsFloorId parse(String id) {
        String text = id.trim().toUpperCase(Locale.ROOT);
        CatacombsMode mode = text.startsWith(MASTER_PREFIX) ? CatacombsMode.MASTER : CatacombsMode.NORMAL;
        if (mode == CatacombsMode.MASTER) text = text.substring(MASTER_PREFIX.length());
        return new CatacombsFloorId(floor(text), mode);
    }

    private static CatacombsFloor floor(String text) {
        if (text.equals("ENTRANCE")) return CatacombsFloor.ENTRANCE;
        if (text.matches("F[1-7]")) return CatacombsFloor.byNumber(text.charAt(1) - '0');
        throw new IllegalArgumentException("Unknown Catacombs floor '" + text + "'");
    }

    public String encode() {
        String base = floor == CatacombsFloor.ENTRANCE ? "ENTRANCE" : "F" + floor.ordinal();
        return mode == CatacombsMode.MASTER ? MASTER_PREFIX + base : base;
    }
}
