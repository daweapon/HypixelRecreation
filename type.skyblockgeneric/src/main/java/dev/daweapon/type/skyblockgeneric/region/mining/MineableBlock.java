package dev.daweapon.type.skyblockgeneric.region.mining;

import lombok.Getter;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.glacite.GlaciteOre;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.mining.handler.MiningHandlers;
import dev.daweapon.type.skyblockgeneric.region.mining.handler.SkyBlockMiningHandler;

@Getter
public enum MineableBlock {
    // Default mineable blocks (pickaxe)
    ANDESITE(Material.ANDESITE, MiningHandlers.pickaxe(15, 1), MiningBlockClassification.defaultBlock()),
    GRANITE(Material.GRANITE, MiningHandlers.pickaxe(15, 1), MiningBlockClassification.defaultBlock()),
    DIORITE(Material.DIORITE, MiningHandlers.pickaxe(15, 1), MiningBlockClassification.defaultBlock()),
    STONE(Material.STONE, MiningHandlers.pickaxe(15, 1), MiningBlockClassification.blockBlock()),
    SAND(Material.SAND, MiningHandlers.pickaxe(5, 1), MiningBlockClassification.blockBlock()),
    NETHERRACK(Material.NETHERRACK, MiningHandlers.pickaxe(4, 1), MiningBlockClassification.blockBlock()),
    COBBLESTONE(Material.COBBLESTONE, MiningHandlers.pickaxe(20, 1), MiningBlockClassification.blockBlock()),
    END_STONE(Material.END_STONE, MiningHandlers.pickaxe(30, 1), MiningBlockClassification.blockBlock()),
    OBSIDIAN(Material.END_STONE, MiningHandlers.pickaxe(500, 4), MiningBlockClassification.blockBlock()),

    // Mineable blocks (any item or hand, vanilla timings)
    ICE(Material.ICE, MiningHandlers.hand(15, 8), MiningBlockClassification.blockBlock()),

    // Ores (pickaxe)
    ORE_IRON(Material.IRON_ORE, MiningHandlers.pickaxe(30, 2), MiningBlockClassification.oreBlock()),
    ORE_COAL(Material.COAL_ORE, MiningHandlers.pickaxe(30, 1), MiningBlockClassification.oreBlock()),
    ORE_GOLD(Material.GOLD_ORE, MiningHandlers.pickaxe(30, 3), MiningBlockClassification.oreBlock()),
    ORE_DIAMOND(Material.DIAMOND_ORE, MiningHandlers.pickaxe(30, 3), MiningBlockClassification.oreBlock()),
    ORE_REDSTONE(Material.REDSTONE_ORE, MiningHandlers.pickaxe(30, 3), MiningBlockClassification.oreBlock()),
    ORE_LAPIS(Material.LAPIS_ORE, MiningHandlers.pickaxe(30, 3), MiningBlockClassification.oreBlock()),
    ORE_EMERALD(Material.EMERALD_ORE, MiningHandlers.pickaxe(30, 3), MiningBlockClassification.oreBlock()),

