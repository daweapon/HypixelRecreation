package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.sources;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// One category's task screen, titled "Tasks -> Core" the way Hypixel titles it.
public class GUIXPSourceGroup implements StatefulView<GUIXPSourceGroup.State> {
    private static final int SORT_SLOT = 50;

    /** Skill Related is a hub of four sub-areas on Hypixel rather than a flat task list. */
    public static void open(ViewContext ctx, SkyBlockXPSource.Group group) {
        if (group == SkyBlockXPSource.Group.SKILL_RELATED) {
            ctx.push(new GUIXPSubAreas(), new GUIXPSubAreas.State());
            return;
        }
        ctx.push(new GUIXPSourceGroup(), new State(group, null, Sort.AVAILABLE_XP));
    }

    public static void open(ViewContext ctx, SkyBlockXPSource.SubArea subArea) {
        ctx.push(new GUIXPSourceGroup(), new State(null, subArea, Sort.AVAILABLE_XP));
    }

    @Override
    public State initialState() {
        return new State(SkyBlockXPSource.Group.CORE, null, Sort.AVAILABLE_XP);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText(
                (state, ctx) -> Text.literal("Tasks ➜ " + state.shortName()),
                InventoryType.CHEST_6_ROW
        );
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(state.filler()));
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> headerItem(state, (SkyBlockPlayer) c.player()));
        layout.slot(SORT_SLOT, (s, c) -> sortItem(s.sort()),
                (click, c) -> c.session(State.class).update(State::nextSort));

        List<SkyBlockXPSource> tasks = state.sortedTasks((SkyBlockPlayer) ctx.player());
        int[] slots = XPTaskLayout.slots(tasks.size(), 1, 4);
        for (int i = 0; i < slots.length; i++) {
            SkyBlockXPSource task = tasks.get(i);
            layout.slot(slots[i],
                    (s, c) -> taskItem(task, (SkyBlockPlayer) c.player()),
                    (click, c) -> XPTaskActions.click(c, task));
        }
    }

    private ItemStack.Builder headerItem(State state, SkyBlockPlayer player) {
        List<SkyBlockXPSource> tasks = state.tasks();
        double earned = tasks.stream().mapToDouble(player.getSkyBlockExperience()::getSourceXP).sum();
        int total = tasks.stream().mapToInt(SkyBlockXPSource::getMaxXP).sum();

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>{} Tasks", tasks.size()));
        lore.add(Text.empty());
        state.description().forEach(line -> lore.add(Text.of("<7>{}", line)));
        lore.add(Text.empty());
        lore.addAll(GUIXPSources.progressBar(earned, total));

        return ItemStacks.item(state.icon(), 1, Text.of("<b>{}", state.displayName()), lore);
    }

    private ItemStack.Builder sortItem(Sort sort) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Change how Tasks are sorted."));
        lore.add(Text.empty());
        for (Sort option : Sort.values()) {
            lore.add(option == sort
                    ? Text.of("<a>▶ {}", option.getDisplayName())
                    : Text.of("<7>{}", option.getDisplayName()));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to switch sort!"));

        return ItemStacks.item(Material.HOPPER, 1, Text.of("<a>Sort"), lore);
    }

    private ItemStack.Builder taskItem(SkyBlockXPSource source, SkyBlockPlayer player) {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        double earned = experience.getSourceXP(source);
        List<String> subTasks = source.getSubTasks();

        List<Text> lore = new ArrayList<>();
        lore.add(subTasks.isEmpty()
                ? Text.of("<7>XP Task")
                : Text.of("<7>{} Tasks", subTasks.size()));
        lore.add(Text.empty());

        if (subTasks.isEmpty()) {
            source.getTaskDescription().forEach(line -> lore.add(Text.of("<7>" + line)));
        } else {
            subTasks.forEach(subTask -> lore.add(Text.of("<c>✖ {}", subTask)));
        }
        lore.add(Text.empty());

        if (source.isUncapped()) {
            lore.add(Text.of("<f>XP Gained from Task: <b>{} XP", GUIXPSources.number(earned)));
        } else {
            lore.add(Text.of("<f>Total Progress: <b>{}",
                    GUIXPSources.percentage(earned, source.getMaxXP())));
            lore.add(GUIXPSources.detailBar(earned, source.getMaxXP()));
        }

        lore.add(Text.empty());
        lore.add(Text.of("<7>This task is worth <b>{} <7>of your Total",
                GUIXPSources.percentage(earned, experience.getTotalXP())));
        lore.add(Text.of("<7>SkyBlock XP!"));

        if (source.getAvailability() != SkyBlockXPSource.Availability.LIVE) {
            lore.add(Text.empty());
            lore.add(Text.of("<8>{} on this server.", source.getAvailability().getDisplayName()));
        }

        lore.add(Text.empty());
        SkyBlockXPSource.TaskAction action = source.getAction();
        action.lines().forEach(line -> lore.add(Text.of(action.clickable() ? "<e>" + line : "<c>" + line)));

        return GUIXPSources.taskIcon(source, Text.of("<a>{}", source.getDisplayName()), lore);
    }

    @lombok.Getter
    public enum Sort {
        UNLOCKED("Unlocked"),
        EARNED_XP("Earned XP"),
        AVAILABLE_XP("Available XP"),
        A_TO_Z("A to Z"),
        Z_TO_A("Z to A"),
        ;

        private final String displayName;

        Sort(String displayName) {
            this.displayName = displayName;
        }
    }

    /** Exactly one of {@code group} and {@code subArea} is set. */
    public record State(SkyBlockXPSource.Group group, SkyBlockXPSource.SubArea subArea, Sort sort) {

        public List<SkyBlockXPSource> tasks() {
            return group != null
                    ? SkyBlockXPSource.getByGroup(group)
                    : SkyBlockXPSource.getBySubArea(subArea);
        }

        public List<SkyBlockXPSource> sortedTasks(SkyBlockPlayer player) {
            DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
            Comparator<SkyBlockXPSource> comparator = switch (sort) {
                case UNLOCKED -> Comparator.comparing(SkyBlockXPSource::isObtainable).reversed();
                case EARNED_XP -> Comparator.comparingDouble(experience::getSourceXP).reversed();
                case AVAILABLE_XP -> Comparator
                        .comparingDouble((SkyBlockXPSource s) -> s.getMaxXP() - experience.getSourceXP(s))
                        .reversed();
                case A_TO_Z -> Comparator.comparing(SkyBlockXPSource::getDisplayName);
                case Z_TO_A -> Comparator.comparing(SkyBlockXPSource::getDisplayName).reversed();
            };
            return tasks().stream().sorted(comparator).toList();
        }

        public State nextSort() {
            Sort[] options = Sort.values();
            return new State(group, subArea, options[(sort.ordinal() + 1) % options.length]);
        }

        public String displayName() {
            return group != null ? group.getDisplayName() : subArea.getDisplayName() + " Tasks";
        }

        public String shortName() {
            return group != null ? group.getShortName() : subArea.getDisplayName();
        }

        public Material icon() {
            return group != null ? group.getIcon() : subArea.getIcon();
        }

        public Material filler() {
            return group != null ? group.getFiller() : subArea.getFiller();
        }

        public List<String> description() {
            return group != null ? group.getDescription() : subArea.getDescription();
        }
    }
}
