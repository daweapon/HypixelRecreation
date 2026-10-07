package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.event.custom.SkyBlockXPModificationEvent;
import net.swofty.type.skyblockgeneric.levels.SkyBlockEmblems;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.*;

// Persists a player's completed SkyBlock XP causes, per-source XP progress, any admin-granted
// bonus XP and their current emblem, and computes total XP, level, and progress to the next level.
public class DatapointSkyBlockExperience extends SkyBlockDatapoint<DatapointSkyBlockExperience.PlayerSkyBlockExperience> {
    private static final Serializer<PlayerSkyBlockExperience> serializer = new Serializer<>() {
        @Override
        public String serialize(PlayerSkyBlockExperience value) {
            List<String> serialized = new ArrayList<>();

            value.getCompletedExperienceCauses().forEach((cause) -> {
                serialized.add(SkyBlockLevelCause.getKey(cause));
            });

            Map<String, Object> json = new HashMap<>();
            json.put("values", serialized);

            if (value.getCurrentEmblem() != null) {
                json.put("emblem", value.getCurrentEmblem().getKey().name() + ":" + value.getCurrentEmblem().getValue());
            }
            if (!value.getSourceProgress().isEmpty()) {
                json.put("sources", new JSONObject(new HashMap<String, Object>(value.getSourceProgress())));
            }
            if (value.getBonusXP() != 0) {
                json.put("bonus", value.getBonusXP());
            }

            return new JSONObject(json).toString();
        }

        @Override
        public PlayerSkyBlockExperience deserialize(String json) {
            List<SkyBlockLevelCauseAbstr> experience = new ArrayList<>();

            JSONObject jsonObject = new JSONObject(json);
            jsonObject.getJSONArray("values").forEach((value) -> {
                if (value instanceof String) {
                    SkyBlockLevelCauseAbstr cause = SkyBlockLevelCause.getCause((String) value);
                    // A cause key can disappear between versions (an item or mission that no
                    // longer exists). Skipping it keeps the rest of the profile loadable.
                    if (cause != null) experience.add(cause);
                }
            });

            Map<String, Double> sources = new LinkedHashMap<>();
            if (jsonObject.has("sources") && !jsonObject.isNull("sources")) {
                JSONObject sourcesJson = jsonObject.getJSONObject("sources");
                for (String key : sourcesJson.keySet()) {
                    sources.put(key, sourcesJson.getDouble(key));
                }
            }

            double bonus = jsonObject.has("bonus") && !jsonObject.isNull("bonus")
                    ? jsonObject.getDouble("bonus") : 0D;

            Map.Entry<SkyBlockEmblems, Integer> currentEmblem = null;
            if (jsonObject.has("emblem") && !jsonObject.isNull("emblem")) {
                String[] emblem = jsonObject.getString("emblem").split(":");
                currentEmblem = new HashMap.SimpleEntry<>(SkyBlockEmblems.valueOf(emblem[0]), Integer.parseInt(emblem[1]));
            }

            return new PlayerSkyBlockExperience(experience, currentEmblem, sources, bonus);
        }

        @Override
        public PlayerSkyBlockExperience clone(PlayerSkyBlockExperience value) {
            return new PlayerSkyBlockExperience(
                    new ArrayList<>(value.getCompletedExperienceCauses()),
                    value.getCurrentEmblem(),
                    new LinkedHashMap<>(value.getSourceProgress()),
                    value.getBonusXP());
        }
    };

    public DatapointSkyBlockExperience(String key, PlayerSkyBlockExperience value) {
        super(key, value, serializer);
    }

    public DatapointSkyBlockExperience(String key) {
        this(key, new PlayerSkyBlockExperience());
    }

    @NoArgsConstructor
    @Getter
    public static class PlayerSkyBlockExperience {
        private List<SkyBlockLevelCauseAbstr> completedExperienceCauses = new ArrayList<>();
        private Map.Entry<SkyBlockEmblems, Integer> currentEmblem = null;
        /**
         * XP earned per {@link SkyBlockXPSource}, keyed by enum name, for every source that
         * isn't already represented by a dedicated cause class. This is how systems that exist
         * but don't yet grant SkyBlock XP get wired up later — one {@code addSourceXP} call.
         */
        private Map<String, Double> sourceProgress = new LinkedHashMap<>();
        /** Admin-granted XP offset, set by the SkyBlock XP commands. Can be negative. */
        private double bonusXP = 0D;
        @Setter
        private SkyBlockPlayer attachedPlayer = null;
        @Getter(AccessLevel.NONE)
        private int cachedCauseCount = -1;
        @Getter(AccessLevel.NONE)
        private double cachedCauseXP;

        public PlayerSkyBlockExperience(List<SkyBlockLevelCauseAbstr> completedExperienceCauses,
                                        Map.Entry<SkyBlockEmblems, Integer> currentEmblem) {
            this(completedExperienceCauses, currentEmblem, new LinkedHashMap<>(), 0D);
        }

