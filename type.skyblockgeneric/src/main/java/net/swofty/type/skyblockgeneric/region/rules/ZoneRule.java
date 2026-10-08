package net.swofty.type.skyblockgeneric.region.rules;

import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

/**
 * A place with its own buff rules. While a player matches, their potion effects and God Potion are paused
 * (when {@code suspendsBuffs}) and their Health is capped at {@code maxHealth} (when set).
 */
public record ZoneRule(String id, Predicate<SkyBlockPlayer> applies, boolean suspendsBuffs, @Nullable Double maxHealth) {

    public static ZoneRule regions(String id, Set<RegionType> regions, boolean suspendsBuffs, @Nullable Double maxHealth) {
        return new ZoneRule(id, player -> inAny(player, regions), suspendsBuffs, maxHealth);
    }

    public ZoneRule or(Predicate<SkyBlockPlayer> other) {
        return new ZoneRule(id, applies.or(other), suspendsBuffs, maxHealth);
    }

    private static boolean inAny(SkyBlockPlayer player, Set<RegionType> regions) {
        SkyBlockRegion region = player.getRegion();
        return region != null && regions.contains(region.getType());
    }
}
