package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

// Persists a player's Commissions progress: active commission slots, daily completion count/bonus,
// and which milestone reward tiers they've claimed.
public class DatapointCommissions extends SkyBlockDatapoint<DatapointCommissions.PlayerCommissionData> {
    private static final Serializer<PlayerCommissionData> serializer = new Serializer<>() {
        @Override
        public String serialize(PlayerCommissionData value) {
            JSONObject json = new JSONObject();
            json.put("totalCompleted", value.totalCompleted);
            json.put("dailyCompletedCount", value.dailyCompletedCount);
            json.put("lastDailyReset", value.lastDailyReset);

            JSONArray commissionsArray = writeCommissions(value.activeCommissions);
            json.put("activeCommissions", commissionsArray);
            json.put("hollowsCommissions", writeCommissions(value.hollowsCommissions));

            JSONArray claimedArray = new JSONArray();
            for (Integer tier : value.claimedMilestones) {
                claimedArray.put(tier);
            }
            json.put("claimedMilestones", claimedArray);

            return json.toString();
        }

        @Override
        public PlayerCommissionData deserialize(String json) {
            if (json == null || json.isEmpty()) {
                return PlayerCommissionData.createDefault();
            }

            try {
                JSONObject obj = new JSONObject(json);
                PlayerCommissionData data = new PlayerCommissionData();
                data.totalCompleted = obj.optInt("totalCompleted", 0);
                data.dailyCompletedCount = obj.optInt("dailyCompletedCount", 0);
                data.lastDailyReset = obj.optLong("lastDailyReset", 0L);
                data.activeCommissions = readCommissions(obj.optJSONArray("activeCommissions"));
                data.hollowsCommissions = readCommissions(obj.optJSONArray("hollowsCommissions"));
                data.claimedMilestones = new ArrayList<>();

                JSONArray claimedArray = obj.optJSONArray("claimedMilestones");
                if (claimedArray != null) {
                    for (int i = 0; i < claimedArray.length(); i++) {
                        data.claimedMilestones.add(claimedArray.getInt(i));
                    }
                }

                return data;
            } catch (Exception e) {
                return PlayerCommissionData.createDefault();
            }
        }

        @Override
        public PlayerCommissionData clone(PlayerCommissionData value) {
            PlayerCommissionData cloned = new PlayerCommissionData();
            cloned.totalCompleted = value.totalCompleted;
            cloned.dailyCompletedCount = value.dailyCompletedCount;
            cloned.lastDailyReset = value.lastDailyReset;
            cloned.activeCommissions = cloneCommissions(value.activeCommissions);
            cloned.hollowsCommissions = cloneCommissions(value.hollowsCommissions);
            cloned.claimedMilestones = new ArrayList<>(value.claimedMilestones);
            return cloned;
        }
    };

    private static JSONArray writeCommissions(List<ActiveCommission> commissions) {
        JSONArray array = new JSONArray();
        for (ActiveCommission commission : commissions) {
            JSONObject commObj = new JSONObject();
            commObj.put("commissionName", commission.commissionName);
            commObj.put("progress", commission.progress);
            commObj.put("completed", commission.completed);
            commObj.put("claimed", commission.claimed);
            array.put(commObj);
        }
        return array;
    }

    private static List<ActiveCommission> readCommissions(JSONArray array) {
        List<ActiveCommission> commissions = new ArrayList<>();
        if (array == null) return commissions;
        for (int i = 0; i < array.length(); i++) {
            JSONObject commObj = array.getJSONObject(i);
            commissions.add(new ActiveCommission(
                    commObj.getString("commissionName"),
                    commObj.getInt("progress"),
                    commObj.getBoolean("completed"),
                    commObj.optBoolean("claimed", false)
            ));
        }
        return commissions;
    }

    private static List<ActiveCommission> cloneCommissions(List<ActiveCommission> source) {
        List<ActiveCommission> copy = new ArrayList<>();
        for (ActiveCommission commission : source) {
            copy.add(new ActiveCommission(commission.commissionName, commission.progress,
                    commission.completed, commission.claimed));
        }
        return copy;
    }

    public DatapointCommissions(String key, PlayerCommissionData value) {
        super(key, value, serializer);
    }

    public DatapointCommissions(String key) {
        super(key, PlayerCommissionData.createDefault(), serializer);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveCommission {
        private String commissionName;
        private int progress;
        private boolean completed;
        private boolean claimed;

        public ActiveCommission(String commissionName) {
            this.commissionName = commissionName;
            this.progress = 0;
            this.completed = false;
            this.claimed = false;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PlayerCommissionData {
        private int totalCompleted;
        private int dailyCompletedCount;
        private long lastDailyReset;
        private List<ActiveCommission> activeCommissions = new ArrayList<>();
        private List<ActiveCommission> hollowsCommissions = new ArrayList<>();
        private List<Integer> claimedMilestones = new ArrayList<>(); // tracks which milestone tiers have been claimed

        public static PlayerCommissionData createDefault() {
            PlayerCommissionData data = new PlayerCommissionData();
            data.totalCompleted = 0;
            data.dailyCompletedCount = 0;
            data.lastDailyReset = 0L;
            data.activeCommissions = new ArrayList<>();
            data.hollowsCommissions = new ArrayList<>();
            data.claimedMilestones = new ArrayList<>();
            return data;
        }

        public boolean checkAndResetDaily() {
            long todayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000;
            if (lastDailyReset < todayStart) {
                dailyCompletedCount = 0;
                lastDailyReset = System.currentTimeMillis();
                return true;
            }
            return false;
        }

        public int getRemainingDailyBonus() {
            checkAndResetDaily();
            return Math.max(0, 4 - dailyCompletedCount);
        }

        public boolean hasDailyBonus() {
            checkAndResetDaily();
            return dailyCompletedCount < 4;
        }

        public void completeCommission() {
            checkAndResetDaily();
            totalCompleted++;
            dailyCompletedCount++;
        }

        public int getMilestoneTier() {
            if (totalCompleted >= 750) return 6;
            if (totalCompleted >= 500) return 5;
            if (totalCompleted >= 250) return 4;
            if (totalCompleted >= 100) return 3;
            if (totalCompleted >= 25) return 2;
            if (totalCompleted >= 5) return 1;
            return 0;
        }

        public int getCommissionSlots() {
            int slots = 2; // base slots
            if (getMilestoneTier() >= 3) {
                slots++; // +1 slot at tier 3
            }
            return slots;
        }

        public boolean isMilestoneClaimed(int tier) {
            return claimedMilestones.contains(tier);
        }

        public boolean claimMilestone(int tier) {
            if (isMilestoneClaimed(tier)) return false;
            if (getMilestoneTier() < tier) return false;
            claimedMilestones.add(tier);
            return true;
        }

        public int getNextClaimableMilestone() {
            int currentTier = getMilestoneTier();
            for (int i = 1; i <= currentTier; i++) {
                if (!isMilestoneClaimed(i)) {
                    return i;
                }
            }
            return -1;
        }

        public double getMilestoneProgress(int tier) {
            int required = getMilestoneRequirement(tier);
            if (required == 0) return 100.0;
            return Math.min(100.0, (totalCompleted * 100.0) / required);
        }

        public int getMilestoneRequirement(int tier) {
            return switch (tier) {
                case 1 -> 5;
                case 2 -> 25;
                case 3 -> 100;
                case 4 -> 250;
                case 5 -> 500;
                case 6 -> 750;
                default -> 0;
            };
        }
    }
}
