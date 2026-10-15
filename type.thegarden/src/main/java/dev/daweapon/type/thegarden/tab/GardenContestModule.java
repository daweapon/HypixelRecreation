package dev.daweapon.type.thegarden.tab;

import dev.daweapon.commons.protocol.objects.election.GetCandidatesProtocol;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContest;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GardenContestModule extends TablistModule {
    private static final long TICK_MILLIS = 50;
    private static final long CANDIDATE_REFRESH_MILLIS = 30_000;
    private static volatile List<GetCandidatesProtocol.CandidateInfo> candidates = List.of();
    private static volatile long candidatesFetchedAt;

    private static void refreshCandidates() {
        long now = System.currentTimeMillis();
        if (now - candidatesFetchedAt < CANDIDATE_REFRESH_MILLIS) return;
        candidatesFetchedAt = now;
        ElectionManager.fetchCandidates().thenAccept(list -> candidates = list).exceptionally(_ -> null);
    }

    static String clock(long millis) {
        long seconds = Math.max(0, millis / 1000);
        if (seconds >= 86_400) return seconds / 86_400 + "d";
        if (seconds >= 3600) return seconds / 3600 + "h " + seconds % 3600 / 60 + "m";
        return seconds >= 60 ? seconds / 60 + "m " + seconds % 60 + "s" : seconds + "s";
    }

    private static String bar(double percent) {
        int filled = (int) Math.round(percent / 10);
        return "|".repeat(filled) + "<8>" + "|".repeat(10 - filled);
    }

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        refreshCandidates();
        List<TablistEntry> entries = new ArrayList<>();
        entries.add(centered("<b><l>Info", p.getLocale(), TablistSkinRegistry.CYAN));
        long closeIn = SkyBlockCalendar.ticksUntilEvent(CalendarEvent.ELECTION_CLOSE) * TICK_MILLIS;
        entries.add(new TablistEntry(Text.of("<a><l>Election: <f>{}", clock(closeIn)), TablistSkinRegistry.GRAY));
        for (GetCandidatesProtocol.CandidateInfo candidate : candidates.stream().limit(5).toList()) {
            entries.add(new TablistEntry(Text.of(" <b>{}: <f>{} <7>({}%)", candidate.mayorName(), Text.parse(bar(candidate.votePercentage())), Math.round(candidate.votePercentage())), TablistSkinRegistry.GRAY));
        }
        entries.add(getGrayEntry());
        entries.add(new TablistEntry(Text.of("<a><l>Jacob's Contest:"), TablistSkinRegistry.GRAY));
        Optional<FarmingContest> active = FarmingContest.active();
        FarmingContest contest = active.orElseGet(() -> FarmingContest.upcoming(1).getFirst());
        long elapsed = SkyBlockCalendar.getElapsed();
        entries.add(new TablistEntry(Text.of(active.isPresent()
                ? " <a>Active <7>(ends in {})"
                : " <7>Starts In: <a>{}", clock(((active.isPresent() ? contest.endElapsed() : contest.startElapsed()) - elapsed) * TICK_MILLIS)), TablistSkinRegistry.GRAY));
        contest.crops().forEach(crop -> entries.add(new TablistEntry(Text.of(" <f>{}", crop.getDisplayName()), TablistSkinRegistry.GRAY)));
        entries.addAll(GardenPestsModule.trapEntries(Gardens.profile((dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer) p)));
        fillRestWithGray(entries);
        return entries;
    }
}
