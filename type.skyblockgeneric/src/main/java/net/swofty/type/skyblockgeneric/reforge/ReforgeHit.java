package net.swofty.type.skyblockgeneric.reforge;

import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class ReforgeHit {
    private ReforgeHit() {
    }

    public record Outgoing(UUID attacker, Set<MobType> mobTypes, boolean critical, RandomGenerator random) {
    }

    public record Incoming(Set<MobType> mobTypes, boolean seaCreature) {
    }
}
