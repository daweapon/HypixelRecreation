package dev.daweapon.type.skyblockgeneric.accessory;

/**
 * Where an accessory works. Rift-only and Bingo-only accessories never add Accessory Power and only
 * apply inside their realm; neither realm exists on this server yet.
 */
public enum AccessoryRealm {
    ANYWHERE,
    RIFT,
    BINGO;

    public boolean isActive() {
        return this == ANYWHERE;
    }

    public boolean countsForPower() {
        return this == ANYWHERE;
    }
}