        public PlayerSkyBlockExperience(List<SkyBlockLevelCauseAbstr> completedExperienceCauses,
                                        Map.Entry<SkyBlockEmblems, Integer> currentEmblem,
                                        Map<String, Double> sourceProgress,
                                        double bonusXP) {
            this.completedExperienceCauses = completedExperienceCauses;
            this.currentEmblem = currentEmblem;
            this.sourceProgress = sourceProgress;
            this.bonusXP = bonusXP;
        }

        /**
         * Retrieves a list of completed experience causes that are instances of the specified class type.
         *
         * @param type the {@link Class} to filter the completed experience causes by.
         * @return a list of {@link SkyBlockLevelCauseAbstr} instances that match the specified type.
         */
        public List<SkyBlockLevelCauseAbstr> getOfType(Class type) {
            return completedExperienceCauses.stream().filter(type::isInstance).toList();
        }

        /**
         * Gets the current SkyBlock emblem, if available.
         *
         * @return the current {@link SkyBlockEmblems.SkyBlockEmblem}, or null if no emblem is set.
         */
        public @Nullable SkyBlockEmblems.SkyBlockEmblem getEmblem() {
            if (currentEmblem == null) return null;
            return currentEmblem.getKey().getEmblems().get(currentEmblem.getValue());
        }

        /**
         * Sets the current emblem by specifying the {@link SkyBlockEmblems} category and the associated emblem.
         *
         * @param emblems the {@link SkyBlockEmblems} category.
         * @param emblem the {@link SkyBlockEmblems.SkyBlockEmblem} to be set.
         */
        public void setEmblem(SkyBlockEmblems emblems, SkyBlockEmblems.SkyBlockEmblem emblem) {
            currentEmblem = new HashMap.SimpleEntry<>(SkyBlockEmblems.getCategoryFromEmblem(emblem),
                    emblems.getEmblems().indexOf(emblem));
        }

        /**
         * Checks if the player has already gained experience for the specified cause.
         *
         * @param cause the {@link SkyBlockLevelCauseAbstr} representing the experience cause.
         * @return true if the cause has been completed, false otherwise.
         */
        public boolean hasExperienceFor(SkyBlockLevelCauseAbstr cause) {
            return completedExperienceCauses.contains(cause);
        }

        /**
         * Adds a new experience cause to the completed causes, and triggers an event if the player is online.
         * If the experience cause has already been completed, it does nothing.
         *
         * @param cause the {@link SkyBlockLevelCauseAbstr} to be added.
         */
        public void addExperience(@NonNull SkyBlockLevelCauseAbstr cause) {
            if (completedExperienceCauses.contains(cause)) return;
            String causeKey = SkyBlockLevelCause.getKey(cause);
            if (completedExperienceCauses.stream().anyMatch((c) -> SkyBlockLevelCause.getKey(c).equals(causeKey))) return;

            double oldXP = getTotalXP();
            completedExperienceCauses.add(cause);
            fireModification(cause, oldXP, getTotalXP());
        }

        // ----- Per-source XP -----

        /**
         * XP this player has earned from a given source. Sources backed by a dedicated cause
         * class are summed from completed causes; everything else reads stored progress.
         */
        public double getSourceXP(@NonNull SkyBlockXPSource source) {
            Class<? extends SkyBlockLevelCauseAbstr> backing = source.getBackingCauseType();
            if (backing != null) {
                double earned = causeXP(backing) + sourceProgress.getOrDefault(source.name(), 0D);
                return source.isUncapped() ? earned : Math.min(source.getMaxXP(), earned);
            }
            return sourceProgress.getOrDefault(source.name(), 0D);
        }

        /**
         * Awards XP for a source, clamped to that source's real Hypixel maximum. This is the
         * entry point for wiring up a system that already tracks progress but doesn't yet grant
         * SkyBlock XP. No-ops for sources backed by a dedicated cause class, which award
         * themselves through {@link #addExperience}.
         */
        public void addSourceXP(@NonNull SkyBlockXPSource source, double amount) {
            if (!source.usesSourceProgress() || amount == 0) return;
            setSourceXP(source, getSourceXP(source) + amount);
        }

        /** Sets a source's earned XP outright, clamped to {@code [0, source max]}. */
        public void setSourceXP(@NonNull SkyBlockXPSource source, double amount) {
            if (!source.usesSourceProgress()) return;

            double clamped = source.isUncapped()
                    ? Math.max(0D, amount)
                    : Math.max(0D, Math.min(amount, source.getMaxXP()));
            double current = sourceProgress.getOrDefault(source.name(), 0D);
            if (clamped == current) return;

            double oldXP = getTotalXP();
            if (clamped == 0D) {
                sourceProgress.remove(source.name());
            } else {
                sourceProgress.put(source.name(), clamped);
            }
            fireModification(null, oldXP, getTotalXP());
        }

