package net.swofty.type.skyblockgeneric.item.set.sets;

import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityEffects;
import net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityMath;
import net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityState;
import net.swofty.type.skyblockgeneric.item.armor.WornArmor;
import net.swofty.type.skyblockgeneric.item.set.impl.SetRepeatable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class FinalDestinationDarkness implements SetRepeatable {
    public static final int CHECK_TICKS = 10;

    public static boolean shouldCharge(boolean sneaking, boolean inCombat, boolean paid, int soulflow) {
        return sneaking && inCombat && !paid && soulflow >= ArmorAbilityMath.DARKNESS_SOULFLOW_COST;
    }

    @Override
    public Task getTask(Scheduler scheduler) {
        return scheduler.buildTask(() -> {
            long now = System.currentTimeMillis();
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getSkyblockDataHandler() == null) continue;
                if (!WornArmor.wearsAll(WornArmor.cachedIds(player), ArmorAbilityEffects.FINAL_DESTINATION)) continue;
                DatapointInteger soulflow = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class);
                if (!shouldCharge(player.isSneaking(), ArmorAbilityState.inCombat(player, now),
                        ArmorAbilityState.darknessPaid(player, now), soulflow.getValue())) continue;
                soulflow.setValue(soulflow.getValue() - ArmorAbilityMath.DARKNESS_SOULFLOW_COST);
                ArmorAbilityState.payDarkness(player, now);
            }
        }).repeat(TaskSchedule.tick(CHECK_TICKS)).schedule();
    }
}
