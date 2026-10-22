package dev.daweapon.type.skyblockgeneric.farming.animal;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public enum FarmAnimal {
    COW("cow", "COW_AXE", "Cow"),
    PIG("pig", "PIG_AXE", "Pig"),
    SHEEP("sheep", "SHEEP_AXE", "Sheep"),
    RABBIT("rabbit", "RABBIT_AXE", "Rabbit"),
    CHICKEN("chicken", "CHICKEN_AXE", "Chicken"),
    MOOSHROOM("mooshroom", "MUSHROOM_COW_AXE", "Mooshroom"),
    OASIS_SHEEP("oasis-sheep", "HARDENED_WOOD", "Oasis Sheep"),
    OASIS_RABBIT("oasis-rabbit", "LUCKY_DICE", "Oasis Rabbit"),
    LOCH_EMPEROR("loch-emperor", "GOLDEN_BALL", "The Loch Emperor");

    private final String dialogueKey;
    private final String unlocks;
    private final String displayName;

    FarmAnimal(String dialogueKey, String unlocks, String displayName) {
        this.dialogueKey = dialogueKey;
        this.unlocks = unlocks;
        this.displayName = displayName;
    }

    public String dialogueKey() {
        return dialogueKey;
    }

    public String unlocks() {
        return unlocks;
    }

    public String displayName() {
        return displayName;
    }

    public boolean consumedOnDelivery() {
        return this != LOCH_EMPEROR;
    }

    public boolean canBeLeashed() {
        return this != LOCH_EMPEROR;
    }

    public static @Nullable FarmAnimal of(SkyBlockMob mob) {
        if (mob instanceof SeaCreatureMob seaCreature) {
            return switch (seaCreature.getSeaCreatureId()) {
                case "OASIS_SHEEP" -> OASIS_SHEEP;
                case "OASIS_RABBIT" -> OASIS_RABBIT;
                case "THE_LOCH_EMPEROR" -> LOCH_EMPEROR;
                default -> null;
            };
        }
        EntityType type = mob.getEntityType();
        if (type == EntityType.COW) return COW;
        if (type == EntityType.PIG) return PIG;
        if (type == EntityType.SHEEP) return SHEEP;
        if (type == EntityType.RABBIT) return RABBIT;
        if (type == EntityType.CHICKEN) return CHICKEN;
        if (type == EntityType.MOOSHROOM) return MOOSHROOM;
        return null;
    }
}