        /** Marks every source this server cannot award through play as fully completed. */
        /**
         * Puts every capped task at 100%. Cause-backed tasks get a top-up for the part of
         * Hypixel's total this server has no content for yet; uncapped tasks keep their real
         * running totals.
         */
        public void completeAllSourceProgress() {
            double oldXP = getTotalXP();
            for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
                if (source.isUncapped()) continue;
                Class<? extends SkyBlockLevelCauseAbstr> backing = source.getBackingCauseType();
                double topUp = backing == null ? source.getMaxXP() : Math.max(0D, source.getMaxXP() - causeXP(backing));
                sourceProgress.put(source.name(), topUp);
            }
            fireModification(null, oldXP, getTotalXP());
        }

        public double getUncappedSourceXP() {
            return Arrays.stream(SkyBlockXPSource.values())
                    .filter(SkyBlockXPSource::isUncapped)
                    .mapToDouble(this::getSourceXP)
                    .sum();
        }

        private double causeXP(Class<? extends SkyBlockLevelCauseAbstr> type) {
            return completedExperienceCauses.stream()
                    .filter(type::isInstance)
                    .mapToDouble(SkyBlockLevelCauseAbstr::xpReward)
                    .sum();
        }

        public double getTotalSourceXP() {
            return sourceProgress.values().stream().mapToDouble(Double::doubleValue).sum();
        }

        // ----- Admin bonus XP -----

        /** Replaces the admin bonus offset. Negative values are allowed, total XP floors at 0. */
        public void setBonusXP(double bonusXP) {
            if (this.bonusXP == bonusXP) return;
            double oldXP = getTotalXP();
            this.bonusXP = bonusXP;
            fireModification(null, oldXP, getTotalXP());
        }

        public void addBonusXP(double amount) {
            setBonusXP(this.bonusXP + amount);
        }

        /**
         * Moves the player's total XP to {@code target} by adjusting the bonus offset, leaving
         * genuinely earned causes and source progress untouched.
         */
        public void setTotalXP(double target) {
            setBonusXP(bonusXP + (Math.max(0D, target) - getTotalXP()));
        }

        /** XP earned without the admin offset — what the player actually worked for. */
        public double getEarnedXP() {
            if (cachedCauseCount != completedExperienceCauses.size()) {
                cachedCauseXP = completedExperienceCauses.stream().mapToDouble(SkyBlockLevelCauseAbstr::xpReward).sum();
                cachedCauseCount = completedExperienceCauses.size();
            }
            return cachedCauseXP + getTotalSourceXP();
        }

        private void fireModification(@Nullable SkyBlockLevelCauseAbstr cause, double oldXP, double newXP) {
            if (oldXP == newXP || getAttachedPlayer() == null) return;
            HypixelEventHandler.callCustomEvent(new SkyBlockXPModificationEvent(
                    getAttachedPlayer(), cause, oldXP, newXP));
        }

        /**
         * Returns a formatted display showing the player's progress to the next level.
         * This includes a visual loading bar based on the experience gained.
         *
         * @return a String representation of the next level's progress or "<c>MAX" if the maximum level is reached.
         */
        public String getNextLevelDisplay() {
            SkyBlockLevelRequirement currentLevel = getLevel();
            SkyBlockLevelRequirement nextLevel = currentLevel.getNextLevel();
            if (nextLevel == null) return "<c>MAX";

            int levelFloor = currentLevel.getCumulativeExperience();
            int nextLevelXP = nextLevel.getCumulativeExperience() - levelFloor;
            if (nextLevelXP <= 0) return "<c>MAX";

            double intoLevel = Math.max(0D, Math.min(getTotalXP() - levelFloor, nextLevelXP));

            String baseLoadingBar = "─────────────────";
            int maxBarLength = baseLoadingBar.length();
            int completedLength = (int) ((intoLevel / nextLevelXP) * maxBarLength);
            completedLength = Math.max(0, Math.min(completedLength, maxBarLength));

            String completedLoadingBar = baseLoadingBar.substring(0, completedLength);
            String uncompletedLoadingBar = baseLoadingBar.substring(completedLength);

            return "<7><3><m>" + completedLoadingBar + "<f>" + uncompletedLoadingBar
                    + "<r> <b>" + Math.round(intoLevel) + "<3>/<b>" + nextLevelXP;
        }

        /**
         * Calculates and returns the total experience earned from all completed experience causes,
         * per-source progress, and any admin bonus offset.
         *
         * @return the total experience points as a {@link Double}.
         */
        public Double getTotalXP() {
            return Math.max(0D, getEarnedXP() + bonusXP);
        }

        /**
         * Determines and returns the player's current level based on their total experience points.
         *
         * @return the current {@link SkyBlockLevelRequirement} for the player.
         */
        public SkyBlockLevelRequirement getLevel() {
            return SkyBlockLevelRequirement.getFromTotalXP(getTotalXP());
        }

        /** The player's SkyBlock level including its fractional part, e.g. {@code 620.19}. */
        public double getPreciseLevel() {
            return getTotalXP() / SkyBlockXPSource.XP_PER_LEVEL;
        }
    }
}
