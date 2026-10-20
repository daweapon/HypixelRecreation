package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class HotfService {
    public static final long WHISPER_CAP = 2_000_000_000L;
    public static final int UNLOCK_ORDER_SPARE_TOKENS = 20;
    private static final long REAL_DAY_MILLIS = 24L * 60L * 60L * 1000L;
    private static final long SKYBLOCK_DAY_MILLIS = 20L * 60L * 1000L;

    private HotfService() {
    }

    public static DatapointHOTF datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HOTF, DatapointHOTF.class);
    }

    public static DatapointHOTF.PlayerHOTFData data(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static void save(SkyBlockPlayer player, DatapointHOTF.PlayerHOTFData data) {
        datapoint(player).setValue(data);
    }

    private static DatapointLong experienceDatapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HOTF_EXPERIENCE, DatapointLong.class);
    }

    public static long experience(SkyBlockPlayer player) {
        return experienceDatapoint(player).getValue();
    }

    public static int tier(SkyBlockPlayer player) {
        return HotfRegistry.get().tierOf(experience(player));
    }

    public static boolean isUnlocked(SkyBlockPlayer player) {
        return data(player).isUnlocked();
    }

    public static boolean unlock(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = data(player);
        if (data.isUnlocked()) return false;
        data.setUnlocked(true);
        save(player, data);
        syncSkyBlockXp(player);
        return true;
    }

    public static boolean onForagingIsland() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_GALATEA;
    }

    public static int tokensEarned(SkyBlockPlayer player) {
        HotfRegistry registry = HotfRegistry.get();
        int tier = tier(player);
        int tokens = data(player).getBonusTokens();
        for (int current = 1; current <= tier; current++) tokens += registry.tierReward(current).tokens();
        for (int level = 1; level <= data(player).getCoreLevel(); level++) tokens += registry.coreLevel(level).tokens();
        return tokens;
    }

    public static int availableTokens(SkyBlockPlayer player) {
        return tokensEarned(player) - data(player).getTokensSpent();
    }

    public static long whispers(SkyBlockPlayer player, HotfCurrency currency) {
        return player.getSkyblockDataHandler().get(currency.getDatapoint(), DatapointLong.class).getValue();
    }

    public static void setWhispers(SkyBlockPlayer player, HotfCurrency currency, long amount) {
        player.getSkyblockDataHandler().get(currency.getDatapoint(), DatapointLong.class)
                .setValue(Math.min(WHISPER_CAP, Math.max(0, amount)));
    }

    public static HotfCurrency effective(HotfCurrency currency) {
        return currency == HotfCurrency.DESERT && !HotfRegistry.get().desertWhispersEnabled() ? HotfCurrency.FOREST : currency;
    }

    public static void addWhispers(SkyBlockPlayer player, HotfCurrency currency, long amount) {
        if (amount <= 0) return;
        HotfCurrency target = effective(currency);
        setWhispers(player, target, whispers(player, target) + amount);
    }

    public static int perkLevel(SkyBlockPlayer player, String perkId) {
        return data(player).perkLevel(perkId);
    }

    public static boolean hasPerk(SkyBlockPlayer player, String perkId) {
        return data(player).isPerkActive(perkId);
    }

    public static boolean togglePerk(SkyBlockPlayer player, String perkId) {
        DatapointHOTF.PlayerHOTFData data = data(player);
        if (data.perkLevel(perkId) <= 0) return false;
        if (!data.getDisabledPerks().remove(perkId)) data.getDisabledPerks().add(perkId);
        save(player, data);
        return true;
    }

    public static double perkValue(SkyBlockPlayer player, String perkId) {
        return perkValue(player, perkId, "main");
    }

    public static double perkValue(SkyBlockPlayer player, String perkId, String key) {
        HotfPerk perk = HotfRegistry.get().perk(perkId);
        if (perk == null || !perk.available()) return 0;
        return perk.value(key, data(player).activeLevel(perkId));
    }

    public static int abilityLevel(SkyBlockPlayer player) {
        return Math.min(HotfAbility.MAX_LEVEL, 1 + (data(player).getCoreLevel() >= 2 ? 1 : 0));
    }

    public static double experienceMultiplier(SkyBlockPlayer player) {
        return 1 + AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("U68")) / 100D;
    }

    public static double whisperMultiplier(SkyBlockPlayer player) {
        return 1 + AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("R88")) / 100D;
    }

    public static void addExperience(SkyBlockPlayer player, long amount) {
        if (amount <= 0) return;
        HotfRegistry registry = HotfRegistry.get();
        long before = experience(player);
        int tierBefore = registry.tierOf(before);
        long cap = registry.maxExperience();
        long after = Math.min(cap, before + amount);
        long excess = before + amount - after;
        experienceDatapoint(player).setValue(after);
        if (excess > 0) addWhispers(player, HotfCurrency.FOREST, excess);
        int tierAfter = registry.tierOf(after);
        if (tierAfter > tierBefore) {
            player.sendMessage("<2><l>HEART OF THE FOREST <r><7>Your Heart of the Forest reached <2>Tier {}<7>!", tierAfter);
        }
        syncSkyBlockXp(player);
    }

    public static void syncSkyBlockXp(SkyBlockPlayer player) {
        HotfRegistry registry = HotfRegistry.get();
        DatapointHOTF.PlayerHOTFData data = data(player);
        if (!data.isUnlocked()) {
            player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.HEART_OF_THE_FOREST, 0);
            player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.CENTER_OF_THE_FOREST, 0);
            return;
        }
        int tier = tier(player);
        double tierXp = 0;
        for (int current = 1; current <= tier; current++) tierXp += registry.tierReward(current).skyBlockXp();
        double whisperXp = WhisperExperience.experience(whispers(player, HotfCurrency.FOREST));
        if (registry.desertWhispersEnabled()) whisperXp += WhisperExperience.experience(whispers(player, HotfCurrency.DESERT));
        double coreXp = 0;
        for (int level = 1; level <= data.getCoreLevel(); level++) coreXp += registry.coreLevel(level).skyBlockXp();
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.HEART_OF_THE_FOREST, tierXp + whisperXp);
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.CENTER_OF_THE_FOREST, coreXp);
    }

    public record Purchase(int levels, String error) {
    }

    public static Purchase buyPerkLevels(SkyBlockPlayer player, String perkId, int count) {
        HotfPerk perk = HotfRegistry.get().perk(perkId);
        if (perk == null) return new Purchase(0, "<c>That perk does not exist.");
        if (!isUnlocked(player)) return new Purchase(0, "<c>Talk to Hina in Tangleburg's Path to unlock your Heart of the Forest.");
        DatapointHOTF.PlayerHOTFData data = data(player);
        int bought = 0;
        String error = null;
        for (int i = 0; i < count; i++) {
            error = purchaseOne(player, data, perk);
            if (error != null) break;
            bought++;
        }
        if (bought > 0) {
            if (perk.isAbility() && data.getSelectedAbility() == null) data.setSelectedAbility(perk.id().toUpperCase());
            save(player, data);
            syncSkyBlockXp(player);
        }
        return new Purchase(bought, bought > 0 && count > 1 ? null : error);
    }

    private static String purchaseOne(SkyBlockPlayer player, DatapointHOTF.PlayerHOTFData data, HotfPerk perk) {
        if (!perk.available()) return "<c>This perk is not available yet: " + perk.unavailable() + ".";
        if (perk.tier() > tier(player)) return "<c>You need Heart of the Forest Tier " + perk.tier() + " to unlock this perk.";
        int level = data.perkLevel(perk.id());
        if (level >= perk.maxLevel()) return "<c>This perk is already at its maximum level.";
        if (level == 0 && perk.exclusive() != null && data.perkLevel(perk.exclusive()) > 0) {
            HotfPerk other = HotfRegistry.get().perk(perk.exclusive());
            return "<c>This perk conflicts with " + (other == null ? perk.exclusive() : other.name()) + ".";
        }
        if (level == 0 && availableTokens(player) < perk.tokenCost()) return "<c>You don't have enough Token of the Forest!";
        long cost = perk.costToReach(level + 1);
        if (perk.usesWhispers() && whispers(player, perk.currency()) < cost) {
            return "<c>You need " + String.format("%,d", cost) + " " + perk.currency().getDisplayName() + " to upgrade this perk.";
        }
        if (level == 0) data.setTokensSpent(data.getTokensSpent() + perk.tokenCost());
        if (perk.usesWhispers()) setWhispers(player, perk.currency(), whispers(player, perk.currency()) - cost);
        data.getPerkLevels().put(perk.id(), level + 1);
        return null;
    }

    public static String upgradeCore(SkyBlockPlayer player) {
        if (!isUnlocked(player)) return "<c>Talk to Hina in Tangleburg's Path to unlock your Heart of the Forest.";
        DatapointHOTF.PlayerHOTFData data = data(player);
        HotfCoreLevel next = HotfRegistry.get().coreLevel(data.getCoreLevel() + 1);
        if (next == null) return "<c>Your Center of the Forest is already maxed.";
        if (whispers(player, next.currency()) < next.cost()) {
            return "<c>You need " + String.format("%,d", next.cost()) + " " + next.currency().getDisplayName() + " to upgrade the Center of the Forest.";
        }
        setWhispers(player, next.currency(), whispers(player, next.currency()) - next.cost());
        data.setCoreLevel(next.level());
        save(player, data);
        syncSkyBlockXp(player);
        return null;
    }

    public static void addTokens(SkyBlockPlayer player, int amount) {
        DatapointHOTF.PlayerHOTFData data = data(player);
        data.setBonusTokens(data.getBonusTokens() + amount);
        save(player, data);
    }

    public static void maxCore(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = data(player);
        data.setCoreLevel(HotfRegistry.get().maxCoreLevel());
        save(player, data);
        syncSkyBlockXp(player);
    }

    public static void reset(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = data(player);
        HotfRegistry registry = HotfRegistry.get();
        for (var entry : data.getPerkLevels().entrySet()) {
            HotfPerk perk = registry.perk(entry.getKey());
            if (perk != null && perk.usesWhispers()) addWhispers(player, perk.currency(), perk.totalCost(entry.getValue()));
        }
        data.getPerkLevels().clear();
        data.getDisabledPerks().clear();
        data.setSelectedAbility(null);
        data.setTokensSpent(0);
        save(player, data);
        syncSkyBlockXp(player);
    }

    public static void maxOut(SkyBlockPlayer player) {
        HotfRegistry registry = HotfRegistry.get();
        DatapointHOTF.PlayerHOTFData data = data(player);
        data.setUnlocked(true);
        experienceDatapoint(player).setValue(registry.maxExperience());
        data.setCoreLevel(registry.maxCoreLevel());
        int tokensSpent = 0;
        data.getPerkLevels().clear();
        data.getDisabledPerks().clear();
        for (HotfPerk perk : registry.perks().values()) {
            if (!perk.available()) continue;
            if (perk.exclusive() != null && data.perkLevel(perk.exclusive()) > 0) continue;
            data.getPerkLevels().put(perk.id(), perk.maxLevel());
            tokensSpent += perk.tokenCost();
        }
        if (data.getSelectedAbility() == null) data.setSelectedAbility(HotfAbility.DAMAGE_BOOST.name());
        data.setTokensSpent(tokensSpent);
        data.setBonusTokens(0);
        int earned = tokensEarned(player);
        data.setBonusTokens(Math.max(0, tokensSpent - earned) + UNLOCK_ORDER_SPARE_TOKENS);
        setWhispers(player, HotfCurrency.FOREST, 100_000_000L);
        setWhispers(player, HotfCurrency.DESERT, 100_000_000L);
        save(player, data);
        syncSkyBlockXp(player);
    }

    public static long dailyDay() {
        return System.currentTimeMillis() / REAL_DAY_MILLIS;
    }

    public static long skyBlockDay() {
        return System.currentTimeMillis() / SKYBLOCK_DAY_MILLIS;
    }

    public static long millisUntilDailyReset() {
        return REAL_DAY_MILLIS - System.currentTimeMillis() % REAL_DAY_MILLIS;
    }
}
