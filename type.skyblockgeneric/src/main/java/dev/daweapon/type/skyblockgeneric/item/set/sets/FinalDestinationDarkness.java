package dev.daweapon.type.skyblockgeneric.item.set.sets;

import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityEffects;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityMath;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityState;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.item.set.impl.SetRepeatable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
