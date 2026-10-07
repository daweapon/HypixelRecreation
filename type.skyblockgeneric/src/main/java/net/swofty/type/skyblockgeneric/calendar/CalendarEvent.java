package net.swofty.type.skyblockgeneric.calendar;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.protocol.objects.darkauction.TriggerDarkAuctionProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContest;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

// Defines a recurring SkyBlock calendar event (New Year, Dark Auction, elections, festivals, etc.)
// with its display info, occurrence times/duration, and the action run when it triggers.
public record CalendarEvent(
    String id,
    ItemStack representation,
    Function<Integer, Text> displayName,
    List<Text> description,
    List<Long> times,
    Duration duration,
    boolean tracksYear,
    BiConsumer<Long, Integer> action
) {
    private static final Duration DARK_AUCTION_DURATION = Duration.ofMinutes(5);
    private static final long THREE_DAYS = 3L * SkyBlockCalendar.DAY;
    private static final long YEAR = SkyBlockCalendar.YEAR;
    private static final long MONTH = SkyBlockCalendar.MONTH;
    private static final long DAY = SkyBlockCalendar.DAY;
    private static final Map<Long, List<CalendarEvent>> eventCache = new HashMap<>();
    private static final List<CalendarEvent> allEvents = new ArrayList<>();

    // Foxy's Extra Event schedules a second Spooky Festival on Late Summer 22nd (wiki: Spooky
    // Festival, "Foxy's Festival Schedule").
    private static final Map<String, PerkOccurrence> PERK_OCCURRENCES = Map.of(
        "spooky_festival", new PerkOccurrence(SkyBlockMayor.Perk.EXTRA_EVENT, List.of(5 * MONTH + 21 * DAY))
    );

    public static CalendarEvent NEW_YEAR = new CalendarEvent(
        "new_year",
        ItemStack.of(Material.CAKE).with(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true),
        year -> Text.of("<d>{} New Year Celebration", StringUtility.ntify(year)),
        List.of(
            Text.of("<7>To celebrate the SkyBlock New Year,"),
            Text.of("<7>the Baker is giving out free Cake!")
        ),
        List.of(10L),
        Duration.ofHours(1),
        true,
        (time, year) -> {
            // The Baker's free-Cake gate (NPCBaker.java) already gives out the real reward by
            // continuously polling getCurrentEvents(); this one-time trigger only handles the
            // celebratory broadcast that polling can't express.
            Text announcement = Text.parseLenient(
                    "<d><l>HAPPY NEW YEAR! <7>Year " + StringUtility.ntify(year) +
                            " has begun! <7>Visit the <a>Baker <7>in the Hub for free Cake!");
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                player.sendMessage(announcement.asComponent());
                player.playSound(Sound.sound(Key.key("entity.firework_rocket.blast"), Sound.Source.MASTER, 1f, 1f));
            }
        }
    );

    // Dark Auction occurs every 3 SkyBlock days at midnight
    public static CalendarEvent DARK_AUCTION = new CalendarEvent(
        "dark_auction",
        ItemStack.of(Material.NETHER_BRICK).with(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true),
        year -> Text.of("<5>Dark Auction"),
        List.of(
            Text.of("<7>The Dark Auction is a secret"),
            Text.of("<7>underground auction where"),
            Text.of("<7>special items are sold.")
        ),
        calculateDarkAuctionTimes(),
        DARK_AUCTION_DURATION,
        false,
            (time, year) -> triggerDarkAuctionUntil(time, System.nanoTime() + DARK_AUCTION_DURATION.toNanos())
    );

    private static void triggerDarkAuctionUntil(long eventTime, long deadlineNanos) {
        RedisClient.isServiceOnline(ServiceType.DARK_AUCTION).thenAccept(online -> {
            if (!online) {
                retryDarkAuction(eventTime, deadlineNanos);
                return;
            }
            new ProxyService(ServiceType.DARK_AUCTION).handleRequest(
                    new TriggerDarkAuctionProtocol(),
                            new TriggerDarkAuctionProtocol.TriggerMessage(eventTime))
                .thenAccept(response -> {
                    if (response.success()) {
                        Logger.info("Dark Auction started successfully");
                    } else {
                        Logger.debug("Dark Auction trigger: {}", response.message());
                    }
                })
                .exceptionally(throwable -> {
                    Logger.debug("Dark Auction service became unavailable while triggering: {}", throwable.getMessage());
                    retryDarkAuction(eventTime, deadlineNanos);
                    return null;
                });
        });
    }

    private static void retryDarkAuction(long eventTime, long deadlineNanos) {
        if (System.nanoTime() >= deadlineNanos) return;
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                .execute(() -> triggerDarkAuctionUntil(eventTime, deadlineNanos));
    }

    private static List<Long> calculateDarkAuctionTimes() {
        List<Long> times = new ArrayList<>();
        for (long time = 0; time < YEAR; time += THREE_DAYS) {
            times.add(time);
        }
        return times;
    }

    // Jerry's Perkpocalypse: every 18 SkyBlock days, re-roll which mayor's perks are piggybacked
    // onto Jerry's term. No-op unless Jerry is currently mayor with Perkpocalypse active.
    private static final long EIGHTEEN_DAYS = 18L * SkyBlockCalendar.DAY;
    public static CalendarEvent PERKPOCALYPSE = new CalendarEvent(
        "perkpocalypse",
        ItemStack.of(Material.LIME_WOOL).with(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true),
        year -> Text.of("<d>Perkpocalypse!"),
        List.of(
            Text.of("<7>Jerry's Perkpocalypse has"),
            Text.of("<7>activated another Mayor's perks!")
        ),
        calculatePerkpocalypseTimes(),
        Duration.ofMinutes(1),
        false,
        (_, _) -> ElectionManager.triggerPerkpocalypseIfEligible()
    );

    private static List<Long> calculatePerkpocalypseTimes() {
        List<Long> times = new ArrayList<>();
        for (long time = 0; time < YEAR; time += EIGHTEEN_DAYS) {
            times.add(time);
        }
        return times;
    }

    // Matches the real SkyBlock Wiki-documented cycle: voting opens Late Summer 27th and stays open
    // until Late Spring 27th of the FOLLOWING year (a ~9 month window spanning the year boundary),
    // not a short same-year window. Late Summer is month index 5 (0-based), Late Spring index 2;
    // since Late Spring's within-year tick position is earlier than Late Summer's, this recurring
    // tick naturally lands a full year later the next time it comes around - exactly matching the
    // real close-in-the-following-year behavior. The actual open/close is driven day-precisely by
    // ElectionManager.checkElectionCycle(); these entries exist for the player-facing Calendar GUI
    // and call the same idempotent (already-open/closed no-ops) methods as a redundant safety net.
    public static CalendarEvent ELECTION_OPEN = new CalendarEvent(
        "election_open",
        ItemStack.of(Material.JUKEBOX),
        year -> Text.of("<b>{} Election Booth Opens", StringUtility.ntify(year)),
        List.of(
            Text.of("<7>The Mayor Election voting booth"),
            Text.of("<7>is now open! Cast your vote for"),
            Text.of("<7>your favorite candidate.")
        ),
        List.of(5 * MONTH + 26 * DAY),
        Duration.ofMinutes(5),
        true,
        (_, _) -> {
            ElectionManager.onElectionStart();
        }
    );

    public static CalendarEvent ELECTION_CLOSE = new CalendarEvent(
        "election_close",
        ItemStack.of(Material.JUKEBOX),
        year -> Text.of("<b>{} Election Over!", StringUtility.ntify(year)),
        List.of(
            Text.of("<7>The Mayor Election has concluded!"),
            Text.of("<7>A new Mayor has been elected.")
        ),
        List.of(2 * MONTH + 26 * DAY),
        Duration.ofMinutes(5),
        true,
        (_, _) -> {
            ElectionManager.onElectionEnd();
        }
    );

    // Twice yearly: Early Summer 1-3 and Early Winter 1-3
    public static CalendarEvent TRAVELING_ZOO = new CalendarEvent(
        "traveling_zoo",
        ItemStack.of(Material.HAY_BLOCK),
        year -> Text.of("<a>Traveling Zoo"),
        List.of(
            Text.of("<7>A traveling zoo has arrived!"),
            Text.of("<7>Visit to see rare animals and"),
            Text.of("<7>buy exclusive pets.")
        ),
        List.of(3 * MONTH, 9 * MONTH),
        Duration.ofMinutes(3L * 20),
        true,
        (_, _) -> {
            // NPCOringo (type.hub) gates its own visibility/click off getCurrentEvents() directly,
            // same pattern as NPCBaker for New Year - this only handles the one-time announcement.
            Text announcement = Text.parseLenient(
                    "<a><l>THE TRAVELING ZOO HAS ARRIVED! <7>Visit <a>Oringo <7>in the Village to see rare animals and buy exclusive pets!");
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                player.sendMessage(announcement.asComponent());
            }
        }
    );

    // Once yearly: Autumn 29-31
    public static CalendarEvent SPOOKY_FESTIVAL = new CalendarEvent(
        "spooky_festival",
        ItemStack.of(Material.JACK_O_LANTERN),
        year -> Text.of("<6>Spooky Festival"),
        List.of(
            Text.of("<7>The Spooky Festival has begun!"),
            Text.of("<7>Trade candy and fight monsters"),
            Text.of("<7>for spooky rewards.")
        ),
        List.of(7 * MONTH + 28 * DAY),
        Duration.ofMinutes(3L * 20),
        true,
        (_, _) -> {
            // SpookyFestivalCandyHandler and NPCFearMongerer both gate off getCurrentEvents()
            // directly - this only handles the one-time announcement.
            Text announcement = Text.parseLenient(
                    "<6><l>THE SPOOKY FESTIVAL HAS BEGUN! <7>Kill monsters for a chance at Candy, then trade it in with the <6>Fear Mongerer<7>!");
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                player.sendMessage(announcement.asComponent());
            }
        }
    );

    // Once yearly, starting Late Winter 23rd. Real Hypixel (SkyBlock Wiki, "Jerry's Workshop"):
    // the island opens for the entirety of Late Winter (~11 real-world hours) - user-reported
    // the previous 1-hour duration was wrong ("the event lasts more than an hour"). This
    // codebase's Late Winter (1 SkyBlock month = MONTH ticks) is itself ~10.3 real-world hours
    // at 20 ticks/sec, so spanning the full month here lines up almost exactly with the wiki.
    public static CalendarEvent SEASON_OF_JERRY = new CalendarEvent(
        "season_of_jerry",
        ItemStack.of(Material.LIME_WOOL),
        year -> Text.of("<d>Season of Jerry"),
        List.of(
            Text.of("<7>Jerry's Workshop is open!"),
            Text.of("<7>Complete gifts and quests"),
            Text.of("<7>for festive rewards.")
        ),
        List.of(11 * MONTH + 23 * DAY),
        Duration.ofSeconds(MONTH / 20L),
        true,
        (_, _) -> {}
    );

    // Wiki: Jacob's Farming Contest - every 3 SkyBlock days from the 2nd, lasting one SkyBlock day.
    public static CalendarEvent FARMING_CONTEST = new CalendarEvent(
        "farming_contest",
        ItemStack.of(Material.WHEAT),
        year -> Text.of("<e>Jacob's Farming Contest"),
        List.of(
            Text.of("<7>Jacob picks <e>3 crops<7>. The top"),
            Text.of("<7>farmers of each crop earn <a>Jacob's"),
            Text.of("<a>Tickets <7>and <d>unique rewards<7>!")
        ),
        FarmingContest.startTimesInYear(),
        Duration.ofMillis(FarmingContest.DURATION * 50L),
        false,
        (_, _) -> FarmingContests.announceStart()
    );

    static {
        registerEvent(NEW_YEAR);
        registerEvent(DARK_AUCTION);
        registerEvent(PERKPOCALYPSE);
        registerEvent(ELECTION_OPEN);
        registerEvent(ELECTION_CLOSE);
        registerEvent(TRAVELING_ZOO);
        registerEvent(SPOOKY_FESTIVAL);
        registerEvent(SEASON_OF_JERRY);
        registerEvent(FARMING_CONTEST);
    }

    @Override
    public List<Long> times() {
        PerkOccurrence extra = PERK_OCCURRENCES.get(id);
        return extra == null ? times : extra.addTo(times);
    }

    private record PerkOccurrence(SkyBlockMayor.Perk perk, List<Long> extraTimes) {
        List<Long> addTo(List<Long> regularTimes) {
            if (!ElectionPerks.isActive(perk)) return regularTimes;
            List<Long> combined = new ArrayList<>(regularTimes);
            combined.addAll(extraTimes);
            return combined;
        }
    }

    public Text getDisplayName(int year) {
        return displayName.apply(year);
    }

    // Ticks remaining until this event's currently-active occurrence ends, or -1 if it isn't
    // active at the given time (elapsed % YEAR). Mirrors getCurrentEvents(long)'s own active-
    // window check so the two never disagree about whether an event is active.
    public long remainingTicksAt(long timeInYear) {
        long durationTicks = duration.toMillis() / 50;
        for (Long eventStartTime : times()) {
            long ticksSinceStart = timeInYear - eventStartTime;
            if (ticksSinceStart >= 0 && ticksSinceStart < durationTicks) {
                return durationTicks - ticksSinceStart;
            }
        }
        return -1;
    }

    private static void registerEvent(CalendarEvent event) {
        allEvents.add(event);
        for (Long time : event.times()) {
            eventCache.computeIfAbsent(time, k -> new ArrayList<>()).add(event);
        }
    }

    public static List<CalendarEvent> getCurrentEvents(long time) {
        List<CalendarEvent> activeEvents = new ArrayList<>();
        for (CalendarEvent event : allEvents) {
            for (Long eventStartTime : event.times()) {
                // convert ticks to milliseconds
                Duration timeSinceEventStart = Duration.ofMillis((time - eventStartTime) * 50);

                // checks if the event is currently active
                if (!timeSinceEventStart.isNegative() && timeSinceEventStart.compareTo(event.duration()) < 0) {
                    activeEvents.add(event);
                    break;
                }
            }
        }
        return activeEvents;
    }

    public static List<CalendarEvent> getAllEvents() {
        return new ArrayList<>(allEvents);
    }

    public static Map<Long, List<CalendarEvent>> getEventCache() {
        return new HashMap<>(eventCache);
    }

    public static CalendarEvent fromId(String id) {
        for (CalendarEvent event : allEvents) {
            if (event.id().equalsIgnoreCase(id)) {
                return event;
            }
        }
        return null;
    }
}
