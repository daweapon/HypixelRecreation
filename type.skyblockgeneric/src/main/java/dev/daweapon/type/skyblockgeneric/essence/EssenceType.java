package dev.daweapon.type.skyblockgeneric.essence;

import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsAvailability;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum EssenceType {
    WITHER("Wither", Source.CATACOMBS),
    UNDEAD("Undead", Source.CATACOMBS),
    CRIMSON("Crimson"),
    SPIDER("Spider"),
    DRAGON("Dragon"),
    GOLD("Gold", Source.CATACOMBS),
    DIAMOND("Diamond", Source.CATACOMBS),
    ICE("Ice"),
    FOREST("Forest"),
    FOSSIL("Fossil"),
    SUN_GECKO("Sun Gecko", "The Rift isn't in this server yet."),
    SAFARI("Safari", "Hunting isn't in this server yet.");

    public static final long CAP = 2_000_000_000L;

    private final String displayName;
    @Getter(AccessLevel.NONE)
    private final @Nullable String unavailableReason;
    @Getter(AccessLevel.NONE)
    private final Source source;

    EssenceType(String displayName) {
        this(displayName, (String) null);
    }

    EssenceType(String displayName, @Nullable String unavailableReason) {
        this.displayName = displayName;
        this.unavailableReason = unavailableReason;
        this.source = Source.FIXED;
    }

    EssenceType(String displayName, Source source) {
        this.displayName = displayName;
        this.unavailableReason = null;
        this.source = source;
    }

    public @Nullable String getUnavailableReason() {
        if (source == Source.CATACOMBS) return CatacombsAvailability.isPublic() ? null : CatacombsAvailability.UNAVAILABLE_REASON;
        return unavailableReason;
    }

    public boolean isAvailable() {
        return getUnavailableReason() == null;
    }

    private enum Source {
        FIXED,
        CATACOMBS
    }

    public String getFullName() {
        return displayName + " Essence";
    }
}
