package dev.daweapon.type.generic.achievement.gui;

import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.function.Consumer;

public record AchievementNav(String label, Consumer<HypixelPlayer> back) {
}
