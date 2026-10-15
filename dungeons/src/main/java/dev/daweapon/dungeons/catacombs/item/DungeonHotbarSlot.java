package dev.daweapon.dungeons.catacombs.item;

import dev.daweapon.dungeons.catacombs.classes.DungeonClassAbility;

public record DungeonHotbarSlot(
        int slot,
        DungeonHotbarAction action,
        DungeonClassAbility ability
) {}
