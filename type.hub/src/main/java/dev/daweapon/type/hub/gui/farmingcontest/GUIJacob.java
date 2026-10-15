package dev.daweapon.type.hub.gui.farmingcontest;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.farmingcontest.ContestBracket;
import dev.daweapon.type.skyblockgeneric.farmingcontest.ContestCrop;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContest;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContestProfile;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContestStatistics;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContests;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Jacob's menu, laid out from the wiki's Jacob/UI subpage.
public class GUIJacob extends StatelessView {
    private static final int UPCOMING_SHOWN = 3;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Jacob's Farming Contests", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);

        layout.slot(11, (_, _) -> ItemStacks.item(Material.OAK_SIGN, """
                <a>What is this?
                <8>Instructions

                <7>Every 3 SkyBlock days, I hold a contest
                <7>for 3 <e>farming <7>collections.

                <7>The contests last 1 SkyBlock day.
                <8>(20 minutes)

                <7>The <e>top <7>players who increase
                <7>their collection the most earn
                <a>Jacob's Tickets <7>and <d>unique
                <d>rewards<7>!

                <8>You may participate in 1 collection
                <8>contest per event!

                <8>Minions do not count in contests!"""));

        layout.autoUpdating(13, (_, c) -> ItemStacks.item(Material.CLOCK,
                        String.join("\n", upcomingLines(FarmingContestStatistics.anitaAccessoryFortune((SkyBlockPlayer) c.player())))),
                Duration.ofSeconds(1));

        layout.slot(15, (_, c) -> ItemStacks.item(Material.WHEAT, String.join("\n", claimLines((SkyBlockPlayer) c.player()))),
                (_, c) -> c.push(new GUIJacobContests(), new GUIJacobContests.State(0)));
    }

    private static List<String> upcomingLines(double anitaFortune) {
        List<String> lines = new ArrayList<>();
        lines.add("<6>Upcoming Contests");
        lines.add("<8>Schedule");
        Optional<FarmingContest> active = FarmingContest.active();
        active.ifPresent(contest -> {
            lines.add("");
            lines.add("<a>" + contestDate(contest) + " <7>(<a>Active <7>- <e>"
                    + StringUtility.formatTimeLeft((contest.endElapsed() - SkyBlockCalendar.getElapsed()) * 50L) + "<7>)");
            contest.crops().forEach(crop -> lines.add(cropLine(contest, crop, anitaFortune)));
        });
        for (FarmingContest contest : FarmingContest.upcoming(UPCOMING_SHOWN)) {
            lines.add("");
            lines.add("<e>" + contestDate(contest) + " <7>(in <e>"
                    + StringUtility.formatTimeLeft((contest.startElapsed() - SkyBlockCalendar.getElapsed()) * 50L) + "<7>)");
            contest.crops().forEach(crop -> lines.add(cropLine(contest, crop, anitaFortune)));
        }
        lines.add("");
        lines.add("<8>View this info in your full");
        lines.add("<8>SkyBlock calendar!");
        return lines;
    }

    private static String cropLine(FarmingContest contest, ContestCrop crop, double anitaFortune) {
        String line = "<e>○ <7>" + crop.getDisplayName();
        if (!crop.isAvailable()) return line + " <8>(Garden only)";
        if (anitaFortune > 0 && contest.anitaCrop() == crop) line += " <6>+" + (int) anitaFortune + "☘";
        return line;
    }

    private static String contestDate(FarmingContest contest) {
        long start = contest.startElapsed();
        return SkyBlockCalendar.getMonthName(start) + " " + StringUtility.ntify(SkyBlockCalendar.getDay(start));
    }

    private static List<String> claimLines(SkyBlockPlayer player) {
        FarmingContestProfile profile = FarmingContests.profile(player);
        List<String> lines = new ArrayList<>();
        lines.add("<b>Claim your rewards!");
        lines.add("<8>Previous contests");
        lines.add("");
        lines.add("<7>You've participated in <e>" + StringUtility.commaify(profile.getContestsParticipated()));
        lines.add("<7>previous contests.");
        int unclaimed = profile.claimable().size();
        if (unclaimed > 0) {
            lines.add("");
            lines.add("<7>Unclaimed rewards: <a>" + unclaimed);
        }
        lines.add("");
        lines.add("<7>Medals inventory:");
        lines.add(ContestBracket.GOLD.formatted() + " <r><7>medals: <6>" + profile.getGoldMedals());
        lines.add(ContestBracket.SILVER.formatted() + " <r><7>medals: <f>" + profile.getSilverMedals());
        lines.add(ContestBracket.BRONZE.formatted() + " <r><7>medals: <c>" + profile.getBronzeMedals());
        lines.add("");
        lines.add("<e>Click to view contests!");
        return lines;
    }

    static String cropList(FarmingContest contest, ContestCrop highlighted) {
        StringBuilder builder = new StringBuilder();
        for (ContestCrop crop : contest.crops()) {
            if (!builder.isEmpty()) builder.append("\n");
            builder.append(crop == highlighted ? "<e>☘ <f>" : "<e>○ <7>").append(crop.getDisplayName());
        }
        return builder.toString();
    }
}
