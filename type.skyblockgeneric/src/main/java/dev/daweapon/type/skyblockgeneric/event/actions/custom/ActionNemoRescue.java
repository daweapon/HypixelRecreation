package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.moonglade.nemo.NemoRescue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.timer.TaskSchedule;

public class ActionNemoRescue implements HypixelEventClass {
    private static final int DELAY_SECONDS = 2;

    public ActionNemoRescue() {
        NemoRescue.register();
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (player.getInstance() != null) NemoRescue.deliver(player);
        }).delay(TaskSchedule.seconds(DELAY_SECONDS)).schedule();
    }
}
