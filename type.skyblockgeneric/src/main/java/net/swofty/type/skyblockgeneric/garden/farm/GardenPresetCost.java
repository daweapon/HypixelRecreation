package net.swofty.type.skyblockgeneric.garden.farm;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public final class GardenPresetCost {
    public static final int ENCHANTED_VALUE = 160;

    public record Source(ItemType item, int value) {
    }

    public record Payment(Map<ItemType, Integer> take, ItemType refundItem, int refund) {
    }

    private static final Map<GardenCrop, Integer> REQUIRED = new EnumMap<>(GardenCrop.class);

    private GardenPresetCost() {
    }

    public static synchronized int required(GardenCrop crop) {
        return REQUIRED.computeIfAbsent(crop, GardenFarmLayout::plantCount);
    }

    public static ItemType seed(GardenCrop crop) {
        return ItemType.valueOf(switch (crop) {
            case WHEAT -> "SEEDS";
            case CARROT -> "CARROT";
            case POTATO -> "POTATO";
            case PUMPKIN -> "PUMPKIN_SEEDS";
            case SUGAR_CANE -> "SUGAR_CANE";
            case MELON -> "MELON_SEEDS";
            case CACTUS -> "CACTUS";
            case COCOA_BEANS -> "COCOA_BEANS";
            case MUSHROOM -> "RED_MUSHROOM";
            case NETHER_WART -> "NETHER_WART";
            case SUNFLOWER -> "SUNFLOWER";
            case MOONFLOWER -> "MOONFLOWER";
            case WILD_ROSE -> "WILD_ROSE";
        });
    }

    public static List<Source> sources(GardenCrop crop) {
        List<Source> sources = new ArrayList<>();
        add(sources, seed(crop).name(), 1);
        switch (crop) {
            case WHEAT -> add(sources, "ENCHANTED_SEEDS", ENCHANTED_VALUE);
            case PUMPKIN -> {
                add(sources, "PUMPKIN", 1);
                add(sources, "ENCHANTED_PUMPKIN", ENCHANTED_VALUE);
            }
            case MELON -> {
                add(sources, "MELON_SLICE", 1);
                add(sources, "ENCHANTED_MELON", ENCHANTED_VALUE);
            }
            default -> add(sources, "ENCHANTED_" + seed(crop).name(), ENCHANTED_VALUE);
        }
        return sources;
    }

    private static void add(List<Source> sources, String name, int value) {
        ItemType item = ItemType.get(name);
        if (item != null) sources.add(new Source(item, value));
    }

    public static @Nullable Payment plan(int required, List<Source> sources, ToIntFunction<ItemType> available) {
        Map<ItemType, Integer> take = new LinkedHashMap<>();
        int remaining = required;
        int refund = 0;
        for (Source source : sources) {
            if (remaining <= 0) break;
            int have = available.applyAsInt(source.item());
            if (have <= 0) continue;
            int needed = (remaining + source.value() - 1) / source.value();
            int used = Math.min(have, needed);
            take.put(source.item(), used);
            int covered = used * source.value();
            if (covered > remaining) refund = covered - remaining;
            remaining -= covered;
        }
        if (remaining > 0) return null;
        return new Payment(take, sources.getFirst().item(), refund);
    }
}
