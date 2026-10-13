package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.mythological.ability.MobProjectile;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Boss Corleone (wiki: Boss_Corleone): Team Treasurite's boss walks down the nearest player and throws a ball that
 * calls down a Smog. The throw interval, the Smog cap, the leash and the loot-share damage are estimates.
 */
public class MobBossCorleone extends CrystalHollowsBossMob {
    private static final double LEASH = 40;
    private static final int THROW_INTERVAL = 400;
    private static final int SMOG_DELAY = 15;
    private static final int MAX_SMOGS = 2;
    private static final double SHARE_FRACTION = 0.05;

    private final List<MobSmog> smogs = new ArrayList<>();
    private @Nullable Pos home;

    public MobBossCorleone() {
        super(CrystalHollowsBossProfiles.CORLEONE);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        if (home == null) home = getPosition();
    }

    @Override
    protected double lootShareDamage() {
        return getMaxHealth() * SHARE_FRACTION;
    }

    @Override
    protected void onAiTick(int ticks) {
        if (home != null && getPosition().distance(home) > LEASH) teleport(home);
        smogs.removeIf(smog -> smog.isDead() || smog.isRemoved());
        if (ticks % THROW_INTERVAL != 0 || smogs.size() >= MAX_SMOGS) return;
        Optional<SkyBlockPlayer> target = nearestPlayer(24, player -> true);
        target.ifPresent(this::summonSmog);
    }

    private void summonSmog(SkyBlockPlayer target) {
        MobProjectile.item(this, target, ItemStack.of(Material.ENDER_PEARL), 0.8, hit -> {
        });
        Pos landing = target.getPosition().add(0, 2, 0);
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (isDead() || isRemoved() || getInstance() == null) return;
            MobSmog smog = new MobSmog();
            smog.setInstance(getInstance(), landing);
            smogs.add(smog);
        }).delay(TaskSchedule.tick(SMOG_DELAY)).schedule();
    }

    @Override
    protected void onDefeated() {
        smogs.forEach(smog -> {
            if (!smog.isRemoved()) smog.remove();
        });
        smogs.clear();
    }

    @Override
    public void remove() {
        smogs.forEach(smog -> {
            if (!smog.isRemoved()) smog.remove();
        });
        smogs.clear();
        super.remove();
    }
}
