package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

/**
 * What a YAML pet ability does. {@link #INACTIVE} is the default, so an ability nobody wired
 * reads "(Not active yet)" rather than claiming an effect it does not have.
 */
public enum PetAbilityKind {
    INACTIVE(false),
    TEXT(true),
    STAT(true),
    PERCENT_STAT(true),
    STAT_PER_STAT(true),
    DROP(true),
    DAMAGE_REDUCTION(true),
    HANDLER(true);

    private final boolean active;

    PetAbilityKind(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
