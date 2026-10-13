package net.swofty.type.skyblockgeneric.garden.farm;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.garden.GardenCleaning;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class GardenCropGrowth {
    public static final int SAMPLES_PER_PLOT = 300;
    private static final int MAX_STACK = 3;

    private GardenCropGrowth() {
    }

    public static void tick() {
        for (SkyBlockGarden garden : SkyBlockGarden.loaded()) {
            Instance instance = garden.getGardenInstance();
            if (instance == null) continue;
            for (Player online : instance.getPlayers()) {
                if (online instanceof SkyBlockPlayer player) {
                    growFor(instance, Gardens.profile(player));
                    break;
                }
            }
        }
    }

    static void growFor(Instance instance, GardenProfile profile) {
        for (Map.Entry<Integer, String> entry : profile.getPlotPresets().entrySet()) {
            int plot = entry.getKey();
            GardenCrop crop = GardenPresets.cropOf(entry.getValue());
            int[] origin = GardenLayout.plotOrigin(plot);
            if (crop == null || origin == null || !profile.isPlotCleaned(plot) || GardenPlotLock.isLocked(instance, plot)) continue;
            for (int i = 0; i < SAMPLES_PER_PLOT; i++) {
                int lx = ThreadLocalRandom.current().nextInt(GardenFarmLayout.MARGIN, GardenFarmLayout.MARGIN + GardenFarmLayout.FARM_SIZE);
                int lz = ThreadLocalRandom.current().nextInt(GardenFarmLayout.MARGIN, GardenFarmLayout.MARGIN + GardenFarmLayout.FARM_SIZE);
                if (instance.getChunkAt(origin[0] + lx, origin[1] + lz) == null) continue;
                growCell(instance, crop, origin[0] + lx, origin[1] + lz, lx, lz);
            }
        }
    }

    private static void growCell(Instance instance, GardenCrop crop, int x, int z, int lx, int lz) {
        List<Block> expected = GardenFarmLayout.column(crop, lx, lz);
        int base = GardenCleaning.GROUND_Y;
        if (expected.size() < 2 || !instance.getBlock(x, base, z).compare(expected.get(0))) {
            return;
        }
        switch (crop) {
            case SUGAR_CANE, CACTUS -> growStack(instance, x, z, expected.get(1));
            case PUMPKIN, MELON -> growStem(instance, crop, x, z, expected.get(1));
            case SUNFLOWER, WILD_ROSE -> regrowDouble(instance, x, z, expected);
            default -> {
                for (int i = 1; i < expected.size(); i++) {
                    growAge(instance, x, base + i, z, expected.get(i));
                }
            }
        }
    }

    private static void growAge(Instance instance, int x, int y, int z, Block seed) {
        Block current = instance.getBlock(x, y, z);
        if (current.isAir()) {
            instance.setBlock(x, y, z, seed);
            return;
        }
        String age = current.getProperty("age");
        if (age == null || !current.compare(seed, Block.Comparator.ID)) return;
        int next = Integer.parseInt(age) + 1;
        Integer max = maxAge(current);
        if (max != null && next <= max) {
            instance.setBlock(x, y, z, current.withProperty("age", String.valueOf(next)));
        }
    }

    private static Integer maxAge(Block block) {
        return switch (GardenFarm.shortName(block)) {
            case "wheat", "carrots", "potatoes", "pumpkin_stem", "melon_stem" -> 7;
            case "nether_wart" -> 3;
            case "cocoa" -> 2;
            default -> null;
        };
    }

    private static void growStack(Instance instance, int x, int z, Block stalk) {
        int y = GardenCleaning.GROUND_Y + 1;
        for (int i = 0; i < MAX_STACK; i++, y++) {
            Block current = instance.getBlock(x, y, z);
            if (current.isAir()) {
                instance.setBlock(x, y, z, stalk);
                return;
            }
            if (!current.compare(stalk, Block.Comparator.ID)) return;
        }
    }

    private static void growStem(Instance instance, GardenCrop crop, int x, int z, Block stem) {
        int y = GardenCleaning.GROUND_Y + 1;
        Block current = instance.getBlock(x, y, z);
        if (current.isAir()) {
            instance.setBlock(x, y, z, stem);
            return;
        }
        if (!current.compare(stem, Block.Comparator.ID)) return;
        if (!"7".equals(current.getProperty("age"))) {
            growAge(instance, x, y, z, stem);
            return;
        }
        Block fruit = crop == GardenCrop.PUMPKIN ? Block.PUMPKIN : Block.MELON;
        if (instance.getBlock(x + 1, y, z).isAir() && instance.getBlock(x + 1, y - 1, z).compare(Block.GRASS_BLOCK)) {
            instance.setBlock(x + 1, y, z, fruit);
        }
    }

    private static void regrowDouble(Instance instance, int x, int z, List<Block> expected) {
        int y = GardenCleaning.GROUND_Y + 1;
        if (instance.getBlock(x, y, z).isAir()) {
            instance.setBlock(x, y, z, expected.get(1));
            instance.setBlock(x, y + 1, z, expected.get(2));
        }
    }
}
