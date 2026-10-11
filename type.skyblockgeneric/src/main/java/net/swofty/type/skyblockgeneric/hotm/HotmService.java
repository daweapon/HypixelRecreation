package net.swofty.type.skyblockgeneric.hotm;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.accessory.AccessoryEconomyHooks;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetBonuses;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public final class HotmService {
    private static final long SKYBLOCK_DAY_MILLIS = 20L * 60L * 1000L;
    private static final long REAL_DAY_MILLIS = 24L * 60L * 60L * 1000L;
    public static final int UNLOCK_ORDER_SPARE_TOKENS = 5;
    public static final int DAILY_POWDER_PER_TIER = 500;

    private HotmService() {
    }

    public static DatapointHOTM datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class);
    }

    public static DatapointHOTM.PlayerHOTMData data(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static void save(SkyBlockPlayer player, DatapointHOTM.PlayerHOTMData data) {
        datapoint(player).setValue(data);
    }

    public static int perkLevel(SkyBlockPlayer player, String perkId) {
        return data(player).perkLevel(perkId);
    }

    public static boolean hasPerk(SkyBlockPlayer player, String perkId) {
        return perkLevel(player, perkId) > 0;
    }

    public static double perkValue(SkyBlockPlayer player, String perkId) {
        HotmPerk perk = HotmRegistry.get().perk(perkId);
        return perk == null ? 0 : perk.value(perkLevel(player, perkId));
    }

    public static boolean onMiningIsland() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_DWARVEN_MINES;
    }

    public static int forgeSlots(SkyBlockPlayer player) {
        return HotmRegistry.get().tierReward(data(player).getTier()).forgeSlots();
    }

    public static int bonusCommissionSlots(SkyBlockPlayer player) {
        return data(player).getCoreLevel() >= 3 ? 1 : 0;
    }

    public static int abilityLevel(SkyBlockPlayer player) {
        return Math.min(HotmAbility.MAX_LEVEL, 1 + (data(player).getCoreLevel() >= 2 ? 1 : 0));
    }

    public static void addExperience(SkyBlockPlayer player, long amount) {
        DatapointHOTM.PlayerHOTMData data = data(player);
        int ups = data.addExperience(amount);
        save(player, data);
        if (ups > 0) {
            player.sendMessage("<5><b>HEART OF THE MOUNTAIN <r><7>Your Heart of the Mountain reached <5>Tier {}<7>!", data.getTier());
        }
        syncSkyBlockXp(player);
    }

    public static void syncSkyBlockXp(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = data(player);
        HotmRegistry registry = HotmRegistry.get();
        double total = 0;
        for (int tier = 1; tier <= data.getTier(); tier++) total += registry.tierReward(tier).skyBlockXp();
        for (int level = 1; level <= data.getCoreLevel(); level++) total += registry.coreLevel(level).skyBlockXp();
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.HEART_OF_THE_MOUNTAIN, total);
    }

    public static String buyPerk(SkyBlockPlayer player, String perkId) {
        HotmPerk perk = HotmRegistry.get().perk(perkId);
        if (perk == null) return "<c>That perk does not exist.";
        DatapointHOTM.PlayerHOTMData data = data(player);
        if (perk.tier() > data.getTier()) return "<c>You need Heart of the Mountain Tier " + perk.tier() + " to unlock this perk.";
        int level = data.perkLevel(perkId);
        if (level >= perk.maxLevel()) return "<c>This perk is already at its maximum level.";
        int next = level + 1;
        if (level == 0 && data.getAvailableTokens() < perk.tokenCost()) return "<c>You need " + perk.tokenCost() + " Token of the Mountain to unlock this perk.";
        long cost = perk.costToReach(next);
        if (perk.usesPowder() && data.powder(perk.powder()) < cost) {
            return "<c>You need " + String.format("%,d", cost) + " " + perk.powder().getDisplayName() + " to upgrade this perk.";
        }
        if (level == 0) data.setTokensSpent(data.getTokensSpent() + perk.tokenCost());
        if (perk.usesPowder()) data.addPowder(perk.powder(), -cost);
        data.getPerkLevels().put(perkId, next);
        save(player, data);
        return null;
    }

    public static String upgradeCore(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = data(player);
        HotmCoreLevel next = HotmRegistry.get().coreLevel(data.getCoreLevel() + 1);
        if (next == null) return "<c>Your Core of the Mountain is already maxed.";
        if (data.powder(next.powder()) < next.cost()) {
            return "<c>You need " + String.format("%,d", next.cost()) + " " + next.powder().getDisplayName() + " to upgrade the Core of the Mountain.";
        }
        data.addPowder(next.powder(), -next.cost());
        data.setCoreLevel(next.level());
        data.setTokens(data.getTokens() + next.tokens());
        save(player, data);
        syncSkyBlockXp(player);
        return null;
    }

    public static void addTokens(SkyBlockPlayer player, int amount) {
        DatapointHOTM.PlayerHOTMData data = data(player);
        data.setTokens(data.getTokens() + amount);
        save(player, data);
    }

    public static void addPowderToAll(SkyBlockPlayer player, long amount) {
        DatapointHOTM.PlayerHOTMData data = data(player);
        for (HotmPowder powder : HotmPowder.values()) data.addPowder(powder, amount);
        save(player, data);
    }

    public static void maxCore(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = data(player);
        HotmRegistry registry = HotmRegistry.get();
        for (int level = data.getCoreLevel() + 1; level <= registry.maxCoreLevel(); level++) {
            data.setTokens(data.getTokens() + registry.coreLevel(level).tokens());
        }
        data.setCoreLevel(registry.maxCoreLevel());
        save(player, data);
        syncSkyBlockXp(player);
    }

    public static void reset(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = data(player);
        HotmRegistry registry = HotmRegistry.get();
        data.getPerkLevels().forEach((id, level) -> {
            HotmPerk perk = registry.perk(id);
            if (perk != null && perk.usesPowder()) data.addPowder(perk.powder(), perk.totalCost(level));
        });
        data.getPerkLevels().clear();
        data.setTokensSpent(0);
        save(player, data);
    }

    public static void maxOut(SkyBlockPlayer player) {
        HotmRegistry registry = HotmRegistry.get();
        DatapointHOTM.PlayerHOTMData data = data(player);
        data.setExperience(1_247_000L);
        data.setCoreLevel(registry.maxCoreLevel());
        int tokensSpent = 0;
        for (HotmPerk perk : registry.perks().values()) {
            data.getPerkLevels().put(perk.id(), perk.maxLevel());
            tokensSpent += perk.tokenCost();
        }
        int earned = Arrays.stream(new int[]{1, 2, 2, 2, 2, 2, 3, 2, 2, 2}).sum()
                + registry.coreLevels().stream().mapToInt(HotmCoreLevel::tokens).sum();
        data.setTokensSpent(tokensSpent);
        data.setTokens(Math.max(earned, tokensSpent) + UNLOCK_ORDER_SPARE_TOKENS);
        for (HotmPowder powder : HotmPowder.values()) data.setPowderBalance(powder, 100_000_000L);
        save(player, data);
        syncSkyBlockXp(player);
    }

    public static long gainPowder(SkyBlockPlayer player, HotmPowder powder, long base) {
        if (base <= 0) return 0;
        double bonusPercent = perkValue(player, "powder_buff");
        if (skyMallBuff(player) == HotmSkyMallBuff.POWDER) bonusPercent += 15;
        long amount = Math.round(base * (1 + bonusPercent / 100));
        if (powder == HotmPowder.MITHRIL) amount = GatheringPetBonuses.mithrilPowder(player, amount);
        amount = AccessoryEconomyHooks.powderGained(player, amount);
        DatapointHOTM.PlayerHOTMData data = data(player);
        data.addPowder(powder, amount);
        save(player, data);
        return amount;
    }

    public static long dailyBonusDay() {
        return System.currentTimeMillis() / REAL_DAY_MILLIS;
    }

    public static HotmSkyMallBuff skyMallBuff(SkyBlockPlayer player) {
        if (!hasPerk(player, "sky_mall") || !onMiningIsland()) return null;
        DatapointHOTM.PlayerHOTMData data = data(player);
        long today = System.currentTimeMillis() / SKYBLOCK_DAY_MILLIS;
        if (data.getSkyMallDay() != today || HotmSkyMallBuff.byId(data.getSkyMallBuff()) == null) {
            HotmSkyMallBuff rolled = LootRoll.pick(player.getUuid(), Arrays.asList(HotmSkyMallBuff.values()), buff -> null,
                    HotmSkyMallBuff::getWeight, ThreadLocalRandom.current());
            data.setSkyMallDay(today);
            data.setSkyMallBuff(rolled.name());
            save(player, data);
            player.sendMessage("<2><b>SKY MALL <r><7>New day! Your Sky Mall buff today: {}<7>.", rolled.getDescription());
            return rolled;
        }
        return HotmSkyMallBuff.byId(data.getSkyMallBuff());
    }
}
