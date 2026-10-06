package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.rewards;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.levels.unlocks.SkyBlockLevelStatisticUnlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every level that grants a permanent stat, and the running total they add up to.
 *
 * Paginated because the table runs the full level range - a fixed slot list would silently
 * truncate it, which is what the sibling reward screens do.
 */
public class GUILevelStatisticRewards extends PaginatedView<SkyBlockLevelRequirement, GUILevelStatisticRewards.State> {
    private static final int[] PAGINATED_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Statistic Rewards", InventoryType.CHEST_6_ROW);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return PAGINATED_SLOTS;
    }

    @Override
    protected ItemStack.Builder renderItem(SkyBlockLevelRequirement level, int index, HypixelPlayer player) {
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        int current = skyBlockPlayer.getSkyBlockExperience().getLevel().asInt();
        boolean unlocked = current >= level.asInt();

        List<Text> lore = new ArrayList<>();
        for (SkyBlockLevelStatisticUnlock unlock : level.getStatisticUnlocks()) {
            lore.addAll(unlock.getDisplay(skyBlockPlayer, level.asInt()));
        }
        lore.add(Text.empty());
        lore.add(unlocked
                ? Text.of("<a>You have unlocked this reward!")
                : Text.of("<7>Levels left to unlock: <3>{}", level.asInt() - current));

        return ItemStacks.item(
                unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, 1,
                Text.of((unlocked ? "<a>" : "<7>") + "Level {}", level.asInt()), lore);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, SkyBlockLevelRequirement item, int index) {
        // Informational only.
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, SkyBlockLevelRequirement item) {
        return false;
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            int current = player.getSkyBlockExperience().getLevel().asInt();

            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<7>Permanent stats granted by"));
            lore.add(Text.of("<7>levelling up your SkyBlock Level."));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Earned so far:"));

            Map<ItemStatistic, Double> earned = totalsUpTo(current);
            if (earned.isEmpty()) {
                lore.add(Text.of("<8>Nothing yet"));
            } else {
                earned.forEach((stat, value) -> lore.add(Text.of(
                        "<8> +<a>{}{} <stat:{}>", trim(value), stat.getSuffix(), stat.name().toLowerCase())));
            }

            lore.add(Text.empty());
            lore.add(Text.of("<7>Unlocked at maximum level:"));
            totalsUpTo(Integer.MAX_VALUE).forEach((stat, value) -> lore.add(Text.of(
                    "<8> +<a>{}{} <stat:{}>", trim(value), stat.getSuffix(), stat.name().toLowerCase())));

            lore.add(Text.empty());
            lore.addAll(GUILevelRewards.getAsDisplay(countUpTo(current), countUpTo(Integer.MAX_VALUE)));

            return ItemStacks.item(Material.DIAMOND_HELMET, 1, Text.of("<a>Statistic Rewards"), lore);
        });
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    /** Levels that actually grant a stat; levels that grant nothing would be empty tiles. */
    public static List<SkyBlockLevelRequirement> rewardingLevels() {
        return SkyBlockLevelRequirement.getAllLevels().stream()
                .filter(level -> !level.getStatisticUnlocks().isEmpty())
                .sorted((a, b) -> Integer.compare(a.asInt(), b.asInt()))
                .toList();
    }

    private static int countUpTo(int level) {
        return (int) rewardingLevels().stream().filter(l -> l.asInt() <= level).count();
    }

    private static Map<ItemStatistic, Double> totalsUpTo(int level) {
        Map<ItemStatistic, Double> totals = new LinkedHashMap<>();
        for (SkyBlockLevelRequirement requirement : rewardingLevels()) {
            if (requirement.asInt() > level) break;
            for (SkyBlockLevelStatisticUnlock unlock : requirement.getStatisticUnlocks()) {
                unlock.getStatistics().getStatisticsAdditive().forEach((stat, value) -> {
                    if (value > 0) totals.merge(stat, value.doubleValue(), Double::sum);
                });
            }
        }
        return totals;
    }

    private static String trim(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    public record State(List<SkyBlockLevelRequirement> items, int page)
            implements PaginatedState<SkyBlockLevelRequirement> {

        public State() {
            this(rewardingLevels(), 0);
        }

        @Override
        public PaginatedState<SkyBlockLevelRequirement> withPage(int page) {
            return new State(items, page);
        }

        @Override
        public PaginatedState<SkyBlockLevelRequirement> withItems(List<SkyBlockLevelRequirement> items) {
            return new State(items, page);
        }
    }
}
