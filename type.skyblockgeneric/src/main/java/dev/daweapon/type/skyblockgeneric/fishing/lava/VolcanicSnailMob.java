package dev.daweapon.type.skyblockgeneric.fishing.lava;

import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * The Volcanic Snail (wiki: Volcanic_Snail) crawls slowly, leaving a fire trail, inside a 5-block ring of fire that
 * deals true damage twice a second. The ring's damage per tick isn't on the wiki; its damage stat is an estimate.
 */
public class VolcanicSnailMob extends LavaSeaCreatureMob {
    static final double RING_RADIUS = 5;

    public VolcanicSnailMob(@Nullable SkyBlockPlayer angler, boolean corrupted) {
        super(LavaSeaCreature.VOLCANIC_SNAIL, angler, corrupted);
    }

    @Override
    protected void onTick(int ticks) {
        Instance instance = getInstance();
        if (instance == null || ticks % 10 != 0) return;
        LavaAttacks.ring(instance, getPosition(), RING_RADIUS, Particle.FLAME);
        MythologicalCombat.particles(instance, Particle.FLAME, getPosition(), 0.2f, 3);
        for (SkyBlockPlayer player : playersWithin(RING_RADIUS)) {
            if (PeaceTreaty.applies(player)) continue;
            player.setHealth((float) Math.max(0, player.getHealth() - getCreature().getDamage()));
        }
    }
}
