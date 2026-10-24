package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class AbiphoneIncomingCalls {
    public enum Result {
        NO_CONTACT, SILENCED, NO_SIGNAL, RINGING
    }

    private static final Set<ServerType> NO_SIGNAL = Set.of(ServerType.SKYBLOCK_DWARVEN_MINES, ServerType.SKYBLOCK_CRYSTAL_HOLLOWS);
    private static final int RINGS = 3;
    private static final int PICK_UP_SECONDS = 15;
    private static final Map<UUID, Map<String, Consumer<SkyBlockPlayer>>> PENDING = new ConcurrentHashMap<>();

    private AbiphoneIncomingCalls() {
    }

    public static boolean hasSignal(SkyBlockPlayer player) {
        return !NO_SIGNAL.contains(HypixelConst.getTypeLoader().getType());
    }

    public static Result ring(SkyBlockPlayer player, String contactId, Consumer<SkyBlockPlayer> onPickUp) {
        AbiphoneNPC npc = AbiphoneRegistry.getFromId(contactId);
        if (npc == null) return Result.NO_CONTACT;
        Optional<SkyBlockItem> phone = AbiphoneCalls.phoneWith(player, npc);
        if (phone.isEmpty()) return Result.NO_CONTACT;
        if (AbiphoneItemSettings.isDoNotDisturb(phone.get(), npc)) return Result.SILENCED;
        if (!hasSignal(player)) return Result.NO_SIGNAL;
        Map<String, Consumer<SkyBlockPlayer>> calls = PENDING.computeIfAbsent(player.getUuid(), _ -> new ConcurrentHashMap<>());
        if (calls.putIfAbsent(contactId, onPickUp) != null) return Result.RINGING;

        Ringtone tone = AbiphoneItemSettings.supports(phone.get(), AbiphoneComponent.AbiphoneFeature.RINGTONES)
                ? AbiphoneItemSettings.ringtone(phone.get()) : Ringtone.CLASSIC;
        player.sendMessage("<e>✆ {} <e>✆", npc.getName());
        for (int ring = 1; ring <= RINGS; ring++) {
            int number = ring;
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (!calls.containsKey(contactId)) return;
                player.sendMessage(ringLine(number, contactId));
                tone.play(player, number - 1);
            }).delay(TaskSchedule.seconds(number - 1)).schedule();
        }
        MinecraftServer.getSchedulerManager().buildTask(() -> calls.remove(contactId))
                .delay(TaskSchedule.seconds(RINGS + PICK_UP_SECONDS)).schedule();
        return Result.RINGING;
    }

    private static Component ringLine(int rings, String contactId) {
        Component pickUp = Component.text(" [PICK UP]", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/abiphonepickup " + contactId))
                .hoverEvent(HoverEvent.showText(Text.of("<e>Click to pick up!")));
        return Component.text("✆ " + "RING... ".repeat(rings).stripTrailing(), NamedTextColor.GREEN).append(pickUp);
    }

    public static boolean pickUp(SkyBlockPlayer player, String contactId) {
        Map<String, Consumer<SkyBlockPlayer>> calls = PENDING.get(player.getUuid());
        Consumer<SkyBlockPlayer> action = calls == null ? null : calls.remove(contactId);
        if (action == null) return false;
        action.accept(player);
        return true;
    }

    public static void speak(SkyBlockPlayer player, String contactId, String... lines) {
        AbiphoneNPC npc = AbiphoneRegistry.getFromId(contactId);
        if (npc == null) return;
        for (int i = 0; i < lines.length; i++) {
            Text line = Text.read(lines[i]);
            MinecraftServer.getSchedulerManager().buildTask(() -> npc.sendNPCMessage(player, line))
                    .delay(TaskSchedule.seconds(2L * i)).schedule();
        }
    }
}
