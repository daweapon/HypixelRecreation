package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sheep's Overheal: after ten seconds without losing health or shield from any source, a shield worth {0}% of max
 * health appears. It is spent like any other shield and the next one needs another quiet ten seconds.
 */
public final class OverhealShield {
    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private OverhealShield() {
    }

    private static final class State {
        private long lastDamageAt;
        private double lastTotal;
        private float granted;
    }

    public static void tick(SkyBlockPlayer player, long now) {
        double percent = PetAbilities.value(player, ItemType.SHEEP_PET, "Overheal", 0);
        State state = percent > 0 ? STATES.computeIfAbsent(player.getUuid(), _ -> start(player, now))
                : STATES.get(player.getUuid());
        if (state == null) return;
        double total = total(player);
        if (total < state.lastTotal - 0.01) state.lastDamageAt = now;
        float absorption = player.getAdditionalHearts();
        if (state.granted > 0 && absorption < state.granted) state.granted = absorption;
        if (percent <= 0) {
            if (state.granted > 0) player.setAdditionalHearts(absorption - state.granted);
            STATES.remove(player.getUuid());
            return;
        }
        if (PetPerkMath.overheals(state.lastDamageAt, now, state.granted > 0)) {
            state.granted = PetPerkMath.overhealShield(player.getMaxHealth(), percent);
            player.setAdditionalHearts(absorption + state.granted);
        }
        state.lastTotal = total(player);
    }

    public static void release(UUID player) {
        STATES.remove(player);
    }

    private static State start(SkyBlockPlayer player, long now) {
        State state = new State();
        state.lastDamageAt = now;
        state.lastTotal = total(player);
        return state;
    }

    private static double total(SkyBlockPlayer player) {
        return player.getHealth() + player.getAdditionalHearts();
    }
}
