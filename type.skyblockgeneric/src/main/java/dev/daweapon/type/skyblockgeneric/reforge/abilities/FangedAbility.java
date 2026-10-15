package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeHit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FangedAbility implements ReforgeAbility.OutgoingDamage {
    public static final int HITS_PER_BONUS = 7;
    public static final double BONUS_MULTIPLIER = 2;

    private final Map<UUID, Integer> hits = new ConcurrentHashMap<>();

    @Override
    public String reforgeId() {
        return "fanged";
    }

    @Override
    public double outgoingMultiplier(ReforgeHit.Outgoing hit) {
        int count = hits.merge(hit.attacker(), 1, Integer::sum);
        return count % HITS_PER_BONUS == 0 ? BONUS_MULTIPLIER : 1;
    }
}
