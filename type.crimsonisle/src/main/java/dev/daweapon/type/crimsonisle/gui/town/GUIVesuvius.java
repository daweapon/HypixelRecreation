package dev.daweapon.type.crimsonisle.gui.town;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraChestStorage;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;
import dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra.GUIKuudraRun;
import dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra.KuudraRunIcon;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.Predicate;

/**
 * Vesuvius's chest storage (wiki Vesuvius/UI): the profile's won Kuudra runs from the last 72 hours, up to
 * 60 of them, filtered by All/Opened/Unopened. A run opens its chests menu, with the same key cost.
 */
public class GUIVesuvius extends StatelessView {
    private static final String HEAD = "11443decbade0555c53938fdf69fb4cfe408a0a600ea8555a69433b3415aa823";
    private static final int[] RUN_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    public enum Filter {
        ALL("All", _ -> true),
        OPENED("Opened", KuudraRun::isOpened),
        UNOPENED("Unopened", run -> !run.isOpened());

        private final String label;
        private final Predicate<KuudraRun> matches;

        Filter(String label, Predicate<KuudraRun> matches) {
            this.label = label;
            this.matches = matches;
        }

        Filter next() {
            return values()[(ordinal() + 1) % values().length];
        }

        Filter previous() {
            return values()[(ordinal() + values().length - 1) % values().length];
        }
    }

    private final Filter filter;
    private final int page;

    public GUIVesuvius() {
        this(Filter.ALL, 0);
    }

    public GUIVesuvius(Filter filter, int page) {
        this.filter = filter;
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Vesuvius", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(4, ItemStacks.head(HEAD, """
                <e>Vesuvius
                <7>After completing a <a>Catacombs <7>or
                <c>Kuudra <7>run, if you don't open a
                <7>chest, I'll save it here for you!

                <7>But beware, I can only hold <6>60 runs
                <7>worth of chests, and will discard
                <7>them after <a>72 hours<7>!"""));

        List<KuudraRun> runs = KuudraChestStorage.runs((SkyBlockPlayer) ctx.player()).stream().filter(filter.matches).toList();
        int pages = Math.max(1, (runs.size() + RUN_SLOTS.length - 1) / RUN_SLOTS.length);
        int shown = Math.min(page, pages - 1);

        if (runs.isEmpty()) {
            String empty = """
                    <c>No treasures!
                    <7>Chests from <c>Kuudra <7>runs where you
                    <7>didn't open the end treasure will
                    <7>accumulate here.""";
            if (!KuudraAvailability.isFightAvailable()) empty += "\n\n" + KuudraAvailability.UNAVAILABLE_LORE;
            layout.slot(22, ItemStacks.item(Material.SKELETON_SKULL, empty));
        }
        long now = System.currentTimeMillis();
        for (int i = 0; i < RUN_SLOTS.length; i++) {
            int index = shown * RUN_SLOTS.length + i;
            if (index >= runs.size()) break;
            KuudraRun run = runs.get(index);
            layout.slot(RUN_SLOTS[i], (_, _) -> KuudraRunIcon.icon(run, now), (_, c) -> c.push(new GUIKuudraRun(run)));
        }

        if (shown > 0) {
            layout.slot(45, ItemStacks.named(Material.ARROW, "<a>Previous Page"),
                    (_, c) -> c.replace(new GUIVesuvius(filter, shown - 1)));
        }
        if (shown < pages - 1) {
            layout.slot(53, ItemStacks.named(Material.ARROW, "<a>Next Page"),
                    (_, c) -> c.replace(new GUIVesuvius(filter, shown + 1)));
        }
        layout.slot(51, (_, _) -> ItemStacks.item(Material.ENDER_EYE, filterLore()),
                (click, c) -> c.replace(new GUIVesuvius(click.click() instanceof Click.Right ? filter.previous() : filter.next(), 0)));
    }

    private String filterLore() {
        StringBuilder lore = new StringBuilder("<a>Filter\n\n");
        for (Filter each : Filter.values()) {
            lore.append(each == filter ? "<b>▶ " : "<7>  ").append(each.label).append('\n');
        }
        return lore.append("\n<b>Right-click to go backwards!\n<e>Click to switch!").toString();
    }
}
