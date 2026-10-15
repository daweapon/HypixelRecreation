package dev.daweapon.type.hub.gui.elizabeth;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.hub.gui.elizabeth.subguis.GUICityProject;
import dev.daweapon.type.hub.gui.elizabeth.subguis.GUIPreviousCityProjects;
import dev.daweapon.type.skyblockgeneric.cityprojects.CityProject;
import dev.daweapon.type.skyblockgeneric.cityprojects.CityProjectContributions;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.gui.inventory.item.GUIItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import dev.daweapon.type.generic.user.HypixelPlayer;

public class GUICityProjects extends HypixelInventoryGUI {
    public GUICityProjects() {
        super("Community Shop", InventoryType.CHEST_6_ROW);
    }

    private final int[] categoriesItemsSlots = {
            10, 11, 12, 13, 14, 16
    };
    private final int[] tabSlots = {
            1, 2, 3, 4, 5, 7
    };

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));

        GUIAccountAndProfileUpgrades.ShopCategorys[] allShopCategorys = GUIAccountAndProfileUpgrades.ShopCategorys.values();
        int index = 0;
        for (int slot : tabSlots) {
            GUIAccountAndProfileUpgrades.ShopCategorys shopCategorys = allShopCategorys[index];
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    if (slot != 1) {
                        shopCategorys.gui.open(p);
                    }
                }
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return shopCategorys.buildStack(slot == 1);
                }
            });
            index++;
        }

        for (int slot : categoriesItemsSlots) {
            set(new GUIItem(slot) {
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(slot != 10
                            ? Material.GRAY_STAINED_GLASS_PANE
                            : Material.GREEN_STAINED_GLASS_PANE, """
                            <8>▲ <7>Categories
                            <8>▼ <7>Items""");
                }
            });
        }
        set(new GUIClickableItem(49) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.openBook(Book.builder()
                        .addPage(Component.text("Purchase ranks, gems and more on our webstore!")
                                .appendNewline()
                                .appendNewline()
                                .append(Component.text("      "))
                                .append(Component.text("VISIT STORE").clickEvent(ClickEvent.openUrl("http://bit.ly/4aG54lt")).color(TextColor.fromHexString("#00AAAA"))))
                        .build()
                );
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.enchanted(ItemStacks.item(Material.EMERALD, """
                        <a>Community Shop
                        <8>Elizabeth

                        <7>Gems: <a>{:,}
                        <8>Purchase on store.hypixel.net!

                        <7>Bits: <b>{:,}
                        <8>Earn from Booster Cookies!

                        <7>Fame Rank: <e>{}
                        <8>Rank up by spending bits, gems\n<8>and contributing to projects!
                        <e>Click to get link!""", player.getGems(), player.getBits(),
                        dev.daweapon.type.skyblockgeneric.elections.FameRank.forFame(player.getFame()).getDisplayName()));
            }
        });
        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUICityProject(CityProject.CURRENT).open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                CityProject project = CityProject.CURRENT;
                return ItemStacks.item(project.getIcon(), """
                        <a>{}
                        <8>City Project

                        <7>Total contributions: <b>{}
                        <7>Stage: <b><l>FUNDING

                        <e>Click to view!""", project.getDisplayName(),
                        dev.daweapon.commons.StringUtility.commaify(CityProjectContributions.globalTotal(project)));
            }
        });
        set(new GUIClickableItem(50) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIPreviousCityProjects().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BOOKSHELF, """
                        <a>Previous Projects
                        <7>The community completed <e>7 <7>projects
                        <7>in the past.

                        <7>You contributed to <b>0 <7>of those
                        <7>projects!

                        <e>Click to view them!""");
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }
    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
