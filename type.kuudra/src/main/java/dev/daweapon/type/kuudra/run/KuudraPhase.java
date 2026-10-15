package dev.daweapon.type.kuudra.run;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The stages of a Kuudra fight. Basic and Hot end after shooting Kuudra; Burning and up alternate stunning
 * and damaging him; Infernal drops into the lair at 25% health.
 */
public enum KuudraPhase {
    SUPPLIES("supplies"),
    BUILD("build"),
    FUEL(null),
    STUN(null),
    DPS(null),
    LAIR(null);

    private final @Nullable String dialogueKey;

    KuudraPhase(@Nullable String dialogueKey) {
        this.dialogueKey = dialogueKey;
    }

    /** Elle's lines when the phase begins, from {@code dialogue.yml}. */
    public @Nullable String dialogueKey(boolean stunRequired) {
        if (this == FUEL) return stunRequired ? "ballista_ready_stun" : "ballista_ready";
        if (this == DPS && stunRequired) return "stunned";
        return dialogueKey;
    }

    public @Nullable KuudraPhase next(boolean stunRequired) {
        return switch (this) {
            case SUPPLIES -> BUILD;
            case BUILD -> FUEL;
            case FUEL -> stunRequired ? STUN : DPS;
            case STUN -> DPS;
            case DPS -> stunRequired ? STUN : null;
            case LAIR -> null;
        };
    }

    public boolean availableIn(boolean stunRequired, boolean lairPhase) {
        return switch (this) {
            case STUN -> stunRequired;
            case LAIR -> lairPhase;
            default -> true;
        };
    }

    /**
     * The scoreboard objective lines (research K1 section 7.1; the stun tiers' three from the Burning and Infernal
     * captures). The lair shows none: the captured scoreboard dropped them after "POW!".
     */
    public List<String> objective(boolean stunRequired) {
        if (stunRequired) {
            switch (this) {
                case FUEL -> {
                    return List.of("<e>Figure out how to", "<e>defeat Kuudra");
                }
                case STUN -> {
                    return List.of("<e>Charge Ballista or", "<e>enter Kuudra");
                }
                case DPS -> {
                    return List.of("<e>Shoot Ballista at", "<e>Kuudra!");
                }
                default -> {
                }
            }
        }
        return switch (this) {
            case SUPPLIES -> List.of("<e>Rescue supplies");
            case BUILD -> List.of("<e>Protect Elle");
            case FUEL, STUN, DPS -> List.of("<e>Defeat Kuudra");
            case LAIR -> List.of();
        };
    }
}
