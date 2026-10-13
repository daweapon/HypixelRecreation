package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;

public final class GreenhouseWorld {
    private GreenhouseWorld() {
    }

    public static void render(Instance instance, int plot, GreenhouseData.Bed bed) {
        GreenhouseConfig config = GreenhouseConfig.get();
        for (int slot = 0; slot < GreenhouseData.SLOTS; slot++) {
            int x = GreenhouseLayout.worldX(plot, slot % GreenhouseData.SIZE);
            int z = GreenhouseLayout.worldZ(plot, slot / GreenhouseData.SIZE);
            if (!instance.isChunkLoaded(x >> 4, z >> 4)) continue;
            GreenhouseData.Plant plant = bed.getPlants().get(slot);
            set(instance, x, GreenhouseLayout.FLOOR_Y, z, floor(bed.getUnlocked()[slot], plant));
            PlantType type = plant == null ? null : config.plant(plant.getType());
            Block[] column = type == null ? new Block[]{Block.AIR, Block.AIR} : crop(type, plant);
            set(instance, x, GreenhouseLayout.CROP_Y, z, column[0]);
            set(instance, x, GreenhouseLayout.CROP_Y + 1, z, column[1]);
        }
    }

    private static void set(Instance instance, int x, int y, int z, Block block) {
        if (!instance.getBlock(x, y, z).compare(block)) instance.setBlock(x, y, z, block);
    }

    public static Block floor(boolean unlocked, GreenhouseData.Plant plant) {
        if (!unlocked) return Block.COARSE_DIRT;
        return Block.FARMLAND.withProperty("moisture", plant != null && plant.getWater() > 0 ? "7" : "0");
    }

    public static Block[] crop(PlantType type, GreenhouseData.Plant plant) {
        boolean grown = plant.grown(type);
        int stages = Math.max(1, type.stages());
        double progress = Math.min(1, plant.getStage() / (double) stages);
        Block air = Block.AIR;
        if (type.id().equals(GreenhouseConfig.DEAD_PLANT)) return new Block[]{Block.DEAD_BUSH, air};
        if (!type.baseCrop()) return new Block[]{air, air};
        GardenCrop crop = type.crop();
        return switch (crop) {
            case WHEAT -> aged(Block.WHEAT, 7, progress);
            case CARROT -> aged(Block.CARROTS, 7, progress);
            case POTATO -> aged(Block.POTATOES, 7, progress);
            case NETHER_WART -> aged(Block.NETHER_WART, 3, progress);
            case COCOA_BEANS -> new Block[]{Block.COCOA.withProperty("age", String.valueOf((int) Math.round(progress * 2))).withProperty("facing", "north"), air};
            case SUGAR_CANE -> new Block[]{grown ? Block.SUGAR_CANE : Block.SHORT_GRASS, air};
            case CACTUS -> new Block[]{grown ? Block.CACTUS : Block.SHORT_GRASS, air};
            case PUMPKIN -> new Block[]{grown ? Block.PUMPKIN : Block.PUMPKIN_STEM.withProperty("age", String.valueOf((int) Math.round(progress * 7))), air};
            case MELON -> new Block[]{grown ? Block.MELON : Block.MELON_STEM.withProperty("age", String.valueOf((int) Math.round(progress * 7))), air};
            case MUSHROOM -> new Block[]{grown ? Block.RED_MUSHROOM : Block.SHORT_GRASS, air};
            case MOONFLOWER -> new Block[]{grown ? Block.BLUE_ORCHID : Block.FERN, air};
            case SUNFLOWER -> tall(Block.SUNFLOWER, grown);
            case WILD_ROSE -> tall(Block.ROSE_BUSH, grown);
        };
    }

    private static Block[] aged(Block block, int max, double progress) {
        return new Block[]{block.withProperty("age", String.valueOf((int) Math.round(progress * max))), Block.AIR};
    }

    private static Block[] tall(Block block, boolean grown) {
        if (!grown) return new Block[]{Block.FERN, Block.AIR};
        return new Block[]{block.withProperty("half", "lower"), block.withProperty("half", "upper")};
    }
}
