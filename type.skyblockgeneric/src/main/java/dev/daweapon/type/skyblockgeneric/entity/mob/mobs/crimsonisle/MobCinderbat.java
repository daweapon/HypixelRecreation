package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Cinderbat (wiki: Cinderbat): hangs from cave ceilings under the volcano, never attacks, and flits about when hit.
 */
public class MobCinderbat extends CrimsonIsleMob {
    public MobCinderbat() {
        super(CrimsonMobs.CINDERBAT);
    }

    @Override
    protected Temper temper() {
        return Temper.PASSIVE;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
        editEntityMeta(BatMeta.class, meta -> meta.setHanging(true));
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        editEntityMeta(BatMeta.class, meta -> meta.setHanging(false));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        setVelocity(new Vec(random.nextDouble(-8, 8), random.nextDouble(-2, 4), random.nextDouble(-8, 8)));
    }
}
