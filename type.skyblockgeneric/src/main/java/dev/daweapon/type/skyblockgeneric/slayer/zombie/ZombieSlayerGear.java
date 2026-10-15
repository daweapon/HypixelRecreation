package dev.daweapon.type.skyblockgeneric.slayer.zombie;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class ZombieSlayerGear {
    public static final Set<String> REVENANT_BLADES = Set.of("REVENANT_FALCHION", "REVENANT_SWORD");
    public static final Set<String> REAPER_BLADES = Set.of("REAPER_FALCHION", "REAPER_SWORD");
    public static final String WARDEN_HELMET = "WARDEN_HELMET";
    public static final double REVENANT_UNDEAD_BONUS = 1.5;
    public static final double REAPER_UNDEAD_BONUS = 2;
    public static final double REAPER_HEAL_PER_HIT = 10;
    public static final double REAPER_UNDEAD_COMBAT_WISDOM = 45;
    public static final double WARDEN_SPEED_FACTOR = 0.5;
    public static final double WARDEN_SPEED_PER_STEP = 25;
    public static final double WARDEN_BONUS_PER_STEP = 0.2;
    public static final double WARDEN_FLAT_BONUS = 0.01;

    private ZombieSlayerGear() {
    }

    public static double undeadBonus(@Nullable String itemId) {
        if (itemId == null) return 0;
        if (REAPER_BLADES.contains(itemId)) return REAPER_UNDEAD_BONUS;
        if (REVENANT_BLADES.contains(itemId)) return REVENANT_UNDEAD_BONUS;
        return 0;
    }

    public static boolean healsOnHit(@Nullable String itemId) {
        return itemId != null && REAPER_BLADES.contains(itemId);
    }

    public static double wardenBonus(double speed) {
        return Math.max(0, speed) / WARDEN_SPEED_PER_STEP * WARDEN_BONUS_PER_STEP + WARDEN_FLAT_BONUS;
    }

    public static ItemStatistics pieceStatistics(@Nullable SkyBlockItem item) {
        if (item == null || item.isAir() || !WARDEN_HELMET.equals(item.getAttributeHandler().getTypeAsString())) {
            return ItemStatistics.empty();
        }
        return ItemStatistics.builder().withMultiplicative(ItemStatistic.SPEED, WARDEN_SPEED_FACTOR).build();
    }

    public static double outgoingMultiplier(SkyBlockPlayer player, @Nullable SkyBlockMob target) {
        double multiplier = 1;
        if (target != null && target.getMobTypes().contains(MobType.UNDEAD)) {
            multiplier += undeadBonus(heldId(player));
        }
        if (wearsWarden(player)) {
            multiplier += wardenBonus(player.getStatistics().allStatistics().getOverall(ItemStatistic.SPEED));
        }
        return multiplier;
    }

    public static void onMeleeHit(SkyBlockPlayer player) {
        if (!healsOnHit(heldId(player))) return;
        player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + REAPER_HEAL_PER_HIT));
    }

    public static double bonusCombatXp(SkyBlockPlayer player, SkyBlockMob killed, double baseXp) {
        if (!healsOnHit(heldId(player)) || !killed.getMobTypes().contains(MobType.UNDEAD)) return 0;
        return baseXp * REAPER_UNDEAD_COMBAT_WISDOM / 100D;
    }

    private static @Nullable String heldId(SkyBlockPlayer player) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        return held.isAir() ? null : held.getAttributeHandler().getTypeAsString();
    }

    private static boolean wearsWarden(SkyBlockPlayer player) {
        SkyBlockItem helmet = new SkyBlockItem(player.getHelmet());
        return !helmet.isAir() && WARDEN_HELMET.equals(helmet.getAttributeHandler().getTypeAsString());
    }
}
