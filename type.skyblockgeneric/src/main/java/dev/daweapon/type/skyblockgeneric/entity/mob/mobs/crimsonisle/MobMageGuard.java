package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonFactionGuard;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

import java.util.List;

public class MobMageGuard extends CrimsonFactionGuard {
    public MobMageGuard() {
        super(CrimsonMobs.MAGE_GUARD);
    }

    @Override
    protected CrimsonFaction faction() {
        return CrimsonFaction.MAGE;
    }

    @Override
    protected List<String> hitLines() {
        return List.of("Are you sure you wanna do this?", "You better watch out!", "You did NOT just do that.",
                "You are making a big mistake.", "Ouch!", "You've had enough!", "That's enough!", "I am war!",
                "Hey! Stop it!", "Guards, get em!", "What do you think you are doing?");
    }

    @Override
    protected List<String> hostileLines() {
        return List.of("That's enough!", "I'll turn you to dust!");
    }
}
