package dev.daweapon.type.skyblockgeneric.shop.catalogue;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** A Catacombs floor completion gate, written in shop YAML as {@code requires: { catacombs_floor: FLOOR_THREE }}. */
public record CatacombsFloorRequirement(CatacombsFloor floor) implements ShopRequirement {

    public static @Nullable CatacombsFloorRequirement parse(@Nullable Object raw) {
        if (!(raw instanceof Map<?, ?> map) || map.get("catacombs_floor") == null) return null;
        try {
            return new CatacombsFloorRequirement(CatacombsFloor.valueOf(String.valueOf(map.get("catacombs_floor")).toUpperCase()));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    public boolean isMet(SkyBlockPlayer player) {
        return CatacombsProgress.hasCompleted(player, floor);
    }

    @Override
    public String lockLine() {
        return "<c>Requires completing The Catacombs " + floor.displayName() + "!";
    }
}
