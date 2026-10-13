package net.swofty.type.skyblockgeneric.garden.farm;

import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;

import java.util.List;

public final class GardenFarmLayout {
    public static final int MARGIN = 3;
    public static final int FARM_SIZE = 90;
    public static final int WATER_PERIOD = 9;

    private static final List<Block> GRASS = List.of(Block.GRASS_BLOCK);

    private GardenFarmLayout() {
    }

    public static boolean inFarm(int lx, int lz) {
        return lx >= MARGIN && lz >= MARGIN && lx < MARGIN + FARM_SIZE && lz < MARGIN + FARM_SIZE;
    }

    public static List<Block> column(GardenCrop crop, int lx, int lz) {
        if (!inFarm(lx, lz)) {
            return GRASS;
        }
        int fx = lx - MARGIN;
        int fz = lz - MARGIN;
        boolean watered = fx % WATER_PERIOD == 4;
        return switch (crop) {
            case WHEAT -> watered ? List.of(Block.WATER) : List.of(farmland(), Block.WHEAT.withProperty("age", "0"));
            case CARROT -> watered ? List.of(Block.WATER) : List.of(farmland(), Block.CARROTS.withProperty("age", "0"));
            case POTATO -> watered ? List.of(Block.WATER) : List.of(farmland(), Block.POTATOES.withProperty("age", "0"));
            case NETHER_WART -> watered ? GRASS : List.of(Block.SOUL_SAND, Block.NETHER_WART.withProperty("age", "0"));
            case PUMPKIN -> switch (fx % 3) {
                case 0 -> List.of(farmland(), Block.PUMPKIN_STEM.withProperty("age", "0"));
                case 1 -> GRASS;
                default -> List.of(Block.WATER);
            };
            case MELON -> switch (fx % 3) {
                case 0 -> List.of(farmland(), Block.MELON_STEM.withProperty("age", "0"));
                case 1 -> GRASS;
                default -> List.of(Block.WATER);
            };
            case SUGAR_CANE -> fx % 3 == 1 ? List.of(Block.WATER) : List.of(Block.DIRT, Block.SUGAR_CANE.withProperty("age", "0"));
            case CACTUS -> (fx + fz) % 2 == 0 ? List.of(Block.SAND, Block.CACTUS.withProperty("age", "0")) : List.of(Block.SAND);
            case MUSHROOM -> (fx + fz) % 2 == 0 ? List.of(Block.MYCELIUM, Block.RED_MUSHROOM) : List.of(Block.MYCELIUM);
            case SUNFLOWER -> doublePlant(fx, fz, Block.SUNFLOWER);
            case WILD_ROSE -> doublePlant(fx, fz, Block.ROSE_BUSH);
            case MOONFLOWER -> fx % 2 == 0 && fz % 2 == 0 ? List.of(Block.GRASS_BLOCK, Block.BLUE_ORCHID) : GRASS;
            case COCOA_BEANS -> cocoa(fx, fz);
        };
    }

    private static List<Block> doublePlant(int fx, int fz, Block plant) {
        if (fx % 2 != 0 || fz % 2 != 0) {
            return GRASS;
        }
        return List.of(Block.GRASS_BLOCK, plant.withProperty("half", "lower"), plant.withProperty("half", "upper"));
    }

    private static List<Block> cocoa(int fx, int fz) {
        if (fz % 2 != 0) {
            return GRASS;
        }
        if (fx % 3 == 0) {
            Block log = Block.JUNGLE_LOG;
            return List.of(Block.DIRT, log, log, log);
        }
        if (fx % 3 == 1) {
            Block pod = Block.COCOA.withProperty("age", "0").withProperty("facing", "west");
            return List.of(Block.GRASS_BLOCK, pod, pod, pod);
        }
        return GRASS;
    }

    private static Block farmland() {
        return Block.FARMLAND.withProperty("moisture", "7");
    }
}
