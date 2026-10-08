package net.swofty.type.jerrysworkshop.present;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.LodestoneTracker;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointPresentYear;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Points a held Gift Compass at the nearest White Gift the player hasn't found this year, through
 * the compass's lodestone target. It keeps that gift until it is found, then moves on; the chat
 * lines on a new target are our own wording.
 */
public final class GiftCompass {
    private static final Map<UUID, Optional<BlockVec>> TARGETS = new ConcurrentHashMap<>();

    private GiftCompass() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(() -> SkyBlockGenericLoader.getLoadedPlayers().forEach(GiftCompass::update))
                .repeat(TaskSchedule.tick(10)).schedule();
    }

    public static void forget(SkyBlockPlayer player) {
        TARGETS.remove(player.getUuid());
    }

    private static void update(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        if (held.material() != Material.COMPASS || player.getInstance() == null
                || new SkyBlockItem(held).getAttributeHandler().getPotentialType() != ItemType.GIFT_COMPASS) {
            return;
        }
        List<BlockVec> unfound = unfound(player);
        Optional<BlockVec> previous = TARGETS.get(player.getUuid());
        BlockVec target = previous != null && previous.isPresent() && unfound.contains(previous.get())
                ? previous.get() : nearest(player, unfound);
        TARGETS.put(player.getUuid(), Optional.ofNullable(target));
        if (previous != null && !previous.equals(Optional.ofNullable(target))) {
            player.sendMessage(target == null
                    ? "<a>You have found every <f>White Gift <a>this year!"
                    : "<a>Your Gift Compass now points towards another <f>White Gift<a>!");
        }
        if (target == null) return;
        LodestoneTracker tracker = new LodestoneTracker(player.getInstance().getDimensionName(), target, false);
        if (!tracker.equals(held.get(DataComponents.LODESTONE_TRACKER))) {
            player.setItemInMainHand(held.with(DataComponents.LODESTONE_TRACKER, tracker));
        }
    }

    private static List<BlockVec> unfound(SkyBlockPlayer player) {
        int year = SkyBlockCalendar.getYear();
        DatapointPresentYear.YearData presents = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP, DatapointPresentYear.class).getValue();
        List<Integer> found = presents.year() == year ? presents.value() : List.of();
        return WhiteGiftLocations.forYear(year).stream()
                .filter(poolIndex -> !found.contains(poolIndex + 1))
                .map(WhiteGiftLocations::position)
                .toList();
    }

    private static @Nullable BlockVec nearest(SkyBlockPlayer player, List<BlockVec> candidates) {
        BlockVec nearest = null;
        double best = Double.MAX_VALUE;
        for (BlockVec position : candidates) {
            double distance = position.distanceSquared(player.getPosition());
            if (distance < best) {
                best = distance;
                nearest = position;
            }
        }
        return nearest;
    }
}
