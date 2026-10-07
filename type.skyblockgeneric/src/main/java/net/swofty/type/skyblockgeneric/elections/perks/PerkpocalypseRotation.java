package net.swofty.type.skyblockgeneric.elections.perks;

import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Jerry's Perkpocalypse order. Hypixel fixes one shuffled order of six Mayors when Jerry is elected
 * and cycles it every 18 SkyBlock days for his whole term (wiki: Candidate Jerry/Perkpocalypse).
 * The order is seeded by the election year and the slot is derived from the calendar, so every
 * server agrees on the active Mayor and a restart does not lose it.
 */
public final class PerkpocalypseRotation {
    public static final long SLOT_LENGTH = 18L * SkyBlockCalendar.DAY;
    static final long SLOTS_PER_YEAR = (SkyBlockCalendar.YEAR + SLOT_LENGTH - 1) / SLOT_LENGTH;

    private static final List<SkyBlockMayor> ELIGIBLE = List.of(
            SkyBlockMayor.AATROX, SkyBlockMayor.COLE, SkyBlockMayor.DIANA,
            SkyBlockMayor.FINNEGAN, SkyBlockMayor.MARINA, SkyBlockMayor.PAUL);

    private static volatile CachedOrder cachedOrder = new CachedOrder(Integer.MIN_VALUE, List.of());

    private PerkpocalypseRotation() {}

    public static List<SkyBlockMayor> order(int electedYear) {
        CachedOrder cached = cachedOrder;
        if (cached.year == electedYear) return cached.order;
        List<SkyBlockMayor> shuffled = new ArrayList<>(ELIGIBLE);
        Collections.shuffle(shuffled, new Random(electedYear));
        List<SkyBlockMayor> order = List.copyOf(shuffled);
        cachedOrder = new CachedOrder(electedYear, order);
        return order;
    }

    public static SkyBlockMayor mayorAt(int electedYear, long elapsed) {
        List<SkyBlockMayor> order = order(electedYear);
        return order.get((int) Math.floorMod(slot(elapsed), (long) order.size()));
    }

    static long slot(long elapsed) {
        long year = Math.floorDiv(elapsed, (long) SkyBlockCalendar.YEAR);
        long timeInYear = Math.floorMod(elapsed, (long) SkyBlockCalendar.YEAR);
        return year * SLOTS_PER_YEAR + timeInYear / SLOT_LENGTH;
    }

    private record CachedOrder(int year, List<SkyBlockMayor> order) {}
}
