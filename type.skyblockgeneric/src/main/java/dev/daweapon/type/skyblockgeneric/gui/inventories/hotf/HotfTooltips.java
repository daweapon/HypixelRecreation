package dev.daweapon.type.skyblockgeneric.gui.inventories.hotf;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.gui.inventories.hotm.HotmTooltips;
import dev.daweapon.type.skyblockgeneric.hotf.HotfAbilities;
import dev.daweapon.type.skyblockgeneric.hotf.HotfAbility;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCoreLevel;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency;
import dev.daweapon.type.skyblockgeneric.hotf.HotfLottery;
import dev.daweapon.type.skyblockgeneric.hotf.HotfPerk;
import dev.daweapon.type.skyblockgeneric.hotf.HotfRegistry;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hotf.HotfTierReward;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HotfTooltips {
    private static final String TOKEN = "<2>Token of the Forest";
    private static final String AXE_NOTE = "<7>Axe Abilities apply to all of your Axes. You can select an Axe Ability from your Heart of the Forest.\n\n<7>Upgrade your Axe Abilities by unlocking <c>Center of the Forest<7> in this menu!";

    private HotfTooltips() {
    }

    public static String perk(SkyBlockPlayer player, HotfPerk perk) {
        return HotmTooltips.wrap(buildPerk(player, perk));
    }

    private static String buildPerk(SkyBlockPlayer player, HotfPerk perk) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        int level = data.perkLevel(perk.id());
        boolean single = perk.maxLevel() == 1;
        boolean maxed = level >= perk.maxLevel();
        boolean disabled = level > 0 && data.getDisabledPerks().contains(perk.id());
        boolean affordable = perk.available() && perk.tier() <= HotfService.tier(player) && HotfService.availableTokens(player) >= perk.tokenCost();
        String title = (level > 0 ? maxed ? "<a>" : "<e>" : affordable ? "<e>" : "<c>") + perk.name();
        StringBuilder text = new StringBuilder(title);
        if (perk.isAbility()) return ability(player, perk, text, level > 0);

        if (!single) text.append("\n<7>Level ").append(maxed ? String.valueOf(level) : Math.max(level, 1) + "/" + perk.maxLevel());
        text.append("\n\n").append(lore(player, perk, Math.max(level, 1)));
        if (!perk.available()) {
            text.append("\n\n<c>Not available yet: ").append(perk.unavailable()).append(".");
            return text.toString();
        }
        if (level > 0 && !maxed) {
            text.append("\n\n<a><l>=====[ UPGRADE ]=====\n<7>Level ").append(level + 1).append("/").append(perk.maxLevel())
                    .append("\n\n").append(lore(player, perk, level + 1));
            if (perk.usesWhispers()) text.append("\n\n<7>Cost\n").append(cost(perk, perk.costToReach(level + 1)));
        }
        if (level == 0) {
            text.append("\n\n<7>Cost\n<f>").append(perk.tokenCost()).append(" Token of the Forest");
            String status = unlockStatus(player, perk);
            if (status != null) text.append("\n\n").append(status);
            else text.append("\n\n<e>Click to unlock!");
        } else {
            text.append("\n\n").append(disabled ? "<c><l>DISABLED" : "<a><l>ENABLED");
            text.append("\n\n<e>Right-click to ").append(disabled ? "<a>enable<e>!" : "<c>disable<e>!");
            if (!maxed) {
                text.append("\n<e>Left-click to upgrade!\n<e>Shift Left-click to upgrade 10 levels!");
                if (perk.usesWhispers() && HotfService.whispers(player, perk.currency()) < perk.costToReach(level + 1)) {
                    text.append("\n<c>You don't have enough ").append(perk.currency().getDisplayName()).append("!");
                }
            }
        }
        return text.toString();
    }

    private static String cost(HotfPerk perk, long amount) {
        return perk.currency().getColour() + String.format("%,d", amount) + " " + perk.currency().getDisplayName();
    }

    private static String ability(SkyBlockPlayer player, HotfPerk perk, StringBuilder text, boolean unlocked) {
        if (!perk.available()) {
            return text.append("\n<6>Axe Ability: ").append(perk.name()).append("\n\n<c>Not available yet: ").append(perk.unavailable()).append(".").toString();
        }
        HotfAbility ability = HotfAbility.valueOf(perk.id().toUpperCase(Locale.ROOT));
        int level = HotfService.abilityLevel(player);
        String description = ability.getDescription().replace("{duration}", String.valueOf(ability.duration(level)));
        text.append("\n<6>Axe Ability: ").append(ability.getDisplayName()).append("\n<7>").append(description);
        text.append("\n<8>Cooldown: <a>").append(ability.cooldown(level)).append("s");
        text.append("\n\n").append(AXE_NOTE);
        if (!unlocked) {
            text.append("\n\n<7>Cost\n<f>").append(perk.tokenCost()).append(" Token of the Forest");
            String status = unlockStatus(player, perk);
            text.append("\n\n").append(status == null ? "<e>Click to unlock!" : status);
        } else if (HotfAbilities.selected(player) == ability) {
            text.append("\n\n<a><l>SELECTED");
        } else {
            text.append("\n\n<e>Click to select!");
        }
        return text.toString();
    }

    private static String unlockStatus(SkyBlockPlayer player, HotfPerk perk) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        if (perk.tier() > HotfService.tier(player)) return "<c>Requires Heart of the Forest Tier " + perk.tier();
        if (perk.exclusive() != null && data.perkLevel(perk.exclusive()) > 0) {
            HotfPerk other = HotfRegistry.get().perk(perk.exclusive());
            return "<c>Conflicts with " + (other == null ? perk.exclusive() : other.name());
        }
        if (HotfService.availableTokens(player) < perk.tokenCost()) return "<c>You don't have enough " + TOKEN.replace("<2>", "") + "!";
        return null;
    }

    private static String lore(SkyBlockPlayer player, HotfPerk perk, int level) {
        List<String> lines = new ArrayList<>();
        for (String line : perk.lore()) {
            if (line.equals("{lottery_effect}")) {
                HotfLottery effect = HotfLottery.current(player);
                if (effect != null) lines.add("<a>Your Current Effect\n<8>■ <7>Gain " + effect.getDescription() + "<7>.");
                continue;
            }
            lines.add(line.startsWith("<") ? line : "<7>" + line);
        }
        return String.join("\n", lines)
                .replace("{value2}", HotmTooltips.format(perk.value("second", level)))
                .replace("{value}", HotmTooltips.format(perk.value(level)));
    }

    public static String tier(SkyBlockPlayer player, int tier) {
        return HotmTooltips.wrap(buildTier(player, tier));
    }

    private static String buildTier(SkyBlockPlayer player, int tier) {
        HotfTierReward reward = HotfRegistry.get().tierReward(tier);
        long experience = HotfService.experience(player);
        boolean reached = HotfService.tier(player) >= tier;
        StringBuilder text = new StringBuilder((reached ? "<a>" : "<c>") + "Tier " + tier + "\n");
        text.append(reached
                ? "<7>You have unlocked this tier. All perks and abilities on this tier are available for unlocking with " + TOKEN + "<7>."
                : "<7>Reach this tier to make its perks and abilities available for unlocking with " + TOKEN + "<7>.");
        text.append("\n\n<7>Rewards\n<2>+").append(reward.tokens()).append(" Token of the Forest");
        for (String line : reward.rewards()) text.append("\n").append(line);
        text.append("\n<b>+").append(reward.skyBlockXp()).append(" SkyBlock XP\n\n");
        if (reached) {
            text.append("<a><l>UNLOCKED");
        } else if (HotfService.tier(player) + 1 == tier) {
            HotfTierReward previous = HotfRegistry.get().tierReward(tier - 1);
            long span = reward.experience() - previous.experience();
            int percent = (int) Math.min(100, (experience - previous.experience()) * 100 / span);
            text.append("<7>Progress: <e>").append(percent).append("%\n<7>")
                    .append(String.format("%,d", experience)).append("<8>/<7>").append(String.format("%,d", reward.experience())).append(" HotF Exp");
        } else {
            text.append("<c><l>LOCKED");
        }
        return text.toString();
    }

    public static String core(SkyBlockPlayer player) {
        return HotmTooltips.wrap(buildCore(player));
    }

    private static String buildCore(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        HotfRegistry registry = HotfRegistry.get();
        int owned = data.getCoreLevel();
        StringBuilder text = new StringBuilder("<a>Center of the Forest\n<7>Level " + (owned >= registry.maxCoreLevel() ? owned : owned + "/" + registry.maxCoreLevel()) + "\n\n");
        int tokens = 0;
        int xp = 0;
        for (HotfCoreLevel level : registry.coreLevels()) {
            if (level.level() > owned) break;
            tokens += level.tokens();
            xp += level.skyBlockXp();
            if (level.tokens() == 0) text.append(level.reward()).append("\n");
        }
        if (tokens > 0) text.append("<2>+").append(tokens).append(" Token of the Forest\n");
        if (xp > 0) text.append("<b>+").append(String.format("%,d", xp)).append(" SkyBlock XP\n");
        HotfCoreLevel next = registry.coreLevel(owned + 1);
        if (next == null) return text.append("\n<a><l>UNLOCKED").toString();
        text.append("\n<a><l>=====[ UPGRADE ]=====\n<7>Level ").append(next.level()).append("/").append(registry.maxCoreLevel()).append("\n\n");
        text.append(next.tokens() > 0 ? "<2>+" + next.tokens() + " Token of the Forest" : next.reward());
        text.append("\n<b>+").append(next.skyBlockXp()).append(" SkyBlock XP\n\n<7>Cost\n");
        text.append(next.cost() == 0 ? "<a>Free" : next.currency().getColour() + String.format("%,d", next.cost()) + " " + next.currency().getDisplayName());
        text.append("\n\n<c>Whispers spent here are not refunded by a reset.");
        text.append("\n\n<e>Click to upgrade!");
        return text.toString();
    }

    public static String heart(SkyBlockPlayer player) {
        return HotmTooltips.wrap(buildHeart(player));
    }

    private static String buildHeart(SkyBlockPlayer player) {
        StringBuilder text = new StringBuilder("<a>Heart of the Forest\n<7>" + TOKEN + "s: <2>" + HotfService.availableTokens(player));
        text.append("\n\n<7>Gain " + TOKEN + "s<7> by leveling up your Heart of the Forest tiers.");
        text.append("\n\n<a>Whispers\n<7>Whispers are dropped from cutting Logs on Galatea, and are used to upgrade the perks you've unlocked!");
        text.append("\n\n<7>Forest Whispers: <3>").append(String.format("%,d", HotfService.whispers(player, HotfCurrency.FOREST)));
        if (HotfRegistry.get().desertWhispersEnabled()) {
            text.append("\n<7>Desert Whispers: <6>").append(String.format("%,d", HotfService.whispers(player, HotfCurrency.DESERT)));
        }
        return text.toString();
    }

    public static String reset(SkyBlockPlayer player) {
        return HotmTooltips.wrap(buildReset(player));
    }

    private static String buildReset(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        HotfRegistry registry = HotfRegistry.get();
        long[] refund = new long[HotfCurrency.values().length];
        data.getPerkLevels().forEach((id, level) -> {
            HotfPerk perk = registry.perk(id);
            if (perk != null && perk.usesWhispers()) refund[perk.currency().ordinal()] += perk.totalCost(level);
        });
        StringBuilder text = new StringBuilder("<c>Reset Heart of the Forest\n<7>Resets the Perks and Abilities of your <2>Heart of the Forest<7>, locking them and resetting their levels.\n\n<7>You will be reimbursed with:");
        text.append("\n<8>– <2>").append(data.getTokensSpent()).append(" Token of the Forest");
        for (HotfCurrency currency : HotfCurrency.values()) {
            if (currency == HotfCurrency.DESERT && !registry.desertWhispersEnabled()) continue;
            text.append("\n<8>– ").append(currency.getColour()).append(String.format("%,d", refund[currency.ordinal()])).append(" ").append(currency.getDisplayName());
        }
        text.append("\n\n<7>You will <a>keep<7> any Tiers and <c>Center of the Forest<7> levels that you have unlocked.");
        text.append("\n\n<c><l>WARNING: This is permanent. You can not go back after resetting your Heart of the Forest!");
        return text.toString();
    }
}
