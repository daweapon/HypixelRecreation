package net.swofty.type.generic.achievement.gui;

import net.swofty.type.generic.user.HypixelPlayer;

import java.util.function.Consumer;

public record AchievementNav(String label, Consumer<HypixelPlayer> back) {
}
