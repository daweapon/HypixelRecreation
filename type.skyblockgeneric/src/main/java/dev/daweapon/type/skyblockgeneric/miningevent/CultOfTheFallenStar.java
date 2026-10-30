package dev.daweapon.type.skyblockgeneric.miningevent;

import net.kyori.adventure.text.format.NamedTextColor;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class CultOfTheFallenStar {
    private static final String JOINED = "joined";
    private static final String JOINED_MEETING = "joined_meeting";
    private static final String ATTENDED = "attended";
    private static final String LAST_MEETING = "last_meeting";
    private static final String HELMET_ID = "FALLEN_STAR_HAT";
    private static final int LINE_DELAY_TICKS = 40;
    private static final int DAYS_IN_MONTH = 31;
    private static final int SECONDS_PER_SKYBLOCK_HOUR = 50;
    public static final int MEETINGS_FOR_CONTACT = 3;
    private static final Set<UUID> BUSY = ConcurrentHashMap.newKeySet();

    private CultOfTheFallenStar() {
    }

    public static boolean meetingActive() {
        MiningEventConfig config = MiningEventConfig.get();
        boolean meetingDay = config.strings("cult_of_the_fallen_star", "meeting_days").contains(String.valueOf(SkyBlockCalendar.getDay()));
        return meetingDay && SkyBlockCalendar.getHour() < config.integer(6, "cult_of_the_fallen_star", "end_hour");
    }

    public static int attended(SkyBlockPlayer player) {
        return (int) state(player).getOrDefault(ATTENDED, 0L).longValue();
    }

    public static boolean joined(SkyBlockPlayer player) {
        return state(player).getOrDefault(JOINED, 0L) != 0L;
    }

    public static void call(SkyBlockPlayer player) {
        int attended = attended(player);
        String total = "<b>✆ <f>You've attended <6>" + attended + "<f> meeting" + (attended == 1 ? "" : "s") + " of the <5>Cult of the Fallen Star<f> so far.";
        if (meetingActive()) {
            player.sendMessage(CultMeetingScripts.format("D:<b>✆ <f>We're meeting right now! Come on over, the star is the brightest at midnight."));
        } else {
            int hours = hoursUntilNextMeeting();
            player.sendMessage(CultMeetingScripts.format("D:<b>✆ <f>Our next meeting is in <6>" + hours / 24 + " SkyBlock days " + hours % 24
                    + " hours<f>, about <6>" + hours * SECONDS_PER_SKYBLOCK_HOUR / 60 + " minutes<f> from now."));
        }
        player.sendMessage(CultMeetingScripts.format("D:" + total));
    }

    private static int hoursUntilNextMeeting() {
        List<String> days = MiningEventConfig.get().strings("cult_of_the_fallen_star", "meeting_days");
        int today = SkyBlockCalendar.getDay();
        int best = Integer.MAX_VALUE;
        for (String day : days) {
            int target = Integer.parseInt(day);
            int delta = target - today;
            if (delta < 0 || delta == 0 && SkyBlockCalendar.getHour() > 0) delta += DAYS_IN_MONTH;
            best = Math.min(best, delta * 24 - SkyBlockCalendar.getHour());
        }
        return Math.max(best, 0);
    }

    public static boolean wearingHelmet(SkyBlockPlayer player) {
        return new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType() == ItemType.valueOf(HELMET_ID);
    }

    public static void interact(SkyBlockPlayer player) {
        if (!meetingActive() || !BUSY.add(player.getUuid())) return;
        Map<String, Long> state = state(player);
        long meeting = currentMeetingId();
        if (state.getOrDefault(JOINED, 0L) == 0L) {
            offerMembership(player);
        } else if (state.getOrDefault(JOINED_MEETING, 0L) == meeting) {
            play(player, List.of(
                    "D:Eager to join our cult I see?",
                    "D:Come back to our next meeting in <6>seven days<f> while wearing the <d>Fallen Star Helmet<f> we gave you."));
        } else if (state.getOrDefault(LAST_MEETING, 0L) == meeting && !wearingHelmet(player)) {
            play(player, List.of(
                    "D:Hi again, <7>" + player.getUsername() + "<f>.",
                    "D:Come back to our next meeting in <6>seven days<f> while wearing the <d>Fallen Star Helmet<f> we gave you.",
                    "D:We'll share some of our cool stars with you."));
        } else if (!wearingHelmet(player)) {
            play(player, List.of(
                    "D:Hey, <7>" + player.getUsername() + "<f>! Glad you came to our cult meeting!",
                    "D:Wait...",
                    "D:Where's the <d>Fallen Star Helmet<f> we gave you?",
                    "D:You need it to join us!",
                    "D:Come back next time wearing our matching hat."));
        } else if (state.getOrDefault(LAST_MEETING, 0L) == meeting) {
            play(player, List.of(randomChatter(player)));
        } else {
            attend(player, state, meeting);
        }
    }

    private static void offerMembership(SkyBlockPlayer player) {
        List<String> intro = List.of(
                "D:Wow there, you found us!",
                "D:Are you trying to stop us from doing our suspicious activities?",
                "D:We are the <d>Cult of the Fallen Star<f>, we mean no harm.",
                "D:To be honest, we just want to look cool.",
                "D:Wanna join us?");
        play(player, intro, () -> {
            player.sendMessage("<b>Join the Cult of the Fallen Star?");
            player.sendMessage("<7>Click an option: ");
            NPCOption.sendOption(player, "cult_of_the_fallen_star", false, List.of(
                    new NPCOption.Option("yes", NamedTextColor.GREEN, true, "YES", _ -> acceptMembership(player)),
                    new NPCOption.Option("no", NamedTextColor.RED, true, "NO", _ -> play(player, List.of("D:Rude.")))));
        });
    }

    private static void acceptMembership(SkyBlockPlayer player) {
        if (!BUSY.add(player.getUuid())) return;
        Map<String, Long> state = state(player);
        if (state.getOrDefault(JOINED, 0L) != 0L) {
            BUSY.remove(player.getUuid());
            return;
        }
        state.put(JOINED, 1L);
        state.put(JOINED_MEETING, currentMeetingId());
        player.addAndUpdateItem(ItemType.valueOf(HELMET_ID));
        play(player, List.of(
                "D:Nice! Well, take this.",
                "D:It's one of our <d>Fallen Star Helmets<f>.",
                "D:Wear it in our next meetings!"));
    }

    private static void attend(SkyBlockPlayer player, Map<String, Long> state, long meeting) {
        int count = attended(player) + 1;
        state.put(ATTENDED, (long) count);
        state.put(LAST_MEETING, meeting);
        int starfall = MiningEventConfig.get().integer(200, "cult_of_the_fallen_star", "starfall_per_meeting");
        player.addAndUpdateItem(ItemType.STARFALL, starfall);
        play(player, CultMeetingScripts.meeting(count, player.getUsername()), () ->
                player.sendMessage("<8> +<f>" + starfall + " <9>Starfall"));
    }

    private static String randomChatter(SkyBlockPlayer player) {
        List<String> lines = CultMeetingScripts.chatter(player.getUsername());
        return lines.get(ThreadLocalRandom.current().nextInt(lines.size()));
    }

    private static void play(SkyBlockPlayer player, List<String> lines) {
        play(player, lines, () -> {
        });
    }

    private static void play(SkyBlockPlayer player, List<String> lines, Runnable after) {
        playFrom(player, lines, 0, after);
    }

    private static void playFrom(SkyBlockPlayer player, List<String> lines, int index, Runnable after) {
        if (index >= lines.size() || !player.isOnline()) {
            BUSY.remove(player.getUuid());
            if (player.isOnline()) after.run();
            return;
        }
        player.sendMessage(CultMeetingScripts.format(lines.get(index)));
        ScheduleUtility.delay(() -> playFrom(player, lines, index + 1, after), LINE_DELAY_TICKS);
    }

    private static long currentMeetingId() {
        return SkyBlockCalendar.getYear() * 10_000L + SkyBlockCalendar.getMonth() * 100L + SkyBlockCalendar.getDay();
    }

    private static Map<String, Long> state(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CULT_OF_THE_FALLEN_STAR, DatapointMapStringLong.class).getValue();
    }
}
