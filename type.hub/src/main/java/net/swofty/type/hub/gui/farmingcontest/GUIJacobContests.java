package net.swofty.type.hub.gui.farmingcontest;

import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestBracket;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestRewards;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContest;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestProfile.Entry;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// Jacob's list of finished contests. Claiming all at once is limited to one page of 28, as on
// Hypixel since March 2024.
public class GUIJacobContests implements View<GUIJacobContests.State> {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    public record State(int page) {}

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Your Contests", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<Entry> contests = FarmingContests.profile(player).finishedNewestFirst();
        int pages = Math.max(1, (contests.size() + SLOTS.length - 1) / SLOTS.length);
        int page = Math.min(state.page(), pages - 1);
        List<Entry> shown = contests.subList(page * SLOTS.length, Math.min(contests.size(), (page + 1) * SLOTS.length));

        if (shown.isEmpty()) {
            layout.slot(22, (_, _) -> ItemStacks.item(Material.DEAD_BUSH, """
                    <c>No contests yet!
                    <7>Collect at least <e>100 <7>of a contest
                    <7>crop during one of my contests."""));
        }
        for (int i = 0; i < shown.size(); i++) {
            Entry entry = shown.get(i);
            layout.slot(SLOTS[i], (_, _) -> contestItem(entry), (_, c) -> {
                if (!entry.isClaimable()) return;
                FarmingContests.claim((SkyBlockPlayer) c.player(), List.of(entry));
                c.session(State.class).refresh();
            });
        }

        List<Entry> claimable = shown.stream().filter(Entry::isClaimable).toList();
        layout.slot(50, (_, _) -> ItemStacks.item(claimable.isEmpty() ? Material.GRAY_DYE : Material.LIME_DYE,
                claimable.isEmpty() ? "<a>Claim All\n<7>No rewards to claim on this page."
                        : "<a>Claim All\n<7>Claim the rewards of <e>" + claimable.size() + " <7>contests\n<7>on this page.\n\n<e>Click to claim!"),
                (_, c) -> {
                    if (claimable.isEmpty()) return;
                    FarmingContests.claim((SkyBlockPlayer) c.player(), claimable);
                    c.session(State.class).refresh();
                });

        if (page > 0) {
            layout.slot(45, (_, _) -> ItemStacks.named(Material.ARROW, "<a>Previous Page"),
                    (_, c) -> c.session(State.class).update(s -> new State(s.page() - 1)));
        }
        if (page < pages - 1) {
            layout.slot(53, (_, _) -> ItemStacks.named(Material.ARROW, "<a>Next Page"),
                    (_, c) -> c.session(State.class).update(s -> new State(s.page() + 1)));
        }
    }

    private static ItemStack.Builder contestItem(Entry entry) {
        FarmingContest contest = entry.contest();
        ContestBracket bracket = entry.getBracket();
        List<String> lines = new ArrayList<>();
        lines.add("<e>" + contest.dateLabel());
        lines.add("<8>Farming Contest");
        lines.add("");
        lines.add(GUIJacob.cropList(contest, entry.getCrop()));
        lines.add("");
        lines.add("<7>Collected: <e>" + StringUtility.commaify(entry.getCollected()) + " <7>" + entry.getCrop().getDisplayName());
        lines.add(bracket.earnsMedal()
                ? "<7>Placed in the " + bracket.formatted() + " <r><7>bracket!"
                : "<7>You didn't earn a medal.");
        lines.add("");
        lines.add("<7>Rewards:");
        lines.addAll(ContestRewards.lore(bracket, entry.getCrop()));
        if (bracket.earnsMedal()) lines.add("<8>" + ContestRewards.TURBO_CROP_UNAVAILABLE);
        lines.add("");
        lines.add(entry.isClaimed() ? "<a>Rewards claimed!" : "<e>Click to claim rewards!");

        ItemStack.Builder builder = ItemStacks.item(entry.getCrop().getIcon(), String.join("\n", lines));
        return entry.isClaimed() ? builder : builder.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
}
