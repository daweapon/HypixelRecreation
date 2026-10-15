package dev.daweapon.type.skyblockgeneric.experiments;

import net.minestom.server.entity.Player;

public final class VanillaExperience {
    private VanillaExperience() {
    }

    public static int pointsToNextLevel(int level) {
        if (level >= 30) return 112 + (level - 30) * 9;
        if (level >= 15) return 37 + (level - 15) * 5;
        return 7 + level * 2;
    }

    public static long totalPoints(int level, float progress) {
        long total = 0;
        for (int i = 0; i < level; i++) total += pointsToNextLevel(i);
        return total + Math.round(progress * pointsToNextLevel(level));
    }

    public static int levelForPoints(long points) {
        int level = 0;
        while (points >= pointsToNextLevel(level)) {
            points -= pointsToNextLevel(level);
            level++;
        }
        return level;
    }

    public static float progressForPoints(long points) {
        int level = 0;
        while (points >= pointsToNextLevel(level)) {
            points -= pointsToNextLevel(level);
            level++;
        }
        return (float) points / pointsToNextLevel(level);
    }

    public static void add(Player player, long points) {
        long total = totalPoints(player.getLevel(), player.getExp()) + points;
        player.setLevel(levelForPoints(total));
        player.setExp(progressForPoints(total));
    }
}
