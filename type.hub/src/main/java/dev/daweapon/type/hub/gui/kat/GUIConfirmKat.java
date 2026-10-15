package dev.daweapon.type.hub.gui.kat;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.KatUpgrade;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.KatService;

public class GUIConfirmKat extends HypixelInventoryGUI {
    private final SkyBlockItem pet;
    private boolean handled = false;

    public GUIConfirmKat(SkyBlockItem pet) {
        super("Confirm Hiring Kat", InventoryType.CHEST_3_ROW);
        this.pet = pet;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));

        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (handled) return;
                if (!KatService.start(player, pet)) {
                    String problem = KatService.problem(player, pet);
                    player.sendMessage("<c>" + (problem == null ? "Kat is already taking care of a pet!" : problem));
                    return;
                }
                handled = true;
                player.sendMessage("<e>[NPC] Kat<f>: I'll take good care of your {}<f>!", pet.getDisplayName());
                player.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                KatUpgrade katUpgrade = KatService.upgradeFor(pet);
                if (katUpgrade == null) {
                    return ItemStacks.item(Material.RED_TERRACOTTA, "<c>Kat can't upgrade this pet any further!");
                }
                return ItemStacks.item(Material.GREEN_TERRACOTTA, """
                        <a>Confirm
                        <c>WARNING: You will not be able to
                        <c>retrieve your pet for {} and its
                        <c>level will change as a result of the
                        <c>rarity upgrade.""", StringUtility.formatTimeLeftWrittenOut(katUpgrade.timeMillis()));
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                returnPet((SkyBlockPlayer) p);
                p.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel");
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        returnPet((SkyBlockPlayer) e.getPlayer());
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        returnPet((SkyBlockPlayer) player);
    }

    private void returnPet(SkyBlockPlayer player) {
        if (handled) return;
        handled = true;
        player.addAndUpdateItem(pet);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
