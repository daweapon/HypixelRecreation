package dev.daweapon.type.skyblockgeneric.moonglade;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class MobTheFish {
    public static final double CHANCE_PERCENT = 0.05;
    public static final long PITY_KILLS = 2500;
    static final String KILLS_KEY = "pity.kills.MOONGLADE_MARSH";

    private MobTheFish() {
    }

    public static boolean pityDue(long kills) {
        return kills >= PITY_KILLS;
    }

    public static boolean appliesOn(ServerType type) {
        return type == ServerType.SKYBLOCK_GALATEA || type == ServerType.SKYBLOCK_TORRHUS_CANYON;
    }

    public static void onMobKilled(SkyBlockPlayer player) {
        ServerType type = HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType();
        if (!appliesOn(type)) return;
        var data = player.getHuntingData();
        boolean pity = type == ServerType.SKYBLOCK_GALATEA && pityDue(data.incrementCounter(KILLS_KEY));
        if (!pity && !LootRoll.roll(player.getUuid(), ItemType.MOB_THE_FISH, CHANCE_PERCENT)) return;
        data.setCounter(KILLS_KEY, 0);
        player.addAndUpdateItem(ItemType.MOB_THE_FISH);
        player.sendMessage("<d><l>RARE DROP! <7>You found <a>Mob the Fish<7>!");
        ChapterTriggers.fire(player, "find.mob_the_fish");
    }
}
