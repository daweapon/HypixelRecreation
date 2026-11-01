package dev.daweapon.type.dwarvenmines.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.fossil.FossilService;
import dev.daweapon.type.skyblockgeneric.fossil.FossilType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Dr. Stone's fossil donations: each unique fossil pays out once and unlocks its pet's Forge recipe.
public class GUIDrStone extends StatelessView {
    private static final int[] SLOTS = {20, 21, 22, 23, 24, 30, 31, 32};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Research Center", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(50, ItemStacks.item(Material.BOOK, """
                <6>Fossil Guide
                <7>Not quite sure how to find fossils?
                <7>This handy guide is just what you
                <7>need!

                <e>Click to view!"""), (_, c) -> c.player().openView(new GUIFossilGuide()));
        FossilType[] fossils = FossilType.values();
        for (int i = 0; i < fossils.length; i++) {
            FossilType fossil = fossils[i];
            layout.slot(SLOTS[i], (_, c) -> fossilItem((SkyBlockPlayer) c.player(), fossil), (_, c) -> {
                donate((SkyBlockPlayer) c.player(), fossil);
                c.session(DefaultState.class).refresh();
            });
        }
    }

    private static ItemStack.Builder fossilItem(SkyBlockPlayer player, FossilType fossil) {
        boolean donated = FossilService.hasDonated(player, fossil);
        if (!donated) {
            return ItemStacks.item(Material.GRAY_DYE, """
                    <c>Unknown Fossil
                    <7>Donating a Fossil will unlock its
                    <7>information.

                    <e>Click a Fossil in your inventory to
                    <e>donate it!""");
        }
        StringBuilder lore = new StringBuilder("\n<7>Rewards:\n<8> - <3>+" + String.format("%,d", FossilService.DONATION_MINING_XP) + " Mining XP"
                + "\n<8> - <b>+" + String.format("%,d", FossilService.DONATION_POWDER) + " Glacite Powder"
                + "\n<8> - <d>+" + FossilService.DONATION_ESSENCE + " Fossil Essence"
                + "\n<8> - <5>+" + String.format("%,d", FossilService.DONATION_HOTM_XP) + " Heart of the Mountain XP"
                + "\n<8> - <b>+" + FossilService.DONATION_SKYBLOCK_XP + " SkyBlock XP");
        if (fossil.unlocksPetRecipe()) lore.append("\n<8> - <6>Unlocks the ").append(fossil.getPetRecipe().split(";")[0].charAt(0))
                .append(fossil.getPetRecipe().split(";")[0].substring(1).toLowerCase()).append(" Forge recipe");
        lore.append("\n\n");
        lore.append("<a>Donated!");
        return ItemStacks.appendLore(PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(fossil.getItem()).getItemStack()), lore.toString());
    }

    private static void donate(SkyBlockPlayer player, FossilType fossil) {
        switch (FossilService.donate(player, fossil)) {
            case DONATED -> {
                player.sendMessage("<e>[NPC] <b>Dr. Stone<f>: " + fossil.getDonationLine());
                if (FossilService.allDonated(player)) {
                    player.sendMessage("<e>[NPC] <b>Dr. Stone<f>: Please, take these rewards. It's the least I can do!");
                }
                player.sendMessage("<a>You donated a <6>{}<a>!", fossil.getDisplayName());
            }
            case ALREADY_DONATED -> player.sendMessage("<c>You have already donated a {}!", fossil.getDisplayName());
            case NO_FOSSIL -> player.sendMessage("<c>You don't have a {}!", fossil.getDisplayName());
        }
    }
}
