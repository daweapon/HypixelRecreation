package net.swofty.type.hub.gui.farmingcontest;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.farmingcontest.AnitaShop;
import net.swofty.type.skyblockgeneric.farmingcontest.AnitaShop.Offer;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestBracket;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestCrop;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestProfile;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestStatistics;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContests;
import net.swofty.type.skyblockgeneric.farmingcontest.MedalCost;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// Anita's shop, laid out from the wiki's Anita/UI subpage.
public class GUIAnita extends StatelessView {
    private static final int[] OFFER_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Anita", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        for (int i = 0; i < AnitaShop.OFFERS.size(); i++) {
            Offer offer = AnitaShop.OFFERS.get(i);
            layout.slot(OFFER_SLOTS[i], (_, c) -> offerItem((SkyBlockPlayer) c.player(), offer), (_, c) -> {
                buy((SkyBlockPlayer) c.player(), offer);
                c.session(DefaultState.class).refresh();
            });
        }

        layout.slot(48, (_, c) -> personalBestsButton(FarmingContests.profile((SkyBlockPlayer) c.player())), (_, c) -> {
            if (FarmingContests.profile((SkyBlockPlayer) c.player()).isPersonalBestsUnlocked()) {
                c.push(new GUIAnitaPersonalBests());
            }
        });
        layout.slot(50, (_, _) -> ItemStacks.item(Material.POWERED_RAIL, """
                <a>Medal Trades
                <7>Convert and sell your medals.

                <e>Click to open!"""), (_, c) -> c.push(new GUIAnitaMedalTrades()));
        layout.slot(51, (_, c) -> uniqueBracketsItem(FarmingContests.profile((SkyBlockPlayer) c.player())));
    }

    private static ItemStack.Builder offerItem(SkyBlockPlayer player, Offer offer) {
        FarmingContestProfile profile = FarmingContests.profile(player);
        return switch (offer.kind()) {
            case ITEM -> {
                ItemStack.Builder builder = PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(offer.item()).getItemStack());
                List<String> lines = new ArrayList<>(List.of(""));
                lines.addAll(offer.cost().lore());
                if (offer.tradesIn() != null) lines.add(new SkyBlockItem(offer.tradesIn()).getDisplayName());
                lines.add("");
                lines.add(offer.cost().affordable(player, profile) ? "<e>Click to trade!" : "<c>Cannot afford this!");
                yield ItemStacks.appendLore(builder, String.join("\n", lines));
            }
            case LEVEL_CAP -> levelCapItem(player, profile, offer);
            case EXTRA_FORTUNE -> extraFortuneItem(player, profile, offer);
            case PERSONAL_BESTS -> personalBestsOffer(player, profile, offer);
            case UNAVAILABLE -> {
                List<String> lines = new ArrayList<>();
                lines.add(offer.color() + offer.name() + (offer.amount() > 1 ? " <8>x" + offer.amount() : ""));
                lines.add("");
                lines.addAll(offer.cost().lore());
                lines.add("");
                lines.add("<8>" + offer.unavailableReason());
                lines.add("<c>Unavailable");
                yield ItemStacks.item(offer.icon(), offer.amount(), String.join("\n", lines));
            }
        };
    }

    private static ItemStack.Builder levelCapItem(SkyBlockPlayer player, FarmingContestProfile profile, Offer offer) {
        int purchased = profile.getLevelCapTier();
        MedalCost cost = AnitaShop.nextTierCost(AnitaShop.LEVEL_CAP_TIERS, purchased);
        List<String> lines = new ArrayList<>();
        lines.add("<e>" + offer.name());
        lines.add("<7>Raise your Farming Skill level cap");
        lines.add("<7>by one, up to <f>+" + AnitaShop.LEVEL_CAP_TIERS.size() + "<7>.");
        lines.add("");
        lines.add("<7>Your cap: <e>Farming " + roman(50 + purchased));
        if (cost == null) {
            lines.add("");
            lines.add("<a>Maxed out!");
            return ItemStacks.item(offer.icon(), String.join("\n", lines));
        }
        int required = AnitaShop.goldCropsRequiredForLevelCap(purchased);
        int reached = profile.cropsWithAtLeast(ContestBracket.GOLD);
        lines.add("");
        lines.add("<7>Requirement");
        lines.add((reached >= required ? "<a>✔" : "<c>✖") + " <7>Win " + ContestBracket.GOLD.formatted() + " <r><7>in <e>" + required
                + " <7>collection" + (required > 1 ? "s" : "") + "! <7>(" + (reached >= required ? "<a>" : "<c>")
                + Math.min(reached, required) + "<7>/<e>" + required + "<7>)");
        lines.add("");
        lines.addAll(cost.lore());
        lines.add("");
        lines.add(reached < required ? "<c>Requirement not met!"
                : cost.affordable(player, profile) ? "<e>Click to trade!" : "<c>Cannot afford this!");
        return ItemStacks.item(offer.icon(), String.join("\n", lines));
    }

    private static ItemStack.Builder extraFortuneItem(SkyBlockPlayer player, FarmingContestProfile profile, Offer offer) {
        int purchased = profile.getExtraFortuneTier();
        MedalCost cost = AnitaShop.nextTierCost(AnitaShop.EXTRA_FORTUNE_TIERS, purchased);
        List<String> lines = new ArrayList<>();
        lines.add("<e>" + offer.name());
        lines.add("<7>Permanently gain <6>+" + FarmingContestStatistics.FORTUNE_PER_EXTRA_TIER + " Farming");
        lines.add("<6>Fortune <7>per tier.");
        lines.add("");
        lines.add("<7>Current: <6>+" + purchased * FarmingContestStatistics.FORTUNE_PER_EXTRA_TIER + "☘ Farming Fortune");
        lines.add("<7>Tier: <e>" + purchased + "<7>/<e>" + AnitaShop.EXTRA_FORTUNE_TIERS.size());
        lines.add("");
        if (cost == null) {
            lines.add("<a>Maxed out!");
        } else {
            lines.addAll(cost.lore());
            lines.add("");
            lines.add(cost.affordable(player, profile) ? "<e>Click to trade!" : "<c>Cannot afford this!");
        }
        return ItemStacks.item(offer.icon(), String.join("\n", lines));
    }

    private static ItemStack.Builder personalBestsOffer(SkyBlockPlayer player, FarmingContestProfile profile, Offer offer) {
        List<String> lines = new ArrayList<>();
        lines.add("<e>" + offer.name());
        lines.add("<7>Beating your <6>Personal Best <7>is great!");
        lines.add("<7>But what if it also gave you <6>Farming");
        lines.add("<6>Fortune<7>?!");
        lines.add("");
        if (profile.isPersonalBestsUnlocked()) {
            lines.add("<a>Unlocked!");
        } else {
            lines.addAll(offer.cost().lore());
            lines.add("");
            lines.add(offer.cost().affordable(player, profile) ? "<e>Click to trade!" : "<c>Cannot afford this!");
        }
        return ItemStacks.item(offer.icon(), String.join("\n", lines));
    }

    private static ItemStack.Builder personalBestsButton(FarmingContestProfile profile) {
        return ItemStacks.item(Material.PAPER, """
                <a>Personal Bests
                <7>The higher your <6>Personal Bests <7>are
                <7>from participating in <e>Jacob's Farming
                <e>Contest<7>, the more <6>Farming Fortune
                <7>you'll obtain for that crop!

                """
                + (profile.isPersonalBestsUnlocked() ? "<e>Click to view!" : "<c>Purchase the perk in Anita's Shop to\n<c>unlock this menu!"));
    }

    private static ItemStack.Builder uniqueBracketsItem(FarmingContestProfile profile) {
        List<String> lines = new ArrayList<>();
        lines.add("<e>Unique Brackets Reached");
        for (ContestCrop crop : ContestCrop.values()) {
            ContestBracket best = profile.uniqueBracket(crop);
            lines.add(symbol(best, ContestBracket.BRONZE, "<c>") + symbol(best, ContestBracket.SILVER, "<f>")
                    + symbol(best, ContestBracket.GOLD, "<6>") + " " + (best == null ? "<c>" : best.getColor())
                    + crop.getDisplayName() + (best != null && best.atLeast(ContestBracket.PLATINUM) ? " " + best.formatted() : ""));
        }
        return ItemStacks.item(Material.GOLD_INGOT, String.join("\n", lines));
    }

    private static String symbol(ContestBracket best, ContestBracket bracket, String color) {
        return color + (best != null && best.atLeast(bracket) ? "●" : "◌");
    }

    private static void buy(SkyBlockPlayer player, Offer offer) {
        FarmingContestProfile profile = FarmingContests.profile(player);
        switch (offer.kind()) {
            case UNAVAILABLE -> player.sendMessage("<c>" + offer.unavailableReason());
            case ITEM -> {
                if (!pay(player, profile, offer.cost(), offer.tradesIn())) return;
                player.addAndUpdateItem(new SkyBlockItem(offer.item()));
                player.sendMessage("<a>You bought " + new SkyBlockItem(offer.item()).getDisplayName() + "<a>!");
            }
            case LEVEL_CAP -> {
                MedalCost cost = AnitaShop.nextTierCost(AnitaShop.LEVEL_CAP_TIERS, profile.getLevelCapTier());
                if (cost == null) return;
                int required = AnitaShop.goldCropsRequiredForLevelCap(profile.getLevelCapTier());
                if (profile.cropsWithAtLeast(ContestBracket.GOLD) < required) {
                    player.sendMessage("<c>You need to win GOLD in " + required + " collection" + (required > 1 ? "s" : "") + " first!");
                    return;
                }
                if (!pay(player, profile, cost, null)) return;
                profile.setLevelCapTier(profile.getLevelCapTier() + 1);
                player.getSkyBlockExperience().addSourceXP(SkyBlockXPSource.ANITAS_SHOP_UPGRADES, AnitaShop.UPGRADE_TIER_XP);
                player.sendMessage("<a>Your Farming level cap is now <e>" + roman(50 + profile.getLevelCapTier()) + "<a>!");
            }
            case EXTRA_FORTUNE -> {
                MedalCost cost = AnitaShop.nextTierCost(AnitaShop.EXTRA_FORTUNE_TIERS, profile.getExtraFortuneTier());
                if (cost == null || !pay(player, profile, cost, null)) return;
                profile.setExtraFortuneTier(profile.getExtraFortuneTier() + 1);
                player.getSkyBlockExperience().addSourceXP(SkyBlockXPSource.ANITAS_SHOP_UPGRADES, AnitaShop.UPGRADE_TIER_XP);
                player.sendMessage("<a>You now have <6>+" + profile.getExtraFortuneTier() * FarmingContestStatistics.FORTUNE_PER_EXTRA_TIER
                        + "☘ Farming Fortune <a>from Anita!");
            }
            case PERSONAL_BESTS -> {
                if (profile.isPersonalBestsUnlocked() || !pay(player, profile, offer.cost(), null)) return;
                profile.setPersonalBestsUnlocked(true);
                player.sendMessage("<a>You unlocked the <6>Personal Bests <a>perk!");
            }
        }
        FarmingContests.save(player, profile);
    }

    private static boolean pay(SkyBlockPlayer player, FarmingContestProfile profile, MedalCost cost, ItemType tradesIn) {
        if (!cost.affordable(player, profile)) {
            player.sendMessage("<c>You can't afford this!");
            return false;
        }
        if (tradesIn != null && player.getAmountInInventory(tradesIn) < 1) {
            player.sendMessage("<c>You need " + new SkyBlockItem(tradesIn).getDisplayName() + " <c>in your inventory!");
            return false;
        }
        if (tradesIn != null) player.takeItem(tradesIn, 1);
        cost.pay(player, profile);
        return true;
    }

    private static String roman(int level) {
        return StringUtility.getAsRomanNumeral(level);
    }
}
