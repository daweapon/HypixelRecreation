package dev.daweapon.type.skyblockgeneric.schematic;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SchematicPaster {
    public static final Executor NEXT_TICK = task -> MinecraftServer.getSchedulerManager().scheduleNextTick(task);
    public static final Executor CALLER = Runnable::run;
    private static final int BATCH = 20_000;

    public record Options(int rotation, boolean skipAir, boolean captureUndo) {
        public static Options defaults() {
            return new Options(0, false, false);
        }

        public Options withRotation(int degrees) {
            return new Options(degrees, skipAir, captureUndo);
        }

        public Options withSkipAir(boolean skip) {
            return new Options(rotation, skip, captureUndo);
        }

        public Options withUndo(boolean capture) {
            return new Options(rotation, skipAir, capture);
        }
    }

    public record Result(Point min, int width, int height, int length, int placed, int skippedAir, int unknown,
                         int outOfWorld, Schematic undo) {
    }

    private SchematicPaster() {
    }

    public static CompletableFuture<Result> paste(Instance instance, Point origin, Schematic schematic, Options options, Executor executor) {
        Schematic shape = schematic.rotated(options.rotation());
        int minX = origin.blockX();
        int minY = origin.blockY();
        int minZ = origin.blockZ();
        List<CompletableFuture<?>> loads = new ArrayList<>();
        for (int cx = Math.floorDiv(minX, 16); cx <= Math.floorDiv(minX + shape.width() - 1, 16); cx++) {
            for (int cz = Math.floorDiv(minZ, 16); cz <= Math.floorDiv(minZ + shape.length() - 1, 16); cz++) {
                loads.add(instance.loadChunk(cx, cz));
            }
        }
        CompletableFuture<Result> done = new CompletableFuture<>();
        CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).whenComplete((_, error) -> {
            if (error != null) {
                done.completeExceptionally(error);
                return;
            }
            executor.execute(() -> new Job(instance, origin, shape, options, executor, done).start());
        });
        return done;
    }

    private static final class Job {
        private final Instance instance;
        private final Point origin;
        private final Schematic shape;
        private final Options options;
        private final Executor executor;
        private final CompletableFuture<Result> done;
        private final int minWorldY;
        private final int maxWorldY;
        private Schematic undo;
        private long cursor;
        private int placed;
        private int skippedAir;
        private int unknown;
        private int outOfWorld;

        Job(Instance instance, Point origin, Schematic shape, Options options, Executor executor, CompletableFuture<Result> done) {
            this.instance = instance;
            this.origin = origin;
            this.shape = shape;
            this.options = options;
            this.executor = executor;
            this.done = done;
            this.minWorldY = instance.getCachedDimensionType().minY();
            this.maxWorldY = minWorldY + instance.getCachedDimensionType().height() - 1;
        }

        void start() {
            try {
                if (options.captureUndo()) {
                    undo = Schematic.capture(instance, origin, minWorldY, maxWorldY, shape.width(), shape.height(), shape.length());
                }
                step();
            } catch (Throwable t) {
                done.completeExceptionally(t);
            }
        }

        void step() {
            try {
                long total = shape.volume();
                long end = Math.min(total, cursor + BATCH);
                for (; cursor < end; cursor++) {
                    int x = (int) (cursor % shape.width());
                    int z = (int) (cursor / shape.width() % shape.length());
                    int y = (int) (cursor / ((long) shape.width() * shape.length()));
                    place(x, y, z);
                }
                if (cursor < total) {
                    executor.execute(this::step);
                    return;
                }
                done.complete(new Result(origin, shape.width(), shape.height(), shape.length(), placed, skippedAir, unknown, outOfWorld, undo));
            } catch (Throwable t) {
                done.completeExceptionally(t);
            }
        }

        private void place(int x, int y, int z) {
            Block block = shape.blockAt(x, y, z);
            if (block == null) {
                unknown++;
                return;
            }
            if (options.skipAir() && block.isAir()) {
                skippedAir++;
                return;
            }
            int worldY = origin.blockY() + y;
            if (worldY < minWorldY || worldY > maxWorldY) {
                outOfWorld++;
                return;
            }
            instance.setBlock(origin.blockX() + x, worldY, origin.blockZ() + z, block, false);
            placed++;
        }
    }
}
