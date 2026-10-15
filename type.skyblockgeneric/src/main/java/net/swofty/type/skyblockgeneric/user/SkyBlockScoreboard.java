package net.swofty.type.skyblockgeneric.user;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import org.tinylog.Logger;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.auctions.DarkAuctionPhase;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextBody;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.utility.BlockUtility;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestBracket;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContest;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestProfile;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContests;
import net.swofty.type.skyblockgeneric.spookyfestival.SpookyFestivalCandyTally;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.darkauction.DarkAuctionHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mission.LocationAssociatedMission;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockProgressMission;
import net.swofty.type.skyblockgeneric.glacite.ColdService;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SkyBlockScoreboard {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final Text BLANK = Text.literal(" ");
    private static final HypixelScoreboard scoreboard = new HypixelScoreboard();
    private static Integer skyblockName = 0;

    // Dark Auction/Perkpocalypse/elections already have their own dedicated scoreboard sections
    // or UI - this covers the 4 seasonal events (Roadmap #19/20/21/24) that previously had no
    // in-game way to see their remaining time.
    private static final List<CalendarEvent> SEASONAL_SCOREBOARD_EVENTS = List.of(
        CalendarEvent.NEW_YEAR, CalendarEvent.TRAVELING_ZOO,
        CalendarEvent.SPOOKY_FESTIVAL, CalendarEvent.SEASON_OF_JERRY, CalendarEvent.FISHING_FESTIVAL
    );

    public static void start() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();

        scheduler.submitTask(() -> {
            skyblockName++;
            if (skyblockName > 50) {
                skyblockName = 0;
            }

            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                try {
                    render(player);
                } catch (Exception exception) {
                    Logger.error(exception, "Couldn't render {}'s scoreboard", player.getUsername());
                }
            }
            return TaskSchedule.tick(4);
        });
    }

    private static void render(SkyBlockPlayer player) {
        Locale l = player.getLocale();
        SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
        SkyBlockRegion region = player.getRegion();
        MissionData missionData = player.getMissionData();

        if (dataHandler == null) {
            return;
        }

        TextBody body = new TextBody();
        body.section("date").line(Text.key("scoreboard.common.date_line",
            DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
            HypixelConst.getServerName()));

        TextBody.Section location = body.section("location");
        location.line(BLANK);
        location.line(Text.key("scoreboard.skyblock.calendar_date_line",
            SkyBlockCalendar.getMonthName(),
            StringUtility.ntify(SkyBlockCalendar.getDay())));
        location.line(Text.key("scoreboard.skyblock.calendar_time_line",
            SkyBlockCalendar.getDisplay(SkyBlockCalendar.getElapsed())));
        Text area = AreaSidebar.areaFor(player);
        if (area != null) {
            location.line(Text.key("scoreboard.skyblock.region_line", area));
        } else try {
            RegionType type = region.getType();
            String name = type.getName();
            if (type == RegionType.PLAYER_MUSEUM) {
                name = name.formatted(player.getUsername());
            }
            location.line(Text.key("scoreboard.skyblock.region_line",
                Text.of("<color:{}>{}", type.getColor(), name)));
        } catch (NullPointerException ignored) {
            location.line(" {}", Text.key("scoreboard.skyblock.region_unknown"));
        }
        AreaSidebar.detailFor(player).forEach(detail -> location.line(detail));
        double cold = ColdService.cold(player);
        if (cold < 0) {
            location.line(Text.of("<b>Cold: {}❄", (int) Math.floor(cold)));
        }

        // TODO: make classes / a manager for regions to display scoreboard information.
        boolean electionRoom = region != null && region.getType() == RegionType.ELECTION_ROOM;
        TextBody.Section election = body.section("election").when(() -> electionRoom);
        if (electionRoom) {
            election.line(BLANK);
            election.line(Text.key("scoreboard.skyblock.election_votes_title",
                SkyBlockCalendar.getYear()));
            Map<String, Long> totalVotes = ElectionManager.getElectionData().tallyVotes();
            long maxVotes = totalVotes.values().stream().mapToLong(Long::longValue).max().orElse(1);
            ElectionManager.getElectionData().getCandidates().forEach(candidate -> {
                long votes = totalVotes.getOrDefault(candidate.getMayorName(), 0L);
                int barLength = maxVotes > 0 ? (int) Math.round((votes * 15.0) / maxVotes) : 0;
                Text bars = Text.of("<color:{0}>{1}", candidate.getColor(), "|".repeat(barLength))
                    .append("<f>{}", "|".repeat(15 - barLength));
                election.line(Text.key("scoreboard.skyblock.election_candidate_line",
                    bars,
                    candidate.getColoredName()));
            });
        }

        boolean purseShown = !electionRoom && !ChallengeSidebar.hidesPurse(player);
        TextBody.Section purse = body.section("purse").when(() -> purseShown);
        if (purseShown) {
            purse.line(BLANK);
            purse.line(Text.key("scoreboard.skyblock.purse_line",
                StringUtility.commaify(dataHandler.get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).getValue())));

            Integer bits = dataHandler.get(SkyBlockDataHandler.Data.BITS, DatapointInteger.class).getValue();
            boolean hasBits = bits != null && bits > 0;
            TextBody.Section bitsLine = purse.child("bits").when(() -> hasBits);
            if (hasBits) {
                bitsLine.line(Text.key("scoreboard.skyblock.bits_line",
                    StringUtility.commaify(bits)));
            }

            net.swofty.type.skyblockgeneric.garden.GardenProfile garden = net.swofty.type.skyblockgeneric.garden.Gardens.profile(player);
            boolean hasCopper = net.swofty.type.generic.HypixelConst.isGardenServer() && garden.getCopper() >= 1;
            TextBody.Section copperLine = purse.child("copper").when(() -> hasCopper);
            if (hasCopper) {
                copperLine.line(Text.key("scoreboard.skyblock.copper_line", StringUtility.commaify(Math.floor(garden.getCopper()))));
            }
            boolean hasSowdust = garden.getSowdust() >= 1;
            TextBody.Section sowdustLine = purse.child("sowdust").when(() -> hasSowdust);
            if (hasSowdust) {
                sowdustLine.line(Text.key("scoreboard.skyblock.sowdust_line", StringUtility.commaify(Math.floor(garden.getSowdust()))));
            }
        }

        List<String> challengeLines = ChallengeSidebar.linesFor(player);
        boolean challenge = challengeLines != null;
        TextBody.Section challengeSection = body.section("challenge").when(() -> challenge);
        if (challenge) {
            challengeSection.line(BLANK);
            challengeLines.forEach(line -> challengeSection.line(Text.of(line)));
        }

        boolean darkAuction = !electionRoom && !challenge
            && DarkAuctionHandler.isPlayerInAuction(player.getUuid())
            && DarkAuctionHandler.getLocalState() != null
            && DarkAuctionHandler.getLocalState().getPhase() == DarkAuctionPhase.BIDDING;
        TextBody.Section auction = body.section("dark_auction").when(() -> darkAuction);
        if (darkAuction) {
            auction.line("<8> ");
            DarkAuctionHandler.DarkAuctionLocalState auctionState = DarkAuctionHandler.getLocalState();
            int timeRemaining = DarkAuctionHandler.getTimeLeft().get();

            auction.line(Text.key("scoreboard.skyblock.dark_auction.time_left_line",
                timeRemaining));
            auction.line(Text.key("scoreboard.skyblock.dark_auction.current_item_label"));

            String currentItem = auctionState.getCurrentItemType();
            if (currentItem != null) {
                try {
                    ItemType itemType = ItemType.valueOf(currentItem);
                    SkyBlockItem item = new SkyBlockItem(itemType);
                    auction.line(" {}",
                        Text.key("scoreboard.skyblock.dark_auction.current_item_line",
                            item.getDisplayName()));
                } catch (Exception e) {
                    auction.line(" {}",
                        Text.key("scoreboard.skyblock.dark_auction.current_item_line",
                            currentItem.replace("_", " ")));
                }
            } else {
                auction.line(" {}", Text.key("scoreboard.skyblock.dark_auction.waiting"));
            }
        }

        long timeInYear = SkyBlockCalendar.getElapsed() % SkyBlockCalendar.YEAR;
        CalendarEvent activeSeasonalEvent = SEASONAL_SCOREBOARD_EVENTS.stream()
            .filter(candidate -> candidate.remainingTicksAt(timeInYear) >= 0)
            .findFirst().orElse(null);
        boolean seasonalEventActive = !electionRoom && !challenge && !darkAuction && activeSeasonalEvent != null;
        TextBody.Section seasonalEvent = body.section("seasonal_event").when(() -> seasonalEventActive);
        if (seasonalEventActive) {
            seasonalEvent.line(BLANK);
            seasonalEvent.line(Text.key("scoreboard.skyblock.event_title"));
            seasonalEvent.line(Text.key("scoreboard.skyblock.event_name_line",
                activeSeasonalEvent.getDisplayName(SkyBlockCalendar.getYear())));
            long remainingMillis = activeSeasonalEvent.remainingTicksAt(timeInYear) * 50L;
            seasonalEvent.line(Text.key("scoreboard.skyblock.event_ends_line",
                StringUtility.formatTimeLeft(remainingMillis)));
            if (activeSeasonalEvent == CalendarEvent.SPOOKY_FESTIVAL) {
                SpookyFestivalCandyTally candy = SpookyFestivalCandyTally.of(player);
                seasonalEvent.line(Text.key("scoreboard.skyblock.spooky_candy_label"));
                seasonalEvent.line(Text.key("scoreboard.skyblock.spooky_candy_line",
                    StringUtility.commaify(candy.green()),
                    StringUtility.commaify(candy.purple()),
                    StringUtility.commaify(candy.points())));
            }
        }

        FarmingContest contest = FarmingContest.active().orElse(null);
        FarmingContestProfile.Entry contestEntry = contest == null ? null
            : FarmingContests.profile(player).entryFor(contest).orElse(null);
        boolean contestShown = !electionRoom && !challenge && !darkAuction && !seasonalEventActive && contestEntry != null;
        TextBody.Section farmingContest = body.section("farming_contest").when(() -> contestShown);
        if (contestShown) {
            farmingContest.line(BLANK);
            farmingContest.line(Text.key("scoreboard.skyblock.contest_title",
                StringUtility.formatTimeLeft((contest.endElapsed() - SkyBlockCalendar.getElapsed()) * 50L)));
            farmingContest.line(Text.key("scoreboard.skyblock.contest_crop_line", "☘",
                contestEntry.getCrop().getDisplayName()));
            String collected = StringUtility.commaify(contestEntry.getCollected());
            ContestBracket bracket = contestEntry.qualifies()
                ? FarmingContests.liveBracket(player).filter(ContestBracket::earnsMedal).orElse(null) : null;
            if (bracket == null) {
                farmingContest.line(Text.key("scoreboard.skyblock.contest_collected_line", collected));
            } else {
                farmingContest.line(Text.key("scoreboard.skyblock.contest_bracket_line",
                    Text.of(bracket.formatted()), collected));
            }
        }

        boolean objectiveAllowed = !challenge && !electionRoom && !darkAuction && !seasonalEventActive && !contestShown;
        boolean missionObjective = objectiveAllowed && region != null
            && !missionData.getActiveMissions(region.getType()).isEmpty();
        QuestObjective questObjective = objectiveAllowed && !missionObjective ? QuestObjectives.current(player) : null;
        boolean objective = missionObjective || questObjective != null;
        TextBody.Section objectives = body.section("objective").when(() -> objective);
        if (missionObjective) {
            objectives.line(BLANK);
            MissionData.ActiveMission mission = missionData.getActiveMissions(region.getType()).getFirst();
            SkyBlockMission skyBlockMission = MissionData.getMissionClass(mission.getMissionID());

            if (skyBlockMission instanceof LocationAssociatedMission locationAssociatedMission) {
                objectives.line(Text.key("scoreboard.skyblock.objective_with_arrow", Text.parse(BlockUtility.getArrow(
                    player.getPosition(),
                    locationAssociatedMission.getLocation()
                ))));
            } else {
                objectives.line(Text.key("scoreboard.skyblock.objective_label"));
            }
            objectives.line("<e>{}", mission.toString());

            SkyBlockProgressMission progressMission = missionData.getAsProgressMission(mission.getMissionID());
            if (progressMission != null) {
                objectives.child("progress").line(Text.key("scoreboard.skyblock.objective_progress",
                    mission.getMissionProgress(),
                    progressMission.getMaxProgress()));
            }
        } else if (questObjective != null) {
            objectives.line(BLANK);
            if (questObjective.target() != null) {
                objectives.line(Text.key("scoreboard.skyblock.objective_with_arrow",
                    Text.parse(BlockUtility.getArrow(player.getPosition(), questObjective.target()))));
            } else {
                objectives.line(Text.key("scoreboard.skyblock.objective_label"));
            }
            objectives.line("<e>{}", questObjective.text());
        }

        body.section("footer").line(BLANK).line(Text.key("scoreboard.common.footer"));

        scoreboard.update(player, Text.of("  {}", getSidebarName(skyblockName, false, l)), body);
    }

    public static void removeCache(Player player) {
        scoreboard.removeScoreboard(player);
    }

    private static Text getSidebarName(int counter, boolean isGuest, Locale locale) {
        String plain = Text.key("scoreboard.skyblock.title_base").plain(locale);
        String endText = isGuest ? " GUEST" : "";

        if (counter > 0 && counter <= 8) {
            return Text.of("<f><l>{}<6>{}<e>{}<a>{}",
                    plain.substring(0, Math.min(counter - 1, plain.length())),
                    counter <= plain.length() ? String.valueOf(plain.charAt(counter - 1)) : "",
                    counter < plain.length() ? plain.substring(counter) : "",
                    endText);
        } else if ((counter >= 9 && counter <= 19) ||
                (counter >= 25 && counter <= 29)) {
            return Text.of("<f><l>{}<a>{}", plain, endText);
        } else {
            return Text.of("<e><l>{}<a>{}", plain, endText);
        }
    }
}
