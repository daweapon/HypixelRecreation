package dev.daweapon.type.skyblockgeneric.user.pets.autopet;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEventType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AutopetOptions {
    public static final String MYTHOLOGICAL_MOB = "MYTHOLOGICAL";
    public static final String SEA_CREATURE = "SEA_CREATURE";

    private static final List<EntityType> VANILLA_MOBS = List.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN,
            EntityType.ENDERMITE, EntityType.SILVERFISH, EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.SLIME, EntityType.GHAST,
            EntityType.WITHER_SKELETON, EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN, EntityType.WITCH, EntityType.GUARDIAN,
            EntityType.SQUID, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.WOLF, EntityType.OCELOT, EntityType.PIG,
            EntityType.COW, EntityType.SHEEP, EntityType.CHICKEN, EntityType.RABBIT, EntityType.HORSE, EntityType.BAT,
            EntityType.VILLAGER, EntityType.HUSK, EntityType.STRAY, EntityType.ENDER_DRAGON, EntityType.WITHER);

    private AutopetOptions() {
    }

    public static List<AutopetOption> of(AutopetParam kind, SkyBlockPlayer player) {
        return switch (kind) {
            case NONE -> List.of();
            case PET -> ownedPets(player);
            case SKILL -> Arrays.stream(SkillCategories.values()).filter(skill -> !skill.isCosmetic())
                    .map(skill -> new AutopetOption(skill.name(), skill.toString(), Material.EXPERIENCE_BOTTLE)).toList();
            case ISLAND -> Arrays.stream(ServerType.values())
                    .filter(type -> type.name().startsWith("SKYBLOCK_") && type != ServerType.SKYBLOCK_CATACOMBS)
                    .map(type -> new AutopetOption(type.name(), StringUtility.toNormalCase(type.skyblockName()), Material.GRASS_BLOCK)).toList();
            case COLLECTION -> collections();
            case MOB -> mobs();
            case EVENT -> Arrays.stream(MiningEventType.values())
                    .map(event -> new AutopetOption(event.name(), event.displayName(), Material.CLOCK)).toList();
            case SLAYER -> Arrays.stream(SlayerType.values())
                    .map(slayer -> new AutopetOption(slayer.name(), slayer.displayName(), Material.ZOMBIE_HEAD)).toList();
        };
    }

    public static String label(AutopetParam kind, String id, SkyBlockPlayer player) {
        return of(kind, player).stream().filter(option -> option.id().equals(id)).map(AutopetOption::label).findFirst()
                .orElse(StringUtility.toNormalCase(id));
    }

    private static List<AutopetOption> ownedPets(SkyBlockPlayer player) {
        return player.getPetData().getPetsMap().keySet().stream()
                .map(pet -> pet.getAttributeHandler().getPotentialType())
                .filter(java.util.Objects::nonNull).distinct()
                .map(type -> new AutopetOption(type.name(), StringUtility.toNormalCase(type.name()), Material.BONE)).toList();
    }

    private static List<AutopetOption> collections() {
        List<AutopetOption> options = new ArrayList<>();
        CollectionCategories.getCategories().forEach(category -> Arrays.stream(category.getCollections())
                .forEach(collection -> options.add(new AutopetOption(collection.type().name(),
                        StringUtility.toNormalCase(collection.type().name()), Material.PAINTING))));
        return options;
    }

    private static List<AutopetOption> mobs() {
        List<AutopetOption> options = new ArrayList<>();
        VANILLA_MOBS.forEach(type -> options.add(new AutopetOption(type.key().value(), StringUtility.toNormalCase(type.key().value()), Material.ROTTEN_FLESH)));
        options.add(new AutopetOption(MYTHOLOGICAL_MOB, "Mythological Mob", Material.GOLDEN_SHOVEL));
        options.add(new AutopetOption(SEA_CREATURE, "Sea Creature", Material.FISHING_ROD));
        return options;
    }
}
