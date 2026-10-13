package net.swofty.type.crystalhollows;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsZones;
import net.swofty.type.skyblockgeneric.crystalhollows.HeatService;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CrystalHollowsSidebar {
    private CrystalHollowsSidebar() {
    }

    public static @Nullable List<Text> detail(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        if (region == null || !CrystalHollowsZones.isCrystalHollows(region.getType())) return null;
        int heat = (int) Math.floor(HeatService.heat(player));
        return heat < 1 ? List.of() : List.of(Text.of("<c>Heat: {}♨", heat));
    }
}
