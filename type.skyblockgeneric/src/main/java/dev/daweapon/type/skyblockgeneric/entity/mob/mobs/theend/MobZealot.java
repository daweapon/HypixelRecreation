package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.metadata.monster.EndermanMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.ClosestEntityRegionTarget;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.theend.ZealotKills;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobZealot extends EndMob {
    private static final int CHEST_ODDS = 50;
    private static final double ENRAGED_RANGE = 32;

    private final boolean carryingChest = ThreadLocalRandom.current().nextInt(CHEST_ODDS) == 0;
    private boolean enraged;

    public MobZealot() {
        super(EntityType.ENDERMAN);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        if (carryingChest) editEntityMeta(EndermanMeta.class, meta -> meta.setCarriedBlock(Block.ENDER_CHEST));
    }

    public void enrage() {
        enraged = true;
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        List<TargetSelector> selectors = new ArrayList<>(super.getTargetSelectors());
        selectors.add(new ClosestEntityRegionTarget(this, ENRAGED_RANGE,
                entity -> enraged && entity instanceof SkyBlockPlayer, homeRegion()));
        return selectors;
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return carryingChest ? EndMobSpecs.ZEALOT_WITH_CHEST : EndMobSpecs.ZEALOT;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.DRAGONS_NEST;
    }

    @Override
    protected List<EndMobSpecs.Extra> extras() {
        return EndMobSpecs.ZEALOT_EXTRAS;
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer player, Instance instance, Pos position) {
        ZealotKills.onKill(player, instance, position, false);
    }
}
