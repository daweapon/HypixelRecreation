package net.swofty.type.skyblockgeneric.item.armor;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.sets.ArmorPieceEffects;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;

import java.util.Set;

public final class ArmorHitEffects {
    private static final Set<String> SPIKY_HATS = Set.of("PUFFERFISH_HAT", "PUFFERFISH_HAT_CELEBRATION");

    private ArmorHitEffects() {
    }

    public static void onMeleeHit(SkyBlockPlayer player, SkyBlockMob target) {
        spiky(player, target);
    }

    public static void onArrowHit(SkyBlockPlayer player, SkyBlockMob target) {
        spiky(player, target);
        if (!ArmorWorn.helmetIs(player, "SKELETON_HAT")) return;
        if (!LootRoll.roll(player.getUuid(), null, ArmorAbilities.SKELETON_HAT_CHANCE)) return;
        splash(player, target, ArmorAbilities.SKELETON_HAT_DAMAGE, ArmorAbilities.SKELETON_HAT_RANGE);
    }

    private static void spiky(SkyBlockPlayer player, SkyBlockMob target) {
        String helmet = ArmorWorn.idOf(player.getHelmet());
        if (helmet == null || !SPIKY_HATS.contains(helmet)) return;
        if (!LootRoll.roll(player.getUuid(), null, ArmorAbilities.SPIKY_CHANCE)) return;
        double strength = player.getStatistics().allStatistics().getOverall(ItemStatistic.STRENGTH);
        splash(player, target, ArmorAbilities.spikyDamage(strength), ArmorAbilities.SPIKY_RANGE);
    }

    private static void splash(SkyBlockPlayer player, SkyBlockMob center, double damage, double range) {
        if (center.getInstance() == null) return;
        for (var entity : center.getInstance().getNearbyEntities(center.getPosition(), range)) {
            if (entity == center || !(entity instanceof SkyBlockMob mob) || mob.isDead() || mob.isRemoved()) continue;
            AttackService.applyHit(player, mob, (float) damage, false);
        }
        AttackService.applyHit(player, center, (float) damage, false);
    }
}
