package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class GreenhouseWorld {
    private record Display(Entity entity, int stage, String type) {
    }

    private static final Map<Instance, Map<Long, Display>> DISPLAYS = new WeakHashMap<>();
    private static final Map<Instance, java.util.Set<Integer>> STATIONS = new WeakHashMap<>();

    private static synchronized void station(Instance instance, int plot) {
        java.util.Set<Integer> built = STATIONS.computeIfAbsent(instance, key -> new java.util.HashSet<>());
        if (built.contains(plot)) return;
        Pos at = GreenhouseLayout.analyzer(plot);
        if (!instance.isChunkLoaded(at)) return;
        built.add(plot);
        net.swofty.type.generic.entity.InteractionEntity click = new net.swofty.type.generic.entity.InteractionEntity(1.2f, 1.6f,
                (player, event) -> ((net.swofty.type.skyblockgeneric.user.SkyBlockPlayer) player)
                        .openView(new net.swofty.type.skyblockgeneric.gui.inventories.garden.GUICropAnalyzer()));
        click.setInstance(instance, at);
        Entity label = new Entity(EntityType.TEXT_DISPLAY);
        label.setNoGravity(true);
        label.editEntityMeta(net.minestom.server.entity.metadata.display.TextDisplayMeta.class, meta -> {
            meta.setText(net.swofty.commons.text.Text.parse("<a><l>CROP ANALYZER\n<e><l>Click to open!").asComponent());
            meta.setBillboardRenderConstraints(net.minestom.server.entity.metadata.display.AbstractDisplayMeta.BillboardConstraints.CENTER);
            meta.setBackgroundColor(0);
        });
        label.setInstance(instance, at.add(0, 1.9, 0));
    }

    private GreenhouseWorld() {
    }

    private static synchronized void display(Instance instance, int plot, int slot, int x, int z, @org.jetbrains.annotations.Nullable PlantType type, GreenhouseData.Plant plant) {
        Map<Long, Display> live = DISPLAYS.computeIfAbsent(instance, key -> new HashMap<>());
        long key = plot * 1000L + slot;
        Display current = live.get(key);
        boolean wanted = type != null && type.mutation() && !type.id().equals(GreenhouseConfig.DEAD_PLANT);
        if (!wanted) {
            if (current != null) {
                current.entity().remove();
                live.remove(key);
            }
            return;
        }
        if (current != null && !current.entity().isRemoved() && current.stage() == plant.getStage() && current.type().equals(type.id())) return;
        if (current != null) current.entity().remove();
        MutationCatalogue.Mutation mutation = MutationCatalogue.get().mutation(type.id());
        ItemType item = mutation == null ? null : mutation.item();
        if (item == null) return;
        int size = type.footprint();
        double progress = type.stages() == 0 ? 1 : 0.45 + 0.55 * Math.min(1, plant.getStage() / (double) type.stages());
        double scale = progress * 1.3 * size;
        Entity entity = new Entity(EntityType.ITEM_DISPLAY);
        entity.setNoGravity(true);
        entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(new SkyBlockItem(item).getItemStack());
            meta.setScale(new Vec(scale, scale, scale));
        });
        entity.setInstance(instance, new Pos(x + size / 2.0, GreenhouseLayout.CROP_Y + 0.5 * progress * size, z + size / 2.0));
        live.put(key, new Display(entity, plant.getStage(), type.id()));
    }

    public static void render(Instance instance, int plot, GreenhouseData.Bed bed) {
        GreenhouseConfig config = GreenhouseConfig.get();
        station(instance, plot);
        for (int slot = 0; slot < GreenhouseData.SLOTS; slot++) {
            int x = GreenhouseLayout.worldX(plot, slot % GreenhouseData.SIZE);
            int z = GreenhouseLayout.worldZ(plot, slot / GreenhouseData.SIZE);
            if (!instance.isChunkLoaded(x >> 4, z >> 4)) continue;
            int anchor = bed.anchorAt(slot);
            GreenhouseData.Plant owner = anchor < 0 ? null : bed.getPlants().get(anchor);
            boolean origin = anchor == slot;
            set(instance, x, GreenhouseLayout.FLOOR_Y, z, floor(bed.getUnlocked()[slot], owner, bed.surface(slot)));
            PlantType type = owner == null ? null : config.plant(owner.getType());
            Block[] column = type == null || !origin ? new Block[]{Block.AIR, Block.AIR} : crop(type, owner);
            display(instance, plot, slot, x, z, origin ? type : null, owner);
            set(instance, x, GreenhouseLayout.CROP_Y, z, column[0]);
            set(instance, x, GreenhouseLayout.CROP_Y + 1, z, column[1]);
        }
    }

    private static void set(Instance instance, int x, int y, int z, Block block) {
        if (!instance.getBlock(x, y, z).compare(block)) instance.setBlock(x, y, z, block);
    }

    public static Block floor(boolean unlocked, GreenhouseData.Plant plant, Surface surface) {
        if (!unlocked) return Block.COARSE_DIRT;
        return switch (surface) {
            case SOUL_SAND -> Block.SOUL_SAND;
            case SAND -> Block.SAND;
            case MYCELIUM -> Block.MYCELIUM;
            case DIRT -> Block.DIRT;
            case END_STONE -> Block.END_STONE;
            case FARMLAND -> Block.FARMLAND.withProperty("moisture", plant != null && plant.getWater() > 0 ? "7" : "0");
        };
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
