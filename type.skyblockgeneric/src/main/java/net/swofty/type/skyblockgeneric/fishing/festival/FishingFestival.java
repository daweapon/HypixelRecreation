package net.swofty.type.skyblockgeneric.fishing.festival;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.registry.SeaCreatureDefinition;
import net.swofty.type.skyblockgeneric.fishing.tag.EventTag;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

/**
 * Marina's Fishing Festival (wiki: Fishing Festival). While it runs, every water Sea Creature
 * catch has a chance to be replaced by a Shark picked by weight from the ones the angler's
 * Fishing level allows.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FishingFestival {
    public static final String TAG_ID = "FISHING_FESTIVAL";
    public static final EventTag EVENT_TAG = EventTag.of(TAG_ID, FishingFestival::isActive);
    public static final double BASE_SHARK_CHANCE = 15D;
    public static final long DURATION_TICKS = 3L * SkyBlockCalendar.DAY;

    private static final String PREFIX = "<b><l>FISHING FESTIVAL <e>";

    public static List<Long> startTimesInYear() {
        return LongStream.range(0, 12).mapToObj(month -> month * SkyBlockCalendar.MONTH).toList();
    }

    public static boolean isActive() {
        return SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.FISHING_FESTIVAL);
    }

    public static CatchPayload.SeaCreature replaceWithShark(FishingContext context, CatchPayload.SeaCreature caught) {
        if (context.medium() != FishingMedium.WATER || !isActive()) return caught;
        if (ThreadLocalRandom.current().nextDouble(100D) >= sharkChance(context)) return caught;
        return pickShark(context)
                .map(shark -> new CatchPayload.SeaCreature(shark.id(), shark.skillXp(), caught.spawnCount()))
                .orElse(caught);
    }

    public static double sharkChance(FishingContext context) {
        double chance = BASE_SHARK_CHANCE;
        for (SharkChanceSource source : SharkChanceSource.values()) {
            chance += source.bonus(context);
        }
        return chance;
    }

    private static Optional<SeaCreatureDefinition> pickShark(FishingContext context) {
        int fishingLevel = context.player().getSkills().getCurrentLevel(SkillCategories.FISHING);
        List<FestivalShark> eligible = new ArrayList<>();
        List<SeaCreatureDefinition> definitions = new ArrayList<>();
        double totalWeight = 0;
        for (FestivalShark shark : FestivalShark.values()) {
            SeaCreatureDefinition definition = FishingRegistry.getSeaCreature(shark.name());
            if (definition == null || fishingLevel < definition.requiredFishingLevel() || !definition.isAvailable(context)) {
                continue;
            }
            eligible.add(shark);
            definitions.add(definition);
            totalWeight += shark.getWeight();
        }
        if (eligible.isEmpty()) return Optional.empty();

        double roll = ThreadLocalRandom.current().nextDouble(totalWeight);
        for (int i = 0; i < eligible.size(); i++) {
            roll -= eligible.get(i).getWeight();
            if (roll < 0) return Optional.of(definitions.get(i));
        }
        return Optional.of(definitions.getLast());
    }

    public static void announceStart() {
        broadcast("The festival is now underway! Break out your fishing rods and watch out for sharks!");
    }

    static void announceEnd() {
        broadcast("The festival has concluded! Time to dry off and repair your rods!");
    }

    static void announceCountdown(int days) {
        broadcast("The event starts in <6>" + days + " <e>" + (days == 1 ? "day" : "days") + "!");
    }

    private static void broadcast(String message) {
        Text text = Text.parseLenient(PREFIX + message);
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.sendMessage(text.asComponent());
        }
    }
}
