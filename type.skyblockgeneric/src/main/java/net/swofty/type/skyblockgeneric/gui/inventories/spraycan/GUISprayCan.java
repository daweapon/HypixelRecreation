package net.swofty.type.skyblockgeneric.gui.inventories.spraycan;

import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.spray.Spray;
import net.swofty.type.skyblockgeneric.spray.SprayCanData;
import net.swofty.type.skyblockgeneric.spray.SprayCatalogue;
import net.swofty.type.skyblockgeneric.spray.SprayDirection;
import net.swofty.type.skyblockgeneric.spray.SprayFilter;
import net.swofty.type.skyblockgeneric.spray.SprayImages;
import net.swofty.type.skyblockgeneric.spray.SprayInk;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The Spray Can's Spray Selector and Ink Manager (wiki Spray Can/UI). */
public final class GUISprayCan implements StatefulView<GUISprayCan.State> {
    private static final int[] SPRAY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};
    private static final int PREVIOUS_PAGE_SLOT = 45;
    private static final int FILTER_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int INK_STORAGE_SLOT = 50;
    private static final int NEXT_PAGE_SLOT = 53;

    @Override
    public State initialState() {
        return new State(-1, 0, SprayFilter.NONE);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Spray Can", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        Optional<SprayCanData> data = SprayCans.read(player, state.slot());
        if (data.isEmpty()) {
            Components.close(layout, CLOSE_SLOT);
            return;
        }
        SprayCanData can = data.get();
        List<Spray> sprays = state.filter().apply(SprayCatalogue.all());
        int pages = Math.max(1, (sprays.size() + SPRAY_SLOTS.length - 1) / SPRAY_SLOTS.length);
        int page = Math.min(state.page(), pages - 1);

        for (int i = 0; i < SPRAY_SLOTS.length; i++) {
            int index = page * SPRAY_SLOTS.length + i;
            if (index >= sprays.size()) break;
            Spray spray = sprays.get(index);
            layout.slot(SPRAY_SLOTS[i], (s, c) -> sprayItem(spray, can), (click, c) -> clickSpray(player, click, c, spray));
        }

        if (page > 0) {
            layout.slot(PREVIOUS_PAGE_SLOT, ItemStacks.item(Material.ARROW, "<a>Previous Page\n<e>Page {}", page),
                    (click, c) -> c.session(State.class).update(s -> s.withPage(page - 1)));
        }
        if (page < pages - 1) {
            layout.slot(NEXT_PAGE_SLOT, ItemStacks.item(Material.ARROW, "<a>Next Page\n<e>Page {}", page + 2),
                    (click, c) -> c.session(State.class).update(s -> s.withPage(page + 1)));
        }
        layout.slot(FILTER_SLOT, (s, c) -> filterItem(s.filter()), (click, c) -> c.session(State.class).update(s ->
                s.withFilter(click.click() instanceof Click.Right ? s.filter().previous() : s.filter().next())));
        Components.close(layout, CLOSE_SLOT);
        layout.slot(INK_STORAGE_SLOT, (s, c) -> inkStorageItem(can), (click, c) -> refill(player, state, c));
    }

    private ItemStack.Builder sprayItem(Spray spray, SprayCanData can) {
        boolean selected = spray.id().equals(can.getSelectedSpray());
        List<Text> lore = new ArrayList<>();
        spray.lore().forEach(line -> lore.add(Text.of("<7>{}", line)));
        if (!spray.hasDirections() && !spray.ink().isEmpty()) {
            lore.add(Text.empty());
            lore.add(Text.of("<7>Required Ink"));
            spray.ink().forEach((dye, percent) -> lore.add(dye.loreLine(percent, can.inkOf(dye) >= percent)));
        }
        lore.add(Text.empty());
        if (!spray.isUnlocked()) {
            String reason = spray.source().unavailableReason();
            lore.add(Text.of("<c>Locked!"));
            if (reason != null) lore.add(Text.of("<8>{}", reason));
            return ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<c>{}", spray.name()), lore);
        }
        if (spray.hasDirections()) {
            lore.add(Text.of("<e>Click to select orientation!"));
        } else {
            lore.add(Text.of("<e>Right-Click to preview!"));
            lore.add(Text.empty());
            lore.add(selected ? Text.of("<a>Selected!") : Text.of("<e>Click to select!"));
        }
        ItemStack.Builder item = ItemStacks.item(Material.PAINTING, 1, Text.of("<a>{}", spray.name()), lore);
        return selected ? ItemStacks.enchanted(item) : item;
    }

    private void clickSpray(SkyBlockPlayer player, ClickContext<State> click, ViewContext ctx, Spray spray) {
        if (!spray.isUnlocked()) {
            String reason = spray.source().unavailableReason();
            player.sendMessage("<c>You haven't unlocked this spray!" + (reason == null ? "" : " " + reason));
            return;
        }
        if (spray.hasDirections()) {
            ctx.push(new GUISprayOrientation(), new GUISprayOrientation.State(click.state().slot(), spray.id()));
            return;
        }
        if (click.click() instanceof Click.Right) {
            preview(player, spray, SprayDirection.RIGHT);
            return;
        }
        SprayCans.select(player, click.state().slot(), spray, SprayDirection.RIGHT);
        ctx.session(State.class).refresh();
    }

    static void preview(SkyBlockPlayer player, Spray spray, SprayDirection direction) {
        player.closeInventory();
        player.sendPacket(SprayImages.packet(spray, direction));
        ItemStack map = ItemStack.builder(Material.FILLED_MAP)
                .set(DataComponents.MAP_ID, SprayImages.mapId(spray, direction))
                .build();
        player.sendPacket(new SetPlayerInventorySlotPacket(player.getHeldSlot(), map));
        player.sendMessage("<a>Previewing <e>" + spray.name() + "<a>! It's only in your hand until your inventory updates.");
    }

    private ItemStack.Builder filterItem(SprayFilter current) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        for (SprayFilter filter : SprayFilter.values()) {
            lore.add(filter == current ? Text.of("<a>▶ {}", filter.getDisplayName()) : Text.of("<7>  {}", filter.getDisplayName()));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<b>Right-click to go backwards!"));
        lore.add(Text.of("<e>Click to switch!"));
        return ItemStacks.item(Material.ENDER_EYE, 1, Text.of("<a>Filter"), lore);
    }

    private ItemStack.Builder inkStorageItem(SprayCanData can) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Drag any dye on this icon to refill"));
        lore.add(Text.of("<7>your Ink Storage!"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Ink Storage"));
        for (SprayInk dye : SprayInk.values()) {
            int percent = can.inkOf(dye);
            lore.add(dye.loreLine(percent, percent > 0));
        }
        return ItemStacks.item(Material.BONE_MEAL, 1, Text.of("<e>Your Ink Storage"), lore);
    }

    private void refill(SkyBlockPlayer player, State state, ViewContext ctx) {
        ItemStack cursor = player.getInventory().getCursorItem();
        if (cursor.isAir()) return;
        ItemType type = new SkyBlockItem(cursor).getAttributeHandler().getPotentialType();
        Optional<SprayInk> dye = SprayInk.fromItem(type);
        if (dye.isEmpty()) {
            player.sendMessage("<c>Only dyes can be used as ink!");
            return;
        }
        int used = SprayCans.refill(player, state.slot(), dye.get(), cursor.amount());
        if (used == 0) {
            player.sendMessage("<c>Your " + dye.get().getDisplayName() + " ink is already full!");
            return;
        }
        player.getInventory().setCursorItem(cursor.withAmount(cursor.amount() - used));
        ctx.session(State.class).refresh();
    }

    public record State(int slot, int page, SprayFilter filter) {
        public static State forHeldCan(SkyBlockPlayer player) {
            return new State(player.getHeldSlot(), 0, SprayFilter.NONE);
        }

        State withPage(int newPage) {
            return new State(slot, newPage, filter);
        }

        State withFilter(SprayFilter newFilter) {
            return new State(slot, 0, newFilter);
        }
    }
}
