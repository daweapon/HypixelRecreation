package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.sources;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Hypixel's "Ways to Level Up" menu: one item per XP task category, each listing its individual
// tasks and their progress inline, with a toggle for the per-task bars.
public class GUIXPSources implements StatefulView<GUIXPSources.State> {
    private static final int RECENTLY_VIEWED_SLOT = 37;
    private static final int[] RECENT_SLOTS = {38, 39, 40, 41, 42, 43};
    private static final int TOGGLE_SLOT = 50;
    private static final int BAR_LENGTH = 20;
    private static final String BAR = "─".repeat(BAR_LENGTH);

    public record State(boolean showProgressBars) {
    }

    @Override
    public State initialState() {
        return new State(true);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Ways to Level Up", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockXPSource.Group[] groups = SkyBlockXPSource.Group.values();
        int[] slots = XPTaskLayout.slots(groups.length, 1, 2);
        for (int i = 0; i < slots.length; i++) {
            SkyBlockXPSource.Group group = groups[i];
            layout.slot(slots[i],
                    (s, c) -> categoryItem(group, (SkyBlockPlayer) c.player(), s.showProgressBars()),
                    (click, c) -> GUIXPSourceGroup.open(c, group));
        }

        layout.slot(RECENTLY_VIEWED_SLOT, (s, c) -> recentlyViewedItem((SkyBlockPlayer) c.player()));
        layoutRecentlyViewed(layout, ctx);

        layout.slot(TOGGLE_SLOT, (s, c) -> progressBarToggleItem(s.showProgressBars()),
                (click, c) -> c.session(State.class)
                        .update(current -> new State(!current.showProgressBars())));
    }

    private void layoutRecentlyViewed(ViewLayout<State> layout, ViewContext ctx) {
        List<SkyBlockXPSource> recent = RecentlyViewedXPTasks.get(ctx.player().getUuid());

        for (int i = 0; i < RECENT_SLOTS.length; i++) {
            if (i >= recent.size()) break;
            SkyBlockXPSource task = recent.get(i);
            layout.slot(RECENT_SLOTS[i],
                    (s, c) -> taskShortcut(task, (SkyBlockPlayer) c.player()),
                    (click, c) -> XPTaskActions.click(c, task));
        }
    }

    private ItemStack.Builder categoryItem(SkyBlockXPSource.Group group, SkyBlockPlayer player, boolean showBars) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        List<SkyBlockXPSource> tasks = SkyBlockXPSource.getByGroup(group);
        double earned = tasks.stream().mapToDouble(experience::getSourceXP).sum();
        int total = SkyBlockXPSource.getTotalXPFor(group);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>{} Tasks", tasks.size()));
        lore.add(Text.empty());
        group.getDescription().forEach(line -> lore.add(Text.of("<7>{}", line)));
        lore.add(Text.empty());

        if (showBars) {
            for (SkyBlockXPSource task : tasks) {
                lore.add(Text.of("<e>▶ <e>{}", task.getDisplayName()));
                lore.add(taskBar(experience.getSourceXP(task), task.getMaxXP()));
            }
            lore.add(Text.empty());
        }

        lore.add(Text.of("<f>Progress to Complete Category: <e>{}", percentage(earned, total)));
        lore.add(categoryBar(earned, total));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to view tasks!"));

        return ItemStacks.item(group.getIcon(), 1, Text.of("<b>{}", group.getDisplayName()), lore);
    }

    private ItemStack.Builder recentlyViewedItem(SkyBlockPlayer player) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>When you view XP Tasks, they will"));
        lore.add(Text.of("<7>show up here for your convenience!"));

        return ItemStacks.item(Material.OAK_SIGN, 1, Text.of("<d>Recently Viewed <d>➡"), lore);
    }

    private ItemStack.Builder taskShortcut(SkyBlockXPSource task, SkyBlockPlayer player) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{}", task.getGroup().getDisplayName()));
        lore.add(Text.empty());
        lore.add(taskBar(player.getSkyBlockExperience().getSourceXP(task), task.getMaxXP()));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to view tasks!"));

        return taskIcon(task, Text.of(task.getAvailability().getColour() + "▶ {}", task.getDisplayName()), lore);
    }

    private ItemStack.Builder progressBarToggleItem(boolean showing) {
        List<Text> lore = List.of(
                Text.of("<7>Toggle whether to display more"),
                Text.of("<7>detailed task progress in each"),
                Text.of("<7>category."),
                Text.empty(),
                showing ? Text.of("<e>Click to hide!") : Text.of("<e>Click to show!")
        );

        return ItemStacks.item(showing ? Material.LIME_DYE : Material.GRAY_DYE, 1,
                showing ? Text.of("<c>Hide Progress Bars") : Text.of("<a>Show Progress Bars"), lore);
    }

    /** A task's icon, which is a custom head for the tasks Hypixel draws that way. */
    public static ItemStack.Builder taskIcon(SkyBlockXPSource source, Text name, List<Text> lore) {
        String texture = source.getHeadTexture();
        return texture != null
                ? ItemStacks.head(texture, 1, name, lore)
                : ItemStacks.item(source.getIcon(), 1, name, lore);
    }

    /** One task's inline bar, e.g. {@code ──────── 185/185 XP}. */
    public static Text taskBar(double earned, double total) {
        int completed = completedChars(earned, total);
        return Text.of("<2>{}<8>{} <a>{}<7>/<a>{} XP",
                BAR.substring(0, completed), BAR.substring(completed),
                number(earned), number(total));
    }

    /** The aqua bar inside a single task's tooltip. */
    public static Text detailBar(double earned, double total) {
        int completed = completedChars(earned, total);
        return Text.of("<b>{}<8>{} <b>{}<3>/<b>{} XP",
                BAR.substring(0, completed), BAR.substring(completed),
                number(earned), number(total));
    }

    /** The gold summary bar under a category's task list. */
    public static Text categoryBar(double earned, double total) {
        int completed = completedChars(earned, total);
        return Text.of("<6>{}<8>{} <e>{}<6>/<e>{} XP",
                BAR.substring(0, completed), BAR.substring(completed),
                number(earned), number(total));
    }

    /** Shared "x% — bar — earned/total" block used by the task screens. */
    public static List<Text> progressBar(double earned, double total) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<f>Progress to Complete Category: <e>{}", percentage(earned, total)));
        lines.add(categoryBar(earned, total));
        return lines;
    }

    public static String percentage(double earned, double total) {
        double ratio = total <= 0 ? 0D : Math.max(0D, Math.min(earned / total, 1D));
        String formatted = String.format(Locale.ROOT, "%.1f", ratio * 100);
        return (formatted.endsWith(".0") ? formatted.substring(0, formatted.length() - 2) : formatted) + "%";
    }

    private static int completedChars(double earned, double total) {
        double ratio = total <= 0 ? 0D : Math.max(0D, Math.min(earned / total, 1D));
        return (int) Math.round(ratio * BAR_LENGTH);
    }

    public static String number(double value) {
        return String.format(Locale.ROOT, "%,d", Math.round(value));
    }
}
