package dev.daweapon.type.skyblockgeneric.spray;

import lombok.Getter;

import java.util.List;
import java.util.function.BiPredicate;

/** The Spray Can menu's filter, cycled in the order the wiki's UI lists it. */
@Getter
public enum SprayFilter {
    NONE("None", (spray, owned) -> true),
    UNLOCKED("Unlocked", (spray, owned) -> spray.isUnlockedFor(owned)),
    LOCKED("Locked", (spray, owned) -> !spray.isUnlockedFor(owned)),
    UTILITY("Utility", (spray, owned) -> spray.category() == SprayCategory.UTILITY),
    MISCELLANEOUS("Miscellaneous", (spray, owned) -> spray.category() == SprayCategory.MISCELLANEOUS),
    WINTER("Winter", (spray, owned) -> spray.category() == SprayCategory.WINTER),
    EASTER("Easter", (spray, owned) -> spray.category() == SprayCategory.EASTER),
    SUMMER("Summer", (spray, owned) -> spray.category() == SprayCategory.SUMMER),
    HALLOWEEN("Halloween", (spray, owned) -> spray.category() == SprayCategory.HALLOWEEN);

    private final String displayName;
    private final BiPredicate<Spray, java.util.Collection<String>> matches;

    SprayFilter(String displayName, BiPredicate<Spray, java.util.Collection<String>> matches) {
        this.displayName = displayName;
        this.matches = matches;
    }

    public List<Spray> apply(List<Spray> sprays, java.util.Collection<String> owned) {
        return sprays.stream().filter(spray -> matches.test(spray, owned)).toList();
    }

    public SprayFilter next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public SprayFilter previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }
}
