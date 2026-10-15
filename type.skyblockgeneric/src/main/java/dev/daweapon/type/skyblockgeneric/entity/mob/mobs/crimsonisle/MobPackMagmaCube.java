package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Pack Magma Cube (wiki: Pack_Magma_Cube): on its first hit it sometimes splits into 2 Pack Magma Cubes (50%,
 * estimate), and its leaps hit for 4,000.
 */
public class MobPackMagmaCube extends CrimsonIsleMob {
    public MobPackMagmaCube() {
        super(CrimsonMobs.PACK_MAGMA_CUBE);
    }

    static final double SPLIT_CHANCE = 50;
    private static final double LEAP_DAMAGE = 4_000;
    private boolean split;

    public MobPackMagmaCube(boolean split) {
        this();
        this.split = split;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(split ? 2 : 4));
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        if (split || getInstance() == null) return;
        split = true;
        if (!LootRoll.roll(null, null, SPLIT_CHANCE)) return;
        for (int i = 0; i < 2; i++) {
            new MobPackMagmaCube(true).setInstance(getInstance(), getPosition().add(i == 0 ? 1 : -1, 0.5, 0));
        }
    }

    @Override
    public void onMeleeHit(SkyBlockPlayer target) {
        strike(target, LEAP_DAMAGE - getProfile().damage());
    }
}
