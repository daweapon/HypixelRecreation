package dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The YAML names of the {@link PetEvent}s an ability can react to.
 */
public enum PetTrigger {
    KILL(PetEvent.Kill.class),
    MELEE_HIT(PetEvent.MeleeHit.class),
    DAMAGED_BY_MOB(PetEvent.DamagedByMob.class),
    FALL_DAMAGE(PetEvent.FallDamage.class),
    XP_GAIN(PetEvent.XpGain.class),
    JUMP(PetEvent.Jump.class),
    INTERACT(PetEvent.PetInteract.class),
    FISH_CAUGHT(PetEvent.FishCaught.class),
    CROP_HARVESTED(PetEvent.CropHarvested.class);

    private final Class<? extends PetEvent> type;

    PetTrigger(Class<? extends PetEvent> type) {
        this.type = type;
    }

    public boolean matches(PetEvent event) {
        return type.isInstance(event);
    }

    public static boolean any(Set<PetTrigger> triggers, PetEvent event) {
        for (PetTrigger trigger : triggers) if (trigger.matches(event)) return true;
        return false;
    }

    public static Set<PetTrigger> parse(Object raw, String where) {
        if (raw == null) return EnumSet.noneOf(PetTrigger.class);
        Collection<?> names = raw instanceof Collection<?> list ? list : List.of(raw);
        List<PetTrigger> triggers = new ArrayList<>();
        for (Object name : names) {
            try {
                triggers.add(valueOf(String.valueOf(name).trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(where + ": unknown trigger '" + name + "'");
            }
        }
        return triggers.isEmpty() ? EnumSet.noneOf(PetTrigger.class) : EnumSet.copyOf(triggers);
    }
}
