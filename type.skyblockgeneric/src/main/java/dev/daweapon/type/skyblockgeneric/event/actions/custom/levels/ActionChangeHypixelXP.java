package dev.daweapon.type.skyblockgeneric.event.actions.custom.levels;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.actions.player.ActionAddSkyBlockXPToNametag;
import dev.daweapon.type.skyblockgeneric.event.custom.SkyBlockXPModificationEvent;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelUnlock;
import dev.daweapon.type.skyblockgeneric.levels.causes.LevelCause;
import dev.daweapon.type.skyblockgeneric.levels.unlocks.SkyBlockLevelStatisticUnlock;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// Announces SkyBlock level-ups and grants their unlock rewards when a player's XP changes
public class ActionChangeHypixelXP implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(SkyBlockXPModificationEvent event) {
        SkyBlockPlayer player = event.getPlayer();

        SkyBlockLevelRequirement oldLevel = SkyBlockLevelRequirement.getFromTotalXP(event.getOldXP());
        SkyBlockLevelRequirement newLevel = SkyBlockLevelRequirement.getFromTotalXP(event.getNewXP());

        // XP can now be taken away by staff commands; keep the nametag honest but stay quiet
        // rather than announcing a "level up" that went backwards.
        if (event.getNewXP() <= event.getOldXP()) {
            if (oldLevel != newLevel) ActionAddSkyBlockXPToNametag.updatePlayerNametag(player);
            return;
        }

        if (event.isReconciliation()) {
            if (oldLevel != newLevel) {
                ActionAddSkyBlockXPToNametag.updatePlayerNametag(player);
                recordLevelCause(player, newLevel);
            }
            return;
        }

        if (oldLevel == newLevel) {
            if (event.getCause() != null && event.getCause().shouldDisplayMessage(player))
                player.sendMessage("<b>+<3>{} <b>SkyBlock XP", event.getNewXP() - event.getOldXP());
        } else {
            ActionAddSkyBlockXPToNametag.updatePlayerNametag(player);
            if (!(event.getCause() instanceof LevelCause)) recordLevelCause(player, newLevel);
            player.sendMessage("<3><m>---------------------------------");
            player.sendMessage("  <b><l>SKYBLOCK LEVEL UP");
            player.sendMessage("  " + newLevel.getColor() + "Level <7>{} <8>-> <7>[" + newLevel.getColor() + "{}<7>]", oldLevel.asInt(), newLevel.asInt());
            List<Text> rewards = rewardsBetween(player, oldLevel.asInt(), newLevel.asInt());
            if (!rewards.isEmpty()) {
                player.sendMessage(" ");
                player.sendMessage("  <6><l>REWARDS");
                rewards.forEach(line -> player.sendMessage(Text.of("  ").append(line)));
            }
            player.sendMessage("<3><m>---------------------------------");
        }
    }

    private static void recordLevelCause(SkyBlockPlayer player, SkyBlockLevelRequirement level) {
        LevelCause levelCause = SkyBlockLevelCause.getLevelCause(level.asInt());
        if (levelCause != null) player.getSkyBlockExperience().addExperience(levelCause);
    }

    private static List<Text> rewardsBetween(SkyBlockPlayer player, int fromLevel, int toLevel) {
        ItemStatistics total = ItemStatistics.empty();
        List<Text> custom = new ArrayList<>();
        for (int level = fromLevel + 1; level <= toLevel; level++) {
            SkyBlockLevelRequirement requirement = SkyBlockLevelRequirement.getLevel(level);
            if (requirement == null) continue;
            for (SkyBlockLevelUnlock unlock : requirement.getUnlocks()) {
                if (unlock instanceof SkyBlockLevelStatisticUnlock statistic)
                    total = ItemStatistics.add(total, statistic.getStatistics());
                else custom.addAll(unlock.getDisplay(player, level));
            }
        }
        List<Text> lines = new ArrayList<>();
        for (ItemStatistic statistic : ItemStatistic.values()) {
            double value = total.getBase(statistic);
            if (value <= 0) continue;
            String amount = value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
            lines.add(Text.of("<a>+" + amount + statistic.getSuffix() + " <stat:" + statistic.name().toLowerCase() + ">"));
        }
        lines.addAll(custom);
        return lines;
    }
}
