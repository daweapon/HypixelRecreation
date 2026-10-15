package dev.daweapon.type.skyblockgeneric.user;

import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;

class PlayerExperienceHandler {
    private final SkyBlockPlayer player;

    PlayerExperienceHandler(SkyBlockPlayer player) {
        this.player = player;
    }

    Long getExperience() {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.EXPERIENCE, DatapointLong.class).getValue();
    }

    void setExperience(long value) {
        long total = Math.max(0, value);
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.EXPERIENCE, DatapointLong.class).setValue(total);
        int level = experienceLevel(total);
        long into = total - experienceAtLevel(level);
        long span = experienceAtLevel(level + 1) - experienceAtLevel(level);
        player.setLevel(level);
        player.setExp(span > 0 ? Math.clamp((float) into / span, 0f, 0.9999f) : 0f);
    }

    /** The vanilla level for a total of experience points; the closed form is only a first guess near boundaries. */
    static int experienceLevel(long total) {
        int level;
        if (total <= 352) {
            level = (int) (Math.sqrt(total + 9) - 3);
        } else if (total <= 1507) {
            level = (int) (8.1 + Math.sqrt((2.0 / 5.0) * (total - 195.975)));
        } else {
            level = (int) (18.0555 + Math.sqrt((2.0 / 9.0) * (total - 752.9861)));
        }
        level = Math.max(0, level);
        while (level > 0 && experienceAtLevel(level) > total) level--;
        while (experienceAtLevel(level + 1) <= total) level++;
        return level;
    }

    static long experienceAtLevel(int level) {
        if (level <= 16) return (long) level * level + 6L * level;
        if (level <= 31) return (long) (2.5 * level * level - 40.5 * level + 360);
        return (long) (4.5 * level * level - 162.5 * level + 2220);
    }

    void addExperience(long value) {
        setExperience(getExperience() + value);
    }

    void removeExperience(long value) {
        setExperience(getExperience() - value);
    }
}
