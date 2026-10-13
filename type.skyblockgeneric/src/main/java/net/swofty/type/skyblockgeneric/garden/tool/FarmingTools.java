package net.swofty.type.skyblockgeneric.garden.tool;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FarmingTools {
    public static final String LORE_HANDLER = "FARMING_TOOL";
    public static final int FORTUNE_PER_LEVEL = 4;
    public static final int MAX_LEVEL = 50;

    private static final int[] LEVEL_XP = {
            0, 1_000, 2_000, 3_000, 5_000, 8_000, 12_000, 16_000, 20_000, 25_000,
            40_000, 60_000, 80_000, 100_000, 120_000, 140_000, 160_000, 180_000, 200_000, 250_000,
            300_000, 350_000, 400_000, 450_000, 500_000, 550_000, 600_000, 650_000, 700_000, 750_000,
            800_000, 850_000, 900_000, 950_000, 1_000_000, 1_050_000, 1_100_000, 1_150_000, 1_200_000, 1_250_000,
            1_300_000, 1_500_000, 2_000_000, 2_500_000, 3_000_000, 3_500_000, 4_000_000, 4_500_000, 5_000_000, 5_500_000};
    private static final int[] MK_LEVEL_CAP = {0, 14, 29, 40};

    public record Tool(GardenCrop crop, GardenCrop alsoCrop, int mk) {
        public boolean works(GardenCrop harvested) {
            return harvested == crop || harvested == alsoCrop;
        }
    }

    private static final Map<String, GardenCrop[]> FAMILIES = Map.ofEntries(
            Map.entry("THEORETICAL_HOE_WHEAT", new GardenCrop[]{GardenCrop.WHEAT, null}),
            Map.entry("THEORETICAL_HOE_CARROT", new GardenCrop[]{GardenCrop.CARROT, null}),
            Map.entry("THEORETICAL_HOE_POTATO", new GardenCrop[]{GardenCrop.POTATO, null}),
            Map.entry("PUMPKIN_DICER", new GardenCrop[]{GardenCrop.PUMPKIN, null}),
            Map.entry("THEORETICAL_HOE_CANE", new GardenCrop[]{GardenCrop.SUGAR_CANE, null}),
            Map.entry("MELON_DICER", new GardenCrop[]{GardenCrop.MELON, null}),
            Map.entry("CACTUS_KNIFE", new GardenCrop[]{GardenCrop.CACTUS, null}),
            Map.entry("COCO_CHOPPER", new GardenCrop[]{GardenCrop.COCOA_BEANS, null}),
            Map.entry("FUNGI_CUTTER", new GardenCrop[]{GardenCrop.MUSHROOM, null}),
            Map.entry("THEORETICAL_HOE_WARTS", new GardenCrop[]{GardenCrop.NETHER_WART, null}),
            Map.entry("THEORETICAL_HOE_SUNFLOWER", new GardenCrop[]{GardenCrop.SUNFLOWER, GardenCrop.MOONFLOWER}),
            Map.entry("THEORETICAL_HOE_WILD_ROSE", new GardenCrop[]{GardenCrop.WILD_ROSE, null}));

    private FarmingTools() {
    }

    public static void registerLore() {
        LoreRegistry.register(LORE_HANDLER, new LoreConfig(FarmingTools::lore, null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    public static @Nullable Tool of(@Nullable String itemId) {
        if (itemId == null) return null;
        int mk = 1;
        String family = itemId;
        int underscore = itemId.lastIndexOf('_');
        if (underscore > 0 && itemId.substring(underscore + 1).matches("[123]")) {
            mk = Integer.parseInt(itemId.substring(underscore + 1));
            family = itemId.substring(0, underscore);
        }
        GardenCrop[] crops = FAMILIES.get(family);
        return crops == null ? null : new Tool(crops[0], crops[1], mk);
    }

    public static int level(long xp, int mk) {
        int level = 1;
        long cumulative = 0;
        for (int next = 2; next <= MAX_LEVEL; next++) {
            cumulative += LEVEL_XP[next - 1];
            if (xp < cumulative) break;
            level = next;
        }
        return Math.min(level, MK_LEVEL_CAP[Math.max(1, Math.min(3, mk))]);
    }

    public static long cumulativeXp(int level) {
        long total = 0;
        for (int i = 2; i <= Math.min(level, MAX_LEVEL); i++) total += LEVEL_XP[i - 1];
        return total;
    }

    public static double xpPerCollection(GardenCrop crop) {
        return switch (crop) {
            case WHEAT -> 2.5;
            case CARROT -> 1 / 3.5;
            case POTATO -> 1 / 3.0;
            case MELON -> 1 / 4.0;
            case SUGAR_CANE -> 1 / 2.0;
            case PUMPKIN -> 1 / 0.85;
            case CACTUS -> 1 / 1.5;
            case COCOA_BEANS -> 1 / 1.5;
            case MUSHROOM -> 1 / 0.95;
            case NETHER_WART -> 1 / 3.0;
            case SUNFLOWER, MOONFLOWER -> 1 / 2.0;
            case WILD_ROSE -> 1 / 2.5;
        };
    }

    public static void addXp(SkyBlockPlayer player, GardenCrop crop, int collectionGained) {
        int slot = player.getHeldSlot();
        SkyBlockItem held = new SkyBlockItem(player.getInventory().getItemStack(slot));
        if (held.isAir()) return;
        Tool tool = of(held.getAttributeHandler().getTypeAsString());
        if (tool == null || !tool.works(crop)) return;

        long before = held.getAttributeHandler().getFarmingToolXp();
        long gained = Math.max(1, Math.round(collectionGained * xpPerCollection(crop)));
        int levelBefore = level(before, tool.mk());
        player.updateItemInSlot(slot, item -> {
            long xp = item.getAttributeHandler().getFarmingToolXp() + gained;
            item.getAttributeHandler().setFarmingToolXp(xp);
            item.getAttributeHandler().setExtraDynamicStatistics(bonus(tool, level(xp, tool.mk())));
        });
        int levelAfter = level(before + gained, tool.mk());
        if (levelAfter > levelBefore) {
            player.sendMessage(Text.of("<a>Your {} <a>reached <6>Tool Level {}<a>!", held.getDisplayNameText(), levelAfter));
        }
    }

    private static ItemStatistics bonus(Tool tool, int level) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        builder.withBase(cropFortune(tool.crop()), (double) FORTUNE_PER_LEVEL * (level - 1));
        return builder.build();
    }

    private static ItemStatistic cropFortune(GardenCrop crop) {
        return switch (crop) {
            case WHEAT -> ItemStatistic.WHEAT_FORTUNE;
            case CARROT -> ItemStatistic.CARROT_FORTUNE;
            case POTATO -> ItemStatistic.POTATO_FORTUNE;
            case PUMPKIN -> ItemStatistic.PUMPKIN_FORTUNE;
            case SUGAR_CANE -> ItemStatistic.SUGAR_CANE_FORTUNE;
            case MELON -> ItemStatistic.MELON_FORTUNE;
            case CACTUS -> ItemStatistic.CACTUS_FORTUNE;
            case COCOA_BEANS -> ItemStatistic.COCOA_BEANS_FORTUNE;
            case MUSHROOM -> ItemStatistic.MUSHROOM_FORTUNE;
            case NETHER_WART -> ItemStatistic.NETHER_WART_FORTUNE;
            case SUNFLOWER -> ItemStatistic.SUNFLOWER_FORTUNE;
            case MOONFLOWER -> ItemStatistic.MOONFLOWER_FORTUNE;
            case WILD_ROSE -> ItemStatistic.WILD_ROSE_FORTUNE;
        };
    }

    static List<String> lore(SkyBlockItem item, SkyBlockPlayer player) {
        Tool tool = of(item.getAttributeHandler().getTypeAsString());
        if (tool == null) return List.of();
        long xp = item.getAttributeHandler().getFarmingToolXp();
        int level = level(xp, tool.mk());
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
        String progress;
        if (level >= MK_LEVEL_CAP[Math.min(3, tool.mk())]) {
            progress = level >= MAX_LEVEL ? "<7>Max level reached!" : "<7>Upgrade the tool to keep levelling!";
        } else {
            long into = xp - cumulativeXp(level);
            progress = "<7>Next Level: <8>(<a>" + format.format(into) + "<7>/<c>" + format.format(LEVEL_XP[level]) + "<8>)";
        }
        return List.of("", "<7>Tool Level: <a>" + level + "<7>/<a>" + MAX_LEVEL, progress);
    }
}
