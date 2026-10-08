package net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.slayer.SlayerTier;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The DDR Apocalypse: up to five small crosses marked in orange terracotta near the player, which
 * turn to fire after 1.5 seconds and burn anyone standing in them. The marks are client-side only,
 * so the world itself never changes.
 */
final class InfernoFirePits {
    private static final int MAX_PITS = 5;
    private static final int WARNING_TICKS = 30;
    private static final int BURN_TICKS = 80;
    private static final int APOCALYPSE_PERIOD_TICKS = 100;
    private static final double SPREAD = 6;
    private static final int[][] CROSS = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final InfernoDemonlordAbility ability;
    private final SlayerBossMob boss;
    private final double damagePerSecond;
    private final List<Point> marked = new ArrayList<>();

    InfernoFirePits(InfernoDemonlordAbility ability, SlayerBossMob boss, SlayerTier tier) {
        this.ability = ability;
        this.boss = boss;
        this.damagePerSecond = tier.number() >= 4 ? 2_865 : 950;
    }

    void startApocalypse() {
        ability.every(boss, 0, APOCALYPSE_PERIOD_TICKS, this::burst);
    }

    void burst() {
        SkyBlockPlayer owner = ability.ownerOf(boss);
        Instance instance = boss.getInstance();
        if (owner == null || instance == null) return;
        int count = 1 + ThreadLocalRandom.current().nextInt(MAX_PITS);
        for (int i = 0; i < count; i++) {
            Pos centre = owner.getPosition().add(
                ThreadLocalRandom.current().nextDouble(-SPREAD, SPREAD), 0, ThreadLocalRandom.current().nextDouble(-SPREAD, SPREAD));
            pit(instance, new Vec(centre.blockX(), centre.blockY() - 1, centre.blockZ()));
        }
    }

    private void pit(Instance instance, Vec floor) {
        List<Point> cells = new ArrayList<>();
        for (int[] offset : CROSS) cells.add(floor.add(offset[0], 0, offset[1]));
        cells.forEach(cell -> show(instance, cell, Block.ORANGE_TERRACOTTA));
        marked.addAll(cells);
        ability.schedule(boss, WARNING_TICKS, () -> {
            for (int tick = 0; tick < BURN_TICKS; tick += 10) {
                boolean damageTick = tick % 20 == 0;
                ability.schedule(boss, tick, () -> burn(instance, cells, damageTick));
            }
            ability.schedule(boss, BURN_TICKS, () -> restore(instance, cells));
        });
    }

    private void burn(Instance instance, List<Point> cells, boolean damageTick) {
        for (Point cell : cells) {
            ParticlePacket packet = new ParticlePacket(Particle.FLAME, true, true,
                cell.x() + 0.5, cell.y() + 1.1, cell.z() + 0.5, 0.3f, 0.1f, 0.3f, 0f, 3);
            instance.getPlayers().forEach(player -> player.sendPacket(packet));
        }
        if (!damageTick) return;
        for (SkyBlockPlayer player : ability.fighters(boss)) {
            Pos feet = player.getPosition();
            boolean inside = cells.stream().anyMatch(cell -> cell.blockX() == feet.blockX()
                && cell.blockZ() == feet.blockZ() && Math.abs(cell.blockY() + 1 - feet.blockY()) <= 1);
            if (inside) ability.hurt(boss, player, damagePerSecond, "DDR Apocalypse", false);
        }
    }

    private void show(Instance instance, Point cell, Block block) {
        BlockChangePacket packet = new BlockChangePacket(cell, block.stateId());
        instance.getPlayers().forEach(player -> player.sendPacket(packet));
    }

    private void restore(Instance instance, List<Point> cells) {
        cells.forEach(cell -> show(instance, cell, instance.getBlock(cell)));
        marked.removeAll(cells);
    }

    void clear() {
        Instance instance = boss.getInstance();
        if (instance == null) return;
        restore(instance, new ArrayList<>(marked));
    }
}
