package dev.daweapon.type.skyblockgeneric.gui.inventories.hotm;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.hotm.HotmAbilities;
import dev.daweapon.type.skyblockgeneric.hotm.HotmAbility;
import dev.daweapon.type.skyblockgeneric.hotm.HotmCoreLevel;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPerk;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmRegistry;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmSkyMallBuff;
import dev.daweapon.type.skyblockgeneric.hotm.HotmTierReward;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HotmTooltips {
    private static final int WRAP_WIDTH = 38;
    private static final java.util.regex.Pattern TAG = java.util.regex.Pattern.compile("<[^>]*>");
    private static final String TOKEN = "<5>Token of the Mountain";
    private static final String PICKAXE_NOTE = "<7>Pickaxe Abilities apply to all of your Pickaxes. You can select a Pickaxe Ability from your Heart of the Mountain.\n\n<7>Upgrade your Pickaxe Abilities by unlocking <c>Core of the Mountain<7> in this menu!";

    private HotmTooltips() {
    }

    public static String perk(SkyBlockPlayer player, HotmPerk perk) {
        return wrap(buildPerk(player, perk));
    }

    private static String buildPerk(SkyBlockPlayer player, HotmPerk perk) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        int level = data.perkLevel(perk.id());
        boolean single = perk.maxLevel() == 1;
        boolean maxed = level >= perk.maxLevel();
        boolean disabled = level > 0 && data.getDisabledPerks().contains(perk.id());
        boolean affordable = level > 0 || data.getAvailableTokens() >= perk.tokenCost();
        String title = (level > 0 ? maxed ? "<a>" : "<e>" : affordable && perk.tier() <= data.getTier() ? "<e>" : "<c>") + perk.name();
        StringBuilder text = new StringBuilder(title);
        if (perk.isAbility()) return ability(player, perk, text, level > 0);

        if (!single) text.append("\n<7>Level ").append(maxed ? String.valueOf(level) : Math.max(level, 1) + "/" + perk.maxLevel());
        text.append("\n\n").append(lore(player, perk, Math.max(level, 1)));
        if (level > 0 && !maxed) {
            text.append("\n\n<a><b>=====[ UPGRADE ]=====\n<7>Level ").append(level + 1).append("/").append(perk.maxLevel())
                    .append("\n\n").append(lore(player, perk, level + 1));
            if (perk.usesPowder()) {
                text.append("\n\n<7>Cost\n").append(perk.powder().getColour()).append(String.format("%,d", perk.costToReach(level + 1)))
                        .append(" ").append(perk.powder().getDisplayName());
            }
        }
        if (level == 0) {
            text.append("\n\n<7>Cost\n").append(TOKEN.replace("Token", perk.tokenCost() + " Token"));
            text.append("\n\n").append(unlockStatus(data, perk));
        } else {
            text.append("\n\n").append(disabled ? "<c><b>DISABLED" : "<a><b>ENABLED");
            text.append("\n\n<e>Right-click to ").append(disabled ? "<a>enable<e>!" : "<c>disable<e>!");
            if (!maxed) text.append("\n<e>Left-click to upgrade!\n<e>Shift Left-click to upgrade 10 levels!");
        }
        return text.toString();
    }

    private static String ability(SkyBlockPlayer player, HotmPerk perk, StringBuilder text, boolean unlocked) {
        HotmAbility ability = HotmAbility.valueOf(perk.id().toUpperCase(Locale.ROOT));
        int level = HotmService.abilityLevel(player);
        String description = ability.getDescription()
                .replace("{value}", String.valueOf(Math.round(ability.value(level))))
                .replace("{duration}", String.valueOf(ability.duration(level)));
        text.append("\n<6>Pickaxe Ability: ").append(ability.getDisplayName()).append("\n<7>").append(description);
        if (ability == HotmAbility.TUNNEL_VISION) {
            text.append("\n\n<7>Rare occurrences include:\n<8>■ <6>Golden Goblins\n<8>■ <5>Fallen Stars\n<8>■ <6>Powder Ghasts\n<8>■ <6>Worms\n<8>■ <b>Glacite Mineshafts");
        }
        text.append("\n<7>Cooldown: <a>").append(Math.round(HotmAbilities.cooldownSeconds(player, ability))).append("s");
        text.append("\n\n").append(PICKAXE_NOTE);
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        if (!unlocked) {
            text.append("\n\n<7>Cost\n").append(TOKEN.replace("Token", perk.tokenCost() + " Token"));
            text.append("\n\n").append(unlockStatus(data, perk));
        } else if (HotmAbilities.selected(player) == ability) {
            text.append("\n\n<a><b>SELECTED");
        } else {
            text.append("\n\n<e>Click to select!");
        }
        return text.toString();
    }

    private static String unlockStatus(DatapointHOTM.PlayerHOTMData data, HotmPerk perk) {
        if (perk.tier() > data.getTier()) return "<c>Requires Heart of the Mountain Tier " + perk.tier();
        if (perk.requires() != null && data.perkLevel(perk.requires()) <= 0) {
            HotmPerk required = HotmRegistry.get().perk(perk.requires());
            return "<c>Requires " + (required == null ? perk.requires() : required.name());
        }
        if (data.getAvailableTokens() < perk.tokenCost()) return "<c>You don't have enough " + TOKEN + "<c>!";
        return "<e>Click to unlock!";
    }

    private static String lore(SkyBlockPlayer player, HotmPerk perk, int level) {
        List<String> lines = new ArrayList<>();
        for (String line : perk.lore()) {
            lines.add(switch (line) {
                case "{daily_powder_lines}" -> dailyLines(player, false);
                case "{daily_grind_lines}" -> dailyLines(player, true);
                case "{resets}" -> "<c>Resets in: " + HotmService.formatDuration(HotmService.millisUntilDailyReset());
                default -> "<7>" + line;
            });
        }
        String joined = String.join("\n", lines)
                .replace("{value2}", format(perk.value("second", level)))
                .replace("{value}", format(perk.value(level)));
        if (perk.id().equals("sky_mall")) {
            HotmSkyMallBuff buff = HotmService.skyMallBuff(player);
            if (buff != null) joined += "\n\n<a>Your Current Effect\n<8>■ " + buff.getDescription();
        }
        return joined;
    }

    private static String dailyLines(SkyBlockPlayer player, boolean areas) {
        String amount = String.format("%,d", HotmService.dailyPowderAmount(player));
        String[] labels = areas ? new String[]{"Dwarven Mines", "Crystal Hollows", "Glacite Tunnels"} : new String[]{"Mithril", "Gemstone", "Glacite"};
        StringBuilder text = new StringBuilder();
        HotmPowder[] powders = HotmPowder.values();
        for (int i = 0; i < powders.length; i++) {
            if (i > 0) text.append("\n");
            text.append(powders[i].getColour()).append(labels[i]).append(": <a>+").append(amount).append(" ").append(powders[i].coloured());
        }
        return text.toString();
    }

    public static String tier(SkyBlockPlayer player, int tier) {
        return wrap(buildTier(player, tier));
    }

    private static String buildTier(SkyBlockPlayer player, int tier) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        HotmTierReward reward = HotmRegistry.get().tierReward(tier);
        boolean reached = data.getTier() >= tier;
        StringBuilder text = new StringBuilder((reached ? "<a>" : "<c>") + "Tier " + tier + "\n");
        text.append(reached
                ? "<7>You have unlocked this tier. All perks and abilities on this tier are available for unlocking with " + TOKEN + "<7>."
                : "<7>Reach this tier to make its perks and abilities available for unlocking with " + TOKEN + "<7>.");
        text.append("\n\n<7>Rewards\n<5>+").append(reward.tokens()).append(" Token of the Mountain");
        for (String line : reward.rewards()) text.append("\n").append(line);
        text.append("\n<b>+").append(reward.skyBlockXp()).append(" SkyBlock XP\n\n");
        if (reached) {
            text.append("<a><b>UNLOCKED");
        } else if (data.getTier() + 1 == tier) {
            text.append("<7>Progress: <e>").append(data.getProgressPercent()).append("%\n<7>")
                    .append(String.format("%,d", data.getExperience())).append("<8>/<7>").append(String.format("%,d", data.getNextTierXp())).append(" HotM Exp");
        } else {
            text.append("<c><b>LOCKED");
        }
        return text.toString();
    }

    public static String core(SkyBlockPlayer player) {
        return wrap(buildCore(player));
    }

    private static String buildCore(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        HotmRegistry registry = HotmRegistry.get();
        int owned = data.getCoreLevel();
        StringBuilder text = new StringBuilder("<a>Core of the Mountain\n<7>Level " + (owned >= registry.maxCoreLevel() ? owned : owned + "/" + registry.maxCoreLevel()) + "\n");
        int tokens = 0;
        int xp = 0;
        text.append("\n");
        for (HotmCoreLevel level : registry.coreLevels()) {
            if (level.level() > owned) break;
            tokens += level.tokens();
            xp += level.skyBlockXp();
            if (level.tokens() == 0) text.append(level.reward()).append("\n");
        }
        if (tokens > 0) text.append("<5>+").append(tokens).append(" Token of the Mountain\n");
        if (xp > 0) text.append("<b>+").append(String.format("%,d", xp)).append(" SkyBlock XP\n");
        HotmCoreLevel next = registry.coreLevel(owned + 1);
        if (next == null) return text.append("\n<a><b>UNLOCKED").toString();
        text.append("\n<a><b>=====[ UPGRADE ]=====\n<7>Level ").append(next.level()).append("/").append(registry.maxCoreLevel()).append("\n\n");
        text.append(next.tokens() > 0 ? "<5>+" + next.tokens() + " Token of the Mountain" : next.reward());
        text.append("\n<b>+").append(next.skyBlockXp()).append(" SkyBlock XP\n\n<7>Cost\n");
        text.append(next.cost() == 0 ? "<a>Free" : next.powder().getColour() + String.format("%,d", next.cost()) + " " + next.powder().getDisplayName());
        text.append("\n\n<e>Click to upgrade!");
        return text.toString();
    }

    public static String heart(SkyBlockPlayer player) {
        return wrap(buildHeart(player));
    }

    private static String buildHeart(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        StringBuilder text = new StringBuilder("<5>Heart of the Mountain\n<7>" + TOKEN + "<7>: <5>" + data.getAvailableTokens());
        text.append("\n\n<7>Unlock more ").append(TOKEN).append(" <7>by leveling up your Heart of the Mountain tiers.\n");
        double bonusPercent = HotmService.perkValue(player, "powder_buff");
        for (HotmPowder powder : HotmPowder.values()) {
            text.append("\n<7>").append(powder.getDisplayName()).append(": ").append(powder.getColour()).append(String.format("%,d", data.powder(powder)));
            int base = HotmService.baseBonus(player, powder);
            if (base > 0) text.append("\n <8>(<7>+").append(base).append(" extra base powder<8>)");
            if (bonusPercent > 0) text.append("\n <8>(<7>+").append(format(bonusPercent)).append("% more powder<8>)");
        }
        text.append("\n\n<7>Obtain <2>Mithril Powder<7> by mining and taking part in events in the <a>Dwarven Mines<7>.");
        text.append("\n<7>Obtain <d>Gemstone Powder<7> by mining <d>Gemstones<7> and opening <6>Treasure Chests<7> in the <5>Crystal Hollows<7>.");
        text.append("\n<7>Obtain <b>Glacite Powder<7> by mining <b>Glacite<7> and looting <b>Frozen Corpses<7> in the <b>Glacite Tunnels<7>.");
        text.append("\n\n<8>Increase your chance to gain extra Powder by unlocking perks, equipping the Mithril Golem Pet, and more!");
        return text.toString();
    }

    public static String reset(SkyBlockPlayer player) {
        return wrap(buildReset(player));
    }

    private static String buildReset(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        HotmRegistry registry = HotmRegistry.get();
        long[] refund = new long[HotmPowder.values().length];
        data.getPerkLevels().forEach((id, level) -> {
            HotmPerk perk = registry.perk(id);
            if (perk != null && perk.usesPowder()) refund[perk.powder().ordinal()] += perk.totalCost(level);
        });
        StringBuilder text = new StringBuilder("<c>Reset Heart of the Mountain\n<7>Resets the Perks and Abilities of your <5>Heart of the Mountain<7>, locking them and resetting their levels.\n\n<7>You will be reimbursed with:");
        text.append("\n<8>– <5>").append(data.getTokensSpent()).append(" Token of the Mountain");
        for (HotmPowder powder : HotmPowder.values()) {
            text.append("\n<8>– ").append(powder.getColour()).append(String.format("%,d", refund[powder.ordinal()])).append(" ").append(powder.getDisplayName());
        }
        text.append("\n\n<7>You will <a>keep<7> any Tiers and <c>Core of the Mountain<7> that you have unlocked.");
        text.append("\n\n<c><b>WARNING: This is permanent. You can not go back after resetting your Heart of the Mountain!");
        return text.toString();
    }

    public static String wrap(String text) {
        StringBuilder out = new StringBuilder();
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) out.append('\n');
            out.append(wrapLine(lines[i]));
        }
        return out.toString();
    }

    private static String wrapLine(String line) {
        if (visibleLength(line) <= WRAP_WIDTH) return line;
        StringBuilder out = new StringBuilder();
        String colour = "";
        int length = 0;
        for (String word : line.split(" ")) {
            int wordLength = visibleLength(word);
            if (length > 0 && length + 1 + wordLength > WRAP_WIDTH) {
                out.append('\n').append(colour);
                length = 0;
            } else if (length > 0) {
                out.append(' ');
                length++;
            }
            out.append(word);
            length += wordLength;
            java.util.regex.Matcher matcher = TAG.matcher(word);
            while (matcher.find()) colour = matcher.group();
        }
        return out.toString();
    }

    private static int visibleLength(String text) {
        return TAG.matcher(text).replaceAll("").length();
    }

    public static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) return String.format(Locale.ROOT, "%,d", Math.round(value));
        return String.format(Locale.ROOT, "%,.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
