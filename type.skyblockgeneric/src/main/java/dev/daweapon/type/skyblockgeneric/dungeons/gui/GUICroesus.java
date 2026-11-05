package dev.daweapon.type.skyblockgeneric.dungeons.gui;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraChestStorage;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestRun;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestStorage;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonRewardChest;
import dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra.GUIKuudraRun;
import dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra.KuudraRunIcon;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Croesus (wiki Croesus/UI): the profile's stored Catacombs and Kuudra runs from the last 72 hours, newest
 * first, with a run-type filter and an opened filter. A Catacombs run opens its chests menu; a Kuudra run
 * opens the same Free/Paid menu Vesuvius uses.
 */
public class GUICroesus extends StatelessView {
    public static final String HEAD = "ab8e63abc0ec2d028900287ac7e71dbf447ee7091c9ff0785fa19e880b30815";
    private static final int[] RUN_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    public enum TypeFilter {
        ALL("All"), CATACOMBS("The Catacombs"), KUUDRA("Kuudra");

        private final String label;

        TypeFilter(String label) {
            this.label = label;
        }
    }

    public enum StatusFilter {
        ALL("All"), OPENED("Opened"), UNOPENED("Unopened");

        private final String label;

        StatusFilter(String label) {
            this.label = label;
        }
    }

    private record Entry(long completedAt, boolean opened, boolean kuudra, Supplier<ItemStack.Builder> icon,
                         Supplier<View<?>> target) {
    }

    private final TypeFilter type;
    private final StatusFilter status;
    private final int page;

    public GUICroesus() {
        this(TypeFilter.ALL, StatusFilter.ALL, 0);
    }

    public GUICroesus(TypeFilter type, StatusFilter status, int page) {
        this.type = type;
        this.status = status;
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Croesus", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(4, ItemStacks.head(HEAD, """
                <e>Croesus
                <7>After completing a <a>Catacombs <7>or
                <c>Kuudra <7>run, if you don't open a
                <7>chest, I'll save it here for you!

                <7>But beware, I can only hold <660 runs
                <7>worth of chests, and will discard
                <7>them after <a>72 hours<7>!"""));

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        long now = System.currentTimeMillis();
        List<Entry> entries = entries(player, now).stream()
                .filter(entry -> type == TypeFilter.ALL || (type == TypeFilter.KUUDRA) == entry.kuudra())
                .filter(entry -> status == StatusFilter.ALL || (status == StatusFilter.OPENED) == entry.opened())
                .toList();
        int pages = Math.max(1, (entries.size() + RUN_SLOTS.length - 1) / RUN_SLOTS.length);
        int shown = Math.min(page, pages - 1);

        if (entries.isEmpty()) {
            layout.slot(22, ItemStacks.item(Material.SKELETON_SKULL, """
                    <c>No Treasures!
                    <7>Chests from your runs where you
                    <7>didn't open the end treasure will
                    <7>appear here."""));
        }
        for (int i = 0; i < RUN_SLOTS.length; i++) {
            int index = shown * RUN_SLOTS.length + i;
            if (index >= entries.size()) break;
            Entry entry = entries.get(index);
            layout.slot(RUN_SLOTS[i], (_, _) -> entry.icon().get(), (_, c) -> c.push(entry.target().get()));
        }

        if (shown > 0) {
            layout.slot(45, ItemStacks.named(Material.ARROW, "<a>Previous Page"),
                    (_, c) -> c.replace(new GUICroesus(type, status, shown - 1)));
        }
        if (shown < pages - 1) {
            layout.slot(53, ItemStacks.named(Material.ARROW, "<a>Next Page"),
                    (_, c) -> c.replace(new GUICroesus(type, status, shown + 1)));
        }
        layout.slot(50, (_, _) -> ItemStacks.item(Material.ENDER_EYE, filterLore(TypeFilter.values(), type)),
                (click, c) -> c.replace(new GUICroesus(cycle(TypeFilter.values(), type, click.click() instanceof Click.Right), status, 0)));
        layout.slot(51, (_, _) -> ItemStacks.item(Material.ENDER_EYE, filterLore(StatusFilter.values(), status)),
                (click, c) -> c.replace(new GUICroesus(type, cycle(StatusFilter.values(), status, click.click() instanceof Click.Right), 0)));
    }

    private static List<Entry> entries(SkyBlockPlayer player, long now) {
        List<Entry> entries = new ArrayList<>();
        for (DungeonChestRun run : DungeonChestStorage.runs(player)) {
            entries.add(new Entry(run.completedAt(), run.isOpened(), false, () -> runIcon(run, now), () -> new GUIDungeonRunChests(run)));
        }
        for (KuudraRun run : KuudraChestStorage.runs(player)) {
            entries.add(new Entry(run.completedAt(), run.isOpened(), true, () -> KuudraRunIcon.icon(run, now), () -> new GUIKuudraRun(run)));
        }
        entries.sort(Comparator.comparingLong(Entry::completedAt).reversed());
        return entries;
    }

    static ItemStack.Builder runIcon(DungeonChestRun run, long now) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Tier: <e>{}", run.floor().displayName()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Players"));
        run.party().forEach(line -> lore.add(Text.of(line)));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Completed: <a>{}", ago(now - run.completedAt())));
        if (!run.isOpened()) lore.add(Text.of("<7>No Chests Opened!"));
        else if (!run.canOpenMore()) lore.add(Text.of("<a>No more Chests to open!"));
        else lore.add(Text.of("<7>Opened Chest: {}", openedNames(run)));
        lore.add(Text.of("<7>Chests expire in <c>{}<7>!", expiry(run.expiresAt() - now)));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to view chests!"));
        DungeonChestTier best = run.chests().getLast().tier();
        String name = run.mode() == CatacombsMode.MASTER ? "<c>Master Mode Catacombs" : "<c>The Catacombs";
        return DungeonChestIcons.chest(best, Text.of(name), lore);
    }

    private static String openedNames(DungeonChestRun run) {
        return String.join("<7>, ", run.chests().stream().filter(DungeonRewardChest::opened)
                .map(chest -> chest.tier().colour() + chest.tier().displayName()).toList());
    }

    static String ago(long millis) {
        Duration duration = Duration.ofMillis(Math.max(0, millis));
        if (duration.toDays() > 0) return plural(duration.toDays(), "day") + " ago";
        if (duration.toHours() > 0) return plural(duration.toHours(), "hour") + " ago";
        if (duration.toMinutes() > 0) return plural(duration.toMinutes(), "min") + " ago";
        return "moments ago";
    }

    static String expiry(long millis) {
        Duration duration = Duration.ofMillis(Math.max(0, millis));
        return duration.toHours() > 0 ? duration.toHours() + "h" : Math.max(1, duration.toMinutes()) + "m";
    }

    private static String plural(long amount, String unit) {
        return amount + " " + unit + (amount == 1 ? "" : "s");
    }

    private static <E extends Enum<E>> E cycle(E[] values, E current, boolean backwards) {
        int step = backwards ? values.length - 1 : 1;
        return values[(current.ordinal() + step) % values.length];
    }

    private static String filterLore(Enum<?>[] values, Enum<?> current) {
        StringBuilder lore = new StringBuilder("<a>Filter\n");
        for (Enum<?> each : values) {
            String label = each instanceof TypeFilter typeFilter ? typeFilter.label : ((StatusFilter) each).label;
            lore.append(each == current ? "<a>▶ " : "<7>  ").append(label).append('\n');
        }
        return lore.append("\n<b>Right-click to go backwards!\n<e>Click to switch!").toString();
    }
}
