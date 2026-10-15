package dev.daweapon.dungeons.catacombs.classes;

public record DungeonClassAbility(
        String id,
        String displayName,
        DungeonClassAbilityType type,
        int cooldownSeconds,
        String description
) {}
