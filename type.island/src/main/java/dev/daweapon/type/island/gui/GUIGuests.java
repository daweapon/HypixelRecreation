package dev.daweapon.type.island.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings.GUIIslandSettingsGuests;
import dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings.GUIIslandSettingsIgnored;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

import java.util.UUID;

// Jerry's guide GUI explaining Island guesting and visiting rules.
public class GUIGuests extends HypixelInventoryGUI {
    public GUIGuests() {
        super ("Jerry's Guide to Guesting", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(FILLER_ITEM);

        set(new GUIClickableItem(31) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIJerry().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ARROW, """
                        <a>Go Back
                        <7>To Jerry the Assistant""");
            }
        });

        set(new GUIClickableItem(10) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {}

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.FEATHER, """
                        <a>Visit player islands
                        <7>You can get Guest on other islands
                        <7>using <a>/visit \\<player>

                        <7>Guests <c>Can't interact with the
                        <7>world, but it's always fun to see
                        <7>what others are up to!""");
            }
        });

        set(new GUIClickableItem(12) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                player.sendMessage("<e>Visit our web store: <6>https://store.hypixel.net");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                UUID profileId = player.getProfiles().getCurrentlySelected();
                int limit = VisitManager.maxGuestLimit(profileId, player.getUuid());

                return ItemStacks.item(Material.SHORT_GRASS, """
                        <a>Guests limit
                        <7>You can only host a limited
                        <7>number of <a>guests <7>on your
                        <7>island concurrently.

                        <7>The limit depends on your rank:
                        <7>- <c>[<f>YOUTUBE<c>] <f>= <a>15
                        <7>- <6>[MVP<c>++<6>] <f>= <a>7
                        <7>- <b>[MVP/MVP+] <f>= <a>5
                        <7>- <a>[VIP/VIP+] <f>= <a>3
                        <7>- Default <f>= <a>1

                        <7>Limit on the island: <a>%d guests

                        <b>Co-op profile use the partner
                        <b>with the highest rank!

                        <e>purchase rank at store.hypixel.net!""".formatted(limit));
            }
        });

        set(new GUIClickableItem(14) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.openView(new GUIIslandSettingsIgnored());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.OAK_FENCE, """
                        <a>Access Permissions
                        <7>View and manage which players
                        <7>are blocked from guesting on
                        <7>your island.

                        <7>Use <c>/islandprivacy \\<mode>
                        <7>to set public/friends/private.

                        <e>Click to open the Ignored Players menu!""");
            }
        });

        set(new GUIClickableItem(16) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.openView(new GUIIslandSettingsGuests());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.REPEATER, """
                        <a>Moderation
                        <7>View everyone currently visiting
                        <7>your island and kick them
                        <7>individually or all at once.

                        <7>Alternatively, use the <c>/sbkick &
                        <a>/sbkickall commands or <a>clicking
                        <a>on them<7>.

                        <e>Click to open the Guest Management menu!""");
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
