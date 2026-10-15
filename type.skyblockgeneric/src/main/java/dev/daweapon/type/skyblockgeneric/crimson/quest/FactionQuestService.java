package dev.daweapon.type.skyblockgeneric.crimson.quest;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.CrimsonTabWidgets;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoGrade;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMinibossMob;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * The daily Town Board: five quests per faction per UTC day, kept in {@link DatapointCrimsonIsle}'s
 * {@code faction_quests} section. Quests start on their own, progress from game events, and are claimed at
 * the Town Board. Also pays the daily reputation for each Kuudra tier cleared.
 */
public final class FactionQuestService {
    public static final String SECTION = "faction_quests";
    public static final int SLOTS = 5;
    private static final String KUUDRA_DAILY = "kuudra:daily:";

    private FactionQuestService() {
    }

    public static void registerTabWidget() {
        CrimsonTabWidgets.provide(CrimsonTabWidgets.Widget.FACTION_QUESTS, FactionQuestService::tabLines);
    }

    public static long today() {
        return LocalDate.now(ZoneOffset.UTC).toEpochDay();
    }

    public static Duration untilReset() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC));
    }

    /** The current faction's board for today, rolling a fresh one at the daily reset. Null without a faction. */
    public static @Nullable FactionQuestBoard board(SkyBlockPlayer player) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        return faction == null ? null : board(player, faction);
    }

    public static FactionQuestBoard board(SkyBlockPlayer player, CrimsonFaction faction) {
        FactionQuestBoard stored = stored(player, faction);
        if (stored != null && stored.getDay() == today()) {
            if (assignMissingRewards(player, stored, new Random())) save(player, faction, stored);
            return stored;
        }
        FactionQuestBoard fresh = roll(player, faction);
        save(player, faction, fresh);
        return fresh;
    }

    public static FactionQuestBoard roll(SkyBlockPlayer player, CrimsonFaction faction) {
        long day = today();
        Random random = new Random(player.getUuid().getMostSignificantBits() ^ (day * 31 + faction.ordinal()));
        List<FactionQuest> quests = FactionQuestGenerator.roll(faction, CrimsonReputationService.tier(player, faction),
                quest -> isAvailable(player, quest), random);
        List<FactionQuestBoard.Slot> slots = new ArrayList<>();
        quests.forEach(quest -> slots.add(new FactionQuestBoard.Slot(quest.id(), 0, false, false)));
        FactionQuestBoard board = new FactionQuestBoard(day, slots);
        assignMissingRewards(player, board, random);
        return board;
    }

    private static boolean assignMissingRewards(SkyBlockPlayer player, FactionQuestBoard board, Random random) {
        boolean changed = false;
        for (FactionQuestBoard.Slot slot : board.getSlots()) {
            if (slot.getReward() != null) continue;
            slot.setReward(FactionQuestRewards.roll(player.getUuid(), slot.quest(), random));
            changed = true;
        }
        return changed;
    }

    /** Whether a quest may be put on a board: Kuudra needs the fight and the tier, Dojo the test unlocked. */
    public static boolean isAvailable(SkyBlockPlayer player, FactionQuest quest) {
        return switch (quest.type()) {
            case KUUDRA -> KuudraAvailability.isFightAvailable() && KuudraProgress.meetsRequirements(player, quest.kuudraTier());
            case DOJO -> DojoProgress.isUnlocked(player, quest.dojoTest());
            default -> true;
        };
    }

    public static void reroll(SkyBlockPlayer player, CrimsonFaction faction) {
        save(player, faction, roll(player, faction));
    }

    /** Staff testing: marks one slot complete, ready to claim, without doing the task. */
    public static boolean forceComplete(SkyBlockPlayer player, int index) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) return false;
        FactionQuestBoard board = board(player, faction);
        FactionQuestBoard.Slot slot = board.slot(index);
        if (slot == null || slot.isCompleted()) return false;
        slot.setProgress(slot.quest().amount());
        slot.setCompleted(true);
        save(player, faction, board);
        player.sendMessage(completionMessage(slot.quest()));
        return true;
    }

    public static void onTrophyFish(SkyBlockPlayer player, String fishId) {
        advance(player, quest -> quest.type() == FactionQuestType.FISHING && quest.target().equals(fishId), 1);
    }

    public static void onMiniboss(SkyBlockPlayer player, CrimsonMiniboss miniboss) {
        advance(player, quest -> quest.type() == FactionQuestType.MINIBOSS && quest.target().equals(miniboss.name()), 1);
    }

    public static void onDojo(SkyBlockPlayer player, DojoTest test, DojoGrade grade) {
        advance(player, quest -> quest.type() == FactionQuestType.DOJO && quest.dojoTest() == test
                && grade.ordinal() >= quest.dojoGrade().ordinal(), 1);
    }

    /** Called when a Kuudra run is won: a higher tier also completes a lower-tier Kuudra quest (0.20.9). */
    public static void onKuudraCleared(SkyBlockPlayer player, KuudraTier tier) {
        advance(player, quest -> quest.type() == FactionQuestType.KUUDRA && tier.ordinal() >= quest.kuudraTier().ordinal(), 1);
        awardDailyKuudraReputation(player, tier);
    }

    public static boolean awardDailyKuudraReputation(SkyBlockPlayer player, KuudraTier tier) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) return false;
        String key = KUUDRA_DAILY + tier.name();
        long marker = today() + 1;
        if (DatapointCrimsonIsle.of(player).getNumber(key) == marker) return false;
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(key, marker));
        CrimsonReputationService.add(player, faction, dailyKuudraReputation(tier), tier.getDisplayName() + " Kuudra");
        return true;
    }

    public static int dailyKuudraReputation(KuudraTier tier) {
        return (tier.ordinal() + 1) * 10;
    }

    public static boolean hasDailyKuudra(SkyBlockPlayer player, KuudraTier tier) {
        return DatapointCrimsonIsle.of(player).getNumber(KUUDRA_DAILY + tier.name()) == today() + 1;
    }

    public static boolean hasDailyMiniboss(SkyBlockPlayer player, CrimsonMiniboss miniboss) {
        return DatapointCrimsonIsle.of(player).getNumber(CrimsonMinibossMob.DAILY_KEY + miniboss.name()) == today() + 1;
    }

    /** The current faction's unfinished Rescue Mission slot, if the board has one. */
    public static @Nullable FactionQuestBoard.Slot activeRescue(SkyBlockPlayer player) {
        FactionQuestBoard board = board(player);
        if (board == null) return null;
        return board.getSlots().stream()
                .filter(slot -> slot.quest().type() == FactionQuestType.RESCUE && !slot.isCompleted())
                .findFirst().orElse(null);
    }

    public static int rescueStage(SkyBlockPlayer player) {
        FactionQuestBoard.Slot slot = activeRescue(player);
        return slot == null ? -1 : slot.getProgress();
    }

    public static void setRescueStage(SkyBlockPlayer player, int stage) {
        editActive(player, slot -> slot.quest().type() == FactionQuestType.RESCUE && !slot.isCompleted(),
                slot -> slot.setProgress(stage));
    }

    public static void completeRescue(SkyBlockPlayer player) {
        advance(player, quest -> quest.type() == FactionQuestType.RESCUE, 1);
    }

    public static int fetchAvailable(SkyBlockPlayer player, FactionQuest quest) {
        ItemType type = ItemType.get(quest.target());
        if (type == null) return 0;
        return player.getAmountInInventory(type) + player.getSackItems().getAmount(type);
    }

    public static boolean isClaimable(SkyBlockPlayer player, FactionQuestBoard.Slot slot) {
        if (slot.isClaimed()) return false;
        if (slot.isCompleted()) return true;
        FactionQuest quest = slot.quest();
        return quest.type() == FactionQuestType.FETCH && fetchAvailable(player, quest) >= quest.amount();
    }

    public enum ClaimResult { CLAIMED, NOT_COMPLETE, MISSING_ITEMS, ALREADY_CLAIMED, NO_FACTION }

    /** Claims a finished slot; a Fetch quest first takes its items from the inventory and sacks. */
    public static ClaimResult claim(SkyBlockPlayer player, int index) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) return ClaimResult.NO_FACTION;
        FactionQuestBoard board = board(player, faction);
        FactionQuestBoard.Slot slot = board.slot(index);
        if (slot == null) return ClaimResult.NOT_COMPLETE;
        if (slot.isClaimed()) return ClaimResult.ALREADY_CLAIMED;
        FactionQuest quest = slot.quest();

        if (quest.type() == FactionQuestType.FETCH && !slot.isCompleted()) {
            ItemType type = ItemType.get(quest.target());
            if (type == null || !player.removeItemFromPlayer(type, quest.amount())) return ClaimResult.MISSING_ITEMS;
            slot.setProgress(quest.amount());
            slot.setCompleted(true);
        }
        if (!slot.isCompleted()) return ClaimResult.NOT_COMPLETE;

        slot.setClaimed(true);
        save(player, faction, board);
        FactionQuestRewards.grant(player, faction, quest, slot.getReward());
        return ClaimResult.CLAIMED;
    }

    public static List<String> tabLines(SkyBlockPlayer player) {
        List<String> lines = new ArrayList<>();
        lines.add("<e><l>Faction Quests:");
        FactionQuestBoard board = board(player);
        if (board == null) {
            lines.add(" <7>Join a faction through <d>Elle<7>.");
            return lines;
        }
        for (FactionQuestBoard.Slot slot : board.getSlots()) {
            lines.add(" " + slot.statusSymbol() + " " + slot.quest().tier().getColour() + slot.quest().tabName());
        }
        return lines;
    }

    public static String completionMessage(FactionQuest quest) {
        return "<a>You completed your " + quest.type().getChatWord() + " quest! Visit the Town Board to claim the rewards.";
    }

    public static String progressText(FactionQuestBoard.Slot slot) {
        FactionQuest quest = slot.quest();
        return switch (quest.type()) {
            case FISHING, MINIBOSS -> "<7>Progress: <e>" + Math.min(slot.getProgress(), quest.amount()) + "<6>/<e>" + quest.amount();
            case FETCH -> "";
            case RESCUE -> switch (slot.isCompleted() ? -1 : slot.getProgress()) {
                case RescueStage.ASSIGNED -> "<7>Talk to the <e>Rescue Recruiter<7>.";
                case RescueStage.RECRUITED -> "<7>Talk to the <e>Undercover Agent<7>.";
                case RescueStage.INFILTRATING -> "<7>Find the hostage without being seen!";
                default -> "";
            };
            case DOJO, KUUDRA -> "";
        };
    }

    private static void advance(SkyBlockPlayer player, Predicate<FactionQuest> matcher, int amount) {
        editActive(player, slot -> !slot.isCompleted() && matcher.test(slot.quest()), slot -> {
            FactionQuest quest = slot.quest();
            int target = quest.type() == FactionQuestType.RESCUE ? Integer.MAX_VALUE : quest.amount();
            slot.setProgress(Math.min(target, slot.getProgress() + amount));
            if (quest.type() == FactionQuestType.RESCUE || slot.getProgress() >= quest.amount()) {
                slot.setCompleted(true);
                player.sendMessage(completionMessage(quest));
            }
        });
    }

    private static void editActive(SkyBlockPlayer player, Predicate<FactionQuestBoard.Slot> filter,
                                   java.util.function.Consumer<FactionQuestBoard.Slot> change) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction == null) return;
        FactionQuestBoard board = board(player, faction);
        FactionQuestBoard.Slot slot = board.getSlots().stream().filter(filter).findFirst().orElse(null);
        if (slot == null) return;
        change.accept(slot);
        save(player, faction, board);
    }

    private static @Nullable FactionQuestBoard stored(SkyBlockPlayer player, CrimsonFaction faction) {
        String section = DatapointCrimsonIsle.of(player).getSection(SECTION);
        if (section == null || section.isBlank()) return null;
        JSONObject root = new JSONObject(section);
        return root.has(faction.name()) ? FactionQuestBoard.fromJson(root.getJSONObject(faction.name())) : null;
    }

    private static void save(SkyBlockPlayer player, CrimsonFaction faction, FactionQuestBoard board) {
        DatapointCrimsonIsle.edit(player, data -> {
            String section = data.getSection(SECTION);
            JSONObject root = section == null || section.isBlank() ? new JSONObject() : new JSONObject(section);
            root.put(faction.name(), board.toJson());
            data.getSections().put(SECTION, root.toString());
        });
    }

    public static String describeReset() {
        Duration left = untilReset();
        return StringUtility.commaify(left.toHours()) + "h " + left.toMinutesPart() + "m";
    }
}
