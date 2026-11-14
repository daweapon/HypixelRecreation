package dev.daweapon.type.bedwarsgame.death;

import lombok.Getter;

@Getter
// Classifies how a Bedwars player died (void, combat, bow, assisted, etc).
public enum BedWarsDeathType {
    VOID,
    VOID_ASSISTED,
    GENERIC,
    GENERIC_ASSISTED,
    BOW,
    ENTITY,
}

