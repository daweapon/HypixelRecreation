package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.guide;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.PaginatedView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.rewards.GUILevelRewards;
import dev.daweapon.type.skyblockgeneric.levels.LevelsGuide;
import dev.daweapon.type.skyblockgeneric.levels.guide.GuideEntry;
import dev.daweapon.type.skyblockgeneric.levels.guide.GuideTask;
import dev.daweapon.type.skyblockgeneric.minion.MinionRegistry;
import dev.daweapon.type.skyblockgeneric.museum.MuseumCatalogue;
import dev.daweapon.type.skyblockgeneric.museum.MuseumEntry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public final class GUIGuideTask extends PaginatedView<GuideEntry, GUIGuideTask.State> {

    public record State(LevelsGuide tier, GuideTask task, List<GuideEntry> items, int page)
            implements PaginatedState<GuideEntry> {
        @Override
        public State withPage(int page) {
            return new State(tier, task, items, page);
        }

        @Override
        public State withItems(List<GuideEntry> items) {
            return new State(tier, task, items, page);
        }
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText(
                (state, ctx) -> Text.literal(StringUtility.toNormalCase(state.tier().name()) + " ➜ " + state.task().name()),
                InventoryType.CHEST_6_ROW);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return DEFAULT_SLOTS;
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    @Override
    protected ItemStack.Builder renderItem(GuideEntry entry, int index, HypixelPlayer player) {
        if (!entry.available()) {
            return ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<c>✖ <8>{}", entry.label()),
                    List.of(Text.empty(), Text.of("<8>Not in the game yet!")));
        }
        boolean done = entry.complete((SkyBlockPlayer) player);
        ItemStack.Builder icon = ItemStacks.item(material(entry), 1,
                Text.of(done ? "<a>✔ <a>{}" : "<c>✖ <f>{}", entry.label()),
                List.of(Text.empty(), Text.of(done ? "<a>Completed!" : "<c>Not completed yet")));
        return done ? ItemStacks.enchanted(icon) : icon;
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, GuideEntry entry, int index) {
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, GuideEntry entry) {
        return false;
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        layout.slot(4, (s, c) -> header(s, (SkyBlockPlayer) c.player()));
    }

    private ItemStack.Builder header(State state, SkyBlockPlayer player) {
        GuideTask task = state.task();
        double progress = task.progress(player);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>{} Tasks", task.entries().size()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>{}", task.description()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Total Worth: <b>+{} XP", StringUtility.commaify(task.xp())));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Total Progress: <a>{}%", Math.round(progress * 100)));
        lore.add(GUILevelRewards.progressBar(progress, 1, task.completed(player), task.entries().size()));
        long unavailable = task.unavailable();
        if (unavailable > 0) {
            lore.add(Text.empty());
            lore.add(Text.of("<8>{} of these are not in the game yet.", unavailable));
        }
        return ItemStacks.text(task.icon(), Text.of("<a>{}", task.name()), lore);
    }

    private static Material material(GuideEntry entry) {
        String ref = entry.ref();
        if (ref == null) return Material.GRAY_DYE;
        try {
            switch (entry.kind()) {
                case ACCESSORY, PET -> {
                    return ItemType.get(ref).material;
                }
                case COLLECTION -> {
                    return ItemType.get(ref.substring(0, ref.indexOf(':'))).material;
                }
                case MINION -> {
                    return MinionRegistry.valueOf(ref.substring(0, ref.indexOf(':'))).getItemType().material;
                }
                case MUSEUM -> {
                    MuseumEntry museum = MuseumCatalogue.get().entry(ref).orElse(null);
                    ItemType icon = museum == null ? null : museum.iconType();
                    return icon == null ? entry.kind().fallbackIcon() : icon.material;
                }
                default -> {
                    return entry.kind().fallbackIcon();
                }
            }
        } catch (RuntimeException exception) {
            return entry.kind().fallbackIcon();
        }
    }
}
