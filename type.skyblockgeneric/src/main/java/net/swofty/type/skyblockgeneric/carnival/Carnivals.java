package net.swofty.type.skyblockgeneric.carnival;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCarnival;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Reads and changes a player's {@link CarnivalProfile}: tickets, tokens, perks and goals. */
public final class Carnivals {
    private static final ZoneId DAILY_RESET_ZONE = ZoneId.of("America/New_York");

    private Carnivals() {}

    public static CarnivalProfile profile(SkyBlockPlayer player) {
        CarnivalProfile profile = datapoint(player).getValue();
        if (Carnival.isOpen()) profile.rollOver(Carnival.year());
        return profile;
    }

    public static void save(SkyBlockPlayer player, CarnivalProfile profile) {
        datapoint(player).setValue(profile);
    }

    private static DatapointCarnival datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CARNIVAL, DatapointCarnival.class);
    }

    public static double perkValue(SkyBlockPlayer player, CarnivalPerk perk) {
        if (player.getSkyblockDataHandler() == null) return 0;
        return perk.valueAt(datapoint(player).getValue().tier(perk));
    }

    /** Scales a drop count by a perk's percentage, rounding the fraction up at random. */
    public static int boostedCount(SkyBlockPlayer player, CarnivalPerk perk, int baseCount) {
        double scaled = baseCount * (1 + perkValue(player, perk) / 100D);
        int whole = (int) Math.floor(scaled);
        return ThreadLocalRandom.current().nextDouble() < scaled - whole ? whole + 1 : whole;
    }

    public static double multiplier(SkyBlockPlayer player, CarnivalPerk perk) {
        return 1 + perkValue(player, perk) / 100D;
    }

    public static boolean canClaimDailyTickets(CarnivalProfile profile) {
        return profile.getLastTicketDay() != today();
    }

    public static void claimDailyTickets(SkyBlockPlayer player) {
        CarnivalProfile profile = profile(player);
        if (!canClaimDailyTickets(profile)) return;
        profile.setLastTicketDay(today());
        save(player, profile);
        player.addAndUpdateItem(new SkyBlockItem(ItemType.CARNIVAL_TICKET, Carnival.DAILY_TICKETS));
    }

    private static long today() {
        return LocalDate.now(DAILY_RESET_ZONE).toEpochDay();
    }

    public static boolean hasTicketsToPlay(SkyBlockPlayer player) {
        return player.getAmountInInventory(ItemType.CARNIVAL_TICKET) >= Carnival.TICKETS_PER_GAME;
    }

    public static void payForGame(SkyBlockPlayer player) {
        player.takeItem(ItemType.CARNIVAL_TICKET, Carnival.TICKETS_PER_GAME);
    }

    public enum PurchaseResult { BOUGHT, MAXED, UNAVAILABLE, TOO_POOR }

    public static PurchaseResult buyPerk(SkyBlockPlayer player, CarnivalPerk perk) {
        if (!perk.isAvailable()) return PurchaseResult.UNAVAILABLE;
        CarnivalProfile profile = profile(player);
        int next = profile.tier(perk) + 1;
        if (next > perk.maxTier()) return PurchaseResult.MAXED;
        int cost = perk.costOf(next);
        if (profile.getTokens() < cost) return PurchaseResult.TOO_POOR;
        profile.setTokens(profile.getTokens() - cost);
        profile.getPerkTiers().put(perk, next);
        save(player, profile);
        player.getSkyBlockExperience().addSourceXP(perk.getEvent().getXpSource(), perk.xpOf(next));
        return PurchaseResult.BOUGHT;
    }

    public static boolean spendTokens(SkyBlockPlayer player, int amount) {
        CarnivalProfile profile = profile(player);
        if (profile.getTokens() < amount) return false;
        profile.setTokens(profile.getTokens() - amount);
        save(player, profile);
        return true;
    }

    /** Records a finished game, pays its tokens and any goals it completes; returns the tokens earned. */
    public static int finishGame(SkyBlockPlayer player, CarnivalGameResult result) {
        CarnivalProfile profile = profile(player);
        CarnivalProfile.GameStats stats = profile.stats(result.game());
        stats.record(result.score());
        int earned = Carnival.tokensFor(result.score());
        profile.setTokens(profile.getTokens() + earned);

        List<CarnivalGoal> completed = new ArrayList<>();
        for (CarnivalGoal goal : CarnivalGoal.values()) {
            if (profile.getCompletedGoals().contains(goal)) continue;
            if (goal.isMetBy(result, stats.getYearAttempts())) completed.add(goal);
        }
        for (CarnivalGoal goal : completed) {
            profile.getCompletedGoals().add(goal);
            profile.setTokens(profile.getTokens() + goal.getTokens());
            player.addAndUpdateItem(new SkyBlockItem(ItemType.CARNIVAL_TICKET, CarnivalGoal.TICKETS_PER_GOAL));
        }
        save(player, profile);
        if (!completed.isEmpty()) announceGoals(player, completed);
        return earned;
    }

    private static void announceGoals(SkyBlockPlayer player, List<CarnivalGoal> goals) {
        StringBuilder message = new StringBuilder("<a><l>CARNIVAL GOALS COMPLETE");
        int tokens = 0;
        for (CarnivalGoal goal : goals) {
            message.append("\n<8>- <7>").append(goal.plainDescription());
            tokens += goal.getTokens();
        }
        message.append("\n\n<7>Rewards:")
                .append("\n   <8>+<e>").append(StringUtility.commaify(tokens)).append(" Carnival Tokens")
                .append("\n   <a>Carnival Ticket <8>x").append(goals.size() * CarnivalGoal.TICKETS_PER_GOAL);
        for (String line : message.toString().split("\n", -1)) player.sendMessage(line);
    }
}
