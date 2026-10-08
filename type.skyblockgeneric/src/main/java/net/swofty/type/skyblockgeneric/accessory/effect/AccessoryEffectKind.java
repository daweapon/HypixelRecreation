package net.swofty.type.skyblockgeneric.accessory.effect;

public enum AccessoryEffectKind {
    STAT,
    DAMAGE_REDUCTION,
    DAMAGE_BONUS,
    HANDLER,
    TEXT,
    INACTIVE;

    public boolean isActive() {
        return this != INACTIVE;
    }
}
