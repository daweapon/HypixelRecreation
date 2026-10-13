package net.swofty.type.skyblockgeneric.garden.farm;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.generic.entity.drop.ItemDrops;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContests;
import net.swofty.type.skyblockgeneric.garden.GardenCleaning;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.tool.FarmingTools;
import net.swofty.type.skyblockgeneric.garden.pest.PestService;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Random;

public final class GardenFarm {
    public static final int CROP_Y = GardenCleaning.GROUND_Y + 1;
    public static final double FORTUNE_PER_UPGRADE_TIER = 5;
    public static final double FORTUNE_PER_PLOT = 3;
    private static final int MAX_STACK = 3;
    private static final Random RANDOM = new Random();

    private GardenFarm() {
    }

    public record Crop(GardenCrop crop, ItemType item, Material material, int baseAmount) {
    }

    private record Harvest(Crop crop, int amount, Runnable apply) {
    }

    public static boolean harvest(SkyBlockPlayer player, Instance instance, Point position, Block block) {
        int plot = GardenLayout.plotAt(position.x(), position.z());
        if (plot == GardenLayout.NONE || plot == GardenLayout.BARN) return false;
        GardenProfile profile = Gardens.profile(player);
        if (!profile.isPlotUnlocked(plot) || !profile.isPlotCleaned(plot)) return false;

        Harvest harvest = plan(instance, position, block);
        if (harvest == null) return false;

        harvest.apply().run();
        PestService.onHarvest(player, plot);
        VisitorService.reduceCooldown(player, VisitorService.CROP_COOLDOWN_SECONDS);
        List<SkyBlockItem> drops = drops(player, profile, harvest);
        HypixelEventHandler.callCustomEvent(new CustomBlockBreakEvent(
                player, harvest.crop().material(), position, drops, false));
        for (SkyBlockItem drop : drops) {
            ItemType type = drop.getAttributeHandler().getPotentialType();
            FarmingContests.recordHarvest(player, type, drop.getAmount());
            FarmingTools.addXp(player, harvest.crop().crop(), drop.getAmount());
            give(player, drop, position);
        }
        return true;
    }

    private static Harvest plan(Instance instance, Point position, Block block) {
        String name = shortName(block);
        return switch (name) {
            case "wheat" -> mature(block, 7, crop(GardenCrop.WHEAT, ItemType.WHEAT, Material.WHEAT, 1), instance, position);
            case "carrots" -> mature(block, 7, crop(GardenCrop.CARROT, ItemType.CARROT, Material.CARROT, 3), instance, position);
            case "potatoes" -> mature(block, 7, crop(GardenCrop.POTATO, ItemType.POTATO, Material.POTATO, 3), instance, position);
            case "nether_wart" -> mature(block, 3, crop(GardenCrop.NETHER_WART, ItemType.NETHER_WART, Material.NETHER_WART, 3), instance, position);
            case "cocoa" -> mature(block, 2, crop(GardenCrop.COCOA_BEANS, ItemType.COCOA_BEANS, Material.COCOA_BEANS, 3), instance, position);
            case "pumpkin" -> single(crop(GardenCrop.PUMPKIN, ItemType.PUMPKIN, Material.PUMPKIN, 1), instance, position, 1);
            case "melon" -> single(crop(GardenCrop.MELON, ItemType.MELON_SLICE, Material.MELON_SLICE, 5), instance, position, 1);
            case "red_mushroom" -> single(crop(GardenCrop.MUSHROOM, ItemType.RED_MUSHROOM, Material.RED_MUSHROOM, 1), instance, position, 1);
            case "blue_orchid" -> single(crop(GardenCrop.MOONFLOWER, ItemType.MOONFLOWER, Material.BLUE_ORCHID, 1), instance, position, 1);
            case "sunflower" -> doublePlant(crop(GardenCrop.SUNFLOWER, ItemType.SUNFLOWER, Material.SUNFLOWER, 1), instance, position, block);
            case "rose_bush" -> doublePlant(crop(GardenCrop.WILD_ROSE, ItemType.WILD_ROSE, Material.ROSE_BUSH, 1), instance, position, block);
            case "sugar_cane" -> column(crop(GardenCrop.SUGAR_CANE, ItemType.SUGAR_CANE, Material.SUGAR_CANE, 1), instance, position, block);
            case "cactus" -> column(crop(GardenCrop.CACTUS, ItemType.CACTUS, Material.CACTUS, 1), instance, position, block);
            default -> null;
        };
    }

