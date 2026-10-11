package net.swofty.type.dwarvenmines.gui;

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
import net.swofty.type.skyblockgeneric.fossil.FossilService;
import net.swofty.type.skyblockgeneric.fossil.FossilType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Dr. Stone's fossil donations: each unique fossil pays out once and unlocks its pet's Forge recipe.
public class GUIDrStone extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 22};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Dr. Stone", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(4, (_, c) -> header((SkyBlockPlayer) c.player()));
        FossilType[] fossils = FossilType.values();
        for (int i = 0; i < fossils.length; i++) {
            FossilType fossil = fossils[i];
            layout.slot(SLOTS[i], (_, c) -> fossilItem((SkyBlockPlayer) c.player(), fossil), (_, c) -> {
                donate((SkyBlockPlayer) c.player(), fossil);
                c.session(DefaultState.class).refresh();
            });
        }
    }

    private static ItemStack.Builder header(SkyBlockPlayer player) {
        return ItemStacks.item(Material.BONE, "<a>Fossil Donations\n<7>Donate each kind of fossil once for\n<7>rewards and a new <6>Forge <7>pet recipe.\n\n<7>Donated: <a>" + FossilService.donated(player).size() + "<7>/<a>" + FossilType.values().length
                + "\n\n<7>Donating all of them pays an extra\n<b>" + String.format("%,d", FossilService.COMPLETION_POWDER) + " Glacite Powder <7>and <5>"
                + String.format("%,d", FossilService.COMPLETION_HOTM_XP) + " Heart of the Mountain XP<7>.");
    }

    private static ItemStack.Builder fossilItem(SkyBlockPlayer player, FossilType fossil) {
        boolean donated = FossilService.hasDonated(player, fossil);
        boolean owned = player.countItem(fossil.getItem()) > 0;
        StringBuilder lore = new StringBuilder("\n<7>Rewards:\n<8> - <3>+" + String.format("%,d", FossilService.DONATION_MINING_XP) + " Mining XP"
                + "\n<8> - <b>+" + String.format("%,d", FossilService.DONATION_POWDER) + " Glacite Powder"
                + "\n<8> - <d>+" + FossilService.DONATION_ESSENCE + " Fossil Essence"
                + "\n<8> - <5>+" + String.format("%,d", FossilService.DONATION_HOTM_XP) + " Heart of the Mountain XP"
                + "\n<8> - <b>+" + FossilService.DONATION_SKYBLOCK_XP + " SkyBlock XP");
        if (fossil.unlocksPetRecipe()) lore.append("\n<8> - <6>Unlocks the ").append(fossil.getPetRecipe().split(";")[0].charAt(0))
                .append(fossil.getPetRecipe().split(";")[0].substring(1).toLowerCase()).append(" Forge recipe");
        lore.append("\n\n");
        lore.append(donated ? "<a>Donated!" : owned ? "<e>Click to donate!" : "<c>You don't have this fossil.");
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
