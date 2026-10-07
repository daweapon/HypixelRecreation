package net.swofty.type.skyblockgeneric.farmingcontest;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Everything a profile has earned from Jacob's Farming Contests and spent at Anita. */
@Getter
@Setter
public class FarmingContestProfile {
    public static final int KEPT_ENTRIES = 112;

    private boolean metJacob;
    private int bronzeMedals;
    private int silverMedals;
    private int goldMedals;
    private int contestsParticipated;
    private int extraFortuneTier;
    private int levelCapTier;
    private boolean personalBestsUnlocked;
    private final Map<ContestCrop, ContestBracket> uniqueBrackets = new EnumMap<>(ContestCrop.class);
    private final Map<ContestCrop, Long> personalBests = new EnumMap<>(ContestCrop.class);
    private final List<Entry> entries = new ArrayList<>();

    public Optional<Entry> entryFor(FarmingContest contest) {
        return entries.stream().filter(entry -> entry.contestId.equals(contest.id())).findFirst();
    }

    public Entry participate(FarmingContest contest, ContestCrop crop, boolean goated) {
        Entry entry = new Entry(contest.id(), crop, 0, goated, null, false, false, false);
        entries.add(entry);
        return entry;
    }

    public List<Entry> claimable() {
        return entries.stream().filter(Entry::isClaimable).toList();
    }

    public List<Entry> finishedNewestFirst() {
        return entries.stream().filter(Entry::isFinalised)
                .sorted((a, b) -> Long.compare(b.contest().startElapsed(), a.contest().startElapsed())).toList();
    }

    public void trimHistory() {
        List<Entry> claimed = new ArrayList<>(entries.stream().filter(Entry::isClaimed).toList());
        claimed.sort((a, b) -> Long.compare(a.contest().startElapsed(), b.contest().startElapsed()));
        int excess = entries.size() - KEPT_ENTRIES;
        for (int i = 0; i < excess && i < claimed.size(); i++) entries.remove(claimed.get(i));
    }

    public long personalBest(ContestCrop crop) {
        return personalBests.getOrDefault(crop, 0L);
    }

    public ContestBracket uniqueBracket(ContestCrop crop) {
        return uniqueBrackets.get(crop);
    }

    public int cropsWithAtLeast(ContestBracket bracket) {
        return (int) uniqueBrackets.values().stream().filter(reached -> reached.atLeast(bracket)).count();
    }

    public int medals(ContestBracket medal) {
        return switch (medal) {
            case BRONZE -> bronzeMedals;
            case SILVER -> silverMedals;
            case GOLD -> goldMedals;
            default -> 0;
        };
    }

    public void addMedals(ContestBracket medal, int amount) {
        switch (medal) {
            case BRONZE -> bronzeMedals += amount;
            case SILVER -> silverMedals += amount;
            case GOLD -> goldMedals += amount;
            default -> throw new IllegalArgumentException(medal + " is not a medal");
        }
    }

    @Getter
    @Setter
    public static class Entry {
        private final String contestId;
        private final ContestCrop crop;
        private long collected;
        private final boolean goated;
        private @Nullable ContestBracket bracket;
        private boolean finalised;
        private boolean claimed;
        private boolean personalBestAnnounced;

        public Entry(String contestId, ContestCrop crop, long collected, boolean goated,
                     @Nullable ContestBracket bracket, boolean finalised, boolean claimed,
                     boolean personalBestAnnounced) {
            this.contestId = contestId;
            this.crop = crop;
            this.collected = collected;
            this.goated = goated;
            this.bracket = bracket;
            this.finalised = finalised;
            this.claimed = claimed;
            this.personalBestAnnounced = personalBestAnnounced;
        }

        public FarmingContest contest() {
            return FarmingContest.fromId(contestId).orElseThrow();
        }

        public boolean qualifies() {
            return collected >= ContestBracket.MINIMUM_COLLECTED;
        }

        public boolean isClaimable() {
            return finalised && !claimed && bracket != null;
        }

        JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("contest", contestId);
            json.put("crop", crop.name());
            json.put("collected", collected);
            json.put("goated", goated);
            if (bracket != null) json.put("bracket", bracket.name());
            json.put("finalised", finalised);
            json.put("claimed", claimed);
            json.put("pbAnnounced", personalBestAnnounced);
            return json;
        }

        static Entry fromJson(JSONObject json) {
            String bracket = json.optString("bracket", null);
            return new Entry(
                    json.getString("contest"),
                    ContestCrop.valueOf(json.getString("crop")),
                    json.optLong("collected", 0),
                    json.optBoolean("goated", false),
                    bracket == null ? null : ContestBracket.valueOf(bracket),
                    json.optBoolean("finalised", false),
                    json.optBoolean("claimed", false),
                    json.optBoolean("pbAnnounced", false));
        }

        Entry copy() {
            return new Entry(contestId, crop, collected, goated, bracket, finalised, claimed, personalBestAnnounced);
        }
    }

    public String serialize() {
        JSONObject json = new JSONObject();
        json.put("metJacob", metJacob);
        json.put("bronze", bronzeMedals);
        json.put("silver", silverMedals);
        json.put("gold", goldMedals);
        json.put("participated", contestsParticipated);
        json.put("extraFortuneTier", extraFortuneTier);
        json.put("levelCapTier", levelCapTier);
        json.put("personalBestsUnlocked", personalBestsUnlocked);
        JSONObject unique = new JSONObject();
        uniqueBrackets.forEach((crop, bracket) -> unique.put(crop.name(), bracket.name()));
        json.put("uniqueBrackets", unique);
        JSONObject bests = new JSONObject();
        personalBests.forEach((crop, best) -> bests.put(crop.name(), best));
        json.put("personalBests", bests);
        JSONArray history = new JSONArray();
        entries.forEach(entry -> history.put(entry.toJson()));
        json.put("entries", history);
        return json.toString();
    }

    public static FarmingContestProfile deserialize(@Nullable String serialized) {
        FarmingContestProfile profile = new FarmingContestProfile();
        if (serialized == null || serialized.isEmpty() || serialized.equals("null")) return profile;
        JSONObject json = new JSONObject(serialized);
        profile.metJacob = json.optBoolean("metJacob", false);
        profile.bronzeMedals = json.optInt("bronze", 0);
        profile.silverMedals = json.optInt("silver", 0);
        profile.goldMedals = json.optInt("gold", 0);
        profile.contestsParticipated = json.optInt("participated", 0);
        profile.extraFortuneTier = json.optInt("extraFortuneTier", 0);
        profile.levelCapTier = json.optInt("levelCapTier", 0);
        profile.personalBestsUnlocked = json.optBoolean("personalBestsUnlocked", false);
        JSONObject unique = json.optJSONObject("uniqueBrackets");
        if (unique != null) {
            for (String crop : unique.keySet()) {
                profile.uniqueBrackets.put(ContestCrop.valueOf(crop), ContestBracket.valueOf(unique.getString(crop)));
            }
        }
        JSONObject bests = json.optJSONObject("personalBests");
        if (bests != null) {
            for (String crop : bests.keySet()) profile.personalBests.put(ContestCrop.valueOf(crop), bests.getLong(crop));
        }
        JSONArray history = json.optJSONArray("entries");
        if (history != null) {
            for (int i = 0; i < history.length(); i++) profile.entries.add(Entry.fromJson(history.getJSONObject(i)));
        }
        return profile;
    }

    public FarmingContestProfile copy() {
        FarmingContestProfile copy = new FarmingContestProfile();
        copy.metJacob = metJacob;
        copy.bronzeMedals = bronzeMedals;
        copy.silverMedals = silverMedals;
        copy.goldMedals = goldMedals;
        copy.contestsParticipated = contestsParticipated;
        copy.extraFortuneTier = extraFortuneTier;
        copy.levelCapTier = levelCapTier;
        copy.personalBestsUnlocked = personalBestsUnlocked;
        copy.uniqueBrackets.putAll(uniqueBrackets);
        copy.personalBests.putAll(personalBests);
        entries.forEach(entry -> copy.entries.add(entry.copy()));
        return copy;
    }
}
