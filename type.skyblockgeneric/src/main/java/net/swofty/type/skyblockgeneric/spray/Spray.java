package net.swofty.type.skyblockgeneric.spray;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * One Spray Can spray. {@code directions} is 1 for a plain picture, or 4 or 8 for the arrow sprays,
 * whose picture is turned to the chosen direction.
 */
public record Spray(String id, String name, SprayCategory category, SpraySource source, int directions,
                    @Nullable String image, @Nullable String rank, List<String> lore, Map<SprayInk, Integer> ink) {

    public boolean isUnlocked() {
        return source.isUnlocked() && image != null;
    }

    public boolean hasDirections() {
        return directions > 1;
    }
}
