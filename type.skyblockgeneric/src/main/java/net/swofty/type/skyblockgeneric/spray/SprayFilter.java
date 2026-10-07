package net.swofty.type.skyblockgeneric.spray;

import lombok.Getter;

import java.util.List;
import java.util.function.Predicate;

/** The Spray Can menu's filter, cycled in the order the wiki's UI lists it. */
@Getter
public enum SprayFilter {
    NONE("None", spray -> true),
    UNLOCKED("Unlocked", Spray::isUnlocked),
    LOCKED("Locked", spray -> !spray.isUnlocked()),
    UTILITY("Utility", spray -> spray.category() == SprayCategory.UTILITY),
    MISCELLANEOUS("Miscellaneous", spray -> spray.category() == SprayCategory.MISCELLANEOUS),
    WINTER("Winter", spray -> spray.category() == SprayCategory.WINTER),
    EASTER("Easter", spray -> spray.category() == SprayCategory.EASTER),
    SUMMER("Summer", spray -> spray.category() == SprayCategory.SUMMER),
    HALLOWEEN("Halloween", spray -> spray.category() == SprayCategory.HALLOWEEN);

    private final String displayName;
    private final Predicate<Spray> matches;

    SprayFilter(String displayName, Predicate<Spray> matches) {
        this.displayName = displayName;
        this.matches = matches;
    }

    public List<Spray> apply(List<Spray> sprays) {
        return sprays.stream().filter(matches).toList();
    }

    public SprayFilter next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public SprayFilter previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }
}
