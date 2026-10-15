package dev.daweapon.type.skyblockgeneric.garden.pest;

import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryData;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryEntry;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.CataloguedBestiaryAreas;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class PestBestiary {
    private static final BestiaryData DATA = new BestiaryData();

    private PestBestiary() {
    }

    public static void recordKill(SkyBlockPlayer player, String pestId) {
        BestiaryMob mob = family(pestId);
        if (mob != null) bestiary(player).increase(mob, 1);
    }

    public static int totalTiers(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        DatapointBestiary.PlayerBestiary bestiary = bestiary(player);
        int tiers = 0;
        for (BestiaryEntry entry : CataloguedBestiaryAreas.GARDEN) {
            List<BestiaryMob> mobs = entry.getMobs();
            tiers += DATA.getCurrentBestiaryTier(mobs.getFirst(), bestiary.getAmount(mobs));
        }
        return tiers;
    }

    private static BestiaryMob family(String pestId) {
        for (BestiaryEntry entry : CataloguedBestiaryAreas.GARDEN) {
            for (BestiaryMob mob : entry.getMobs()) {
                if (mob.getMobID().equals(pestId)) return mob;
            }
        }
        return null;
    }

    private static DatapointBestiary.PlayerBestiary bestiary(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class).getValue();
    }
}
