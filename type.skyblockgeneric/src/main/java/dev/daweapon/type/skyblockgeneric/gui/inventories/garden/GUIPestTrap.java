package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestData;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestItems;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestTraps;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIPestTrap extends StatelessView {
    private static final int[] CAUGHT_SLOTS = {12, 13, 14, 15};
    private static final int BAIT_SLOT = 11;
    private static final int COLLECT_SLOT = 16;
    private static final int BACK_SLOT = 30;
    private static final int CLOSE_SLOT = 31;
    private static final int PICK_UP_SLOT = 35;

    private final String trapId;

    public GUIPestTrap(String trapId) {
        this.trapId = trapId;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Pest Trap"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, CLOSE_SLOT);
        Components.back(layout, BACK_SLOT, ctx);

        layout.slot(BAIT_SLOT, (_, c) -> bait((SkyBlockPlayer) c.player()), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (click.click() instanceof Click.Right) {
                PestTraps.removeBait(player, trapId);
            } else {
                PestTraps.addBaitFromInventory(player, trapId);
            }
            c.session(DefaultState.class).refresh();
        });

        PestData.Trap placed = PestTraps.find((SkyBlockPlayer) ctx.player(), trapId);
        PestTraps.Kind placedKind = placed == null ? null : PestTraps.KINDS.get(placed.getKind());
        int shown = Math.min(CAUGHT_SLOTS.length, placedKind == null ? 2 : placedKind.capacity());
        for (int i = 0; i < shown; i++) {
            int index = i;
            layout.slot(CAUGHT_SLOTS[CAUGHT_SLOTS.length - shown + i], (_, c) -> caught((SkyBlockPlayer) c.player(), index));
        }

        layout.slot(COLLECT_SLOT, (_, c) -> collect((SkyBlockPlayer) c.player()), (_, c) -> {
            PestTraps.collectCaught((SkyBlockPlayer) c.player(), trapId);
            c.session(DefaultState.class).refresh();
        });

        layout.slot(PICK_UP_SLOT, (_, _) -> ItemStacks.item(Material.BEDROCK, Text.of("<a>Pickup Trap"), List.of(
                        Text.of("<e>Click to pickup!"))),
                (_, c) -> {
                    PestTraps.pickUpTrap((SkyBlockPlayer) c.player(), trapId);
                    c.player().closeInventory();
                });
    }

    private net.minestom.server.item.ItemStack.Builder bait(SkyBlockPlayer player) {
        PestData.Trap trap = PestTraps.find(player, trapId);
        if (trap == null) return ItemStacks.item(Material.BARRIER, Text.of("<c>Trap missing"), List.of());
        boolean hasBait = trap.getBait() > 0 && !trap.getMaterial().isEmpty();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Lure <2>ൠ Pests <7>into this trap by"));
        lore.add(Text.of("<7>leaving bait items into this slot. <8>1 bait"));
        lore.add(Text.of("<8>will be consumed when a pest is"));
        lore.add(Text.of("<8>caught."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Bait: " + (hasBait ? "<a>" + trap.getBait() + "x " + PestItems.materialName(trap.getMaterial()) : "<c>None")
                + " <8>(max " + PestTraps.BAIT_LIMIT + ")"));
        if (hasBait && trap.getNextAt() > 0) {
            long seconds = Math.max(0, (trap.getNextAt() - System.currentTimeMillis()) / 1000);
            lore.add(Text.of("<7>Next catch in: <a>{}m {}s", seconds / 60, seconds % 60));
        }
        lore.add(Text.empty());
        String addable = PestTraps.baitInInventory(player, trap);
        lore.add(addable != null ? Text.of("<e>Click to add " + PestItems.materialName(addable) + " <e>from your inventory!")
                : Text.of("<c>You have no bait to add!"));
        if (hasBait) lore.add(Text.of("<e>Right-click to remove the bait!"));
        return ItemStacks.item(hasBait ? Material.HONEYCOMB : Material.ORANGE_STAINED_GLASS_PANE, Text.of("<6>Trap Bait"), lore);
    }

    private net.minestom.server.item.ItemStack.Builder caught(SkyBlockPlayer player, int index) {
        PestData.Trap trap = PestTraps.find(player, trapId);
        PestTraps.Kind kind = trap == null ? null : PestTraps.KINDS.get(trap.getKind());
        int capacity = kind == null ? 0 : kind.capacity();
        if (trap == null || index >= capacity) {
            return ItemStacks.item(Material.IRON_BARS, Text.of("<8>No Slot"), List.of());
        }
        if (index >= trap.getCaught().size()) {
            return ItemStacks.item(Material.IRON_BARS, Text.of("<a>Pest Slot"), List.of(
                    Text.of("<7>When a curious <2>ൠ Pest <7>wanders into"),
                    Text.of("<7>this trap they'll appear here, waiting"),
                    Text.of("<7>to be set free.")));
        }
        PestCatalogue.Pest pest = PestCatalogue.get().pest(trap.getCaught().get(index));
        if (pest == null) return ItemStacks.item(Material.BARRIER, Text.of("<c>Unknown Pest"), List.of());
        return ItemStacks.head(pest.texture(), Text.of("<a>" + pest.name()), List.of(
                Text.of("<7>Caught in this Trap."),
                Text.empty(),
                Text.of("<e>Click Release to let it out!")));
    }

    private net.minestom.server.item.ItemStack.Builder collect(SkyBlockPlayer player) {
        PestData.Trap trap = PestTraps.find(player, trapId);
        PestTraps.Kind kind = trap == null ? null : PestTraps.KINDS.get(trap.getKind());
        int count = trap == null ? 0 : trap.getCaught().size();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Click this handy button to release all"));
        lore.add(Text.of("<7>caught pests from this trap!"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Current Capacity: <a>{}<7>/{}", count, kind == null ? 0 : kind.capacity()));
        lore.add(Text.empty());
        lore.add(count > 0 ? Text.of("<e>Click to release all pests!") : Text.of("<c>There are no <2>ൠ Pests <c>to release!"));
        return ItemStacks.item(Material.RED_TERRACOTTA, Text.of("<a>Release All Pests"), lore);
    }
}
