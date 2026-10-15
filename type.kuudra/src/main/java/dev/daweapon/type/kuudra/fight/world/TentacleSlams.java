package dev.daweapon.type.kuudra.fight.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Tentacle slam telegraphs (research K1 3.5, 3.7): the floor along the slam's path turns green, lime, yellow,
 * orange and finally red terracotta, then the tentacle slams and the floor goes back to normal.
 */
public final class TentacleSlams {
    public record Column(int x, int z) {
    }

    public record Slam(List<Column> path, Object source) {
    }

    private static final Block[] STAGES = {Block.GREEN_TERRACOTTA, Block.LIME_TERRACOTTA, Block.YELLOW_TERRACOTTA,
            Block.ORANGE_TERRACOTTA, Block.RED_TERRACOTTA};

    private final class Active {
        private final Slam slam;
        private int ticks;
        private int stage = -1;

        private Active(Slam slam) {
            this.slam = slam;
        }
    }

    private final Instance instance;
    private final int floorBlockY;
    private final int telegraphTicks;
    private final List<Active> active = new ArrayList<>();
    private final Map<Column, Block> originals = new HashMap<>();
    private final Map<Column, Integer> uses = new HashMap<>();

    public TentacleSlams(Instance instance, int floorBlockY, int telegraphTicks) {
        this.instance = instance;
        this.floorBlockY = floorBlockY;
        this.telegraphTicks = Math.max(STAGES.length, telegraphTicks);
    }

    /** The floor columns a slam from {@code from} toward {@code toward} covers: a strip {@code width} wide. */
    public static List<Column> path(Pos from, Pos toward, int length, int width) {
        double dx = toward.x() - from.x();
        double dz = toward.z() - from.z();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 1e-6) {
            dx = 1;
            dz = 0;
            distance = 1;
        }
        double ux = dx / distance;
        double uz = dz / distance;
        Set<Column> columns = new LinkedHashSet<>();
        double half = (width - 1) / 2.0;
        for (double along = 0; along <= length; along += 0.5) {
            for (double side = -half; side <= half + 1e-9; side += 0.5) {
                double x = from.x() + ux * along - uz * side;
                double z = from.z() + uz * along + ux * side;
                columns.add(new Column((int) Math.floor(x), (int) Math.floor(z)));
            }
        }
        return List.copyOf(columns);
    }

    public void start(Slam slam) {
        List<Column> floor = slam.path().stream().filter(column -> !instance.getBlock(column.x(), floorBlockY, column.z()).isAir()).toList();
        if (floor.isEmpty()) return;
        Slam onFloor = new Slam(floor, slam.source());
        for (Column column : floor) {
            originals.putIfAbsent(column, instance.getBlock(column.x(), floorBlockY, column.z()));
            uses.merge(column, 1, Integer::sum);
        }
        active.add(new Active(onFloor));
    }

    /** Advances the telegraphs; slams that land this tick are handed to {@code onSlam}. */
    public void tick(Consumer<Slam> onSlam) {
        Iterator<Active> iterator = active.iterator();
        while (iterator.hasNext()) {
            Active each = iterator.next();
            each.ticks++;
            int stage = Math.min(STAGES.length - 1, each.ticks * STAGES.length / telegraphTicks);
            if (stage != each.stage) {
                each.stage = stage;
                for (Column column : each.slam.path()) instance.setBlock(column.x(), floorBlockY, column.z(), STAGES[stage]);
            }
            if (each.ticks >= telegraphTicks) {
                iterator.remove();
                onSlam.accept(each.slam);
                release(each.slam);
            }
        }
    }

    private void release(Slam slam) {
        for (Column column : slam.path()) {
            int left = uses.merge(column, -1, Integer::sum);
            if (left > 0) continue;
            uses.remove(column);
            Block original = originals.remove(column);
            if (original != null) instance.setBlock(column.x(), floorBlockY, column.z(), original);
        }
    }

    public void clear() {
        active.clear();
        originals.forEach((column, block) -> instance.setBlock(column.x(), floorBlockY, column.z(), block));
        originals.clear();
        uses.clear();
    }

    public static boolean covers(Slam slam, Pos position) {
        return slam.path().contains(new Column(position.blockX(), position.blockZ()));
    }
}
