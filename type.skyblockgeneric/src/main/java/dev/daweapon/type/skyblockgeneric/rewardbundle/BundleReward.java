package dev.daweapon.type.skyblockgeneric.rewardbundle;

import dev.daweapon.type.generic.user.categories.Rank;

public record BundleReward(BundleSeason season, int year, Rank rank, Type type, String id, int amount) {
    public enum Type {
        FURNITURE, SPRAY, MINION_SKIN
    }

    public String key() {
        return season + ":" + year + ":" + rank + ":" + type + ":" + id;
    }
}
