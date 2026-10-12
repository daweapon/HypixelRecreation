package net.swofty.type.skyblockgeneric.staff;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointQuiver;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSackOfSacks;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUIQuiver;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUISackOfSacks;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.SackComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code /maxprofile}'s bag steps: the largest sack of every sack family placed in the Sack of Sacks, and
 * every unlocked quiver slot filled with a full stack of Flint Arrows. Driven by the item registry, so new
 * sacks are covered without touching this class.
 */
public final class MaxBags {
    private static final Pattern SIZE_PREFIX = Pattern.compile("^(SMALL|MEDIUM|LARGE|BRONZE|SILVER|GOLD|DIAMOND)_");
    private static final List<String> SIZE_ORDER = List.of("SMALL", "MEDIUM", "LARGE", "BRONZE", "SILVER", "GOLD", "DIAMOND");
    private static final int ARROW_STACK = 64;

    private MaxBags() {
    }

    public static List<ItemType> sackPlan() {
        Map<String, ItemType> best = new TreeMap<>();
        Map<ItemType, Integer> capacity = new HashMap<>();
        for (String id : ConfigurableSkyBlockItem.getIDs()) {
            ConfigurableSkyBlockItem config = ConfigurableSkyBlockItem.getFromID(id);
            ItemType type = ItemType.get(id);
            if (config == null || type == null || !config.hasComponent(SackComponent.class)) continue;
            capacity.put(type, config.getComponent(SackComponent.class).getMaxCapacity());
            best.merge(family(id), type, (current, candidate) -> outranks(candidate, current, capacity) ? candidate : current);
        }
        return best.values().stream()
                .sorted(Comparator.comparingInt((ItemType type) -> capacity.get(type)).reversed().thenComparing(ItemType::name))
                .toList();
    }

    private static boolean outranks(ItemType candidate, ItemType current, Map<ItemType, Integer> capacity) {
        int byCapacity = Integer.compare(capacity.get(candidate), capacity.get(current));
        if (byCapacity != 0) return byCapacity > 0;
        return tier(candidate.name()) > tier(current.name());
    }

    private static int tier(String sackId) {
        Matcher matcher = SIZE_PREFIX.matcher(sackId);
        return matcher.find() ? SIZE_ORDER.indexOf(matcher.group(1)) : -1;
    }

    static String family(String sackId) {
        return SIZE_PREFIX.matcher(sackId).replaceFirst("");
    }

    public static void maxSacks(SkyBlockPlayer player) {
        DatapointSackOfSacks datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SACK_OF_SACKS, DatapointSackOfSacks.class);
        List<ItemType> plan = sackPlan();
        List<SkyBlockItem> sacks = new ArrayList<>(plan.stream().map(SkyBlockItem::new).toList());
        for (SkyBlockItem existing : datapoint.getValue().getAllSacks()) {
            ItemType type = existing.getAttributeHandler().getPotentialType();
            if (type == null || plan.stream().noneMatch(planned -> family(planned.name()).equals(family(type.name())))) {
                sacks.add(existing);
            }
        }

        int slots = GUISackOfSacks.getTotalSlots(player);
        List<Map.Entry<Integer, SkyBlockItem>> placed = new ArrayList<>();
        for (int slot = 0; slot < Math.min(slots, sacks.size()); slot++) {
            placed.add(Map.entry(slot, sacks.get(slot)));
        }
        datapoint.setValue(new DatapointSackOfSacks.PlayerSackOfSacks(placed));

        int left = sacks.size() - placed.size();
        if (left > 0) {
            Logger.warn("/maxprofile could not fit {} sacks into {}'s Sack of Sacks", left, player.getUsername());
            player.sendMessage("<c>" + left + " sacks didn't fit in your Sack of Sacks.");
        }
    }

    public static void maxQuiver(SkyBlockPlayer player) {
        DatapointQuiver datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.QUIVER, DatapointQuiver.class);
        Map<Integer, SkyBlockItem> quiver = new HashMap<>();
        for (int slot = 0; slot < GUIQuiver.getUnlockedSlots(player); slot++) {
            quiver.put(slot, new SkyBlockItem(ItemType.FLINT_ARROW, ARROW_STACK));
        }
        datapoint.setValue(new DatapointQuiver.PlayerQuiver(quiver));
    }
}
