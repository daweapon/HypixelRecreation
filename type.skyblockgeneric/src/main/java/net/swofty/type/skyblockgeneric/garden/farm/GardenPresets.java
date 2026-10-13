package net.swofty.type.skyblockgeneric.garden.farm;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
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
        return player.getAmountInInventory(type) + player.getSackItems().getAmount(type);
    }

    public static @Nullable GardenPresetCost.Payment payment(SkyBlockPlayer player, GardenCrop crop) {
        return GardenPresetCost.plan(GardenPresetCost.required(crop), GardenPresetCost.sources(crop), type -> available(player, type));
    }

    private static boolean charge(SkyBlockPlayer player, GardenCrop crop) {
        GardenPresetCost.Payment payment = payment(player, crop);
        if (payment == null) {
            player.sendMessage(Text.of("<c>You need <a>{}x {}<c> to paste this preset!",
                    GardenPresetCost.required(crop), new SkyBlockItem(GardenPresetCost.seed(crop)).getDisplayNameText()));
            return false;
        }
        payment.take().forEach(player::removeItemFromPlayer);
        for (int left = payment.refund(); left > 0; left -= 64) {
            player.addAndUpdateItem(new SkyBlockItem(payment.refundItem(), Math.min(64, left)));
        }
        return true;
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
        GardenCrop crop = cropOf(preset);
        if (crop != null && !charge(player, crop)) return;
        player.sendMessage(Text.key("garden.preset.pasting", preset));
        Thread.startVirtualThread(() -> {
            AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
            for (int cx = origin[0] >> 4; cx <= (origin[0] + GardenLayout.PLOT_SIZE - 1) >> 4; cx++) {
                for (int cz = origin[1] >> 4; cz <= (origin[1] + GardenLayout.PLOT_SIZE - 1) >> 4; cz++) {
                    instance.loadChunk(cx, cz).join();
                }
            }
            for (int lx = 0; lx < GardenLayout.PLOT_SIZE; lx++) {
                for (int lz = 0; lz < GardenLayout.PLOT_SIZE; lz++) {
                    int x = origin[0] + lx;
                    int z = origin[1] + lz;
                    List<Block> column = crop == null ? List.of(Block.GRASS_BLOCK) : GardenFarmLayout.column(crop, lx, lz);
                    for (int y = GardenCleaning.GROUND_Y; y <= GardenCleaning.SCAN_TOP_Y; y++) {
                        int index = y - GardenCleaning.GROUND_Y;
                        Block target = index < column.size() ? column.get(index) : Block.AIR;
                        if (!instance.getBlock(x, y, z).compare(target)) {
                            batch.setBlock(x, y, z, target);
                        }
                    }
                }
            }
            batch.apply(instance, _ -> {
                profile.setPlotPreset(plot, preset);
                Gardens.save(player, profile);
                player.sendMessage(Text.key("garden.preset.done", plot));
            });
        });
    }
}
