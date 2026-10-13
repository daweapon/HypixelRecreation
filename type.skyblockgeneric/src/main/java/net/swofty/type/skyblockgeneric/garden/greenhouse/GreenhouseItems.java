package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.pest.PestVacuum;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUICropDiagnostics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GreenhouseItems {
    public static final String CAN = "WATERING_CAN";
    public static final String DIAGNOSTICS = "PLANT_DIAGNOSTICS_TOOL";
    public static final long HOLD_WINDOW_MILLIS = 450;
    private static final double TICK_SECONDS = 0.1;
    private static final double REACH = 7;

    public record Can(String id, double capacity, double perSecond, int burst) {
    }

    private static final Map<String, Can> CANS = Map.of(
            "HYDRO_CAN_1000", new Can("HYDRO_CAN_1000", 3_000, 150, 0),
            "HYDRO_CAN_TURBO_2000", new Can("HYDRO_CAN_TURBO_2000", 5_000, 200, 0),
            "HYDRO_CAN_ULTRA_3000", new Can("HYDRO_CAN_ULTRA_3000", 10_000, 250, 1),
            "AQUAMASTER_X", new Can("AQUAMASTER_X", 15_000, 300, 1),
            "AQUAMASTER_HYDROMAX", new Can("AQUAMASTER_HYDROMAX", 20_000, 350, 2));
    private static final Map<UUID, Held> HOLDING = new ConcurrentHashMap<>();

    private record Held(Can can, long until) {
    }

    private GreenhouseItems() {
    }

    public static void registerInteractables() {
        RosewaterFlask.registerInteractables();
        InteractableRegistry.register(CAN, InteractableItemConfig.builder().rightClickHandler(GreenhouseItems::useCan).build());
        InteractableRegistry.register(DIAGNOSTICS, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            GardenProfile profile = Gardens.profile(player);
            GreenhouseLayout.Spot spot = aimedSpot(player, profile);
            if (spot == null) {
                player.sendMessage(Text.of("<c>Aim at a plant inside your Greenhouse!"));
                return;
            }
            int anchor = profile.greenhouse().bed(spot.plot()).anchorAt(spot.slot());
            if (anchor < 0) {
                player.sendMessage(Text.of("<c>There is no plant in that slot!"));
                return;
            }
            player.openView(new GUICropDiagnostics(new GreenhouseLayout.Spot(spot.plot(), anchor)));
        }).build());
    }

    public static void registerLore() {
        RosewaterFlask.registerLore();
        LoreRegistry.register(CAN, new LoreConfig((item, player) -> {
            Can can = CANS.get(item.getAttributeHandler().getTypeAsString());
            if (can == null) return List.of();
            return List.of("", "<7>Water: <b>" + format(tank(player, can)) + "<7>/<b>" + format(can.capacity()));
        }, null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    public static @Nullable Can can(SkyBlockItem item) {
        return CANS.get(item.getAttributeHandler().getTypeAsString());
    }

    public static double tank(SkyBlockPlayer player, Can can) {
        return Gardens.profile(player).greenhouse().getTanks().getOrDefault(can.id(), can.capacity());
    }

    private static String format(double value) {
        return value >= 1000 ? (value % 1000 == 0 ? (long) (value / 1000) + "k" : String.format("%.1fk", value / 1000)) : String.valueOf((long) value);
    }

    private static void useCan(SkyBlockPlayer player, SkyBlockItem item) {
        Can can = can(item);
        if (can == null || !PestVacuum.inGarden(player)) return;
        if (aimingAtWater(player)) {
            Gardens.profile(player).greenhouse().getTanks().put(can.id(), can.capacity());
            player.sendMessage(Text.of("<b>Your {} <b>is full!", item.getDisplayNameText()));
            return;
        }
        HOLDING.put(player.getUuid(), new Held(can, System.currentTimeMillis() + HOLD_WINDOW_MILLIS));
    }

    public static void tick(Instance instance) {
        GreenhouseMinigames.tick(instance);
        long now = System.currentTimeMillis();
        for (Player online : instance.getPlayers()) {
            if (!(online instanceof SkyBlockPlayer player)) continue;
            Held held = HOLDING.get(player.getUuid());
            if (held == null) continue;
            if (held.until() < now) {
                HOLDING.remove(player.getUuid());
                continue;
            }
            pour(player, held.can());
        }
    }

    private static void pour(SkyBlockPlayer player, Can can) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData data = profile.greenhouse();
        GreenhouseLayout.Spot spot = aimedSpot(player, profile);
        if (spot == null) return;
        double tank = data.getTanks().getOrDefault(can.id(), can.capacity());
        if (tank <= 0) {
            player.sendActionBar(Text.of("<c>Your can is empty! Right-click water to fill it.").asComponent());
            return;
        }
        double amount = Math.min(tank, can.perSecond() * TICK_SECONDS);
        double levels = amount / GreenhouseConfig.get().waterPerLevel();
        int radius = can.burst();
        int plot = spot.plot();
        GreenhouseData.Bed bed = data.bed(plot);
        java.util.Set<Integer> watered = new java.util.HashSet<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int column = spot.column() + dx;
                int row = spot.row() + dz;
                if (column < 0 || row < 0 || column >= GreenhouseData.SIZE || row >= GreenhouseData.SIZE) continue;
                int slot = GreenhouseData.slot(column, row);
                int anchor = bed.anchorAt(slot);
                if (anchor >= 0 && !watered.add(anchor)) continue;
                GreenhouseService.water(player, new GreenhouseLayout.Spot(plot, slot), levels);
            }
        }
        data.getTanks().put(can.id(), tank - amount);
        player.sendActionBar(Text.of("<b>Water: {}<7>/<b>{}", format(tank - amount), format(can.capacity())).asComponent());
    }

    public static @Nullable GreenhouseLayout.Spot aimedSpot(SkyBlockPlayer player, GardenProfile profile) {
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec look = player.getPosition().direction();
        for (double d = 0.5; d <= REACH; d += 0.25) {
            Vec at = eye.asVec().add(look.mul(d));
            if (at.y() < GreenhouseLayout.FLOOR_Y || at.y() > GreenhouseLayout.CROP_Y + 2) continue;
            GreenhouseLayout.Spot spot = GreenhouseService.spotAt(profile, at);
            if (spot != null) return spot;
        }
        return null;
    }

    private static boolean aimingAtWater(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec look = player.getPosition().direction();
        for (double d = 0.5; d <= REACH; d += 0.25) {
            Vec at = eye.asVec().add(look.mul(d));
            Block block = instance.getBlock(at);
            if (block.compare(Block.WATER) || block.compare(Block.WATER_CAULDRON)) return true;
            if (block.isSolid()) return false;
        }
        return false;
    }
}
