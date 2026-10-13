package net.swofty.type.skyblockgeneric.user.garden;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenCleaning;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.quest.GardenQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GardenPlotCleaner {
    private static final Tag<Map<Integer, PlotState>> STATES = Tag.Transient("garden_clean_states");
    private static final int PLOT_SIZE = GardenLayout.PLOT_SIZE;

    private GardenPlotCleaner() {
    }

    private static final class PlotState {
        private final int total;
        private int remaining;
        private boolean leavesDecayed;

        private PlotState(int total, int remaining) {
            this.total = total;
            this.remaining = remaining;
        }
    }

    public static boolean breakBlock(SkyBlockPlayer player, Instance instance, Point position, Block block, boolean scythe) {
        int plot = GardenLayout.plotAt(position.x(), position.z());
        if (plot == GardenLayout.NONE || plot == GardenLayout.BARN) return false;
        GardenProfile profile = Gardens.profile(player);
        if (!profile.isPlotUnlocked(plot) || profile.isPlotCleaned(plot)) return false;
        if (!GardenCleaning.isCleanable(name(block), position.blockY())) return false;

        PlotState state = state(player, instance, plot, profile);
        int cleared = clear(instance, position, state);
        if (scythe) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    Point target = position.add(dx, 0, dz);
                    if (GardenLayout.plotAt(target.x(), target.z()) != plot) continue;
                    if (GardenCleaning.isScytheTarget(name(instance.getBlock(target)))) {
                        cleared += clear(instance, target, state);
                    }
                }
            }
        }
        if (cleared > 0) afterClearing(player, instance, plot, state, profile);
        return true;
    }

    public static void cleanInstantly(SkyBlockPlayer player, Instance instance, int plot) {
        GardenProfile profile = Gardens.profile(player);
        if (!profile.isPlotUnlocked(plot) || profile.isPlotCleaned(plot)) return;
        PlotState state = state(player, instance, plot, profile);
        forEachCleanable(instance, plot, (x, y, z) -> clear(instance, new Vec(x, y, z), state));
        finish(player, plot, profile);
    }

    private static void afterClearing(SkyBlockPlayer player, Instance instance, int plot, PlotState state, GardenProfile profile) {
        if (!state.leavesDecayed && GardenCleaning.leavesDecay(state.total, state.remaining)) {
            state.leavesDecayed = true;
            forEachCleanable(instance, plot, (x, y, z) -> {
                if (GardenCleaning.isLeaf(name(instance.getBlock(x, y, z)))) clear(instance, new Vec(x, y, z), state);
            });
        }
        if (GardenCleaning.isClean(state.total, state.remaining)) {
            finish(player, plot, profile);
            return;
        }
        int percent = (int) Math.floor(GardenCleaning.progress(state.total, state.remaining) * 100);
        player.sendActionBar(Component.text("Plot - " + plot + " is " + percent + "% clean", net.kyori.adventure.text.format.NamedTextColor.GREEN));
    }

    private static void finish(SkyBlockPlayer player, int plot, GardenProfile profile) {
        profile.markPlotCleaned(plot);
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<a>Plot <7>- <b>{}<a> is now clean!", plot));
        GardenQuests.plotCleaned(player);
    }

    private static int clear(Instance instance, Point position, PlotState state) {
        Block block = instance.getBlock(position);
        String name = name(block);
        if (!GardenCleaning.isCleanable(name, position.blockY())) return 0;
        instance.setBlock(position, Block.AIR);
        state.remaining = Math.max(0, state.remaining - 1);
        int cleared = 1;
        if ("lower".equals(block.getProperty("half"))) {
            Block above = instance.getBlock(position.add(0, 1, 0));
            if (name(above).equals(name)) {
                instance.setBlock(position.add(0, 1, 0), Block.AIR);
                state.remaining = Math.max(0, state.remaining - 1);
                cleared++;
            }
        }
        return cleared;
    }

    private static PlotState state(SkyBlockPlayer player, Instance instance, int plot, GardenProfile profile) {
        Map<Integer, PlotState> states = instance.getTag(STATES);
        if (states == null) {
            synchronized (instance) {
                states = instance.getTag(STATES);
                if (states == null) {
                    states = new ConcurrentHashMap<>();
                    instance.setTag(STATES, states);
                }
            }
        }
        return states.computeIfAbsent(plot, key -> scan(player, instance, plot, profile));
    }

    private static PlotState scan(SkyBlockPlayer player, Instance instance, int plot, GardenProfile profile) {
        int[] counter = {0};
        forEachCleanable(instance, plot, (x, y, z) -> counter[0]++);
        int total = profile.cleanTotal(plot);
        if (total <= 0 || counter[0] > total) {
            total = counter[0];
            profile.setCleanTotal(plot, total);
            Gardens.save(player, profile);
        }
        return new PlotState(total, counter[0]);
    }

    private static void forEachCleanable(Instance instance, int plot, CellVisitor visitor) {
        int[] origin = GardenLayout.plotOrigin(plot);
        if (origin == null) return;
        for (int cx = origin[0] >> 4; cx <= (origin[0] + PLOT_SIZE - 1) >> 4; cx++) {
            for (int cz = origin[1] >> 4; cz <= (origin[1] + PLOT_SIZE - 1) >> 4; cz++) {
                instance.loadChunk(cx, cz).join();
            }
        }
        for (int x = origin[0]; x < origin[0] + PLOT_SIZE; x++) {
            for (int z = origin[1]; z < origin[1] + PLOT_SIZE; z++) {
                for (int y = GardenCleaning.GROUND_Y + 1; y <= GardenCleaning.SCAN_TOP_Y; y++) {
                    if (GardenCleaning.isCleanable(name(instance.getBlock(x, y, z)), y)) {
                        visitor.visit(x, y, z);
                    }
                }
            }
        }
    }

    private static String name(Block block) {
        String key = block.name();
        int colon = key.indexOf(':');
        return colon < 0 ? key : key.substring(colon + 1);
    }

    @FunctionalInterface
    private interface CellVisitor {
        void visit(int x, int y, int z);
    }
}
