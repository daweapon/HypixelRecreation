package net.swofty.type.hub.gui.maxwell;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.accessory.power.AccessoryPowerType;
import net.swofty.type.skyblockgeneric.accessory.power.AccessoryPowers;
import net.swofty.type.skyblockgeneric.accessory.power.PowerStones;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers.PowerData;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Learn Power From Stones: place nine identical Power Stones in the grid and combine them. */
public class GUILearnPowers extends StatelessView {
    private static final List<Integer> GRID = List.of(13, 14, 15, 22, 23, 24, 31, 32, 33);
    private static final int LEARN_SLOT = 20;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Learn Power From Stones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        layout.editableSlots(GRID);
        layout.autoUpdating(LEARN_SLOT, (s, c) -> learnButton(c), (click, c) -> learn(c), Duration.ofMillis(500));
        layout.slot(50, ItemStacks.item(Material.BOOK, """
                        <a>Power Stones Guide
                        <7>View all power stones and how to
                        <7>acquire them!

                        <e>Click to browse!"""),
                (click, c) -> c.push(new GUIPowerStonesGuide()));
    }

    private static List<SkyBlockItem> grid(ViewContext ctx) {
        List<SkyBlockItem> items = new ArrayList<>();
        for (int slot : GRID) {
            ItemStack stack = ctx.inventory().getItemStack(slot);
            items.add(stack.isAir() ? null : new SkyBlockItem(stack));
        }
        return items;
    }

    private static ItemStack.Builder learnButton(ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<SkyBlockItem> grid = grid(ctx);
        PowerStones.LearnResult result = PowerStones.check(AccessoryPowers.data(player), grid, AccessoryPowers.combatLevel(player));
        AccessoryPowerType power = PowerStones.stonePower(grid);
        String header = """
                <7>Fill all the slots on the right
                <7>with identical <9>Power Stones
                <7>to permanently unlock their
                <7><a>power<7>.

                """;
        return switch (result) {
            case LEARNED -> ItemStacks.item(Material.LIME_DYE, "<a>Learn New Power\n" + header
                    + "<7>Power: <a>" + power.getDisplayName() + "\n\n<e>Click to learn power!");
            case ALREADY_KNOWN -> ItemStacks.item(Material.GRAY_DYE, "<c>Learn New Power\n" + header
                    + "<c>You already know the <a>" + power.getDisplayName() + " <c>power!");
            case COMBAT_TOO_LOW -> ItemStacks.item(Material.GRAY_DYE, "<c>Learn New Power\n" + header
                    + "<c>Requires <a>Combat Skill " + power.getCombatLevel() + "<c>!");
            case MIXED -> ItemStacks.item(Material.GRAY_DYE, "<c>Learn New Power\n" + header + "<c>The stones must be identical!");
            case NOT_A_STONE -> ItemStacks.item(Material.GRAY_DYE, "<c>Learn New Power\n" + header + "<c>Only Power Stones can be combined!");
            case MISSING -> ItemStacks.item(Material.GRAY_DYE, "<c>Learn New Power\n" + header + "<c>Missing some stones!");
        };
    }

    private static void learn(ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PowerData data = AccessoryPowers.data(player);
        List<SkyBlockItem> grid = grid(ctx);
        if (PowerStones.check(data, grid, AccessoryPowers.combatLevel(player)) != PowerStones.LearnResult.LEARNED) return;
        AccessoryPowerType power = PowerStones.stonePower(grid);
        consume(ctx, AccessoryPowerType.STONES_TO_LEARN);
        PowerStones.learnFrom(player, data, power);
        player.sendMessage("<e>[NPC] Maxwell<f>: <7>" + player.getUsername() + "<f>, that's an excellent power!");
        player.sendMessage("<a>You learned the <e>" + power.getDisplayName() + " <a>power! <8>(+"
                + AccessoryPowers.SKYBLOCK_XP_PER_POWER + " SkyBlock XP)");
    }

    private static void consume(ViewContext ctx, int amount) {
        int left = amount;
        for (int slot : GRID) {
            if (left <= 0) break;
            ItemStack stack = ctx.inventory().getItemStack(slot);
            if (stack.isAir()) continue;
            int taken = Math.min(left, stack.amount());
            ctx.inventory().setItemStack(slot, stack.amount() - taken <= 0 ? ItemStack.AIR : stack.withAmount(stack.amount() - taken));
            left -= taken;
        }
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        return true;
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        for (int slot : GRID) {
            ItemStack stack = ctx.inventory().getItemStack(slot);
            if (stack.isAir()) continue;
            ctx.inventory().setItemStack(slot, ItemStack.AIR);
            player.addAndUpdateItem(new SkyBlockItem(stack));
        }
    }
}
