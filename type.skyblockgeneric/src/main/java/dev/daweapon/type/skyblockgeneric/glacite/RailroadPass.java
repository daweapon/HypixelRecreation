package dev.daweapon.type.skyblockgeneric.glacite;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class RailroadPass {
    public static final int REQUIRED_HOTM_TIER = 7;

    private RailroadPass() {}

    public static boolean meetsRequirement(SkyBlockPlayer player) {
        return HotmService.data(player).getTier() >= REQUIRED_HOTM_TIER;
    }

    public static boolean isUnlocked(SkyBlockPlayer player) {
        return HotmService.data(player).isRailroadPassShown();
    }

    public static boolean showPass(SkyBlockPlayer player) {
        if (isUnlocked(player)) return true;
        if (player.countItem(ItemType.SECRET_RAILROAD_PASS) <= 0) return false;
        unlock(player);
        return true;
    }

    public static void unlock(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        data.setRailroadPassShown(true);
        HotmService.save(player, data);
    }
}
