package net.swofty.type.skyblockgeneric.crimson.mob.miniboss;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import org.jetbrains.annotations.Nullable;

/**
 * One of Ashfang's twelve summons. Every one that dies (rather than being absorbed) leaves a Blazing Soul.
 */
public abstract class AshfangMinionMob extends CrimsonIsleMob {
    private @Nullable AshfangArena arena;
    private boolean absorbed;

    protected AshfangMinionMob(CrimsonMobProfile profile) {
        super(profile);
    }

    public void bind(AshfangArena arena) {
        this.arena = arena;
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    public void absorb() {
        absorbed = true;
        remove();
    }

    public void collapse() {
        damage(new Damage(DamageType.MAGIC, null, null, null, getHealth() + 1));
    }

    @Override
    public void kill() {
        super.kill();
        if (arena != null && !absorbed) arena.dropSoul(getPosition());
    }
}
