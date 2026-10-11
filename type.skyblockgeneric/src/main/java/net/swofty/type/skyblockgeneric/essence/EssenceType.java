package net.swofty.type.skyblockgeneric.essence;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum EssenceType {
    WITHER("Wither", "Dungeons aren't in this server yet."),
    UNDEAD("Undead", "Dungeons aren't in this server yet."),
    CRIMSON("Crimson", null),
    SPIDER("Spider", "Arachne isn't in this server yet."),
    DRAGON("Dragon", "The Ender Dragon fight isn't in this server yet."),
    GOLD("Gold", "Dungeons aren't in this server yet."),
    DIAMOND("Diamond", "Dungeons aren't in this server yet."),
    ICE("Ice", null),
    FOREST("Forest", null),
    FOSSIL("Fossil", null),
    SUN_GECKO("Sun Gecko", "The Rift isn't in this server yet."),
    SAFARI("Safari", "Hunting isn't in this server yet.");

    public static final long CAP = 2_000_000_000L;

    private final String displayName;
    private final @Nullable String unavailableReason;

    EssenceType(String displayName, @Nullable String unavailableReason) {
        this.displayName = displayName;
        this.unavailableReason = unavailableReason;
    }

    public boolean isAvailable() {
        return unavailableReason == null;
    }

    public String getFullName() {
        return displayName + " Essence";
    }
}
