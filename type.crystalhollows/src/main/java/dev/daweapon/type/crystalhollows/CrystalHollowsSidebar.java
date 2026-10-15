package dev.daweapon.type.crystalhollows;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsZones;
import dev.daweapon.type.skyblockgeneric.crystalhollows.HeatService;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CrystalHollowsSidebar {
    private CrystalHollowsSidebar() {
    }

    public static @Nullable List<Text> detail(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        if (region == null || !CrystalHollowsZones.isCrystalHollows(region.getType())) return null;
        int heat = (int) Math.floor(HeatService.heat(player));
        boolean hot = HeatService.isHot(region.getType(), player.getPosition().y());
        return heat < 1 && !hot ? List.of() : List.of(Text.of("<f>Heat: <c>{}♨", heat));
    }
}
