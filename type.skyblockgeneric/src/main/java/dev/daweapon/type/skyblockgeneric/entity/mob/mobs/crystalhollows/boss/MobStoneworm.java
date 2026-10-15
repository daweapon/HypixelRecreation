package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.HollowsSkins;

public class MobStoneworm extends MobWorm {

    public MobStoneworm() {
        super(CrystalHollowsBossProfiles.STONEWORM);
    }

    @Override
    protected String headTexture() {
        return HollowsSkins.STONEWORM_HEAD;
    }
}
