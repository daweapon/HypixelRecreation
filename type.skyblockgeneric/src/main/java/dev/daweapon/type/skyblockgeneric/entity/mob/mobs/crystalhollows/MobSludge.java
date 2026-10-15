package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.cube.SlimeMeta;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public abstract class MobSludge extends CrystalHollowsMob implements RegionPopulator {
    private static final int SPLIT_COUNT = 2;

    protected MobSludge() {
        super(EntityType.SLIME);
    }

    protected abstract int slimeSize();

    protected abstract Supplier<? extends MobSludge> splitInto();

    protected abstract int populationTarget();

    @Override
    public void onInit() {
        editEntityMeta(SlimeMeta.class, meta -> meta.setSize(slimeSize()));
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.JUNGLE;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.JUNGLE, populationTarget()));
    }

    @Override
    public void kill() {
        Instance instance = getInstance();
        Pos position = getPosition();
        boolean wasAlive = !isDead();
        super.kill();
        Supplier<? extends MobSludge> child = splitInto();
        if (!wasAlive || instance == null || child == null) return;
        for (int i = 0; i < SPLIT_COUNT; i++) {
            double offsetX = ThreadLocalRandom.current().nextDouble(-0.8, 0.8);
            double offsetZ = ThreadLocalRandom.current().nextDouble(-0.8, 0.8);
            child.get().setInstance(instance, position.add(offsetX, 0.2, offsetZ));
        }
    }
}
