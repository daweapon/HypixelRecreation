package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneVenom;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMobs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class MobArachnesKeeper extends SpiderDenMob {
    private static final int DASH_INTERVAL_TICKS = 100;
    private static final double DASH_RANGE = 14;
    private static final double DASH_SPEED = 22;
    private static final int WEB_INTERVAL_TICKS = 10;
    private static final int WEB_LIFETIME_TICKS = 80;

    public MobArachnesKeeper() {
        super(SpiderDenMobs.ARACHNES_KEEPER);
    }

    @Override
    protected boolean immuneToMagic() {
        return true;
    }

    @Override
    public boolean isImmuneToArrows() {
        return true;
    }

    @Override
    public void attack(Entity target, boolean swingHand) {
        super.attack(target, swingHand);
        if (target instanceof SkyBlockPlayer player) ArachneVenom.poison(player);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % WEB_INTERVAL_TICKS == 0) layWeb();
        if (ticks % DASH_INTERVAL_TICKS != 0) return;
        rangedTarget(DASH_RANGE).ifPresent(target -> {
            Pos from = getPosition();
            Pos to = target.getPosition();
            double dx = to.x() - from.x();
            double dz = to.z() - from.z();
            double length = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
            setVelocity(new net.minestom.server.coordinate.Vec(dx / length * DASH_SPEED, 6, dz / length * DASH_SPEED));
        });
    }

    private void layWeb() {
        Instance instance = getInstance();
        if (instance == null) return;
        Pos at = getPosition();
        if (!instance.isChunkLoaded(at) || !instance.getBlock(at).isAir()) return;
        instance.setBlock(at, Block.COBWEB);
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (instance.getBlock(at).compare(Block.COBWEB)) instance.setBlock(at, Block.AIR);
        }).delay(TaskSchedule.tick(WEB_LIFETIME_TICKS)).schedule();
    }
}
