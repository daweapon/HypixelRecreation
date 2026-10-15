package dev.daweapon.type.skyblockgeneric.garden.farm;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public final class GardenPresetCost {
    public static final int ENCHANTED_VALUE = 160;
    public static final int COMPACTED_VALUE = ENCHANTED_VALUE * ENCHANTED_VALUE;

    public record Source(ItemType item, int value) {
    }

    public record Payment(Map<ItemType, Integer> take, Map<ItemType, Integer> refund) {
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
            case WHEAT -> {
                add(sources, "ENCHANTED_SEEDS", ENCHANTED_VALUE);
                add(sources, "BOX_OF_SEEDS", COMPACTED_VALUE);
            }
            case PUMPKIN -> {
                add(sources, "PUMPKIN", 1);
                add(sources, "ENCHANTED_PUMPKIN", ENCHANTED_VALUE);
                add(sources, "POLISHED_PUMPKIN", COMPACTED_VALUE);
            }
            case MELON -> {
                add(sources, "MELON_SLICE", 1);
                add(sources, "ENCHANTED_MELON", ENCHANTED_VALUE);
            }
            case POTATO -> {
                add(sources, "ENCHANTED_POTATO", ENCHANTED_VALUE);
                add(sources, "ENCHANTED_BAKED_POTATO", COMPACTED_VALUE);
            }
            case SUGAR_CANE -> {
                add(sources, "ENCHANTED_SUGAR", ENCHANTED_VALUE);
                add(sources, "ENCHANTED_SUGAR_CANE", COMPACTED_VALUE);
            }
            case NETHER_WART -> {
                add(sources, "ENCHANTED_NETHER_WART", ENCHANTED_VALUE);
                add(sources, "MUTANT_NETHER_WART", COMPACTED_VALUE);
            }
            case SUNFLOWER, MOONFLOWER, WILD_ROSE -> {
                add(sources, "ENCHANTED_" + crop.name(), ENCHANTED_VALUE);
                add(sources, "COMPACTED_" + crop.name(), COMPACTED_VALUE);
            }
            default -> add(sources, "ENCHANTED_" + seed(crop).name(), ENCHANTED_VALUE);
        }
        return sources;
    }

    private static void add(List<Source> sources, String name, int value) {
        ItemType item = ItemType.get(name);
        if (item != null) sources.add(new Source(item, value));
    }

    public static long equivalent(List<Source> sources, ToIntFunction<ItemType> available) {
        long total = 0;
        for (Source source : sources) {
            total += (long) available.applyAsInt(source.item()) * source.value();
        }
        return total;
    }

    public static @Nullable Payment plan(int required, List<Source> sources, ToIntFunction<ItemType> available) {
        Map<ItemType, Integer> take = new LinkedHashMap<>();
        int remaining = required;
        int overshoot = 0;
        for (Source source : sources) {
            if (remaining <= 0) break;
            int have = available.applyAsInt(source.item());
            if (have <= 0) continue;
            int needed = (remaining + source.value() - 1) / source.value();
            int used = Math.min(have, needed);
            take.put(source.item(), used);
            int covered = used * source.value();
            if (covered > remaining) overshoot = covered - remaining;
            remaining -= covered;
        }
        if (remaining > 0) return null;
        return new Payment(take, change(overshoot, sources));
    }

    private static Map<ItemType, Integer> change(int overshoot, List<Source> sources) {
        Map<ItemType, Integer> refund = new LinkedHashMap<>();
        for (int i = sources.size() - 1; i >= 0 && overshoot > 0; i--) {
            Source source = sources.get(i);
            int count = overshoot / source.value();
            if (count > 0) {
                refund.put(source.item(), count);
                overshoot -= count * source.value();
            }
        }
        return refund;
    }
}
