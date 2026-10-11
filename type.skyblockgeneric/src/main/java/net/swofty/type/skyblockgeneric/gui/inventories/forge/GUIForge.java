package net.swofty.type.skyblockgeneric.gui.inventories.forge;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM.ForgeProcess;
import net.swofty.type.skyblockgeneric.forge.ForgeRecipe;
import net.swofty.type.skyblockgeneric.forge.ForgeRegistry;
import net.swofty.type.skyblockgeneric.forge.ForgeService;
import net.swofty.type.skyblockgeneric.forge.ForgeTooltips;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

public class GUIForge extends StatelessView {
    private static final int FIRST_FURNACE_SLOT = 10;
    private static final int[] LANE_ROWS = {2, 3, 4};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("The Forge", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        for (int col = 1; col <= ForgeService.MAX_SLOTS; col++) {
            int forgeSlot = col;
            for (int index = 0; index < LANE_ROWS.length; index++) {
                int lane = index;
                layout.autoUpdating(LANE_ROWS[index] * 9 + col, (_, _) -> ItemStacks.item(lanePane(player, forgeSlot, lane), " "), Duration.ofSeconds(1));
            }
        }

        for (int forgeSlot = 1; forgeSlot <= ForgeService.MAX_SLOTS; forgeSlot++) {
            int slot = forgeSlot;
            layout.autoUpdating(FIRST_FURNACE_SLOT + forgeSlot - 1, (_, _) -> furnace(player, slot), (_, c) -> {
                if (slot > ForgeService.slots(player)) return;
                ForgeProcess process = ForgeService.processIn(player, slot);
                if (process == null) {
                    c.push(new GUIForgeProcess(slot));
                    return;
                }
                if (!ForgeService.isFinished(process)) return;
                ForgeRecipe recipe = ForgeRegistry.get().recipe(process.recipeId());
                String error = ForgeService.claim(player, slot);
                player.sendMessage(error != null ? error : "<a>You collected " + ForgeTooltips.name(recipe) + "<a> from the Forge!");
                c.session(Object.class).refresh();
            }, Duration.ofSeconds(1));
        }

        layout.slot(48, ItemStacks.item(Material.CHEST, "<a>Collect Finished Items\n<7>Collect every finished forge process.\n\n<e>Click to collect!"),
                (_, c) -> {
                    int collected = 0;
                    for (int slot = 1; slot <= ForgeService.slots(player); slot++) {
                        ForgeProcess process = ForgeService.processIn(player, slot);
                        if (process != null && ForgeService.isFinished(process) && ForgeService.claim(player, slot) == null) collected++;
                    }
                    player.sendMessage(collected == 0 ? "<c>You have nothing to collect." : "<a>You collected " + collected + " item(s) from the Forge!");
                    c.session(Object.class).refresh();
                });
        Components.close(layout, 49);
        layout.slot(50, ItemStacks.item(Material.BOOK, "<a>The Forge\n<7>Forge your ores into powerful items.\n\n<7>Forge slots: <a>" + ForgeService.slots(player) + "<7>/" + ForgeService.MAX_SLOTS
                + "\n<7>Time reduction: <a>" + (int) ForgeService.reductionPercent(player) + "%\n\n<8>Forging can't be cancelled."));
    }

    private static Material lanePane(SkyBlockPlayer player, int forgeSlot, int lane) {
        ForgeProcess process = forgeSlot > ForgeService.slots(player) ? null : ForgeService.processIn(player, forgeSlot);
        if (process == null) return Material.RED_STAINED_GLASS_PANE;
        int lime = (int) Math.floor(process.progress(System.currentTimeMillis()) * LANE_ROWS.length + 1e-9);
        return lane < lime ? Material.LIME_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE;
    }

    private static ItemStack.Builder furnace(SkyBlockPlayer player, int forgeSlot) {
        if (forgeSlot > ForgeService.slots(player)) {
            return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, "<c>Locked Slot #" + forgeSlot
                    + "\n<7>Requires <5>Heart of the Mountain Tier " + Math.max(2, forgeSlot) + "<7>.");
        }
        ForgeProcess process = ForgeService.processIn(player, forgeSlot);
        if (process == null) {
            return ItemStacks.item(Material.FURNACE, "<a>Slot #" + forgeSlot
                    + "\n<7>View and start forge processes using the materials that you have.\n\n<e>Click to select a process!");
        }
        ForgeRecipe recipe = ForgeRegistry.get().recipe(process.recipeId());
        if (ForgeService.isFinished(process)) {
            return ItemStacks.appendLore(ForgeTooltips.icon(recipe), "\n<a>Slot #" + forgeSlot + "\n<a>Finished!\n\n<e>Click to collect!");
        }
        String name = ForgeTooltips.name(recipe);
        return ItemStacks.text(ForgeTooltips.icon(recipe), "<a>" + recipe.name() + "\n<7>Currently making: " + name
                + "\n<7>Time Remaining: <a>" + ForgeTooltips.duration(ForgeService.remainingMillis(process)));
    }
}
