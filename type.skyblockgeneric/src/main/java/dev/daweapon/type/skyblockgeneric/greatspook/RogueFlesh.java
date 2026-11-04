package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerQuest;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerTier;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class RogueFlesh {
    public static final double CHANCE = 100;
    public static final int TIMER_SECONDS = 1;

    private RogueFlesh() {
    }

    public static boolean qualifies(SlayerQuest quest, int timerSecondsShown) {
        return quest.type() == SlayerType.REVENANT_HORROR && quest.tier() == SlayerTier.I
                && timerSecondsShown <= TIMER_SECONDS;
    }

    public static void onBossKilled(SkyBlockPlayer player, SlayerQuest quest, SlayerBossMob boss) {
        if (!GreatSpookIngredients.questOpen(player)) return;
        if (!qualifies(quest, boss.remainingSecondsShown())) return;
        if (!LootRoll.roll(player.getUuid(), ItemType.ROGUE_FLESH, CHANCE)) return;
        GreatSpookIngredients.give(player, ItemType.ROGUE_FLESH);
    }
}
