package net.swofty.type.hub.gui.kat;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.KatUpgrade;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.KatService;

import java.util.ArrayList;
import java.util.List;

public class GUIKat extends HypixelInventoryGUI {

    public GUIKat() {
        super("Pet Sitter", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(40));

        updateFromItem(null);
    }

    public void updateFromItem(SkyBlockItem item) {

        if (item == null) {
            set(new GUIClickableItem(13) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    ItemStack stack = p.getInventory().getCursorItem();

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) {
                        updateFromItem(null);
                        return;
                    }

                    SkyBlockItem item = new SkyBlockItem(stack);
                    updateFromItem(item);
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStack.builder(Material.AIR);
                }
            });
            set(new GUIClickableItem(22) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    player.sendMessage("<c>Place a pet in the empty slot for Kat to take care of!");
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStacks.item(Material.RED_TERRACOTTA, """
                            <e>Pet Sitter
                            <7>Place a pet above for Kat to take
                            <7>care of!

                            <7>After some time, your pet <9>Rarity <7>will
                            <7>be upgraded!""");
                }
            });
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        set(new GUIClickableItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack stack = e.getClickedItem();
                if (stack.isAir()) return;

                updateFromItem(null);

                player.addAndUpdateItem(stack);
            }
        });

        if (item.getAmount() > 1 || !(item.hasComponent(PetComponent.class))) {
            set(new GUIItem(22) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStacks.item(Material.BARRIER, "<c>Error!\n<c>Kat only takes care of pets!");
                }
            });
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                String problem = KatService.problem(player, item);
                if (problem != null) {
                    player.sendMessage("<c>" + problem);
                    return;
                }
                getInventory().setItemStack(13, ItemStack.AIR);
                new GUIConfirmKat(item).open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                KatUpgrade katUpgrade = KatService.upgradeFor(item);
                if (katUpgrade == null) {
                    return ItemStacks.item(Material.RED_TERRACOTTA, "<c>Kat can't upgrade this pet any further!");
                }
                Rarity next = KatService.nextRarity(item);
                String problem = KatService.problem(player, item);
                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<7>Kat will take care of your {}", item.getDisplayName()));
                lore.add(Text.of("<7>for <9>{} <7>then its <9>rarity<7> will be",
                        StringUtility.formatTimeLeftWrittenOut(katUpgrade.timeMillis())));
                lore.add(Text.of("<7>upgraded to <color:{}>{}<7>!", next.getColor(), next.name()));
                lore.add(Text.empty());
                lore.add(Text.of("<7>Cost"));
                for (KatUpgrade.ItemCost cost : katUpgrade.items()) {
                    lore.add(Text.of("<9>{} <8>x{}", cost.item().getDisplayName(), cost.amount()));
                }
                long coins = KatService.coins(item, katUpgrade);
                if (coins != 0) lore.add(Text.of("<6>{:,} Coins", coins));
                lore.add(Text.of("<8>Requires Taming {}", KatService.requiredTaming(next)));
                lore.add(Text.empty());
                lore.add(problem == null ? Text.of("<e>Click to hire Kat!") : Text.of("<c>{}", problem));
                return ItemStacks.item(problem == null ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA, 1,
                        Text.of("<a>Hire Kat"), lore);
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
        returnPet((SkyBlockPlayer) e.getPlayer(), e.getInventory());
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        returnPet((SkyBlockPlayer) player, inventory);
    }

    private static void returnPet(SkyBlockPlayer player, AbstractInventory inventory) {
        ItemStack stack = inventory.getItemStack(13);
        if (stack.isAir()) return;
        inventory.setItemStack(13, ItemStack.AIR);
        player.addAndUpdateItem(new SkyBlockItem(stack));
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }
}
