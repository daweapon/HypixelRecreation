package dev.daweapon.type.hub.gui.elizabeth;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.gui.inventory.item.GUIItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.elections.FameRank;
import dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityUpgradeCatalog;
import dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityUpgradeData;
import dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityUpgradeId;
import dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityUpgrades;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIAccountAndProfileUpgrades extends HypixelInventoryGUI {

    public GUIAccountAndProfileUpgrades() {
        super("Community Shop", InventoryType.CHEST_6_ROW);
    }

    private final int[] categoriesItemsSlots = {
            10, 11, 12, 13, 14, 16
    };
    private final int[] tabSlots = {
            1, 2, 3, 4, 5, 7
    };
    public enum ShopCategorys {
        CITY_PROJECTS(new GUICityProjects(), Material.GOLDEN_HORSE_ARMOR, false, """
                <a>City Projects
                <7>Participate with the whole SkyBlock
                <7>community to upgrade the village
                <7>and more.

                <b>Contribute <7>to various projects to
                <7>obtain unique perks!

                """),
        ACCOUNT_AND_PROFILE_UPGRADES(new GUIAccountAndProfileUpgrades(), Material.HOPPER, false, """
                <d>Account & Profile Upgrades
                <7>Upgrade your current profile and your
                <7>SkyBlock account with permanent
                <7>upgrades.

                <7>Profile: {profile}
                <7>Account: {account}

                """),
        BOOSTER_COOKIE(new GUIBuyBoosterCookies(), Material.COOKIE, true, """
                <6>Booster Cookie
                <7>Obtain a temporary buff letting
                <7>you earn <b>bits</b>, as well as <d>tons of
                <d>perks<7>.

                """),
        BITS_SHOP(new GUIBitsShop(), Material.DIAMOND, true, """
                <b>Bits Shop
                <7>Spend <b>bits </b>on a variety of
                <7>powerful items.

                <7>Earn bits from <6>Booster Cookies</6>.

                """),
        FIRE_SALES(new GUIFIRESales(), Material.BLAZE_POWDER, true, """
                <6>♨ <c><l>FIRE </l>Sales <6>♨
                <7>Acquire <6>exclusive <7>cosmetics which are
                <7>only available in <c>limited quantity
                <7>across all of SkyBlock.

                <6><l>UPCOMING SALE</l>
                <c><l>0 </l><7>Fire Sales are starting soon.

                """),
        HYPIXEL_RANKS(new GUIHypixelRanks(), Material.EMERALD, true, """
                <e>Hypixel Ranks
                <7>Browse the SkyBlock perks of our
                <e>server-wide <7>ranks such as the
                <6>[MVP<2>++<6>] <7>rank.

                """),
        ;
        public final HypixelInventoryGUI gui;
        private final Material material;
        private final boolean enchanted;
        private final String textBlock;

        ShopCategorys(HypixelInventoryGUI gui, Material material, boolean enchanted, String textBlock) {
            this.gui = gui;
            this.material = material;
            this.enchanted = enchanted;
            this.textBlock = textBlock;
        }

        public ItemStack.Builder buildStack(boolean selected, HypixelPlayer player) {
            String text = textBlock;
            if (this == ACCOUNT_AND_PROFILE_UPGRADES) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                text = text.replace("{profile}", status(skyBlockPlayer, CommunityUpgradeId.Scope.PROFILE, "<8>Nothing going on!"))
                        .replace("{account}", status(skyBlockPlayer, CommunityUpgradeId.Scope.ACCOUNT, "<8>None underway!"));
            }
            ItemStack.Builder builder = ItemStacks.item(material,
                    text + (selected ? "<a>Currently selected!" : "<e>Click to view!"));
            return enchanted ? ItemStacks.enchanted(builder) : builder;
        }
    }

    private static String status(SkyBlockPlayer player, CommunityUpgradeId.Scope scope, String idle) {
        CommunityUpgradeData.Active active = CommunityUpgrades.active(player, scope);
        if (active == null) return idle;
        return "<e>" + active.id().getDisplayName() + " " + StringUtility.getAsRomanNumeral(active.targetTier());
    }

    private static final int[] PROFILE_SLOTS = {19, 20, 21, 22, 23};
    private static final int[] ACCOUNT_SLOTS = {28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40};

    private void placeUpgrades() {
        place(CommunityUpgradeId.Scope.PROFILE, PROFILE_SLOTS);
        place(CommunityUpgradeId.Scope.ACCOUNT, ACCOUNT_SLOTS);
    }

    private void place(CommunityUpgradeId.Scope scope, int[] slots) {
        int index = 0;
        for (CommunityUpgradeId id : CommunityUpgradeId.values()) {
            if (id.getScope() != scope) continue;
            set(new GUIClickableItem(slots[index++]) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    CommunityUpgradeData.Active active = CommunityUpgrades.active(player, scope);
                    boolean running = active != null && active.id() == id;
                    CommunityUpgrades.Result result = running && e.getClick() instanceof net.minestom.server.inventory.click.Click.LeftShift
                            ? CommunityUpgrades.skip(player, scope)
                            : running ? CommunityUpgrades.Result.BUSY : CommunityUpgrades.start(player, id);
                    announce(player, id, result);
                    new GUIAccountAndProfileUpgrades().open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return upgradeStack((SkyBlockPlayer) p, id, scope);
                }
            });
        }
    }

    private static void announce(SkyBlockPlayer player, CommunityUpgradeId id, CommunityUpgrades.Result result) {
        switch (result) {
            case STARTED -> player.sendMessage("<a>You started the {} upgrade!", id.getDisplayName());
            case FINISHED -> player.sendMessage("<a>{} upgraded!", id.getDisplayName());
            case SKIPPED -> player.sendMessage("<a>You skipped the wait!");
            case NO_GEMS -> player.sendMessage("<c>You don't have enough Gems!");
            case BUSY -> player.sendMessage("<c>You already have a {} upgrade underway!",
                    id.getScope() == CommunityUpgradeId.Scope.PROFILE ? "Profile" : "Account");
            case MAXED -> player.sendMessage("<c>This upgrade is already maxed!");
            case UNAVAILABLE -> player.sendMessage("<c>This upgrade is not available yet.");
            default -> {
            }
        }
    }

    private static ItemStack.Builder upgradeStack(SkyBlockPlayer player, CommunityUpgradeId id, CommunityUpgradeId.Scope scope) {
        int tier = CommunityUpgrades.tier(player, id);
        int max = CommunityUpgradeCatalog.maxTier(id);
        boolean implemented = id.isImplemented();
        List<String> lore = new ArrayList<>();
        lore.add("<8>" + (scope == CommunityUpgradeId.Scope.PROFILE ? "Profile Upgrade" : "Account Upgrade"));
        lore.add("");
        lore.add("<7>" + id.getDescription());
        lore.add("");
        lore.add("<7>Tier: <a>" + tier + "<7>/<a>" + max);
        if (tier > 0) lore.add("<7>Current: <a>" + id.formatValue(CommunityUpgrades.valueAt(id, tier)));
        CommunityUpgradeCatalog.Tier next = CommunityUpgrades.nextTier(player, id);
        CommunityUpgradeData.Active active = CommunityUpgrades.active(player, scope);
        boolean running = active != null && active.id() == id;
        if (next == null) {
            lore.add("");
            lore.add("<a>Maxed out!");
        } else {
            lore.add("<7>Next: <a>" + id.formatValue(next.value()));
            lore.add("");
            lore.add("<7>Cost: " + (next.gems() == 0 ? "<a>Free" : "<a>" + StringUtility.commaify(next.gems()) + " Gems"));
            lore.add("<7>Time: <a>" + (next.hours() == 0 ? "Instant" : formatHours(next.hours())));
            lore.add("");
            if (!implemented) {
                lore.add("<c>Not available yet.");
            } else if (running) {
                long remaining = Math.max(0, active.completesAt() - System.currentTimeMillis());
                lore.add("<7>Underway: <e>" + formatHours((long) Math.ceil(remaining / 3_600_000D)) + " left");
                lore.add("<e>Shift-click to skip for <a>" + CommunityUpgrades.skipCost(active) + " Gems<e>.");
            } else if (active != null) {
                lore.add("<c>Another " + (scope == CommunityUpgradeId.Scope.PROFILE ? "Profile" : "Account") + " upgrade is underway.");
            } else {
                lore.add("<e>Click to start!");
            }
        }
        ItemStack.Builder builder = ItemStacks.item(id.getIcon(), "<a>" + id.getDisplayName() + (tier > 0 ? " " + StringUtility.getAsRomanNumeral(tier) : "")
                + "\n" + String.join("\n", lore));
        return running || next == null ? ItemStacks.enchanted(builder) : builder;
    }

    private static String formatHours(long hours) {
        if (hours >= 24 && hours % 24 == 0) return hours / 24 + (hours == 24 ? " day" : " days");
        if (hours >= 24) return hours / 24 + "d " + hours % 24 + "h";
        return hours + (hours == 1 ? " hour" : " hours");
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        CommunityUpgrades.finishDue((SkyBlockPlayer) getPlayer());
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));

        ShopCategorys[] allShopCategorys = ShopCategorys.values();
        int index = 0;
        for (int slot : tabSlots) {
            ShopCategorys shopCategorys = allShopCategorys[index];
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    if (slot != 2) {
                        shopCategorys.gui.open(p);
                    }
                }
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return shopCategorys.buildStack(slot == 2, p);
                }
            });
            index++;
        }

        for (int slot : categoriesItemsSlots) {
            set(new GUIItem(slot) {
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(slot != 11
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
                        <8>Rank up by spending gems & bits!
                        <e>Click to get link!""", player.getGems(), player.getBits(), FameRank.forFame(player.getFame()).getDisplayName()));
            }
        });
        placeUpgrades();
        updateItemStacks(getInventory(), getPlayer());
    }
    @Override
    public boolean allowHotkeying() {
        return true;
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
