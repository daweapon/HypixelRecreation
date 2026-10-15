package dev.daweapon.type.skyblockgeneric.commission;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsZones;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCommissions;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public final class CommissionProgress {
    private static final Map<ItemType, Objective.BlockTarget> GEMSTONE_DROPS = Map.of(
            ItemType.ROUGH_AMBER_GEM, Objective.BlockTarget.GEM_AMBER,
            ItemType.ROUGH_AMETHYST_GEM, Objective.BlockTarget.GEM_AMETHYST,
            ItemType.ROUGH_JADE_GEM, Objective.BlockTarget.GEM_JADE,
            ItemType.ROUGH_RUBY_GEM, Objective.BlockTarget.GEM_RUBY,
            ItemType.ROUGH_SAPPHIRE_GEM, Objective.BlockTarget.GEM_SAPPHIRE,
            ItemType.ROUGH_TOPAZ_GEM, Objective.BlockTarget.GEM_TOPAZ);

    private CommissionProgress() {
    }

    public static void update(SkyBlockPlayer player, ObjectiveType type, Objective.BlockTarget target, int amount, RegionType regionType) {
        DatapointCommissions.PlayerCommissionData commissionData = Commissions.getCommissionData(player);
        boolean anyUpdated = false;

        for (CommissionZone zone : CommissionZone.values()) {
            for (DatapointCommissions.ActiveCommission activeCommission : zone.active(commissionData)) {
                if (activeCommission.isCompleted()) continue;

                Commission commission = Commissions.getCommissionByName(activeCommission.getCommissionName());
                if (commission == null) continue;
                if (commission.objective.type != type) continue;
                if (commission.objective.target != target) continue;

                if (!commission.objective.location.isAny() && regionType != null) {
                    if (!commission.objective.location.allows(regionType)) continue;
                }

                int newProgress = activeCommission.getProgress() + amount;
                activeCommission.setProgress(newProgress);
                anyUpdated = true;

                if (newProgress >= commission.objective.amount) {
                    activeCommission.setCompleted(true);
                    onComplete(player, commission);
                }
            }
        }

        if (anyUpdated) {
            Commissions.saveCommissionData(player, commissionData);
        }
    }

    public static void recordHollowsDrop(SkyBlockPlayer player, ItemType dropped, int amount, RegionType regionType) {
        if (dropped == null || !CrystalHollowsZones.isCrystalHollows(regionType)) return;
        Objective.BlockTarget gemstone = GEMSTONE_DROPS.get(dropped);
        if (gemstone != null) {
            update(player, ObjectiveType.COLLECT, gemstone, amount, regionType);
        } else if (dropped == ItemType.HARD_STONE) {
            update(player, ObjectiveType.MINE, Objective.BlockTarget.HARD_STONE, amount, regionType);
        }
    }

    public static void recordCrystalFound(SkyBlockPlayer player) {
        update(player, ObjectiveType.COLLECT, Objective.BlockTarget.HOLLOWS_CRYSTAL, 1, null);
    }

    public static void recordChestOpened(SkyBlockPlayer player) {
        update(player, ObjectiveType.OPEN, Objective.BlockTarget.CHEST, 1, null);
    }

    public static void recordKill(SkyBlockPlayer player, SkyBlockMob mob) {
        if (!(mob instanceof dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob bestiaryMob)) return;
        Objective.BlockTarget target = killTarget(bestiaryMob.getMobID());
        if (target == null) return;
        update(player, ObjectiveType.SLAY, target, 1, player.getRegion() == null ? null : player.getRegion().getType());
    }

    static Objective.BlockTarget killTarget(String mobId) {
        if (mobId == null) return null;
        return switch (mobId) {
            case "AUTOMATON_MONSTER", "AUTOMATON_MONSTER_71" -> Objective.BlockTarget.HOLLOWS_AUTOMATON;
            case "SLUDGE_5", "SLUDGE_10", "SLUDGE_100" -> Objective.BlockTarget.HOLLOWS_SLUDGE;
            case "TEAM_TREASURITE_GRUNT", "TEAM_TREASURITE_VIPER", "TEAM_TREASURITE_SEBASTIAN", "TEAM_TREASURITE_WENDY" -> Objective.BlockTarget.HOLLOWS_TREASURITE_MEMBER;
            case "GOBLIN_HOLDOUT" -> Objective.BlockTarget.HOLLOWS_GOBLIN;
            case "YOG" -> Objective.BlockTarget.HOLLOWS_YOG;
            case "THYST" -> Objective.BlockTarget.HOLLOWS_THYST;
            case "BOSS_CORLEONE" -> Objective.BlockTarget.HOLLOWS_BOSS_CORLEONE;
            default -> null;
        };
    }

    private static void onComplete(SkyBlockPlayer player, Commission commission) {
        player.sendMessage(commission.nameColourTag() + "<l>{} </l><e>Commission Complete! Visit {} to claim your rewards!", commission.name,
                commission.zone.claimGiver());
    }
}
