package dev.daweapon.type.skyblockgeneric.communityupgrades;

import dev.daweapon.commons.data.SharedData;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointStorage;
import org.json.JSONTokener;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class CommunityUpgrades {
    public static final int GEMS_PER_HOUR = 6;
    private static final long HOUR_MILLIS = 3_600_000L;

    public enum Result {
        STARTED, FINISHED, SKIPPED, MAXED, BUSY, NO_GEMS, NOT_READY, UNAVAILABLE
    }

    private CommunityUpgrades() {
    }

    public static CommunityUpgradeData data(SkyBlockPlayer player, CommunityUpgradeId.Scope scope) {
        return CommunityUpgradeData.fromJson(datapoint(player, scope).getValue());
    }

    public static int tier(SkyBlockPlayer player, CommunityUpgradeId id) {
        return data(player, id.getScope()).tier(id);
    }

    public static double value(SkyBlockPlayer player, CommunityUpgradeId id) {
        return valueAt(id, tier(player, id));
    }

    public static double extra(SkyBlockPlayer player, CommunityUpgradeId id) {
        int tier = tier(player, id);
        return tier <= 0 ? 0 : CommunityUpgradeCatalog.tiers(id).get(tier - 1).extra();
    }

    public static int profileIntValue(java.util.UUID profileId, CommunityUpgradeId id) {
        String raw = SharedData.profile().get(profileId, SkyBlockDataHandler.Data.COMMUNITY_UPGRADES_PROFILE.coopField());
        if (raw == null || raw.isBlank()) return 0;
        Object parsed = new JSONTokener(raw).nextValue();
        String json = parsed instanceof String text ? text : raw;
        return (int) valueAt(id, CommunityUpgradeData.fromJson(json).tier(id));
    }

    public static int intValue(SkyBlockPlayer player, CommunityUpgradeId id) {
        return (int) value(player, id);
    }

    public static double valueAt(CommunityUpgradeId id, int tier) {
        return tier <= 0 ? 0 : CommunityUpgradeCatalog.tiers(id).get(Math.min(tier, CommunityUpgradeCatalog.maxTier(id)) - 1).value();
    }

    public static CommunityUpgradeCatalog.Tier nextTier(SkyBlockPlayer player, CommunityUpgradeId id) {
        int tier = tier(player, id);
        return tier >= CommunityUpgradeCatalog.maxTier(id) ? null : CommunityUpgradeCatalog.tiers(id).get(tier);
    }

    public static CommunityUpgradeData.Active active(SkyBlockPlayer player, CommunityUpgradeId.Scope scope) {
        return data(player, scope).active();
    }

    public static Result start(SkyBlockPlayer player, CommunityUpgradeId id) {
        if (!id.isImplemented()) return Result.UNAVAILABLE;
        CommunityUpgradeData data = data(player, id.getScope());
        int tier = data.tier(id);
        if (tier >= CommunityUpgradeCatalog.maxTier(id)) return Result.MAXED;
        if (data.active() != null) return Result.BUSY;
        CommunityUpgradeCatalog.Tier next = CommunityUpgradeCatalog.tiers(id).get(tier);
        if (next.gems() > 0) {
            if (player.getGems() < next.gems()) return Result.NO_GEMS;
            player.removeGems(next.gems());
        }
        long now = System.currentTimeMillis();
        if (next.hours() <= 0) {
            data.setTier(id, tier + 1);
            save(player, id.getScope(), data);
            return Result.FINISHED;
        }
        data.setActive(new CommunityUpgradeData.Active(id, tier + 1, now, now + next.hours() * HOUR_MILLIS));
        save(player, id.getScope(), data);
        return Result.STARTED;
    }

    public static int skipCost(CommunityUpgradeData.Active active) {
        long remaining = Math.max(0, active.completesAt() - System.currentTimeMillis());
        return (int) Math.ceil(remaining / (double) HOUR_MILLIS * GEMS_PER_HOUR);
    }

    public static Result skip(SkyBlockPlayer player, CommunityUpgradeId.Scope scope) {
        CommunityUpgradeData data = data(player, scope);
        CommunityUpgradeData.Active active = data.active();
        if (active == null) return Result.NOT_READY;
        int cost = skipCost(active);
        if (player.getGems() < cost) return Result.NO_GEMS;
        player.removeGems(cost);
        complete(player, scope, data, active);
        return Result.SKIPPED;
    }

    public static void finishDue(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return;
        syncEnderChestPages(player);
        for (CommunityUpgradeId.Scope scope : CommunityUpgradeId.Scope.values()) {
            CommunityUpgradeData data = data(player, scope);
            CommunityUpgradeData.Active active = data.active();
            if (active != null && active.completesAt() <= System.currentTimeMillis()) {
                complete(player, scope, data, active);
            }
        }
    }

    private static void syncEnderChestPages(SkyBlockPlayer player) {
        int extra = intValue(player, CommunityUpgradeId.ENDER_CHEST_PAGES);
        var datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.STORAGE, DatapointStorage.class);
        DatapointStorage.PlayerStorage storage = datapoint.getValue();
        boolean changed = false;
        if (!storage.hasPage(1)) {
            storage.addPage(1);
            storage.addPage(2);
            changed = true;
        }
        for (int page = 3; page <= Math.min(9, 2 + extra); page++) {
            if (!storage.hasPage(page)) {
                storage.addPage(page);
                changed = true;
            }
        }
        if (changed) datapoint.setValue(storage);
    }

    public static void maxAll(SkyBlockPlayer player) {
        for (CommunityUpgradeId.Scope scope : CommunityUpgradeId.Scope.values()) {
            CommunityUpgradeData data = data(player, scope);
            for (CommunityUpgradeId id : CommunityUpgradeId.values()) {
                if (id.getScope() == scope) data.setTier(id, CommunityUpgradeCatalog.maxTier(id));
            }
            data.setActive(null);
            save(player, scope, data);
        }
    }

    private static void complete(SkyBlockPlayer player, CommunityUpgradeId.Scope scope, CommunityUpgradeData data,
                                 CommunityUpgradeData.Active active) {
        data.setTier(active.id(), Math.max(data.tier(active.id()), active.targetTier()));
        data.setActive(null);
        save(player, scope, data);
        CommunityUpgradeNotifier.finished(player, active.id(), active.targetTier());
    }

    private static void save(SkyBlockPlayer player, CommunityUpgradeId.Scope scope, CommunityUpgradeData data) {
        datapoint(player, scope).setValue(data.toJson());
    }

    private static DatapointString datapoint(SkyBlockPlayer player, CommunityUpgradeId.Scope scope) {
        return scope == CommunityUpgradeId.Scope.ACCOUNT
                ? player.getDataHandler().get(HypixelDataHandler.Data.COMMUNITY_UPGRADES, DatapointString.class)
                : player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COMMUNITY_UPGRADES_PROFILE, DatapointString.class);
    }
}
