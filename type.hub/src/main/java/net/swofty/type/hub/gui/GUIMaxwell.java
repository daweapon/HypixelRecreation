package net.swofty.type.hub.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.swofty.type.skyblockgeneric.accessory.OwnedAccessory;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUIAccessoryBag;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GUIMaxwell extends HypixelInventoryGUI {

    public GUIMaxwell() {
        super("Accessory Bag Thaumaturgy", InventoryType.CHEST_6_ROW);
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIClickableItem(47) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.openView(new GUIAccessoryBag());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.head("961a918c0c49ba8d053e522cb91abc74689367b4d8aa06bfc1ba9154730985ff", """
                        <a>Accessory Bag Shortcut
                        <7>Quickly access your accessory bag
                        <7>from right here!

                        <e>Click to open!""");
            }
        });
        set(new GUIItem(48) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                Map<Rarity, Integer> counts = new EnumMap<>(Rarity.class);
                for (OwnedAccessory owned : AccessoryOwnership.active(player)) {
                    if (!owned.inBag() || !owned.definition().getRealm().countsForPower()) continue;
                    counts.merge(owned.item().getAttributeHandler().getEffectiveRarity(), 1, Integer::sum);
                }
                int fromRarity = 0;
                StringBuilder lines = new StringBuilder();
                for (Rarity rarity : BREAKDOWN) {
                    int count = counts.getOrDefault(rarity, 0);
                    int power = rarity.getAccessoryPower();
                    fromRarity += count * power;
                    lines.append("<6>").append(power).append(" AP <7>x <").append(colourCode(rarity)).append(">")
                            .append(count).append(" Accs. <7>= <6>").append(count * power).append(" AP\n");
                }
                int total = player.getAccessoryPower();
                return ItemStacks.item(Material.FILLED_MAP, "<a>Accessories Breakdown\n<8>From your bag\n\n"
                        + lines + "<6>+" + (total - fromRarity) + " AP <7>from special bonuses\n\n"
                        + "<7>Total: <6>" + StringUtility.commaify(total) + " Accessory Power");
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }

    private static final List<Rarity> BREAKDOWN = List.of(Rarity.DIVINE, Rarity.MYTHIC, Rarity.LEGENDARY, Rarity.EPIC,
            Rarity.RARE, Rarity.UNCOMMON, Rarity.COMMON, Rarity.SPECIAL, Rarity.VERY_SPECIAL);

    private static String colourCode(Rarity rarity) {
        return switch (rarity) {
            case DIVINE -> "b";
            case MYTHIC -> "d";
            case LEGENDARY -> "6";
            case EPIC -> "5";
            case RARE -> "9";
            case UNCOMMON -> "a";
            case SPECIAL, VERY_SPECIAL -> "c";
            default -> "f";
        };
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
        e.setCancelled(true);
    }
}
