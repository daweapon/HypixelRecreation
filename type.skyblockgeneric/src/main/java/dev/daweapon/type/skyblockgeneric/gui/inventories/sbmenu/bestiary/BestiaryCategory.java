package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.CataloguedBestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobAshfang;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobAutomaton67;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobAutomaton71;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobButterfly;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobExecutiveSebastian;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobExecutiveViper;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobExecutiveWendy;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobGrunt;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobKeyGuardian;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobSludgeLarge;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobSludgeMedium;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobSludgeSmall;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobThyst;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.MobYog;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobBal;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobBossCorleone;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobScatha;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobStoneworm;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.DiamondGoblin;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.GoldenGoblin;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.MobGlaciteWalker;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteBowman;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteCaver;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteMage;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteMutt;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobLittlefoot;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.MobTreasureHoarder;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobCreeperlobber;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobMurderlover;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobPitfighter;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobPowderGhast;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobRaidFireslinger;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobRaidWeakling;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobStarSentry;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobSuperprotectron;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobFireslinger;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobGoblin;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobGoblinFlamethrower;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobKnifethrower;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBarbarianDukeX;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBezal;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBladesoul;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBlaze;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobDiveGhast;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobFlamingSpider;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobFlare;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobGhast;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobHellwisp;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobKadaKnight;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMageOutlaw;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMagmaBoss;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMagmaCube;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMagmaCubeRider;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMatcho;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMillenniaAgedBlaze;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMushroomBull;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMutatedBlaze;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobPackMagmaCube;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobSmolderingBlaze;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobVanquisher;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobWitherSkeleton;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobWitherSpectre;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobEmeraldSlime_05;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobEmeraldSlime_10;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobLapisZombie;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobMinerSkeleton_15;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobMinerSkeleton_20;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobMinerZombie_15;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobMinerZombie_20;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobRedstonePigman;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns.MobSneakyCreeper;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobGraveyardZombie;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobGraveyardZombieVillager;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobRuinsOldWolf;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobRuinsWolf;
import dev.daweapon.type.skyblockgeneric.entity.mob.island.IslandMobSpecies;
import dev.daweapon.type.skyblockgeneric.entity.mob.island.PrivateIslandMob;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalBestiary;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreature;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        BAT("<a>Bat", "Can be found hanging around.", new GUIMaterial("382fc3f71b41769376a9e92fe3adbaac3772b999b219c9d6b4680ba9983e527"), CataloguedBestiaryAreas.placeholder("BAT", "Bat", 2, 5, MobType.AIRBORNE, "382fc3f71b41769376a9e92fe3adbaac3772b999b219c9d6b4680ba9983e527")),
        SLIME("<a>Slime", "World's loudest creatures.", new GUIMaterial("895aeec6b842ada8669f846d65bc49762597824ab944f22f45bf3bbb941abe6c"), CataloguedBestiaryAreas.placeholder("SLIME", "Slime", 2, 5, MobType.CUBIC, "895aeec6b842ada8669f846d65bc49762597824ab944f22f45bf3bbb941abe6c")),
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
        CRYPT_GHOUL("<a>Crypt Ghoul", "Long buried, newly unburied.", new GUIMaterial("b3666d1bdf4558b0d16990b241e8185cbf58c3e03c24d904544e8cc65b1c38d1"), CataloguedBestiaryAreas.placeholder("CRYPT_GHOUL", "Crypt Ghoul", 2, 20, MobType.UNDEAD, "b3666d1bdf4558b0d16990b241e8185cbf58c3e03c24d904544e8cc65b1c38d1")),
        GOLDEN_GHOUL("<a>Golden Ghoul", "A ghoul that fell into molten gold.", new GUIMaterial("69be1cf0639c6b7db506686f26af222671ce52321e1c921b296933cd40ac4c62"), CataloguedBestiaryAreas.placeholder("GOLDEN_GHOUL", "Golden Ghoul", 4, 15, MobType.UNDEAD, "69be1cf0639c6b7db506686f26af222671ce52321e1c921b296933cd40ac4c62")),
        SHINY_PIG("<a>Shiny Pig", "Found all around the Hub during the Year of the Pig!", new GUIMaterial("621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4"), CataloguedBestiaryAreas.placeholder("SHINY_PIG", "Shiny Pig", 5, 15, MobType.ANIMAL, "621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4")),
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
                new GUIMaterial("a68224cf8ed4c03b457bf49beb6f544193679268b8421b01ffce6d27b25ac32d"), List.of(new MobSneakyCreeper())),
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
        GHOST("<a>Ghost", "It is hard to find that which you cannot see.", CataloguedBestiaryAreas.placeholder("GHOST", "Ghost", 2, 20, MobType.ELUSIVE, "981977918a1180e0dec879e6bcd1aa39494977bb913be2ab01aff1db1fa4")),
        GLACITE_BOWMAN("<a>Glacite Bowman", "Unable to see, it hunts by sound.", List.of(new MobGlaciteBowman())),
        GLACITE_CAVER("<a>Glacite Caver", "Dug too deep and never looked back.", List.of(new MobGlaciteCaver())),
        GLACITE_MAGE("<a>Glacite Mage", "Unknowing and unpredictable in its destruction.", List.of(new MobGlaciteMage())),
        GLACITE_MUTT("<a>Glacite Mutt", "Remarkably loyal, even in death.", List.of(new MobGlaciteMutt())),
        GOBLIN_RAIDERS("<a>Goblin Raiders", "If it moves, it gets smashed.",
                List.of(new MobRaidWeakling(), new MobRaidFireslinger(), new MobCreeperlobber(), new MobPitfighter(), new MobMurderlover(), new MobSuperprotectron())),
        LITTLEFOOT("<a>Littlefoot", "You've probably heard of his older brother.", List.of(new MobLittlefoot())),
        POWDER_GHAST("<a>Powder Ghast", "Powder and coal replaces ash and soot!", List.of(new MobPowderGhast())),
        STAR_SENTRY("<a>Star Sentry", "Defenders of the Fallen Star.", List.of(new MobStarSentry())),
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
        BLAZING_GOLEM("BLAZING_GOLEM", "Blazing Golem", "Forged in the heat of Kuudra's Hollow.", 6, 10, Material.IRON_BLOCK, MobType.CONSTRUCT),
        BLIGHT("BLIGHT", "Blight", "Can't be hurt while it's angry.", 3, 20, Material.LEATHER_CHESTPLATE, MobType.UNDEAD),
        DROPSHIP("DROPSHIP", "Dropship", "Bombs away!", 6, 10, Material.GHAST_TEAR, MobType.AIRBORNE),
        EXPLOSIVE_IMP("EXPLOSIVE_IMP", "Explosive Imp", "Goes out with a bang.", 4, 20, Material.TNT, MobType.INFERNAL),
        INFERNO_MAGMA_CUBE("INFERNO_MAGMA_CUBE", "Inferno Magma Cube", "Hotter than your average Magma Cube.", 3, 20, Material.MAGMA_CREAM, MobType.CUBIC),
        KUUDRA_BERSERKER("KUUDRA_BERSERKER", "Kuudra Berserker", "Leaves something nasty behind.", 3, 20, Material.GOLDEN_AXE, MobType.INFERNAL),
        KUUDRA_FOLLOWER("KUUDRA_FOLLOWER", "Kuudra Follower", "Believes in the return of Kuudra.", 2, 20, Material.WITHER_SKELETON_SKULL, MobType.UNDEAD),
        KUUDRA_KNOCKER("KUUDRA_KNOCKER", "Kuudra Knocker", "Hits like a truck.", 3, 20, Material.PISTON, MobType.INFERNAL),
        KUUDRA_LANDMINE("KUUDRA_LANDMINE", "Kuudra Landmine", "Watch your step.", 3, 20, Material.STONE_PRESSURE_PLATE, MobType.INFERNAL),
        KUUDRA_SLASHER("KUUDRA_SLASHER", "Kuudra Slasher", "Leaps from place to place.", 6, 10, Material.IRON_SWORD, MobType.INFERNAL),
        MAGMA_FOLLOWER("MAGMA_FOLLOWER", "Magma Follower", "Stronger together.", 6, 10, Material.MAGMA_BLOCK, MobType.CUBIC),
        WANDERING_BLAZE("WANDERING_BLAZE", "Wandering Blaze", "Emerges from Kuudra's mouth.", 4, 20, Material.BLAZE_ROD, MobType.INFERNAL),
        WITHER_SENTRY("WITHER_SENTRY", "Wither Sentry", "Never leaves its post.", 6, 10, Material.WITHER_SKELETON_SKULL, MobType.WITHER),
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

    /**
     * The 35 Catacombs Bestiary families (wiki Bestiary list: bracket, max tier and lore; NEU constants/bestiary.json:
     * icons and which mobs each family holds). Kills are kept under the family's own id; {@code dungeonMobIds} are the
     * dungeon mob ids whose kills count for it, including ids of mobs the Catacombs server does not spawn yet.
     */
    @Getter
    public enum CATACOMBS implements BestiaryEntry {
        ANGRY_ARCHAEOLOGIST("Angry Archaeologist", "Angered by what he's found below.", 5, 20, MobType.HUMANOID,
                "c48c783458e4cf8518e8ab5863fbc4cb948f90568eeb9a60d16c4fde2b96c033", "ANGRY_ARCHAEOLOGIST"),
        BAT("Bat", "Likes to keep to themselves.", 4, 15, MobType.AIRBORNE,
                "382fc3f71b41769376a9e92fe3adbaac3772b999b219c9d6b4680ba9983e527", "BAT"),
        CELLAR_SPIDER("Cellar Spider", "Creepy crawler.", 5, 15, MobType.ARTHROPOD,
                "41645dfd77d09923107b3496e94eeb5c30329f97efc96ed76e226e98224", "CELLAR_SPIDER"),
        CRYPT_DREADLORD("Crypt Dreadlord", "Trained in the dark art of wither magic.", 5, 25, MobType.UNDEAD,
                "66f349270a3b851896cdad842f5eec6e140bd9119b75c074955c3be7865e27c7", "CRYPT_DREADLORD"),
        CRYPT_LURKER("Crypt Lurker", "Lurking in the shadows.", 2, 15, MobType.UNDEAD,
                "b3666d1bdf4558b0d16990b241e8185cbf58c3e03c24d904544e8cc65b1c38d1", "CRYPT_LURKER"),
        CRYPT_SOULEATER("Crypt Souleater", "A soul a day keeps the doctor away.", 5, 25, MobType.UNDEAD,
                "66f349270a3b851896cdad842f5eec6e140bd9119b75c074955c3be7865e27c7", "CRYPT_SOULEATER"),
        FELS("Fels", "Hide and Seek Champion.", 3, 15, MobType.ENDER,
                "bc6a4072c72e27b03234d650b0e52815d84657536daa23d1140b4c7b7f8d1dde", "FEL"),
        GOLEM("Golem", "Prefers to sleep in, grumpy if disturbed.", 4, 15, MobType.CONSTRUCT,
                "22bcaceeb4162f400d44743315932ac820d3119ac8986a0161a726161ccc93fc", "SADAN_GOLEM"),
        KING_MIDAS("King Midas", "He loves gold.", 7, 20, MobType.HUMANOID,
                "62bca085750043503f5df9f7def824a2e3acfc27842bcd09d2b6695881e832f5", "KING_MIDAS"),
        LONELY_SPIDER("Lonely Spider", "So lonely.", 3, 15, MobType.ARTHROPOD,
                "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1", "LONELY_SPIDER"),
        LOST_ADVENTURER("Lost Adventurer", "Long lost to the depths of the Catacombs.", 7, 25, MobType.HUMANOID,
                "1e0329f42192ee7c1a0c704f82dbbbe7c00fbfa3f020c1067a208630b991b988", "LOST_ADVENTURER", "FROZEN_ADVENTURER"),
        MIMIC("Mimic", "Classic RPG trope, in block form.", 4, 15, MobType.UNDEAD,
                "e19c12543bc7792605ef68e1f8749ae8f2a381d9085d4d4b780ba1282d3597a0", "MIMIC"),
        SCARED_SKELETON("Scared Skeleton", "Scared of its own shadow.", 5, 20, MobType.SKELETAL,
                "88eb68a4d38e9f446b8e99275f160302f366ef10216abf948487e582512bd0f3", "SCARED_SKELETON"),
        SHADOW_ASSASSIN("Shadow Assassin", "Silent, but deadly.", 7, 25, MobType.HUMANOID,
                "3399e00f404411e465d74388df132d51fe868ecf86f1c073faffa1d9172ec0f3", "SHADOW_ASSASSIN"),
        SKELETON_GRUNT("Skeleton Grunt", "Just more grunts in the platoon.", 3, 15, MobType.SKELETAL,
                "70471677b37ae842c2bd232e16eeb84d5a493231eecec072da38be3127dd5c8", "SKELETON_GRUNT"),
        SKELETON_LORD("Skeleton Lord", "Despite the lack of organs, still managed to be promoted.", 5, 20, MobType.SKELETAL,
                "2d16de24cc605243df322a75dcdb90ea65fbec5e45f2f10beda5640a9e5f0626", "SKELETON_LORD"),
        SKELETON_MASTER("Skeleton Master", "Leader of the Catacombs' skeleton army.", 5, 25, MobType.SKELETAL,
                "74e95e1b7dc82ba7845a68fc6a312f4cd90ee2f6cce26a68c88b04b1072d879", "SKELETON_MASTER"),
        SKELETON_SOLDIER("Skeleton Soldier", "Defenders of the Catacombs.", 3, 20, MobType.SKELETAL,
                "219688e0c2f05aeb979d6a1b8c911957b7d3657e14b57af93c5ef6f6a59569dd", "SKELETON_SOLDIER"),
        SKELETOR("Skeletor", "I am Skeletor.", 3, 15, MobType.SKELETAL,
                "89d074ad9b9971879eb325bddff3675f7224856bd6d569fc8d483c133d73005d", "SKELETOR", "SKELETOR_PRIME"),
        SNIPER("Sniper", "Said to have sniped adventurers from floors away.", 4, 15, MobType.SKELETAL,
                "b18c071f080dba50a62a6263ff724edc157ce4fb4883cceff2491d5bbde830c1", "SNIPER"),
        SUPER_ARCHER("Super Archer", "Bouncy, yet dangerous.", 3, 15, MobType.SKELETAL,
                "dcae6db0b59a64305076d966d8e7b9a97be46dae3a807714fa86d37848f6", "SUPER_ARCHER"),
        SUPER_TANK_ZOMBIE("Super Tank Zombie", "Heftiest zombie.", 2, 15, MobType.UNDEAD,
                "b3666d1bdf4558b0d16990b241e8185cbf58c3e03c24d904544e8cc65b1c38d1", "SUPER_TANK_ZOMBIE"),
        TANK_ZOMBIE("Tank Zombie", "Hefty zombie.", 3, 15, MobType.UNDEAD,
                "b3666d1bdf4558b0d16990b241e8185cbf58c3e03c24d904544e8cc65b1c38d1", "TANK_ZOMBIE"),
        TERRACOTTA("Terracotta", "Stained Hardened Clay.", 2, 15, MobType.CONSTRUCT,
                "6570a2d2a362adeeb05e6f25edcd54cac335f22f0e4669e9c1ebd9a02573bcab", "TERRACOTTA"),
        UNDEAD("Undead", "The Watcher's personal henchmen.", 2, 15, MobType.UNDEAD,
                "f4624a9a8c69ca204504abb043d47456cd9b09749a36357462303f276a229d4", "WATCHER_UNDEAD"),
        UNDEAD_SKELETON("Undead Skeleton", "Redundant name.", 5, 25, MobType.SKELETAL,
                "88eb68a4d38e9f446b8e99275f160302f366ef10216abf948487e582512bd0f3", "UNDEAD_SKELETON"),
        WITHER_GUARD("Wither Guard", "Never stopped anyone from entering the factory, but is trying his best.", 5, 25, MobType.WITHER,
                "f5ec964645a8efac76be2f160d7c9956362f32b6517390c59c3085034f050cff", "WITHER_GUARD"),
        WITHER_HUSK("Wither Husk", "A shell of their former self.", 5, 25, MobType.WITHER,
                "f5ec964645a8efac76be2f160d7c9956362f32b6517390c59c3085034f050cff", "WITHER_HUSK"),
        WITHER_MINER("Wither Miner", "Kept in servitude for breaking blocks around the factory.", 4, 25, MobType.WITHER,
                "f5ec964645a8efac76be2f160d7c9956362f32b6517390c59c3085034f050cff", "WITHER_MINER"),
        WITHERMANCER("Withermancer", "Learned their sorcery from the most vicious of teachers.", 5, 25, MobType.WITHER,
                "f5ec964645a8efac76be2f160d7c9956362f32b6517390c59c3085034f050cff", "WITHERMANCER"),
        ZOMBIE_COMMANDER("Zombie Commander", "Leader of the Undead.", 4, 20, MobType.UNDEAD,
                "e3976ef4f9d68cfc0f24f6b313713a5f2abbcfff32a27c514c1392dfe619191e", "ZOMBIE_COMMANDER"),
        ZOMBIE_GRUNT("Zombie Grunt", "Just more grunts in the platoon.", 3, 15, MobType.UNDEAD,
                "e3976ef4f9d68cfc0f24f6b313713a5f2abbcfff32a27c514c1392dfe619191e", "ZOMBIE_GRUNT"),
        ZOMBIE_KNIGHT("Zombie Knight", "Knighted by Necron himself.", 3, 15, MobType.UNDEAD,
                "25d2f31ba162fe6272e831aed17f53213db6fa1c4cbe4fc827f3963cc98b9", "ZOMBIE_KNIGHT"),
        ZOMBIE_LORD("Zombie Lord", "Commander among commanders.", 5, 20, MobType.UNDEAD,
                "e3976ef4f9d68cfc0f24f6b313713a5f2abbcfff32a27c514c1392dfe619191e", "ZOMBIE_LORD"),
        ZOMBIE_SOLDIER("Zombie Soldier", "Defenders of the Catacombs.", 3, 20, MobType.UNDEAD,
                "e3976ef4f9d68cfc0f24f6b313713a5f2abbcfff32a27c514c1392dfe619191e", "ZOMBIE_SOLDIER"),
        ;

        public static final String KILL_KEY_PREFIX = "CATACOMBS_";
        private static final Map<String, CATACOMBS> BY_DUNGEON_MOB = Arrays.stream(values())
                .flatMap(family -> family.dungeonMobIds.stream().map(id -> Map.entry(id, family)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        private final String name, description;
        private final List<BestiaryMob> mobs;
        private final List<String> dungeonMobIds;

        CATACOMBS(String displayName, String description, int bracket, int maxTier, MobType type, String texture, String... dungeonMobIds) {
            this.name = "<a>" + displayName;
            this.description = description;
            this.mobs = List.of(CataloguedBestiaryMob.of(new CataloguedBestiaryMob.Family(
                    KILL_KEY_PREFIX + name(), displayName, bracket, maxTier, new GUIMaterial(texture), type)));
            this.dungeonMobIds = List.of(dungeonMobIds);
        }

        @Override
        public GUIMaterial getGuiMaterial() {
            return mobs.getFirst().getGuiMaterial();
        }

        /** The family mob whose kill count a kill of this dungeon mob raises, if the mob is in the Bestiary. */
        public static Optional<BestiaryMob> forDungeonMob(String dungeonMobId) {
            return Optional.ofNullable(BY_DUNGEON_MOB.get(dungeonMobId)).map(family -> family.mobs.getFirst());
        }
    }

    /** The 7 Spooky Festival land-mob families (wiki Bestiary list; NEU icons). None of these mobs spawn yet. */
    @Getter
    public enum SPOOKY_FESTIVAL implements BestiaryEntry {
        CRAZY_WITCH("BATTY_WITCH", "Crazy Witch", "This witch is brewing up trouble.", 6,
                "fce6604157fc4ab5591e4bcf507a749918ee9c41e357d47376e0ee7342074c90"),
        HEADLESS_HORSEMAN("HEADLESS_HORSEMAN", "Headless Horseman", "Some say he lost his mind long before he lost his head.", 7,
                "4c6570f1242992f6eba23ee582598c39e3e745383273deef8b3977583fe3cf5"),
        PHANTOM_SPIRIT("PHANTOM_SPIRIT", "Phantom Spirit", "Possess the supernatural ability to...possess...you.", 6,
                "653b2f7c5e177bd67cead30d0ee53425c7684c975c8c2a5273a09ca49a1f6cdd"),
        SCARY_JERRY("SCARY_JERRY", "Scary Jerry", "He's very scary.", 6,
                "df202dc24d15f7ce36e02fb4b3e8153b146ab9c1724aaa5d484c741da0eeb6fd"),
        TRICK_OR_TREATER("TRICK_OR_TREATER", "Trick or Treater", "It's always a trick.", 6,
                "d7cbe061b445b88cb2df59accc8d2c1c12118fe0b2127e6e75813550adac67cf"),
        WITHER_GOURD("WITHER_GOURD", "Wither Gourd", "Who throws a pumpkin? Honestly!", 6,
                "68f2ffc6fb4e9959b9a7a317f51a6775a159ddc2241dbd6c774d3ac08b6"),
        WRAITH("WRAITH", "Wraith", "Haunting SkyBlock islands everywhere.", 6,
                "aeabc3075cf41f38e6df1236b95caafcba1bee22c49d804b9427488c2f6e0efc"),
        ;

        private static final int MAX_TIER = 15;

        private final String name, description;
        private final List<BestiaryMob> mobs;

        SPOOKY_FESTIVAL(String mobId, String displayName, String description, int bracket, String texture) {
            this.name = "<a>" + displayName;
            this.description = description;
            this.mobs = List.of(CataloguedBestiaryMob.of(new CataloguedBestiaryMob.Family(
                    mobId, displayName, bracket, MAX_TIER, new GUIMaterial(texture), MobType.SPOOKY)));
        }

        @Override
        public GUIMaterial getGuiMaterial() {
            return mobs.getFirst().getGuiMaterial();
        }
    }
}
