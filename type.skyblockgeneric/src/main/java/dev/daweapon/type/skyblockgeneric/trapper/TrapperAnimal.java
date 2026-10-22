package dev.daweapon.type.skyblockgeneric.trapper;

import lombok.Getter;
import net.minestom.server.entity.EntityType;

@Getter
public enum TrapperAnimal {
    COW(EntityType.COW, "Cow", 70),
    PIG(EntityType.PIG, "Pig", 70),
    SHEEP(EntityType.SHEEP, "Sheep", 70),
    RABBIT(EntityType.RABBIT, "Rabbit", 70),
    CHICKEN(EntityType.CHICKEN, "Chicken", 70),
    HORSE(EntityType.HORSE, "Horse", 130);

    private final EntityType entityType;
    private final String displayName;
    private final int speedBonus;

    TrapperAnimal(EntityType entityType, String displayName, int speedBonus) {
        this.entityType = entityType;
        this.displayName = displayName;
        this.speedBonus = speedBonus;
    }
}
