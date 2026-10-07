package net.swofty.type.skyblockgeneric.carnival;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** A profile's Carnival Tokens, Doug's event perks, Chantelle's goals and game stats. */
@Getter
@Setter
public class CarnivalProfile {
    private int tokens;
    private long lastTicketDay = -1;
    private int year = -1;
    private final Map<CarnivalPerk, Integer> perkTiers = new EnumMap<>(CarnivalPerk.class);
    private final Set<String> metNpcs = new HashSet<>();
    private final Set<CarnivalGoal> completedGoals = EnumSet.noneOf(CarnivalGoal.class);
    private final Map<CarnivalGame, GameStats> stats = new EnumMap<>(CarnivalGame.class);

    public int tier(CarnivalPerk perk) {
        return perkTiers.getOrDefault(perk, 0);
    }

    public boolean hasMet(CarnivalNpc npc) {
        return metNpcs.contains(npc.name());
    }

    public void meet(CarnivalNpc npc) {
        metNpcs.add(npc.name());
    }

    public GameStats stats(CarnivalGame game) {
        return stats.computeIfAbsent(game, _ -> new GameStats());
    }

    /** Yearly scores and goals belong to one Carnival; a new Foxy term starts them over. */
    public void rollOver(int currentYear) {
        if (year == currentYear) return;
        year = currentYear;
        completedGoals.clear();
        stats.values().forEach(GameStats::resetYear);
    }

    @Getter
    @Setter
    public static class GameStats {
        private int lifetimeAttempts;
        private long lifetimeHighScore;
        private int yearAttempts;
        private long yearHighScore;

        public void record(long score) {
            lifetimeAttempts++;
            yearAttempts++;
            lifetimeHighScore = Math.max(lifetimeHighScore, score);
            yearHighScore = Math.max(yearHighScore, score);
        }

        void resetYear() {
            yearAttempts = 0;
            yearHighScore = 0;
        }

        JSONObject toJson() {
            return new JSONObject()
                    .put("lifetimeAttempts", lifetimeAttempts)
                    .put("lifetimeHigh", lifetimeHighScore)
                    .put("yearAttempts", yearAttempts)
                    .put("yearHigh", yearHighScore);
        }

        static GameStats fromJson(JSONObject json) {
            GameStats stats = new GameStats();
            stats.lifetimeAttempts = json.optInt("lifetimeAttempts", 0);
            stats.lifetimeHighScore = json.optLong("lifetimeHigh", 0);
            stats.yearAttempts = json.optInt("yearAttempts", 0);
            stats.yearHighScore = json.optLong("yearHigh", 0);
            return stats;
        }

        GameStats copy() {
            return fromJson(toJson());
        }
    }

    public String serialize() {
        JSONObject json = new JSONObject();
        json.put("tokens", tokens);
        json.put("lastTicketDay", lastTicketDay);
        json.put("year", year);
        JSONObject perks = new JSONObject();
        perkTiers.forEach((perk, tier) -> perks.put(perk.name(), tier));
        json.put("perks", perks);
        json.put("met", new JSONArray(metNpcs));
        json.put("goals", new JSONArray(completedGoals.stream().map(Enum::name).toList()));
        JSONObject games = new JSONObject();
        stats.forEach((game, gameStats) -> games.put(game.name(), gameStats.toJson()));
        json.put("stats", games);
        return json.toString();
    }

    public static CarnivalProfile deserialize(@Nullable String serialized) {
        CarnivalProfile profile = new CarnivalProfile();
        if (serialized == null || serialized.isEmpty() || serialized.equals("null")) return profile;
        JSONObject json = new JSONObject(serialized);
        profile.tokens = json.optInt("tokens", 0);
        profile.lastTicketDay = json.optLong("lastTicketDay", -1);
        profile.year = json.optInt("year", -1);
        JSONObject perks = json.optJSONObject("perks");
        if (perks != null) {
            for (String perk : perks.keySet()) {
                try {
                    profile.perkTiers.put(CarnivalPerk.valueOf(perk), perks.getInt(perk));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        JSONArray met = json.optJSONArray("met");
        if (met != null) met.forEach(npc -> profile.metNpcs.add(String.valueOf(npc)));
        JSONArray goals = json.optJSONArray("goals");
        if (goals != null) goals.forEach(goal -> profile.completedGoals.add(CarnivalGoal.valueOf(String.valueOf(goal))));
        JSONObject games = json.optJSONObject("stats");
        if (games != null) {
            for (String game : games.keySet()) {
                profile.stats.put(CarnivalGame.valueOf(game), GameStats.fromJson(games.getJSONObject(game)));
            }
        }
        return profile;
    }

    public CarnivalProfile copy() {
        CarnivalProfile copy = new CarnivalProfile();
        copy.tokens = tokens;
        copy.lastTicketDay = lastTicketDay;
        copy.year = year;
        copy.perkTiers.putAll(perkTiers);
        copy.metNpcs.addAll(metNpcs);
        copy.completedGoals.addAll(completedGoals);
        stats.forEach((game, gameStats) -> copy.stats.put(game, gameStats.copy()));
        return copy;
    }
}
