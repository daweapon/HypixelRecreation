package net.swofty.type.skyblockgeneric.drill;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MachineFuel {
    private static final Map<String, Integer> FUEL = new LinkedHashMap<>();

    static {
        FUEL.put("POPPY", 2);
        FUEL.put("DANDELION", 2);
        FUEL.put("PLANT_MATTER", 150);
        FUEL.put("RED_THORNLEAF", 200);
        FUEL.put("ENCHANTED_DANDELION", 360);
        FUEL.put("ENCHANTED_POPPY", 1_024);
        FUEL.put("GOBLIN_EGG", 2_000);
        FUEL.put("BIOFUEL", 3_000);
        FUEL.put("GREEN_GOBLIN_EGG", 5_000);
        FUEL.put("YELLOW_GOBLIN_EGG", 10_000);
        FUEL.put("VOLTA", 10_000);
        FUEL.put("OIL_BARREL", 10_000);
        FUEL.put("RED_GOBLIN_EGG", 15_000);
        FUEL.put("BLUE_GOBLIN_EGG", 20_000);
        for (String fossil : new String[]{"CLAW", "CLUBBED", "FOOTPRINT", "SPINE", "TUSK", "UGLY", "WEBBED"}) {
            FUEL.put(fossil + "_FOSSIL", 50_000);
        }
        FUEL.put("SUNFLOWER_OIL", 20_000);
        FUEL.put("COALROOT", 5_000);
    }

    private MachineFuel() {
    }

    public static int amount(@Nullable String itemId) {
        return itemId == null ? 0 : FUEL.getOrDefault(itemId, 0);
    }

    public static boolean isFuel(@Nullable String itemId) {
        return amount(itemId) > 0;
    }

    public static Map<String, Integer> all() {
        return Map.copyOf(FUEL);
    }
}
