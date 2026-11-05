package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestRules;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A won Catacombs run's reward chests, rolled when the boss dies and kept at Croesus for 72 hours. One chest
 * can be bought per run; a Dungeon Chest Key buys a second, and a Kismet Feather rerolls one chest once.
 */
public record DungeonChestRun(String id, CatacombsFloor floor, CatacombsMode mode, int score, DungeonScoreRank rank,
                              long completedAt, List<String> party, List<DungeonRewardChest> chests, boolean keyUsed,
                              boolean rerolled) {
    public static final Duration LIFETIME = Duration.ofHours(DungeonChestRules.CROESUS_RETENTION_HOURS);

    public static DungeonChestRun create(CatacombsFloor floor, CatacombsMode mode, int score, DungeonScoreRank rank,
                                         long completedAt, List<String> party, List<DungeonRewardChest> chests) {
        return new DungeonChestRun(UUID.randomUUID().toString(), floor, mode, score, rank, completedAt, List.copyOf(party),
                List.copyOf(chests), false, false);
    }

    public @Nullable DungeonRewardChest chest(DungeonChestTier tier) {
        return chests.stream().filter(chest -> chest.tier() == tier).findFirst().orElse(null);
    }

    public int openedCount() {
        return (int) chests.stream().filter(DungeonRewardChest::opened).count();
    }

    public boolean isOpened() {
        return openedCount() > 0;
    }

    /** True when the next chest needs a Dungeon Chest Key: one is already bought and no key has been used. */
    public boolean nextNeedsKey() {
        return openedCount() == DungeonChestRules.BASE_CLAIMS_PER_RUN && !keyUsed;
    }

    public boolean canOpenMore() {
        return openedCount() < DungeonChestRules.claimsPerRun(true) && (openedCount() == 0 || !keyUsed);
    }

    public boolean canReroll(DungeonChestTier tier) {
        DungeonRewardChest chest = chest(tier);
        return !rerolled && chest != null && !chest.opened() && canOpenMore();
    }

    public DungeonChestRun open(DungeonChestTier tier, boolean withKey) {
        return new DungeonChestRun(id, floor, mode, score, rank, completedAt, party,
                replaced(tier, chest(tier).open()), keyUsed || withKey, rerolled);
    }

    public DungeonChestRun reroll(DungeonChestTier tier, DungeonRewardChest fresh) {
        return new DungeonChestRun(id, floor, mode, score, rank, completedAt, party,
                replaced(tier, chest(tier).rerolledTo(fresh)), keyUsed, true);
    }

    private List<DungeonRewardChest> replaced(DungeonChestTier tier, DungeonRewardChest chest) {
        List<DungeonRewardChest> updated = new ArrayList<>(chests);
        updated.replaceAll(each -> each.tier() == tier ? chest : each);
        return List.copyOf(updated);
    }

    public long expiresAt() {
        return completedAt + LIFETIME.toMillis();
    }

    public boolean isExpired(long now) {
        return now >= expiresAt();
    }

    public String title() {
        return (mode == CatacombsMode.MASTER ? "Master Mode Catacombs - " : "The Catacombs - ") + floor.displayName();
    }

    public JSONObject toJson() {
        JSONArray chestArray = new JSONArray();
        chests.forEach(chest -> chestArray.put(chest.toJson()));
        return new JSONObject()
                .put("id", id)
                .put("floor", floor.name())
                .put("mode", mode.name())
                .put("score", score)
                .put("rank", rank.name())
                .put("completedAt", completedAt)
                .put("party", new JSONArray(party))
                .put("chests", chestArray)
                .put("keyUsed", keyUsed)
                .put("rerolled", rerolled);
    }

    public static DungeonChestRun fromJson(JSONObject json) {
        List<DungeonRewardChest> chests = new ArrayList<>();
        JSONArray chestArray = json.optJSONArray("chests");
        if (chestArray != null) for (int i = 0; i < chestArray.length(); i++) chests.add(DungeonRewardChest.fromJson(chestArray.getJSONObject(i)));
        List<String> party = new ArrayList<>();
        JSONArray partyArray = json.optJSONArray("party");
        if (partyArray != null) for (int i = 0; i < partyArray.length(); i++) party.add(partyArray.getString(i));
        return new DungeonChestRun(json.getString("id"), CatacombsFloor.valueOf(json.getString("floor")),
                CatacombsMode.valueOf(json.optString("mode", CatacombsMode.NORMAL.name())), json.optInt("score", 0),
                DungeonScoreRank.valueOf(json.optString("rank", DungeonScoreRank.D.name())), json.getLong("completedAt"),
                List.copyOf(party), List.copyOf(chests), json.optBoolean("keyUsed", false), json.optBoolean("rerolled", false));
    }
}
