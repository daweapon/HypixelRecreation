package dev.daweapon.type.skyblockgeneric.gui.inventories.museum;

import net.kyori.adventure.text.Component;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.TrackedItem;
import dev.daweapon.commons.protocol.objects.itemtracker.TrackedItemRetrieveProtocol;
import dev.daweapon.commons.text.Text;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.generic.gui.inventory.HypixelPaginatedGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.gui.inventory.item.GUIItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.utility.PaginationList;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.museum.MuseumDisplays;
import dev.daweapon.type.skyblockgeneric.museum.MuseumEntry;
import dev.daweapon.type.skyblockgeneric.museum.MuseumService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.ItemPriceCalculator;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// GUI for donating items to fill an empty museum display slot
public class GUIMuseumEmptyDisplay extends HypixelPaginatedGUI<Object> {
    private final MuseumDisplays display;
    private final int position;

    public GUIMuseumEmptyDisplay(MuseumDisplays display, int position) {
        super(InventoryType.CHEST_6_ROW);

        this.display = display;
        this.position = position;
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public int[] getPaginatedSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    @Override
    public PaginationList<Object> fillPaged(HypixelPlayer p, PaginationList<Object> paged) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        DatapointMuseum.MuseumData data = player.getMuseumData();

        Map<MuseumEntry, List<SkyBlockItem>> byEntry = new LinkedHashMap<>();
        for (SkyBlockItem item : data.getNotInDisplay()) {
            MuseumService.entryFor(data, item).ifPresent(entry -> byEntry.computeIfAbsent(entry, _ -> new ArrayList<>()).add(item));
        }
        byEntry.forEach((entry, items) -> {
            if (entry.isSet() && display.isAcceptsSets() && items.size() == entry.pieces().size()) {
                paged.add(Map.entry(entry, items));
            } else if (!entry.isSet()) {
                paged.addAll(items);
            }
        });

        return paged;
    }

    @Override
    public boolean shouldFilterFromSearch(String query, Object item) {
        if (item instanceof SkyBlockItem skyBlockItem) {
            return skyBlockItem.getDisplayName().toLowerCase().contains(query.toLowerCase());
        } else if (item instanceof Map.Entry<?, ?> entry && entry.getKey() instanceof MuseumEntry museumEntry) {
            return museumEntry.name().toLowerCase().contains(query.toLowerCase());
        }
        return false;
    }

