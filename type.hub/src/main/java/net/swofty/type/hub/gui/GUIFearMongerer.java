package net.swofty.type.hub.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.spookyfestival.SpookyFestivalTiers;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// The Fear Mongerer's candy-leaderboard reward claim GUI (Roadmap #19). Real Hypixel (SkyBlock
// Wiki, confirmed against the live page after an earlier wrong guess treated this as a plain
// candy shop): 7 point tiers (Wood -> Top 50, Top 50 placement-based rather than a point
// threshold), each granting an additively-stacking bundle of coins/items on top of the previous
// tier's rewards - see SpookyFestivalTiers for the exact wiki-sourced thresholds/bundles.
public class GUIFearMongerer implements View<GUIFearMongerer.State> {

    private static final int[] TIER_SLOTS = {10, 12, 14, 16, 28, 30, 32};

    public record State(double points, int rank) {
        public static State forPlayer(SkyBlockPlayer player) {
            String key = leaderboardKey();
            Double score = LeaderboardService.getPlayerScore(key, player.getUuid());
            int rank = LeaderboardService.getPlayerRank(key, player.getUuid());
            return new State(score == null ? 0 : score, rank);
        }
    }

    private static String leaderboardKey() {
        return "spooky_festival_" + SkyBlockCalendar.getYear();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Fear Mongerer", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        // Matches the real tooltip's wording where the data backs it (combined score + rank).
        // Real Hypixel also shows separate "Your Green Candy: X" / "Your Purple Candy: Y" lines -
        // not shown here since this only tracks combined leaderboard points, not a per-candy-type
        // lifetime counter (a real, documented gap, not silently approximated).
        layout.slot(4, (_, _) -> ItemStacks.item(Material.PUMPKIN,
                "<6><l>Spooky Rewards\n<7>{} Spooky Festival\n \n<7>+1 pt. per Green Candy\n<7>+5 pts. per Purple Candy\n \n<7>Your score is <a>top #{} <7>for this event.",
                StringUtility.ntify(SkyBlockCalendar.getYear()),
                state.rank() <= 0 ? "Unranked" : String.valueOf(state.rank())));

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int claimedTier = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_CLAIMED_TIER, DatapointInteger.class)
                .getValue();
        int claimedYear = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_CLAIMED_YEAR, DatapointInteger.class)
                .getValue();
        if (claimedYear != SkyBlockCalendar.getYear()) {
            claimedTier = 0;
        }

        SpookyFestivalTiers.Tier[] tiers = SpookyFestivalTiers.TIERS;
        for (int i = 0; i < tiers.length; i++) {
            SpookyFestivalTiers.Tier tier = tiers[i];
            boolean unlocked = tier.rankBased()
                    ? (state.rank() > 0 && state.rank() <= 50)
                    : state.points() >= tier.threshold();
            boolean claimed = claimedTier > i;
            int slotIndex = i;
            int finalClaimedTier = claimedTier;

            layout.slot(TIER_SLOTS[i], (_, _) -> tierItem(tier, unlocked, claimed), (_, viewCtx) -> {
                if (claimed || !unlocked) return;
                SkyBlockPlayer clicker = (SkyBlockPlayer) viewCtx.player();
                grantTierRewards(clicker, tier);
                clicker.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_CLAIMED_TIER, DatapointInteger.class)
                        .setValue(Math.max(finalClaimedTier, slotIndex + 1));
                clicker.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_CLAIMED_YEAR, DatapointInteger.class)
                        .setValue(SkyBlockCalendar.getYear());
                clicker.sendMessage("<a>You claimed the <6>{} <a>tier reward!", tier.name());
                clicker.closeInventory();
                clicker.openView(new GUIFearMongerer(), State.forPlayer(clicker));
            });
        }

        Components.close(layout, 49);
    }

    private static void grantTierRewards(SkyBlockPlayer player, SpookyFestivalTiers.Tier tier) {
        player.addCoins(tier.coins());
        if (tier.carnivalTickets() > 0) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.CARNIVAL_TICKET, tier.carnivalTickets()));
        }
        if (tier.spookyPie()) player.addAndUpdateItem(new SkyBlockItem(ItemType.SPOOKY_PIE));
        if (tier.spookyDisc()) player.addAndUpdateItem(new SkyBlockItem(ItemType.SPOOKY_DISC));
        if (tier.witchMask()) player.addAndUpdateItem(new SkyBlockItem(ItemType.WITCH_MASK));
        if (tier.skeletonHorsePet()) player.addAndUpdateItem(new SkyBlockItem(ItemType.SKELETON_HORSE_PET));
        if (tier.goldenTreat()) player.addAndUpdateItem(new SkyBlockItem(ItemType.GOLDEN_TREAT));
        if (tier.vampirismBook()) {
            SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
            book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.VAMPIRISM, 6));
            player.addAndUpdateItem(book);
        }
    }

    private static ItemStack.Builder tierItem(SpookyFestivalTiers.Tier tier, boolean unlocked, boolean claimed) {
        // Real Hypixel represents each tier with its namesake block (Wood/Stone/Iron/Gold/
        // Diamond/Emerald), not one of the reward items (user-confirmed via screenshot - the
        // earlier version showed Spooky Disc/Witch Mask icons instead of the ore/material blocks).
        Material icon = switch (tier.name()) {
            case "Wood" -> Material.OAK_PLANKS;
            case "Stone" -> Material.STONE;
            case "Iron" -> Material.IRON_BLOCK;
            case "Gold" -> Material.GOLD_BLOCK;
            case "Diamond" -> Material.DIAMOND_BLOCK;
            case "Emerald" -> Material.EMERALD_BLOCK;
            default -> Material.NETHER_STAR;
        };

        ItemStack.Builder builder = ItemStack.builder(icon);

        String status = claimed ? "<a>CLAIMED" : unlocked ? "<e><l>CLICK TO CLAIM" : "<c>LOCKED";
        String requirement = tier.rankBased()
                ? "<7>Requires: <a>Top 50 on leaderboard"
                : "<7>Requires: <a>" + tier.threshold() + " points";

        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        lore.add(Text.of("<6>{} Tier", tier.name()));
        lore.add(Text.of(requirement));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards:"));
        lore.add(Text.of("<7>- <6>{} Coins", tier.coins()));
        lore.add(Text.of("<7>- <a>{}x Carnival Ticket", tier.carnivalTickets()));
        if (tier.spookyPie()) lore.add(Text.of("<7>- <a>Spooky Pie"));
        if (tier.spookyDisc()) lore.add(Text.of("<7>- <a>Spooky Disc"));
        if (tier.vampirismBook()) lore.add(Text.of("<7>- <a>Enchanted Book (Vampirism VI)"));
        if (tier.witchMask()) lore.add(Text.of("<7>- <a>Witch Mask"));
        if (tier.skeletonHorsePet()) lore.add(Text.of("<7>- <a>Skeleton Horse Pet"));
        if (tier.goldenTreat()) lore.add(Text.of("<7>- <a>Golden Treat"));
        lore.add(Text.empty());
        lore.add(Text.of(status));

        return ItemStacks.appendLore(builder, lore);
    }
}
