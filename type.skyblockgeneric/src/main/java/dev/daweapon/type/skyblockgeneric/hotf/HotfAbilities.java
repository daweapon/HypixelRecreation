package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.hotf.throwing.AxeThrowing;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HotfAbilities {
    private static final Map<UUID, Map<HotfAbility, Long>> ACTIVE_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> SLICER_SWEEP = new ConcurrentHashMap<>();
    private static final double MANA_PER_SWEEP = 100;

    private HotfAbilities() {
    }

    public static boolean isUnlocked(SkyBlockPlayer player, HotfAbility ability) {
        return HotfService.data(player).perkLevel(ability.perkId()) > 0;
    }

    public static HotfAbility selected(SkyBlockPlayer player) {
        HotfAbility selected = HotfAbility.byId(HotfService.data(player).getSelectedAbility());
        return selected != null && isUnlocked(player, selected) ? selected : null;
    }

    public static boolean select(SkyBlockPlayer player, HotfAbility ability) {
        if (!isUnlocked(player, ability)) return false;
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        data.setSelectedAbility(ability.name());
        HotfService.save(player, data);
        return true;
    }

    public static boolean isActive(SkyBlockPlayer player, HotfAbility ability) {
        Map<HotfAbility, Long> active = ACTIVE_UNTIL.get(player.getUuid());
        if (active == null) return false;
        Long until = active.get(ability);
        if (until == null) return false;
        if (until <= System.currentTimeMillis()) {
            active.remove(ability);
            if (ability == HotfAbility.MANIAC_SLICER) SLICER_SWEEP.remove(player.getUuid());
            return false;
        }
        return true;
    }

    public static long cooldownRemainingMillis(SkyBlockPlayer player) {
        long latest = 0;
        for (Long readyAt : HotfService.data(player).getAbilityReadyAt().values()) latest = Math.max(latest, readyAt);
        return Math.max(0, latest - System.currentTimeMillis());
    }

    public static boolean usableHere() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_HUB;
    }

    public static boolean activate(SkyBlockPlayer player) {
        HotfAbility ability = selected(player);
        if (ability == null) return false;
        if (!usableHere()) {
            player.sendMessage("<c>Axe Abilities can't be used in the Hub!");
            return true;
        }
        long remaining = cooldownRemainingMillis(player);
        if (remaining > 0) {
            player.sendMessage("<c>This ability is on cooldown for {}s.", Math.round(remaining / 1000.0));
            return true;
        }
        int level = HotfService.abilityLevel(player);
        if (ability == HotfAbility.MANIAC_SLICER) {
            double mana = player.getMana();
            if (mana < MANA_PER_SWEEP) {
                player.sendMessage("<c>You need at least 100 Mana to use this ability!");
                return true;
            }
            SLICER_SWEEP.put(player.getUuid(), Math.floor(mana / MANA_PER_SWEEP));
            player.setMana(0);
        }
        ACTIVE_UNTIL.computeIfAbsent(player.getUuid(), id -> new ConcurrentHashMap<>())
                .put(ability, System.currentTimeMillis() + ability.duration(level) * 1000L);
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        data.getAbilityReadyAt().put(ability.name(), System.currentTimeMillis() + ability.cooldown(level) * 1000L);
        HotfService.save(player, data);
        player.sendMessage("<a>You used <6>{}<a>!", ability.getDisplayName());
        if (ability == HotfAbility.AXE_TOSS) {
            AxeThrowing.throwWithCooldown(player, new SkyBlockItem(player.getItemInMainHand()), null);
        }
        return true;
    }

    public static double outgoingMultiplier(SkyBlockPlayer player) {
        double boost = HotfService.onForagingIsland() && isActive(player, HotfAbility.DAMAGE_BOOST) ? 2 : 1;
        return boost * dev.daweapon.type.skyblockgeneric.foraging.booster.ForagingBoosters.fightingMultiplier(player);
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (!isActive(player, HotfAbility.MANIAC_SLICER)) return ItemStatistics.empty();
        double sweep = SLICER_SWEEP.getOrDefault(player.getUuid(), 0D);
        return ItemStatistics.empty().addBase(ItemStatistic.SWEEP, sweep);
    }

    public static void clear(UUID player) {
        ACTIVE_UNTIL.remove(player);
        AxeThrowing.clear(player);
        SLICER_SWEEP.remove(player);
    }
}
