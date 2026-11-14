package dev.daweapon.type.murdermysterygame.death;

import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import org.jetbrains.annotations.Nullable;

public record MurderMysteryDeathResult(
        MurderMysteryPlayer victim,
        @Nullable MurderMysteryPlayer killer,
        MurderMysteryDeathType deathType
) {
    public boolean hasKiller() {
        return killer != null;
    }
}
