package dev.daweapon.type.generic.achievement;

import lombok.RequiredArgsConstructor;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointAchievementData;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.List;
import java.util.function.Function;

// Per-player handler for tracking achievement progress and firing unlock notifications.
@RequiredArgsConstructor
public class PlayerAchievementHandler {
    private static volatile Function<HypixelPlayer, List<HypixelPlayer>> coopResolver = p -> List.of();

    private final HypixelPlayer player;

    public static void setCoopResolver(Function<HypixelPlayer, List<HypixelPlayer>> resolver) {
        coopResolver = resolver;
    }


    public AchievementData getAchievementData() {
        return player.getDataHandler()
                .get(HypixelDataHandler.Data.ACHIEVEMENT_DATA, DatapointAchievementData.class)
                .getValue();
    }

    public boolean addProgress(String achievementId, int amount) {
        return applyProgress(achievementId, amount, false, true);
    }

    public boolean setProgress(String achievementId, int value) {
        return applyProgress(achievementId, value, true, true);
    }

    public void addProgressByTrigger(String trigger, int amount) {
        for (AchievementDefinition def : AchievementRegistry.getByTrigger(trigger)) {
            if (def.isPerGame()) continue;
            addProgress(def.getId(), amount);
        }
    }

    public void setProgressByTrigger(String trigger, int value) {
        for (AchievementDefinition def : AchievementRegistry.getByTrigger(trigger)) {
            if (def.isPerGame()) continue;
            setProgress(def.getId(), value);
        }
    }

    public void completeAchievement(String achievementId) {
        completeAchievement(achievementId, true);
    }

    private void completeAchievement(String achievementId, boolean share) {
        AchievementDefinition def = AchievementRegistry.get(achievementId);
        if (def == null || def.isLegacy()) return;

        if (def.getType() == AchievementType.TIERED) {
            applyProgress(achievementId, def.getGoalForTier(def.getMaxTier()), true, share);
            return;
        }

        AchievementData.AchievementProgress progress = getAchievementData().getOrCreate(achievementId);
        if (!progress.isCompleted()) {
            progress.complete();
            notifyUnlocked(def, 0);
        }
        if (share && def.isCoop()) {
            for (PlayerAchievementHandler other : coopHandlers()) other.completeAchievement(achievementId, false);
        }
    }

    private boolean applyProgress(String achievementId, int amount, boolean absolute, boolean share) {
        AchievementDefinition def = AchievementRegistry.get(achievementId);
        if (def == null || def.isLegacy()) return false;

        AchievementData.AchievementProgress progress = getAchievementData().getOrCreate(achievementId);
        int previousTier = progress.getCurrentTier();
        int unlockedUnits = absolute ? progress.raiseProgress(def, amount) : progress.addProgress(def, amount);

        if (unlockedUnits > 0) {
            if (def.getType() == AchievementType.TIERED) {
                for (int tier = previousTier + 1; tier <= progress.getCurrentTier(); tier++) notifyUnlocked(def, tier);
            } else {
                notifyUnlocked(def, 0);
            }
        }

        if (share && def.isCoop()) {
            for (PlayerAchievementHandler other : coopHandlers()) other.applyProgress(achievementId, amount, absolute, false);
        }
        return unlockedUnits > 0;
    }

    private List<PlayerAchievementHandler> coopHandlers() {
        return coopResolver.apply(player).stream()
                .filter(other -> !other.getUuid().equals(player.getUuid()))
                .map(HypixelPlayer::getAchievementHandler)
                .toList();
    }

    public boolean hasAchievement(String achievementId) {
        return getAchievementData().isCompleted(achievementId);
    }

    public boolean hasFullyCompletedAchievement(String achievementId) {
        return getAchievementData().isFullyCompleted(achievementId);
    }

    public int getAchievementTier(String achievementId) {
        return getAchievementData().getCurrentTier(achievementId);
    }

    public int getProgress(String achievementId) {
        return getAchievementData().getProgress(achievementId);
    }

    public AchievementData.AchievementProgress getProgressData(String achievementId) {
        return getAchievementData().get(achievementId);
    }

    public int getTotalPoints() {
        return getAchievementData().getTotalPoints();
    }

    public int getTotalPoints(AchievementCategory category) {
        return getAchievementData().getTotalPoints(category);
    }

    public int getPoints(AchievementCategory category, AchievementType type) {
        return getAchievementData().getPoints(category, type);
    }

    public int getUnlockedCount(AchievementCategory category) {
        return getAchievementData().getUnlockedCount(category);
    }

    public int getUnlockedCount(AchievementCategory category, AchievementType type) {
        return getAchievementData().getUnlockedCount(category, type);
    }

    public double getCompletionPercentage(AchievementCategory category) {
        return getAchievementData().getCompletionPercentage(category);
    }

    public int getTotalUnlockedCount() {
        return getAchievementData().getTotalUnlockedCount();
    }

    public boolean isTracked(String achievementId) {
        return getAchievementData().isTracked(achievementId);
    }

    public String getTrackedAchievement(AchievementCategory category) {
        return getAchievementData().getTrackedAchievement(category);
    }

    public boolean toggleTracking(String achievementId) {
        AchievementDefinition def = AchievementRegistry.get(achievementId);
        if (def == null || def.getType() != AchievementType.TIERED) {
            player.sendMessage("<c>Only tiered achievements can be tracked!");
            return false;
        }

        if (hasFullyCompletedAchievement(achievementId)) {
            player.sendMessage("<c>You have already completed all tiers of this achievement!");
            return false;
        }

        boolean nowTracking = getAchievementData().toggleTracking(achievementId);

        if (nowTracking) {
            player.sendMessage("<a>Now tracking: <e>{}", def.getName());
            player.playSound(net.kyori.adventure.sound.Sound.sound(
                    net.minestom.server.sound.SoundEvent.BLOCK_NOTE_BLOCK_PLING,
                    net.kyori.adventure.sound.Sound.Source.MASTER, 1.0f, 2.0f));
        } else {
            player.sendMessage("<c>Stopped tracking: <e>{}", def.getName());
        }

        return nowTracking;
    }

    private void notifyUnlocked(AchievementDefinition def, int tier) {
        AchievementStatisticsService.recordUnlock(def, tier);

        boolean tiered = def.getType() == AchievementType.TIERED;
        String tierText = tiered ? " " + toRoman(tier) : "";
        int points = tiered ? def.getPointsForTier(tier) : def.getPoints();

        Text tierHover = Text.of("""
                <a>{}{}
                <7>{}

                <7>Reward:
                 <8>+<e>{} <7>Achievement Points

                <e>Click to open achievements menu!""",
                def.getName(), tierText, def.getDescriptionForTier(tier), points);

        player.sendMessage("<e><k>A</k><a>>>   Achievement Unlocked: <hover:'{0}'><click:run:'/achievements'><6>{1}{2}</click></hover><a>   \\<\\<<e><k>A",
                tierHover, def.getName(), tierText);

        player.playSound(net.kyori.adventure.sound.Sound.sound(
                net.minestom.server.sound.SoundEvent.ENTITY_PLAYER_LEVELUP,
                net.kyori.adventure.sound.Sound.Source.MASTER, 1.0f, 1.0f));
    }

    private String toRoman(int tier) {
        return AchievementTier.toRomanNumeral(tier);
    }
}
