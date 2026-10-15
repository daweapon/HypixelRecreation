package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;

import java.util.Arrays;
import java.util.Optional;

/** The floor ids the orchestrator request carries: ENTRANCE and F1 to F7 (a MASTER_ prefix is reserved for Master Mode). */
public final class CatacombsFloorIds {
    public static final String ENTRANCE = "ENTRANCE";

    private CatacombsFloorIds() {
    }

    public static String of(CatacombsFloor floor) {
        return floor == CatacombsFloor.ENTRANCE ? ENTRANCE : "F" + floor.ordinal();
    }

    public static Optional<CatacombsFloor> parse(String id) {
        return Arrays.stream(CatacombsFloor.values()).filter(floor -> of(floor).equals(id)).findFirst();
    }
}
