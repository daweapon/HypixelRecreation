package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
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

import java.util.List;
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
}
