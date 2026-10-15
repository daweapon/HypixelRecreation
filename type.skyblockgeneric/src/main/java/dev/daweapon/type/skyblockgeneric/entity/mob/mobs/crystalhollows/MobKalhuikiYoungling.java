package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class MobKalhuikiYoungling extends KalhuikiMob {
    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("KALHUIKI_YOUNGLING", "Kalhuiki Youngling", 245, 1_000_000, 20_000)
            .bestiary(1, 10, Material.PLAYER_HEAD)
            .rewards(367, 81, 269)
            .types(MobType.HUMANOID, MobType.WOODLAND)
            .passive();

    public MobKalhuikiYoungling() {
        super();
    }

    @Override
    protected boolean angersTribeWhenHit() {
        return true;
    }

    @Override
    public void kill() {
        super.kill();
        if (getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player) {
            player.sendMessage("<c>{}<f>: Didn't mean to!", getDisplayName());
        }
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.JUNGLE, 6));
    }
}
