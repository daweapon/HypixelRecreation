package dev.daweapon.type.skyblockgeneric.user.pets.autopet;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record AutopetRule(AutopetTrigger trigger, @Nullable String param, String petType, String petRarity, List<Condition> exceptions) {
    public static final int MAX_EXCEPTIONS = 5;

    public record Condition(AutopetException exception, @Nullable String param) {
    }
}
