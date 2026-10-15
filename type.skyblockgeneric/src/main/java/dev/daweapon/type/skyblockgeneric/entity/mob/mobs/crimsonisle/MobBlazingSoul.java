package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.AshfangArena;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * A Blazing Soul (wiki: Ashfang): left by every Ashfang summon that dies and gone after 10 s. Punching it
 * sends it flying where the player looks; if it strikes Ashfang it deals a flat 2,000,000.
 */
public class MobBlazingSoul extends CrimsonIsleMob {
    private static final int LIFETIME = 200;
    private static final double FLIGHT_SPEED = 0.8;
    private final @Nullable AshfangArena arena;
    private @Nullable SkyBlockPlayer launcher;
    private @Nullable Vec flight;
    private int flightTicks;

    public MobBlazingSoul() {
        this(null);
    }

    public MobBlazingSoul(@Nullable AshfangArena arena) {
        super(CrimsonMobs.BLAZING_SOUL);
        this.arena = arena;
    }

    @Override
    protected Temper temper() {
        return Temper.PASSIVE;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
        editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setInvisible(true);
            meta.setSmall(true);
        });
        setHelmet(ItemStack.of(Material.MAGMA_BLOCK));
    }

    @Override
    public boolean damage(Damage damage) {
        if (flight == null && damage.getAttacker() instanceof SkyBlockPlayer player) {
            launcher = player;
            flight = player.getPosition().direction().normalize().mul(FLIGHT_SPEED);
        }
        return false;
    }

    @Override
    protected void onAiTick(int ticks) {
        MythologicalCombat.particles(getInstance(), Particle.FLAME, getPosition().add(0, 0.8, 0), 0.2f, 3);
        if (arena == null || ticks > LIFETIME) {
            remove();
            return;
        }
        if (flight == null || launcher == null) return;
        teleport(getPosition().add(flight));
        if (arena.soulHit(launcher, getPosition()) || ++flightTicks > 60) remove();
    }
}
