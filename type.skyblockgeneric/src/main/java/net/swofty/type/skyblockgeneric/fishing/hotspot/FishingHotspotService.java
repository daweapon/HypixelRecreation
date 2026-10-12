package net.swofty.type.skyblockgeneric.fishing.hotspot;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.accessory.handler.UtilityAccessories;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.rod.FishingRodPartService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
// Fishing hotspots (wiki: Fishing_Hotspots): each server shows up to maxActive rings at a time, each sitting on one
// of its spawn points for durationSeconds and granting one stat picked from its buff list. Rings rotate on a
// staggered clock, so every caller sees the same hotspots without shared state. Players see and use them only after
// talking to Hattie in the Backwater Bayou.
public final class FishingHotspotService {
    public static final double RADIUS = 5.0D;
    private static final double VERTICAL_REACH = 3.0D;

    public static boolean unlocked(SkyBlockPlayer player) {
        return player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_HATTIE);
    }

    public static boolean isBobberInHotspot(SkyBlockPlayer player, FishingMedium medium, Pos bobberPos) {
        return activeAt(player, medium, bobberPos).isPresent();
    }

    public static ItemStatistics getActiveHotspotBuffs(SkyBlockPlayer player, FishingMedium medium, Pos bobberPos) {
        Optional<ActiveHotspot> hotspot = activeAt(player, medium, bobberPos);
        if (hotspot.isEmpty()) return ItemStatistics.empty();
        double value = hotspot.get().value() * sinkerMultiplier(player);
        return ItemStatistics.add(ItemStatistics.builder().withBase(hotspot.get().statistic(), value).build(),
                ItemStatistics.add(UtilityAccessories.hotspotBuffs(player),
                        net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityEffects.hotspotBuffs(player)));
    }

    public static Optional<ActiveHotspot> activeAt(SkyBlockPlayer player, FishingMedium medium, Pos bobberPos) {
        if (!unlocked(player)) return Optional.empty();
        for (ActiveHotspot hotspot : active(currentServer(), System.currentTimeMillis())) {
            if (hotspot.definition().medium() != medium) continue;
            double dx = bobberPos.x() - hotspot.position().x();
            double dz = bobberPos.z() - hotspot.position().z();
            if (dx * dx + dz * dz <= RADIUS * RADIUS && Math.abs(bobberPos.y() - hotspot.position().y()) <= VERTICAL_REACH) {
                return Optional.of(hotspot);
            }
        }
        return Optional.empty();
    }

    public static List<ActiveHotspot> active(@Nullable String serverType, long now) {
        List<ActiveHotspot> result = new ArrayList<>();
        if (serverType == null) return result;
        for (HotspotDefinition definition : FishingRegistry.getHotspots()) {
            List<HotspotDefinition.SpawnPoint> points = definition.spawnPoints().stream()
                    .filter(point -> point.serverType() == null || point.serverType().equals(serverType))
                    .toList();
            List<ItemStatistic> stats = buffStatistics(definition);
            if (points.isEmpty() || stats.isEmpty()) continue;
            int slots = Math.min(definition.maxActive(), points.size());
            long period = definition.durationSeconds() * 1000L;
            List<Integer> taken = new ArrayList<>();
            for (int slot = 0; slot < slots; slot++) {
                long cycle = Math.floorDiv(now + slot * period / slots, period);
                long seed = mix(definition.id().hashCode(), slot, cycle);
                int index = (int) Math.floorMod(seed, (long) points.size());
                while (taken.contains(index)) index = (index + 1) % points.size();
                taken.add(index);
                HotspotDefinition.SpawnPoint point = points.get(index);
                ItemStatistic statistic = stats.get((int) Math.floorMod(seed >>> 17, (long) stats.size()));
                long endsAt = (cycle + 1) * period - slot * period / slots;
                result.add(new ActiveHotspot(definition, new Pos(point.x(), point.y(), point.z()), statistic,
                        definition.buffs().getOverall(statistic), endsAt));
            }
        }
        return result;
    }

    private static List<ItemStatistic> buffStatistics(HotspotDefinition definition) {
        List<ItemStatistic> stats = new ArrayList<>();
        for (ItemStatistic statistic : ItemStatistic.values()) {
            if (definition.buffs().getOverall(statistic) != 0.0D) stats.add(statistic);
        }
        return stats;
    }

    private static long mix(long a, long b, long c) {
        long h = a * 0x9E3779B97F4A7C15L ^ b * 0xC2B2AE3D27D4EB4FL ^ c * 0x165667B19E3779F9L;
        h ^= h >>> 31;
        h *= 0xBF58476D1CE4E5B9L;
        return h ^ (h >>> 29);
    }

    private static double sinkerMultiplier(SkyBlockPlayer player) {
        if (player.getItemInMainHand() == null) return 1.0D;
        var sinker = FishingRodPartService.getSinker(new SkyBlockItem(player.getItemInMainHand()));
        return sinker != null && sinker.getHotspotBuffMultiplier() > 0 ? sinker.getHotspotBuffMultiplier() : 1.0D;
    }

    private static @Nullable String currentServer() {
        return HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType().name();
    }

    public record ActiveHotspot(HotspotDefinition definition, Pos position, ItemStatistic statistic, double value,
                                long endsAt) {
    }
}