    @Override
    public void performSearch(HypixelPlayer player, String query, int page, int maxPage) {
        if (!new ProxyService(ServiceType.ITEM_TRACKER).isOnline().join()) {
            player.sendMessage(Text.key("gui_museum.empty_display.item_tracker_offline"));
            player.closeInventory();
            return;
        }

        border(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getCloseItem(49));
        set(createSearchItem(this, 48, query));

        if (page > 1) {
            set(createNavigationButton(this, 45, query, page, false));
        }
        if (page < maxPage) {
            set(createNavigationButton(this, 53, query, page, true));
        }

        if (maxPage == 0) {
            // GUI is empty
            set(new GUIItem(22) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.BARRIER, 1,
                            Text.key("gui_museum.empty_display.no_items", display.toString(), position + 1),
                            Text.keyLines("gui_museum.empty_display.no_items.lore"));
                }
            });
        }
    }

    @Override
    public Text getTitleText(HypixelPlayer player, String query, int page, PaginationList<Object> paged) {
        return Text.key("gui_museum.empty_display.title", display.toString(), position + 1, page, paged.getPageCount());
    }

    @Override
    @SuppressWarnings("unchecked")
    public GUIClickableItem createItemFor(Object item, int slot, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        if (item instanceof SkyBlockItem skyBlockItem) {
            return createIndividualItemDisplay(skyBlockItem, slot, player);
        } else if (item instanceof Map.Entry<?, ?> entry && entry.getKey() instanceof MuseumEntry) {
            return createArmorSetDisplay((Map.Entry<MuseumEntry, List<SkyBlockItem>>) entry, slot, player);
        }

        // Fallback
        Logger.error("Unknown item type: " + item.getClass().getName());
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BARRIER, 1, Text.key("gui_museum.empty_display.error"),
                        List.of(Text.key("gui_museum.empty_display.error.lore")));
            }
        };
    }

    private GUIClickableItem createIndividualItemDisplay(SkyBlockItem item, int slot, SkyBlockPlayer player) {
        TrackedItemRetrieveProtocol.TrackedItemRetrieveMessage message = new TrackedItemRetrieveProtocol.TrackedItemRetrieveMessage(
                UUID.fromString(item.getAttributeHandler().getUniqueTrackedID())
        );
        ProxyService proxyService = new ProxyService(ServiceType.ITEM_TRACKER);
        TrackedItemRetrieveProtocol.TrackedItemResponse trackedItemResponse = (TrackedItemRetrieveProtocol.TrackedItemResponse) proxyService.handleRequest(message).join();
        TrackedItem trackedItem = trackedItemResponse.trackedItem();

        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                player.sendMessage(Text.key("gui_museum.empty_display.set_display_single", display.toString(), position + 1,
                        item.getDisplayName()));
                DatapointMuseum.MuseumData data = player.getMuseumData();
                data.getDisplayHandler().addToDisplay(item, display, position);
                player.setMuseumData(data);
                MuseumDisplays.updateDisplay(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                DatapointMuseum.MuseumData data = player.getMuseumData();
                ItemStack.Builder stack = new NonPlayerItemUpdater(item).getUpdatedItem();
                List<Text> lore = new ArrayList<>();

                lore.add(Text.of("<8><m>---------------------"));
                lore.add(Text.key("gui_museum.empty_display.item_created_label"));
                lore.add(Text.of("<a>{}", StringUtility.formatAsDate(trackedItem.getCreated())));
                lore.add(Text.of("<6>  {} <7>created", StringUtility.commaifyAndTh(trackedItem.getNumberMade())));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_museum.empty_display.item_clean_value_label"));
                lore.add(Text.of("<6>{:,} Coins", new ItemPriceCalculator(item).calculateCleanPrice()));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_museum.empty_display.item_value_label"));
                UUID itemUuid = UUID.fromString(item.getAttributeHandler().getUniqueTrackedID());
                if (data.getCalculatedPrices().containsKey(itemUuid)) {
                    lore.add(Text.of("<6>{:,} Coins", data.getCalculatedPrices().get(itemUuid)));
                } else {
                    lore.add(Text.key("gui_museum.category.uncalculated"));
                }
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_museum.empty_display.click_to_display"));

                return ItemStacks.appendLore(stack, lore);
            }
        };
    }

    private GUIClickableItem createArmorSetDisplay(Map.Entry<MuseumEntry, List<SkyBlockItem>> armorSetEntry, int slot, SkyBlockPlayer player) {
        MuseumEntry armorSet = armorSetEntry.getKey();
        List<SkyBlockItem> armorPieces = armorSetEntry.getValue();
        SkyBlockItem displayItem = armorPieces.getFirst();

        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                player.sendMessage(Text.key("gui_museum.empty_display.set_display_armor", display.toString(), position + 1,
                        armorSet.name()));
                DatapointMuseum.MuseumData data = player.getMuseumData();

                // Add all armor pieces to the same display slot
                data.getDisplayHandler().addToDisplay(armorPieces, display, position);
                player.setMuseumData(data);
                MuseumDisplays.updateDisplay(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();

                lore.add(Text.key("gui_museum.empty_display.armor_set_lore_prefix"));
                for (SkyBlockItem piece : armorPieces) {
                    lore.add(Text.of("<8>• {}", piece.getDisplayName()));
                }

                lore.add(Text.of("<8><m>---------------------"));

                // Calculate total value
                int totalCleanValue = armorPieces.stream()
                        .mapToInt(piece -> new ItemPriceCalculator(piece).calculateCleanPrice().intValue())
                        .sum();

                lore.add(Text.key("gui_museum.empty_display.set_clean_value_label"));
                lore.add(Text.of("<6>{:,} Coins", totalCleanValue));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_museum.empty_display.click_to_display_armor"));

                return ItemStacks.item(displayItem.getMaterial(), 1,
                        Text.of("<a>{}", armorSet.name()), lore);
            }
        };
    }
}