    private static Crop crop(GardenCrop crop, ItemType item, Material material, int baseAmount) {
        return new Crop(crop, item, material, baseAmount);
    }

    private static Harvest mature(Block block, int maxAge, Crop crop, Instance instance, Point position) {
        if (!String.valueOf(maxAge).equals(block.getProperty("age"))) return null;
        return new Harvest(crop, crop.baseAmount(), () -> instance.setBlock(position, block.withProperty("age", "0")));
    }

    private static Harvest single(Crop crop, Instance instance, Point position, int count) {
        return new Harvest(crop, crop.baseAmount() * count, () -> instance.setBlock(position, Block.AIR));
    }

    private static Harvest doublePlant(Crop crop, Instance instance, Point position, Block block) {
        Point lower = "upper".equals(block.getProperty("half")) ? position.sub(0, 1, 0) : position;
        return new Harvest(crop, crop.baseAmount(), () -> {
            instance.setBlock(lower, Block.AIR);
            instance.setBlock(lower.add(0, 1, 0), Block.AIR);
        });
    }

    private static Harvest column(Crop crop, Instance instance, Point position, Block block) {
        int count = 0;
        for (int i = 0; i < MAX_STACK; i++) {
            if (!instance.getBlock(position.add(0, i, 0)).compare(block, Block.Comparator.ID)) break;
            count++;
        }
        int removed = count;
        return new Harvest(crop, crop.baseAmount() * removed, () -> {
            for (int i = 0; i < removed; i++) {
                instance.setBlock(position.add(0, i, 0), Block.AIR);
            }
        });
    }

    private static List<SkyBlockItem> drops(SkyBlockPlayer player, GardenProfile profile, Harvest harvest) {
        Crop crop = harvest.crop();
        double fortune = fortune(player, profile, crop.crop());
        int guaranteed = (int) Math.floor(fortune / 100);
        int total = harvest.amount() * (1 + guaranteed);
        if (LootRoll.roll(player.getUuid(), crop.item(), fortune - guaranteed * 100, RANDOM)) {
            total += harvest.amount();
        }
        return List.of(new SkyBlockItem(crop.item(), total));
    }

    public static double fortune(SkyBlockPlayer player, GardenProfile profile, GardenCrop crop) {
        var statistics = player.getStatistics().allStatistics();
        double total = statistics.getOverall(ItemStatistic.FARMING_FORTUNE)
                + statistics.getOverall(specificFortune(crop))
                + profile.cropUpgradeTier(crop) * FORTUNE_PER_UPGRADE_TIER
                + profile.getUnlockedPlots().size() * FORTUNE_PER_PLOT
                + PestService.fortuneBoost(profile, System.currentTimeMillis());
        return total * (1 - PestService.fortuneLoss(player, profile) / 100.0);
    }

    private static ItemStatistic specificFortune(GardenCrop crop) {
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

    private static void give(SkyBlockPlayer player, SkyBlockItem drop, Point source) {
        ItemType type = drop.getAttributeHandler().getPotentialType();
        int amount = drop.getAmount();
        if (player.canInsertItemIntoSacks(type, amount)) {
            player.getSackItems().increase(type, amount);
            return;
        }
        if (player.getSkyBlockExperience().getLevel().asInt() >= 6) {
            player.addAndUpdateItem(drop);
            return;
        }
        ItemDrops.dropFromBlock(new DroppedItemEntityImpl(drop, player), player.getInstance(), Pos.fromPoint(source).add(0, 1, 0));
    }

    static String shortName(Block block) {
        String key = block.name();
        int colon = key.indexOf(':');
        return colon < 0 ? key : key.substring(colon + 1);
    }
}
