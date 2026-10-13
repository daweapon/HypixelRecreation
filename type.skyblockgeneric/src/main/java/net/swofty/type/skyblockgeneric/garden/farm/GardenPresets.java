package net.swofty.type.skyblockgeneric.garden.farm;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.garden.GardenCleaning;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class GardenPresets {
    public static final String FLAT = "Default";

    private GardenPresets() {
    }

    public static @Nullable GardenCrop cropOf(@Nullable String preset) {
        if (preset == null) return null;
        for (GardenCrop crop : GardenCrop.values()) {
            if (crop.getDisplayName().equals(preset)) return crop;
        }
        return null;
    }

    public static boolean isPastable(String preset) {
        return FLAT.equals(preset) || cropOf(preset) != null;
    }

    public static int available(SkyBlockPlayer player, ItemType type) {
        return player.getAmountAnywhere(type);
    }

    public static @Nullable GardenPresetCost.Payment payment(SkyBlockPlayer player, GardenCrop crop) {
        return GardenPresetCost.plan(GardenPresetCost.required(crop), GardenPresetCost.sources(crop), type -> available(player, type));
    }

    private static boolean charge(SkyBlockPlayer player, GardenCrop crop) {
        GardenPresetCost.Payment payment = payment(player, crop);
        if (payment == null) {
            player.sendMessage(Text.of("<c>You need <a>{}x {}<c> to paste this preset! You only have <a>{}<c> worth.",
                    GardenPresetCost.required(crop), new SkyBlockItem(GardenPresetCost.seed(crop)).getDisplayNameText(),
                    available(player, crop)));
            return false;
        }
        payment.take().forEach(player::removeItemFromPlayer);
        payment.refund().forEach((type, amount) -> {
            for (int left = amount; left > 0; left -= 64) {
                player.addAndUpdateItem(new SkyBlockItem(type, Math.min(64, left)));
            }
        });
        return true;
    }

    public static long available(SkyBlockPlayer player, GardenCrop crop) {
        return GardenPresetCost.equivalent(GardenPresetCost.sources(crop), type -> available(player, type));
    }

    public static void paste(SkyBlockPlayer player, int plot, String preset) {
        Instance instance = player.getInstance();
        GardenProfile profile = Gardens.profile(player);
        int[] origin = GardenLayout.plotOrigin(plot);
        if (instance == null || origin == null || !profile.isPlotUnlocked(plot) || !isPastable(preset)) return;
        if (profile.greenhouse().bed(plot) != null) {
            player.sendMessage(Text.of("<c>A Greenhouse cannot be changed back!"));
            return;
        }
        if (!profile.isPlotCleaned(plot)) {
            player.sendMessage(Text.key("garden.preset.not_clean", plot));
            return;
        }
        if (GardenPlotLock.isLocked(instance, plot)) {
            player.sendMessage(Text.of("<c>This plot is already being pasted!"));
            return;
        }
        GardenCrop crop = cropOf(preset);
        if (!GardenPlotLock.tryLock(instance, plot)) return;
        if (crop != null && !charge(player, crop)) {
            GardenPlotLock.unlock(instance, plot);
            return;
        }
        profile.setPlotPreset(plot, preset);
        Gardens.save(player, profile);
        player.sendMessage(Text.key("garden.preset.pasting", preset));
        Thread.startVirtualThread(() -> startRows(player, instance, plot, origin, crop));
    }

    private static void startRows(SkyBlockPlayer player, Instance instance, int plot, int[] origin, @Nullable GardenCrop crop) {
        try {
            for (int cx = origin[0] >> 4; cx <= (origin[0] + GardenLayout.PLOT_SIZE - 1) >> 4; cx++) {
                for (int cz = origin[1] >> 4; cz <= (origin[1] + GardenLayout.PLOT_SIZE - 1) >> 4; cz++) {
                    instance.loadChunk(cx, cz).join();
                }
            }
        } catch (Throwable throwable) {
            GardenPlotLock.unlock(instance, plot);
            return;
        }
        int[] row = {0};
        instance.scheduler().submitTask(() -> {
            try {
                if (!instance.isRegistered()) {
                    GardenPlotLock.unlock(instance, plot);
                    return TaskSchedule.stop();
                }
                pasteRow(instance, origin, row[0], crop);
                if (++row[0] < GardenLayout.PLOT_SIZE) return TaskSchedule.seconds(1);
                GardenPlotLock.unlock(instance, plot);
                if (player.isOnline()) player.sendMessage(Text.key("garden.preset.done", plot));
                return TaskSchedule.stop();
            } catch (Throwable throwable) {
                GardenPlotLock.unlock(instance, plot);
                if (player.isOnline()) player.sendMessage(Text.of("<c>Pasting plot {} failed part way through.", plot));
                return TaskSchedule.stop();
            }
        });
    }

    private static void pasteRow(Instance instance, int[] origin, int lx, @Nullable GardenCrop crop) {
        int x = origin[0] + lx;
        for (int lz = 0; lz < GardenLayout.PLOT_SIZE; lz++) {
            int z = origin[1] + lz;
            List<Block> column = crop == null ? List.of(Block.GRASS_BLOCK) : GardenFarmLayout.column(crop, lx, lz);
            for (int y = GardenCleaning.GROUND_Y; y <= GardenCleaning.SCAN_TOP_Y; y++) {
                int index = y - GardenCleaning.GROUND_Y;
                Block target = index < column.size() ? column.get(index) : Block.AIR;
                if (!instance.getBlock(x, y, z).compare(target)) {
                    instance.setBlock(x, y, z, target);
                }
            }
        }
    }
}
