package net.swofty.type.skyblockgeneric.gui.inventories.drill;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeDrillData.DrillData;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.drill.DrillFuel;
import net.swofty.type.skyblockgeneric.drill.DrillPart;
import net.swofty.type.skyblockgeneric.drill.DrillParts;
import net.swofty.type.skyblockgeneric.drill.DrillSlot;
import net.swofty.type.skyblockgeneric.drill.MachineFuel;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIDrillAnvil extends StatelessView {
    public static final int REMOVAL_FEE = 9_999;
    public static final int DRILL_SLOT = 20;
    public static final int INPUT_SLOT = 24;
    public static final int APPLY_SLOT = 22;
    public static final int INFO_SLOT = 13;
    private static final List<DrillSlot> SLOT_ORDER = List.of(DrillSlot.TANK, DrillSlot.ENGINE, DrillSlot.MODULE);
    private static final int[] INSTALLED_SLOTS = {29, 30, 31};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Drill Anvil", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        for (int slot : new int[]{DRILL_SLOT, INPUT_SLOT}) {
            ItemStack stack = ctx.inventory().getItemStack(slot);
            if (stack.isAir()) continue;
            player.addAndUpdateItem(new SkyBlockItem(stack));
            ctx.inventory().setItemStack(slot, ItemStack.AIR);
        }
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);

        layout.editable(DRILL_SLOT, (slot, oldItem, newItem, s) -> reject(ctx, slot, newItem, !isDrill(newItem),
                "<c>Only Drills can be placed here!"));
        layout.editable(INPUT_SLOT, (slot, oldItem, newItem, s) -> reject(ctx, slot, newItem, !isInput(newItem),
                "<c>Place a Drill Part or Machine Fuel here!"));

        layout.slot(INFO_SLOT, (s, c) -> info(c));
        layout.slot(APPLY_SLOT, (s, c) -> applyButton(c), (click, c) -> apply(c));
        for (int index = 0; index < SLOT_ORDER.size(); index++) {
            DrillSlot drillSlot = SLOT_ORDER.get(index);
            layout.slot(INSTALLED_SLOTS[index], (s, c) -> installedIcon(c, drillSlot), (click, c) -> remove(c, drillSlot));
        }
    }

    private static void reject(ViewContext ctx, int slot, ItemStack newItem, boolean invalid, String message) {
        if (newItem.isAir() || !invalid) return;
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        player.sendMessage(message);
        ctx.inventory().setItemStack(slot, ItemStack.AIR);
        player.addAndUpdateItem(new SkyBlockItem(newItem));
    }

    private static boolean isDrill(ItemStack stack) {
        return DrillFuel.isDrill(new SkyBlockItem(stack));
    }

    private static boolean isInput(ItemStack stack) {
        String id = idOf(stack);
        return DrillParts.get(id) != null || MachineFuel.isFuel(id);
    }

    private static String idOf(ItemStack stack) {
        return stack.isAir() ? null : new SkyBlockItem(stack).getAttributeHandler().getTypeAsString();
    }

    private static SkyBlockItem drillIn(ViewContext ctx) {
        ItemStack stack = ctx.inventory().getItemStack(DRILL_SLOT);
        return stack.isAir() ? null : new SkyBlockItem(stack);
    }

    private static ItemStack.Builder info(ViewContext ctx) {
        SkyBlockItem drill = drillIn(ctx);
        if (drill == null) {
            return ItemStacks.item(Material.BOOK, """
                    <a>Drill Anvil
                    <7>Place a <a>Drill <7>on the left and a
                    <7>Drill Part or <2>Machine Fuel <7>on the right,
                    <7>then click the hopper to apply it.

                    <7>Removing an installed part costs <6>%s coins<7>.""".formatted(DrillFuel.format(REMOVAL_FEE)));
        }
        DrillData data = drill.getAttributeHandler().getDrillData();
        return ItemStacks.item(Material.BOOK, """
                <a>Drill Anvil
                <7>Fuel: <2>%s<8>/%s

                <7>Removing an installed part costs <6>%s coins<7>.""".formatted(DrillFuel.format(data.fuel()),
                DrillFuel.formatCapacity(DrillFuel.capacity(data)), DrillFuel.format(REMOVAL_FEE)));
    }

    private static ItemStack.Builder applyButton(ViewContext ctx) {
        SkyBlockItem drill = drillIn(ctx);
        ItemStack input = ctx.inventory().getItemStack(INPUT_SLOT);
        if (drill == null) {
            return ItemStacks.item(Material.BARRIER, "<c>No Drill\n<7>Place a <a>Drill <7>on the left.");
        }
        if (input.isAir()) {
            return ItemStacks.item(Material.HOPPER, "<7>Nothing to apply\n<7>Place a <a>Drill Part <7>or <2>Machine Fuel <7>on the right.");
        }
        String id = idOf(input);
        DrillData data = drill.getAttributeHandler().getDrillData();
        DrillPart part = DrillParts.get(id);
        if (part != null) {
            if (DrillParts.installed(data, part.slot()) != null) {
                return ItemStacks.item(Material.BARRIER, "<c>%s Occupied\n<7>Remove the installed %s first."
                        .formatted(part.slot().getDisplayName(), part.slot().getDisplayName()));
            }
            return ItemStacks.item(Material.ANVIL, "<a>Apply Drill Part\n<7>Install into the <6>%s <7>slot.\n\n<e>Click to apply!"
                    .formatted(part.slot().getDisplayName()));
        }
        int perItem = MachineFuel.amount(id);
        int items = DrillFuel.itemsToFill(data, perItem, input.amount());
        if (items == 0) {
            return ItemStacks.item(Material.BARRIER, "<c>Tank Full\n<7>This Drill cannot hold any more <2>Fuel<7>.");
        }
        int added = Math.min(DrillFuel.capacity(data) - data.fuel(), perItem * items);
        return ItemStacks.item(Material.ANVIL, "<a>Refuel Drill\n<7>Adds <2>+%s Fuel <7>using <a>%d <7>item(s).\n\n<e>Click to refuel!"
                .formatted(DrillFuel.format(added), items));
    }

    private static void apply(ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockItem drill = drillIn(ctx);
        ItemStack input = ctx.inventory().getItemStack(INPUT_SLOT);
        if (drill == null || input.isAir()) return;
        String id = idOf(input);
        DrillData data = drill.getAttributeHandler().getDrillData();
        DrillPart part = DrillParts.get(id);
        int consumed;
        DrillData updated;
        if (part != null) {
            if (DrillParts.installed(data, part.slot()) != null) {
                player.sendMessage("<c>Remove the installed {} first!", part.slot().getDisplayName());
                return;
            }
            updated = DrillParts.install(data, part);
            consumed = 1;
            player.sendMessage("<a>You installed the <6>{} <a>part!", part.slot().getDisplayName());
        } else {
            int perItem = MachineFuel.amount(id);
            consumed = DrillFuel.itemsToFill(data, perItem, input.amount());
            if (consumed == 0) return;
            updated = DrillFuel.refuel(data, perItem, consumed);
            player.sendMessage("<a>Your Drill was refueled with <2>{} Fuel<a>!", DrillFuel.format(updated.fuel() - data.fuel()));
        }
        drill.getAttributeHandler().setDrillData(updated);
        writeDrill(ctx, player, drill);
        ctx.inventory().setItemStack(INPUT_SLOT, input.amount() <= consumed ? ItemStack.AIR : input.withAmount(input.amount() - consumed));
        ctx.session(DefaultState.class).refresh();
    }

    private static ItemStack.Builder installedIcon(ViewContext ctx, DrillSlot slot) {
        SkyBlockItem drill = drillIn(ctx);
        if (drill == null) {
            return ItemStacks.item(Material.GRAY_STAINED_GLASS_PANE, "<7>%s\n<8>Place a Drill to view its parts.".formatted(slot.getDisplayName()));
        }
        DrillPart part = DrillParts.get(DrillParts.installed(drill.getAttributeHandler().getDrillData(), slot));
        if (part == null) {
            return ItemStacks.item(Material.GRAY_STAINED_GLASS_PANE, "<7>%s\n<c>Not Installed".formatted(slot.getDisplayName()));
        }
        StringBuilder text = new StringBuilder("<9>" + new SkyBlockItem(part.itemId()).getDisplayName() + "\n");
        DrillParts.effectLines(part).forEach(line -> text.append(line).append('\n'));
        text.append("\n<7>Cost: <6>").append(DrillFuel.format(REMOVAL_FEE)).append(" coins\n<e>Click to remove!");
        return ItemStacks.item(iconFor(slot), text.toString());
    }

    private static Material iconFor(DrillSlot slot) {
        return switch (slot) {
            case TANK -> Material.BUCKET;
            case ENGINE -> Material.PISTON;
            case MODULE -> Material.REDSTONE;
        };
    }

    private static void remove(ViewContext ctx, DrillSlot slot) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockItem drill = drillIn(ctx);
        if (drill == null) return;
        DrillData data = drill.getAttributeHandler().getDrillData();
        String installed = DrillParts.installed(data, slot);
        if (installed == null) return;
        if (player.getCoins() < REMOVAL_FEE) {
            player.sendMessage("<c>You need {} coins to remove a Drill Part!", DrillFuel.format(REMOVAL_FEE));
            return;
        }
        player.removeCoins(REMOVAL_FEE);
        drill.getAttributeHandler().setDrillData(DrillParts.remove(data, slot));
        writeDrill(ctx, player, drill);
        player.addAndUpdateItem(new SkyBlockItem(installed));
        player.sendMessage("<a>You removed the <6>{} <a>part for <6>{} coins<a>!", slot.getDisplayName(), DrillFuel.format(REMOVAL_FEE));
        ctx.session(DefaultState.class).refresh();
    }

    private static void writeDrill(ViewContext ctx, SkyBlockPlayer player, SkyBlockItem drill) {
        ctx.inventory().setItemStack(DRILL_SLOT, PlayerItemUpdater.playerUpdate(player, drill.getItemStack()).build());
    }
}
