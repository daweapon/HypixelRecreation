package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.Optional;

public final class AbiphoneCalls {
    private AbiphoneCalls() {
    }

    public static void call(SkyBlockPlayer player, SkyBlockItem abiphone, AbiphoneNPC npc) {
        AbiphoneItemSettings.recordCall(player, abiphone, npc);
        Ringtone tone = AbiphoneItemSettings.supports(abiphone, AbiphoneComponent.AbiphoneFeature.RINGTONES)
                ? AbiphoneItemSettings.ringtone(abiphone) : Ringtone.CLASSIC;
        int rings = AbiphoneCases.singleRing(AbiphoneCases.effective(player)) ? 1 : 3;
        ring(player, tone, 1, rings, () -> npc.onCall(player));
    }

    private static void ring(SkyBlockPlayer player, Ringtone tone, int ring, int rings, Runnable answer) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            player.sendMessage(Text.key("gui_abiphone.ring_" + ring));
            tone.play(player, ring - 1);
            if (ring < rings) ring(player, tone, ring + 1, rings, answer);
            else MinecraftServer.getSchedulerManager().buildTask(answer).delay(TaskSchedule.seconds(1)).schedule();
        }).delay(TaskSchedule.seconds(1)).schedule();
    }

    public static Optional<SkyBlockItem> phoneWith(SkyBlockPlayer player, AbiphoneNPC npc) {
        for (SkyBlockItem item : player.getAllInventoryItems()) {
            if (item == null || !item.hasComponent(AbiphoneComponent.class)) continue;
            if (item.getAttributeHandler().hasAbiphoneNPC(npc)) return Optional.of(item);
        }
        return Optional.empty();
    }

    public static boolean speedDial(SkyBlockPlayer player, SkyBlockItem abiphone) {
        if (!AbiphoneItemSettings.supports(abiphone, AbiphoneComponent.AbiphoneFeature.SPEED_DIAL)) return false;
        AbiphoneNPC npc = AbiphoneRegistry.getFromId(AbiphoneItemSettings.speedDial(abiphone));
        if (npc == null || !abiphone.getAttributeHandler().hasAbiphoneNPC(npc)) {
            player.sendMessage("<c>You have not set a Speed Dial contact on this Abiphone!");
            return true;
        }
        call(player, abiphone, npc);
        return true;
    }
}
