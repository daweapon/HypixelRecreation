package dev.daweapon.type.island.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings.GUIIslandBiome;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.portal.HubPortalRules;

// Main menu GUI for Jerry the Assistant on the Island: patch notes, deliveries, guesting, and moving Jerry.
public class GUIJerry extends HypixelInventoryGUI {
    public GUIJerry() {
        super("Jerry The Assistant", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(FILLER_ITEM);
        set(GUIClickableItem.getCloseItem(31));

        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIPatchNotes().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BOOK, """
                        <a>Patch Notes
                        <7>View the latest features and
                        <7>changes to the game.

                        <e>Click to open!""");
            }
        });

        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.sendMessage("<c>No new deliveries.");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ENDER_CHEST, """
                        <a>Deliveries
                        <7>Any items that may be delivered to
                        <7>yourself or your island will appear
                        <7>here for collection!

                        <e>Click to check!""");
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIGuests().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.EMERALD, """
                        <a>Visits and Guesting
                        <7>Learn all about how to <a>/visit
                        <7>players across the SkyBlock universe!

                        <e>Click to learn!""");
            }
        });

        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                ((SkyBlockPlayer) p).openView(new GUIIslandBiome());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.GRASS_BLOCK, """
                        <a>Change Island Biome
                        <7>Change the biome of your whole
                        <7>Private Island.

                        <e>Click to choose!""");
            }
        });

        set(new GUIClickableItem(20) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                HubPortalRules.removeFromJerry(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ENDER_PEARL, """
                        <a>Remove Hub Portal
                        <7>Remove the Portal to the Hub from
                        <7>your island.
                        <8>Needs any skill at level V.

                        <e>Click to remove!""");
            }
        });

        if (HubPortalRules.hasRemovedHubPortal((SkyBlockPlayer) getPlayer())) {
            set(new GUIClickableItem(24) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    player.closeInventory();
                    HubPortalRules.buyReplacement(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.OBSIDIAN, """
                            <a>Portal to the Hub
                            <7>Buy a new Hub Portal to place
                            <7>on your island.

                            <7>Cost: <6>%d coins

                            <e>Click to buy!""".formatted(HubPortalRules.REPLACEMENT_COST));
                }
            });
        }

        set(new GUIClickableItem(35) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                player.sendMessage("<a>I have given you an egg, place this where you would like me to move to!");

                player.addAndUpdateItem(ItemType.MOVE_JERRY);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.named(Material.BEDROCK, "<a>Move Jerry");
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {

    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {

    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }
}
