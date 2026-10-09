package net.swofty.type.kuudra.run;

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
    LAIR("lair_opens");

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

    /** The scoreboard objective lines; quoted Hypixel strings where known (research K1 section 7.1). */
    public List<String> objective(boolean stunRequired) {
        return switch (this) {
            case SUPPLIES -> List.of("<e>Rescue supplies");
            case BUILD -> List.of("<e>Protect Elle");
            case FUEL, STUN, DPS -> stunRequired ? List.of("<e>Defeat Kuudra", "<e>and stun him") : List.of("<e>Defeat Kuudra");
            case LAIR -> List.of("<e>Defeat Kuudra");
        };
    }
}
