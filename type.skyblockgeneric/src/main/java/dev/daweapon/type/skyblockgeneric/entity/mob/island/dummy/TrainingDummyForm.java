package dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Objects;

public enum TrainingDummyForm {
    BLAZE("Blaze", EntityType.BLAZE, "blaze", MobType.INFERNAL),
    CAVE_SPIDER("Cave Spider", EntityType.CAVE_SPIDER, "cave_spider", MobType.ARTHROPOD),
    CREEPER("Creeper", EntityType.CREEPER, "creeper", MobType.CUBIC),
    ENDERMAN("Enderman", EntityType.ENDERMAN, "enderman", MobType.ENDER),
    ENDERMITE("Endermite", EntityType.ENDERMITE, "endermite", MobType.ARTHROPOD, MobType.ENDER),
    GUARDIAN("Guardian", EntityType.GUARDIAN, "guardian", MobType.AQUATIC, MobType.ARCANE, MobType.SKELETAL),
    IRON_GOLEM("Iron Golem", EntityType.IRON_GOLEM, "iron_golem", MobType.CONSTRUCT),
    MAGMA_CUBE("Magma Cube", EntityType.MAGMA_CUBE, "magma_cube", MobType.CUBIC, MobType.INFERNAL),
    ZOMBIE_PIGMAN("Zombie Pigman", EntityType.ZOMBIFIED_PIGLIN, "zombified_piglin", MobType.ANIMAL, MobType.HUMANOID),
    SILVERFISH("Silverfish", EntityType.SILVERFISH, "silverfish", MobType.ARTHROPOD),
    SKELETON("Skeleton", EntityType.SKELETON, "skeleton", MobType.SKELETAL),
    SLIME("Slime", EntityType.SLIME, "slime", MobType.CUBIC),
    SPIDER("Spider", EntityType.SPIDER, "spider", MobType.ARTHROPOD),
    WITCH("Witch", EntityType.WITCH, "witch", MobType.ARCANE, MobType.HUMANOID),
    WITHER_SKELETON("Wither Skeleton", EntityType.WITHER_SKELETON, "wither_skeleton", MobType.SKELETAL),
    ZOMBIE("Zombie", EntityType.ZOMBIE, "zombie", MobType.UNDEAD);

    public static final TrainingDummyForm DEFAULT = ZOMBIE;

    private final String label;
    private final EntityType entityType;
    private final String eggKey;
    private final List<MobType> mobTypes;

    TrainingDummyForm(String label, EntityType entityType, String eggKey, MobType... mobTypes) {
        this.label = label;
        this.entityType = entityType;
        this.eggKey = eggKey;
        this.mobTypes = List.of(mobTypes);
    }

    public String label() {
        return label;
    }

    public EntityType entityType() {
        return entityType;
    }

    public List<MobType> mobTypes() {
        return mobTypes;
    }

    public Material icon() {
        return Objects.requireNonNull(Material.fromKey("minecraft:" + eggKey + "_spawn_egg"), eggKey);
    }

    public static TrainingDummyForm parse(String raw) {
        for (TrainingDummyForm form : values()) {
            if (form.name().equals(raw)) return form;
        }
        return DEFAULT;
    }
}