    // Break time with axe = blockStrength / axe_strength from AxeComponent
    // Vanilla times: Hand=3s, Wooden=1.5s, Stone=0.75s, Iron=0.5s, Diamond=0.4s, Golden=0.25s
    ACACIA_LOG(Material.ACACIA_LOG, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    ACACIA_WOOD(Material.ACACIA_WOOD, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    BIRCH_LOG(Material.BIRCH_LOG, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    BIRCH_WOOD(Material.BIRCH_WOOD, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    JUNGLE_LOG(Material.JUNGLE_LOG, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    JUNGLE_WOOD(Material.JUNGLE_WOOD, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    SPRUCE_LOG(Material.SPRUCE_LOG, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    SPRUCE_WOOD(Material.SPRUCE_WOOD, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    STRIPPED_SPRUCE_LOG(Material.STRIPPED_SPRUCE_LOG, MiningHandlers.axe(60, 60), MiningBlockClassification.figForagingBlock()),
    STRIPPED_SPRUCE_WOOD(Material.STRIPPED_SPRUCE_WOOD, MiningHandlers.axe(60, 60), MiningBlockClassification.figForagingBlock()),
    DARK_OAK_LOG(Material.DARK_OAK_LOG, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    DARK_OAK_WOOD(Material.DARK_OAK_WOOD, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    MANGROVE_LOG(Material.MANGROVE_LOG, MiningHandlers.axe(60, 60), MiningBlockClassification.mangroveForagingBlock()),
    MANGROVE_WOOD(Material.MANGROVE_WOOD, MiningHandlers.axe(60, 60), MiningBlockClassification.mangroveForagingBlock()),
    OAK_LOG(Material.OAK_LOG, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),
    OAK_WOOD(Material.OAK_WOOD, MiningHandlers.axe(60, 60), MiningBlockClassification.foragingBlock()),

    // Farming mineable blocks (instant break)
    WHEAT(Material.WHEAT, MiningHandlers.instant(), MiningBlockClassification.wheatBlock()),
    SUGAR_CANE(Material.SUGAR_CANE, MiningHandlers.instant(), MiningBlockClassification.sugarCaneBlock()),
    CACTUS(Material.CACTUS, MiningHandlers.instant(), MiningBlockClassification.cactusBlock()),
    BROWN_MUSHROOM(Material.BROWN_MUSHROOM, MiningHandlers.instant(), MiningBlockClassification.mushroomBlock()),
    RED_MUSHROOM(Material.RED_MUSHROOM, MiningHandlers.instant(), MiningBlockClassification.mushroomBlock()),
    POTATO(Material.POTATO, MiningHandlers.instant(), MiningBlockClassification.potatoBlock()),
    CARROT(Material.CARROT, MiningHandlers.instant(), MiningBlockClassification.carrotBlock()),
    NETHER_WART(Material.NETHER_WART, MiningHandlers.instant(), MiningBlockClassification.netherWartBlock()),

    // Farming blocks with strength (using instant for now)
    PUMPKIN(Material.PUMPKIN, MiningHandlers.instant(), MiningBlockClassification.pumpkinBlock()),
    CARVED_PUMPKIN(Material.CARVED_PUMPKIN, MiningHandlers.instant(), MiningBlockClassification.pumpkinBlock()),
    MELON(Material.MELON, MiningHandlers.instant(), MiningBlockClassification.melonBlock()),

    // Dwarven mines mineable blocks (pickaxe)
    MITHRIL_WEAK(Material.GRAY_WOOL, MiningHandlers.pickaxe(500, 4), MiningBlockClassification.dwarvenMetalBlock()),
    MITHRIL_WEAK2(Material.CYAN_TERRACOTTA, MiningHandlers.pickaxe(500, 4), MiningBlockClassification.dwarvenMetalBlock()),
    MITHRIL_MEDIUM(Material.PRISMARINE, MiningHandlers.pickaxe(800, 4), MiningBlockClassification.dwarvenMetalBlock()),
    MITHRIL_MEDIUM2(Material.DARK_PRISMARINE, MiningHandlers.pickaxe(800, 4), MiningBlockClassification.dwarvenMetalBlock()),
    MITHRIL_MEDIUM3(Material.PRISMARINE_BRICKS, MiningHandlers.pickaxe(800, 4), MiningBlockClassification.dwarvenMetalBlock()),
    MITHRIL_STRONG(Material.LIGHT_BLUE_WOOL, MiningHandlers.pickaxe(1500, 4), MiningBlockClassification.dwarvenMetalBlock()),
    TITANIUM(Material.POLISHED_DIORITE, MiningHandlers.pickaxe(2500, 5), MiningBlockClassification.dwarvenMetalBlock()),
    DWARVEN_GOLD(Material.GOLD_BLOCK, MiningHandlers.pickaxe(600, 3), MiningBlockClassification.oreBlock()),

    // Glacite Tunnels
    UMBER_ORE(Material.BROWN_TERRACOTTA, MiningHandlers.pickaxe(5600, 9), MiningBlockClassification.dwarvenMetalBlock()),
    TUNGSTEN_ORE(Material.CLAY, MiningHandlers.pickaxe(5600, 9), MiningBlockClassification.dwarvenMetalBlock()),
    GLACITE_ORE(Material.PACKED_ICE, MiningHandlers.pickaxe(6000, 9), MiningBlockClassification.dwarvenMetalBlock()),
    AQUAMARINE_ORE(Material.BLUE_STAINED_GLASS, MiningHandlers.pickaxe(5200, 9), MiningBlockClassification.gemstoneBlock()),
    CITRINE_ORE(Material.BROWN_STAINED_GLASS, MiningHandlers.pickaxe(5200, 9), MiningBlockClassification.gemstoneBlock()),
    ONYX_ORE(Material.BLACK_STAINED_GLASS, MiningHandlers.pickaxe(5200, 9), MiningBlockClassification.gemstoneBlock()),
    PERIDOT_ORE(Material.GREEN_STAINED_GLASS, MiningHandlers.pickaxe(5200, 9), MiningBlockClassification.gemstoneBlock()),

    // Crystal Hollows
    HARD_STONE(Material.STONE, MiningHandlers.pickaxe(50, 4), MiningBlockClassification.blockBlock()),
    HOLLOWS_EMERALD_BLOCK(Material.EMERALD_BLOCK, MiningHandlers.pickaxe(600, 3), MiningBlockClassification.oreBlock()),
    HOLLOWS_DIAMOND_BLOCK(Material.DIAMOND_BLOCK, MiningHandlers.pickaxe(600, 3), MiningBlockClassification.oreBlock()),
    HOLLOWS_LAPIS_BLOCK(Material.LAPIS_BLOCK, MiningHandlers.pickaxe(600, 3), MiningBlockClassification.oreBlock()),
    HOLLOWS_REDSTONE_BLOCK(Material.REDSTONE_BLOCK, MiningHandlers.pickaxe(600, 3), MiningBlockClassification.oreBlock()),
    HOLLOWS_IRON_BLOCK(Material.IRON_BLOCK, MiningHandlers.pickaxe(600, 3), MiningBlockClassification.oreBlock()),
    HOLLOWS_COAL_BLOCK(Material.COAL_BLOCK, MiningHandlers.pickaxe(600, 3), MiningBlockClassification.oreBlock()),
    HOLLOWS_QUARTZ_BLOCK(Material.QUARTZ_BLOCK, MiningHandlers.pickaxe(600, 3), MiningBlockClassification.oreBlock()),
    HOLLOWS_QUARTZ_PILLAR(Material.QUARTZ_PILLAR, MiningHandlers.pickaxe(600, 3), MiningBlockClassification.oreBlock()),
    RUBY_ORE(Material.RED_STAINED_GLASS, MiningHandlers.pickaxe(2300, 6), MiningBlockClassification.gemstoneBlock()),
    AMETHYST_ORE(Material.PURPLE_STAINED_GLASS, MiningHandlers.pickaxe(3000, 7), MiningBlockClassification.gemstoneBlock()),
    JADE_ORE(Material.LIME_STAINED_GLASS, MiningHandlers.pickaxe(3000, 7), MiningBlockClassification.gemstoneBlock()),
    SAPPHIRE_ORE(Material.LIGHT_BLUE_STAINED_GLASS, MiningHandlers.pickaxe(3000, 7), MiningBlockClassification.gemstoneBlock()),
    AMBER_ORE(Material.ORANGE_STAINED_GLASS, MiningHandlers.pickaxe(3000, 7), MiningBlockClassification.gemstoneBlock()),
    TOPAZ_ORE(Material.YELLOW_STAINED_GLASS, MiningHandlers.pickaxe(3800, 8), MiningBlockClassification.gemstoneBlock()),
    JASPER_ORE(Material.MAGENTA_STAINED_GLASS, MiningHandlers.pickaxe(4800, 9), MiningBlockClassification.gemstoneBlock()),

    // Crimson Isle
    MYCELIUM(Material.MYCELIUM, MiningHandlers.hand(12, 6), MiningBlockClassification.blockBlock()),
    RED_SAND(Material.RED_SAND, MiningHandlers.hand(12, 6), MiningBlockClassification.blockBlock()),
    GLOWSTONE(Material.GLOWSTONE, MiningHandlers.hand(9, 4), MiningBlockClassification.blockBlock()),
    ORE_NETHER_QUARTZ(Material.NETHER_QUARTZ_ORE, MiningHandlers.pickaxe(30, 1), MiningBlockClassification.oreBlock()),
    SULPHUR_ORE(Material.SPONGE, MiningHandlers.pickaxe(500, 8), MiningBlockClassification.oreBlock()),
    DEAD_BUSH(Material.DEAD_BUSH, MiningHandlers.instant(), MiningBlockClassification.farmingBlock()),
    ;

    private final Material material;
    private final SkyBlockMiningHandler miningHandler;
    private final MiningBlockClassification blockType;

    MineableBlock(Material material, SkyBlockMiningHandler miningHandler, MiningBlockClassification blockType) {
        this.material = material;
        this.miningHandler = miningHandler;
        this.blockType = blockType;
    }

    /**
     * Get block strength from the handler (backwards compatibility).
     */
    public double getStrength() {
        return miningHandler.getStrength();
    }

    /**
     * Get mining power requirement from the handler (backwards compatibility).
     */
    public int getMiningPowerRequirement() {
        return miningHandler.getMiningPowerRequirement();
    }

    public static MineableBlock get(Block block, RegionType region) {
        GlaciteOre ore = GlaciteOre.of(block, region);
        if (ore != null) return ore.getMineable();
        net.minestom.server.item.Material material = net.minestom.server.item.Material.fromKey(block.name());
        dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsOre gemstone =
                dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsOre.of(material, region);
        if (gemstone != null) return gemstone.getMineable();
        if (dev.daweapon.type.skyblockgeneric.crystalhollows.HardStone.isHardStone(material, region)) return HARD_STONE;
        return get(block);
    }

    public static MineableBlock get(Block block) {
        for (MineableBlock mineableBlock : values()) {
            if (block.key().equals(mineableBlock.getMaterial().key()))
                return mineableBlock;
        }
        return null;
    }
}
