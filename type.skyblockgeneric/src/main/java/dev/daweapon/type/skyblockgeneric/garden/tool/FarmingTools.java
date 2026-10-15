package dev.daweapon.type.skyblockgeneric.garden.tool;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FarmingTools {
    public static final String LORE_HANDLER = "FARMING_TOOL";
    public static final int FORTUNE_PER_LEVEL = 4;
    public static final int MAX_LEVEL = 50;
    public static final int MAX_OVERCLOCKERS = 10;
    public static final long CAPSULE_XP = 100_000;
    public static final long OVERFLOW_PER_CAPSULE = 200_000;

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

    public static @Nullable String familyOf(String itemId) {
        int underscore = itemId.lastIndexOf('_');
        if (underscore > 0 && itemId.substring(underscore + 1).matches("[123]") && FAMILIES.containsKey(itemId.substring(0, underscore))) {
            return itemId.substring(0, underscore);
        }
        return FAMILIES.containsKey(itemId) ? itemId : null;
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

    public static @Nullable ItemType nextTier(@Nullable String itemId) {
        Tool tool = of(itemId);
        String family = itemId == null ? null : familyOf(itemId);
        if (tool == null || family == null || tool.mk() >= 3) return null;
        return ItemType.get(family + "_" + (tool.mk() + 1));
    }

    public record Gain(long xp, long overflow, int capsules) {
    }

    public static int levelCap(int mk, int overclockers) {
        int base = MK_LEVEL_CAP[Math.max(1, Math.min(3, mk))];
        return mk >= 3 ? Math.min(MAX_LEVEL, base + Math.max(0, Math.min(MAX_OVERCLOCKERS, overclockers))) : base;
    }

    public static int level(long xp, int mk) {
        return level(xp, mk, 0);
    }

    public static Gain gain(long xp, long overflow, int mk, int overclockers, long gained, boolean keepOverflow) {
        long total = xp + Math.max(0, gained);
        if (levelCap(mk, overclockers) < MAX_LEVEL || total <= cumulativeXp(MAX_LEVEL)) {
            return new Gain(total, overflow, 0);
        }
        long surplus = total - cumulativeXp(MAX_LEVEL);
        if (!keepOverflow) return new Gain(cumulativeXp(MAX_LEVEL), overflow, 0);
        long banked = overflow + surplus;
        return new Gain(cumulativeXp(MAX_LEVEL), banked % OVERFLOW_PER_CAPSULE, (int) (banked / OVERFLOW_PER_CAPSULE));
    }

    public static int level(long xp, int mk, int overclockers) {
        int level = 1;
        long cumulative = 0;
        for (int next = 2; next <= MAX_LEVEL; next++) {
            cumulative += LEVEL_XP[next - 1];
            if (xp < cumulative) break;
            level = next;
        }
        return Math.min(level, levelCap(mk, overclockers));
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

        double chipBoost = 1 + dev.daweapon.type.skyblockgeneric.garden.chip.ChipService.bonus(player, dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip.MECHAMIND) / 100.0;
        long gained = Math.max(1, Math.round(collectionGained * xpPerCollection(crop) * chipBoost * dev.daweapon.type.skyblockgeneric.garden.GardenPetBonuses.toolXpMultiplier(player)));
        int[] levels = new int[2];
        int[] capsules = new int[1];
        player.updateItemInSlot(slot, item -> {
            levels[0] = levelOf(item, tool);
            capsules[0] = grant(item, tool, gained, true);
            levels[1] = levelOf(item, tool);
        });
        if (levels[1] > levels[0]) {
            player.sendMessage(Text.of("<a>Your {} <a>reached <6>Tool Level {}<a>!", held.getDisplayNameText(), levels[1]));
        }
        if (capsules[0] > 0) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.TOOL_EXP_CAPSULE, capsules[0]));
            player.sendMessage(Text.of("<3><l>OVERFLOW!</l> <7>Your {} <7>has just dropped a <9>Tool Exp Capsule<7>!", held.getDisplayNameText()));
        }
    }

    public static int grant(SkyBlockItem item, Tool tool, long gained, boolean keepOverflow) {
        var handler = item.getAttributeHandler();
        Gain result = gain(handler.getFarmingToolXp(), handler.getFarmingToolOverflow(), tool.mk(),
                handler.getFarmingToolOverclockers(), gained, keepOverflow);
        handler.setFarmingToolXp(result.xp());
        handler.setFarmingToolOverflow(result.overflow());
        refresh(item, tool);
        return result.capsules();
    }

    public static int levelOf(SkyBlockItem item, Tool tool) {
        var handler = item.getAttributeHandler();
        return level(handler.getFarmingToolXp(), tool.mk(), handler.getFarmingToolOverclockers());
    }

    public static void refresh(SkyBlockItem item) {
        Tool tool = of(item.getAttributeHandler().getTypeAsString());
        if (tool != null) refresh(item, tool);
    }

    private static void refresh(SkyBlockItem item, Tool tool) {
        item.getAttributeHandler().setExtraDynamicStatistics(bonus(tool, levelOf(item, tool)));
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
        int overclockers = item.getAttributeHandler().getFarmingToolOverclockers();
        int cap = levelCap(tool.mk(), overclockers);
        int level = level(xp, tool.mk(), overclockers);
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
        String progress;
        if (level >= MAX_LEVEL) {
            long overflow = item.getAttributeHandler().getFarmingToolOverflow();
            progress = "<7>Next Capsule: <8>(<a>" + format.format(overflow) + "<7>/<c>" + format.format(OVERFLOW_PER_CAPSULE) + "<8>)";
        } else if (level >= cap) {
            progress = tool.mk() >= 3 ? "<7>Apply an <6>Overclocker 3000 <7>to keep levelling!" : "<7>Upgrade the tool to keep levelling!";
        } else {
            long into = xp - cumulativeXp(level);
            progress = "<7>Next Level: <8>(<a>" + format.format(into) + "<7>/<c>" + format.format(LEVEL_XP[level]) + "<8>)";
        }
        List<String> lines = new java.util.ArrayList<>(List.of("", "<7>Tool Level: <a>" + level + "<7>/<a>" + MAX_LEVEL, progress));
        if (overclockers > 0) {
            lines.add("<7>Overclocker 3000s: <6>" + overclockers + "<7>/<6>" + MAX_OVERCLOCKERS);
        }
        return lines;
    }
}
