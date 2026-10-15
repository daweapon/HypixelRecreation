package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonFactionGuard;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

import java.util.List;

public class MobBarbarianGuard extends CrimsonFactionGuard {
    public MobBarbarianGuard() {
        super(CrimsonMobs.BARBARIAN_GUARD);
    }

    @Override
    protected CrimsonFaction faction() {
        return CrimsonFaction.BARBARIAN;
    }

    @Override
    protected List<String> hitLines() {
        return List.of("Are you sure you wanna do this?", "You better run.", "You did NOT just do that.",
                "You are making a big mistake.", "Ouch!", "You've had enough!", "That's enough!", "I am war!",
                "Hey! Stop it!", "Guards, get em!", "That hurt!");
    }

    @Override
    protected List<String> hostileLines() {
        return List.of("Get ready for a slapping.");
    }
}
