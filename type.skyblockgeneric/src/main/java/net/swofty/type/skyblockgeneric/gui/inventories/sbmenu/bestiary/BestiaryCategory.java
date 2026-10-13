package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.CataloguedBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobAshfang;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobAutomaton67;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobAutomaton71;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobButterfly;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobExecutiveSebastian;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobExecutiveViper;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobExecutiveWendy;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobGrunt;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobKeyGuardian;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobSludgeLarge;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobSludgeMedium;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobSludgeSmall;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobThyst;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobYog;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobBal;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobBossCorleone;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobScatha;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobStoneworm;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.DiamondGoblin;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.GoldenGoblin;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.MobGlaciteWalker;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.MobTreasureHoarder;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobFireslinger;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobGoblin;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobGoblinFlamethrower;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobKnifethrower;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBarbarianDukeX;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBezal;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBladesoul;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBlaze;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobDiveGhast;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobFlamingSpider;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobFlare;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobGhast;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobHellwisp;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobKadaKnight;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMageOutlaw;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMagmaBoss;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMagmaCube;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMagmaCubeRider;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMatcho;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMillenniaAgedBlaze;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMushroomBull;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMutatedBlaze;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobPackMagmaCube;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobSmolderingBlaze;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobVanquisher;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobWitherSkeleton;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobWitherSpectre;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobEmeraldSlime_05;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobEmeraldSlime_10;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobLapisZombie;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobMinerSkeleton_15;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobMinerSkeleton_20;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobMinerZombie_15;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobMinerZombie_20;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobRedstonePigman;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobSneakyCreeper;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.hub.MobGraveyardZombie;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.hub.MobGraveyardZombieVillager;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.hub.MobRuinsOldWolf;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.hub.MobRuinsWolf;
import net.swofty.type.skyblockgeneric.entity.mob.island.IslandMobSpecies;
import net.swofty.type.skyblockgeneric.entity.mob.island.PrivateIslandMob;
import net.swofty.type.skyblockgeneric.mythological.MythologicalBestiary;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreature;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum BestiaryCategory {
    ;

    @Getter
    public enum PRIVATE_ISLAND implements BestiaryEntry {
        ZOMBIE("<a>Zombie", "Brains.", new GUIMaterial(Material.ZOMBIE_HEAD), allLevels(IslandMobSpecies.ZOMBIE)),
        SKELETON("<a>Skeleton", "Just a normal skelly.", new GUIMaterial(Material.SKELETON_SKULL), allLevels(IslandMobSpecies.SKELETON)),
        SPIDER("<a>Spider", "Likes to climb.", new GUIMaterial(Material.SPIDER_EYE), allLevels(IslandMobSpecies.SPIDER)),
        CREEPER("<a>Creeper", "Look out for their hiss!", new GUIMaterial(Material.CREEPER_HEAD), allLevels(IslandMobSpecies.CREEPER)),
        ENDERMAN("<a>Enderman", "Don't like making eye contact.", new GUIMaterial(Material.ENDER_PEARL), allLevels(IslandMobSpecies.ENDERMAN)),
        WITCH("<a>Witch", "Masters of alchemy.", new GUIMaterial(Material.POTION), allLevels(IslandMobSpecies.WITCH)),
        ;

        private static List<BestiaryMob> allLevels(IslandMobSpecies species) {
            return IntStream.rangeClosed(1, species.getMaxLevel())
                    .mapToObj(level -> (BestiaryMob) PrivateIslandMob.of(species, level))
                    .toList();
        }

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        PRIVATE_ISLAND(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }

    @Getter
    public enum HUB implements BestiaryEntry {
        GRAVEYARD_ZOMBIE("<a>Graveyard Zombie", "Brains.", new GUIMaterial(Material.ZOMBIE_HEAD), List.of(new MobGraveyardZombie())),
        OLD_WOLF("<a>Old Wolf", "Wolves older than the island itself.", new GUIMaterial("d359537c15534f61c1cd886bc118774ed22280e7cdab6613870160aad4ca39"), List.of(new MobRuinsOldWolf())),
        WOLF("<a>Wolf", "Roaming the remains of a Castle far from its best days.", new GUIMaterial("f4cb7a6bf6c32c49f2589147e6f0f888e9e35875dd1ea2a8af379ca710589e6b"), List.of(new MobRuinsWolf())),
        ZOMBIE_VILLAGER("<a>Zombie Villager", "The real enemy isn't the dead - it's the living.", new GUIMaterial("69198f410a10f99314aa0fbe9a3db10697bbc1c011f019507d96673c64217f5a"), List.of(new MobGraveyardZombieVillager())),
        ;

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        HUB(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }

    @Getter
    public enum MYTHOLOGICAL_CREATURES implements BestiaryEntry {
        MINOS_HUNTER(MythologicalCreature.MINOS_HUNTER),
        SIAMESE_LYNX(MythologicalCreature.SIAMESE_LYNXES),
        STRANDED_NYMPH(MythologicalCreature.STRANDED_NYMPH),
        CRETAN_BULL(MythologicalCreature.CRETAN_BULL),
        HARPY(MythologicalCreature.HARPY),
        GAIA_CONSTRUCT(MythologicalCreature.GAIA_CONSTRUCT),
        MINOTAUR(MythologicalCreature.MINOTAUR),
        MINOS_CHAMPION(MythologicalCreature.MINOS_CHAMPION),
        SPHINX(MythologicalCreature.SPHINX),
        MINOS_INQUISITOR(MythologicalCreature.MINOS_INQUISITOR),
        MANTICORE(MythologicalCreature.MANTICORE),
        KING_MINOS(MythologicalCreature.KING_MINOS),
        ;

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        MYTHOLOGICAL_CREATURES(MythologicalCreature creature) {
            this.name = "<a>" + creature.getDisplayName();
            this.description = creature.getBestiary().description();
            this.guiMaterial = new GUIMaterial(creature.getIconTexture());
            this.mobs = MythologicalBestiary.family(creature);
        }
    }

    @Getter
    public enum DEEP_CAVERNS implements BestiaryEntry {
        EMERALD_SLIME("<a>Emerald Slime", "It is said that these slimes absorb emeralds to grow larger.",
                new GUIMaterial("895aeec6b842ada8669f846d65bc49762597824ab944f22f45bf3bbb941abe6c"), List.of(new MobEmeraldSlime_05(), new MobEmeraldSlime_10())),
        LAPIS_ZOMBIE("<a>Lapis Zombie", "These zombies adapted to their environment, using the lapis around them as a defense mechanism.",
                new GUIMaterial("e9f7979b25001087969d58c06e14d00b8dab57dab060b4c8b483c1b7f869940"), List.of(new MobLapisZombie())),
        MINER_SKELETON("<a>Miner Skeleton", "These skeletons have crafted gear from the diamonds around them - resulting in a look both fashionable and protective.",
                new GUIMaterial("8de8bbd7f6d77a1614865ef6a1d31f53f797550d14ee21d107a8415c14b48ca6"), List.of(new MobMinerSkeleton_15(), new MobMinerSkeleton_20())),
        MINER_ZOMBIE("<a>Miner Zombie", "Like their skeleton counterparts, these zombies have bedazzled themselves throughout the years.",
                new GUIMaterial("1b8a707e8a58d2ffe297474d18daee86951b21994566358dc0b5d7dcc9e2ed9b"), List.of(new MobMinerZombie_15(), new MobMinerZombie_20())),
        REDSTONE_PIGMAN("<a>Redstone Pigman", "These pigmen will defend their redstone to the death.",
                new GUIMaterial("74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb"), List.of(new MobRedstonePigman())),
        SNEAKY_CREEPER("<a>Sneaky Creeper", "They be creepin'.",
                new GUIMaterial("74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb"), List.of(new MobSneakyCreeper())),
        ;

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        DEEP_CAVERNS(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }

    @Getter
    public enum DWARVEN_MINES implements BestiaryEntry {
        DIAMOND_GOBLIN("<a>Diamond Goblin", "Does nothing but steal, even from its fellow goblins.", List.of(new DiamondGoblin())),
        GLACITE_WALKER("<a>Glacite Walker", "Guardians of the Gate to the icy caves below.", List.of(new MobGlaciteWalker())),
        GOBLIN("<a>Goblin", "The Goblins have plagued the Dwarven Mines for centuries.",
                List.of(new MobGoblin(), new MobFireslinger(), new MobKnifethrower(), new MobGoblinFlamethrower())),
        GOLDEN_GOBLIN("<a>Golden Goblin", "Take the things you love in life, and run with them.", List.of(new GoldenGoblin())),
        TREASURE_HOARDER("<a>Treasure Hoarder", "The real treasure was the gold we found along the way.", List.of(new MobTreasureHoarder())),
        ;

        private final String name, description;
        private final List<BestiaryMob> mobs;

        DWARVEN_MINES(String name, String description, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.mobs = mobs;
        }

        @Override
        public GUIMaterial getGuiMaterial() {
            return mobs.getFirst().getGuiMaterial();
        }
    }

    /**
     * The 20 Crimson Isle Bestiary families (wiki Bestiary list and Module:BestiaryTiers); each family's icon is
     * its first mob's Bestiary head.
     */
    @Getter
    public enum CRIMSON_ISLE implements BestiaryEntry {
        ASHFANG("<a>Ashfang", "They also call themselves \"The Twins\".", List.of(new MobAshfang())),
        BARBARIAN_DUKE_X("<a>Barbarian Duke X", "He's got anger issues.", List.of(new MobBarbarianDukeX())),
        BLADESOUL("<a>Bladesoul", "Used to be a prince.", List.of(new MobBladesoul())),
        BLAZE("<a>Blaze", "They started the fire.", List.of(new MobBlaze(), new MobMutatedBlaze(), new MobBezal())),
        FLAMING_SPIDER("<a>Flaming Spider", "Can't stop burning.", List.of(new MobFlamingSpider())),
        FLARE("<a>Flare", "Oh baby, a triple!", List.of(new MobFlare())),
        GHAST("<a>Ghast", "Their screams can be heard from miles away.", List.of(new MobGhast(), new MobDiveGhast())),
        KADA_KNIGHT("<a>Kada Knight", "A short walk for you is a long walk for them.", List.of(new MobKadaKnight())),
        MAGE_OUTLAW("<a>Mage Outlaw", "Ate too much Sulphur.", List.of(new MobMageOutlaw())),
        MAGMA_BOSS("<a>Magma Boss", "Like a Magma Cube, but bigger.", List.of(new MobMagmaBoss())),
        MAGMA_CUBE("<a>Magma Cube", "Like Slime, but hotter.", List.of(new MobMagmaCube(), new MobPackMagmaCube())),
        MAGMA_CUBE_RIDER("<a>Magma Cube Rider", "Relatively docile...until you push them.", List.of(new MobMagmaCubeRider())),
        MATCHO("<a>Matcho", "Has quite an explosive temper.", List.of(new MobMatcho())),
        MILLENNIA_AGED_BLAZE("<a>Millennia-Aged Blaze", "Blazes rarely live past the age of 900.", List.of(new MobMillenniaAgedBlaze())),
        MUSHROOM_BULL("<a>Mushroom Bull", "Born with digestive problems.", List.of(new MobMushroomBull())),
        SMOLDERING_BLAZE("<a>Smoldering Blaze", "It's on fire. But more than usual.", List.of(new MobSmolderingBlaze())),
        TENTACLE("<a>Tentacle", "Offspring of The Matriarch.", List.of(new MobHellwisp())),
        VANQUISHER("<a>Vanquisher", "The embodiment of vengeful souls that come for revenge.", List.of(new MobVanquisher())),
        WITHER_SKELETON("<a>Wither Skeleton", "Keepers of the Crimson Isle.", List.of(new MobWitherSkeleton())),
        WITHER_SPECTRE("<a>Wither Spectre", "Hasn't seen light in ages.", List.of(new MobWitherSpectre())),
        ;

        private final String name, description;
        private final List<BestiaryMob> mobs;

        CRIMSON_ISLE(String name, String description, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.mobs = mobs;
        }

        @Override
        public GUIMaterial getGuiMaterial() {
            return mobs.getFirst().getGuiMaterial();
        }
    }

    @Getter
    public enum CRYSTAL_HOLLOWS implements BestiaryEntry {
        AUTOMATON("<a>Automaton", "Ancient robots that still guard the Lost Precursor City.", List.of(new MobAutomaton67(), new MobAutomaton71())),
        BAL("<a>Bal", "A giant that guards the Topaz Crystal.", List.of(new MobBal())),
        BOSS_CORLEONE("<a>Boss Corleone", "Don't let him call down the Smog.", List.of(new MobBossCorleone())),
        BUTTERFLY("<a>Butterfly", "Flutters through the Fairy Grotto.", List.of(new MobButterfly())),
        GRUNT("<a>Grunt", "Team Treasurite's foot soldiers and the executives that command them.",
                List.of(new MobGrunt(), new MobExecutiveSebastian(), new MobExecutiveWendy(), new MobExecutiveViper())),
        KEY_GUARDIAN("<a>Key Guardian", "Keeps the key to the Jungle Temple.", List.of(new MobKeyGuardian())),
        SLUDGE("<a>Sludge", "Splits into smaller Sludges when defeated.",
                List.of(new MobSludgeSmall(), new MobSludgeMedium(), new MobSludgeLarge())),
        STONEWORM("<a>Stoneworm", "Hear it coming before you see it.", List.of(new MobStoneworm(), new MobScatha())),
        THYST("<a>Thyst", "Burrows out of Amethyst when it is mined.", List.of(new MobThyst())),
        YOG("<a>Yog", "A molten cube that roams the Magma Fields.", List.of(new MobYog())),
        ;

        private final String name, description;
        private final List<BestiaryMob> mobs;

        CRYSTAL_HOLLOWS(String name, String description, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.mobs = mobs;
        }

        @Override
        public GUIMaterial getGuiMaterial() {
            return mobs.getFirst().getGuiMaterial();
        }
    }

    /**
     * The 13 Kuudra Bestiary families with their kill caps and brackets (NEU repo constants, research K1 5.4).
     * The fight's mobs live on the Kuudra server and record kills under these ids.
     */
    @Getter
    public enum KUUDRA implements BestiaryEntry {
        BLAZING_GOLEM("BLAZING_GOLEM", "Blazing Golem", "Forged in the heat of Kuudra's Hollow.", 3, 10, Material.IRON_BLOCK, MobType.CONSTRUCT),
        BLIGHT("BLIGHT", "Blight", "Can't be hurt while it's angry.", 3, 20, Material.LEATHER_CHESTPLATE, MobType.UNDEAD),
        DROPSHIP("DROPSHIP", "Dropship", "Bombs away!", 3, 10, Material.GHAST_TEAR, MobType.AIRBORNE),
        EXPLOSIVE_IMP("EXPLOSIVE_IMP", "Explosive Imp", "Goes out with a bang.", 4, 20, Material.TNT, MobType.INFERNAL),
        INFERNO_MAGMA_CUBE("INFERNO_MAGMA_CUBE", "Inferno Magma Cube", "Hotter than your average Magma Cube.", 3, 20, Material.MAGMA_CREAM, MobType.CUBIC),
        KUUDRA_BERSERKER("KUUDRA_BERSERKER", "Kuudra Berserker", "Leaves something nasty behind.", 3, 20, Material.GOLDEN_AXE, MobType.INFERNAL),
        KUUDRA_FOLLOWER("KUUDRA_FOLLOWER", "Kuudra Follower", "Believes in the return of Kuudra.", 2, 20, Material.WITHER_SKELETON_SKULL, MobType.UNDEAD),
        KUUDRA_KNOCKER("KUUDRA_KNOCKER", "Kuudra Knocker", "Hits like a truck.", 3, 20, Material.PISTON, MobType.INFERNAL),
        KUUDRA_LANDMINE("KUUDRA_LANDMINE", "Kuudra Landmine", "Watch your step.", 3, 20, Material.STONE_PRESSURE_PLATE, MobType.INFERNAL),
        KUUDRA_SLASHER("KUUDRA_SLASHER", "Kuudra Slasher", "Leaps from place to place.", 5, 10, Material.IRON_SWORD, MobType.INFERNAL),
        MAGMA_FOLLOWER("MAGMA_FOLLOWER", "Magma Follower", "Stronger together.", 5, 10, Material.MAGMA_BLOCK, MobType.CUBIC),
        WANDERING_BLAZE("WANDERING_BLAZE", "Wandering Blaze", "Emerges from Kuudra's mouth.", 4, 20, Material.BLAZE_ROD, MobType.INFERNAL),
        WITHER_SENTRY("WITHER_SENTRY", "Wither Sentry", "Never leaves its post.", 4, 10, Material.WITHER_SKELETON_SKULL, MobType.WITHER),
        ;

        private final String name, description;
        private final List<BestiaryMob> mobs;

        KUUDRA(String mobId, String displayName, String description, int bracket, int maxTier, Material icon, MobType type) {
            this.name = "<a>" + displayName;
            this.description = description;
            this.mobs = List.of(CataloguedBestiaryMob.of(new CataloguedBestiaryMob.Family(
                    mobId, displayName, bracket, maxTier, new GUIMaterial(icon), type)));
        }

        @Override
        public GUIMaterial getGuiMaterial() {
            return mobs.getFirst().getGuiMaterial();
        }

        public static Set<String> mobIds() {
            return Arrays.stream(values()).map(family -> family.getMobs().getFirst().getMobID()).collect(Collectors.toUnmodifiableSet());
        }
    }
}
