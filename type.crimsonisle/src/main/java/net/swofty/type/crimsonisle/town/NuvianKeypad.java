package net.swofty.type.crimsonisle.town;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.crimsonisle.gui.town.GUIKeypad;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nuvian's secret Abiphone room in the Dragontail brewery. The 4-digit code changes every real day;
 * players overhear it by crouching still beside the brewery window while Kutral and Nall talk, then
 * enter it on the keypad by the door. The room has no way out in the map, so a click spot inside
 * takes the player back to the keypad.
 */
public final class NuvianKeypad {
    private static final String KUTRAL = "<d>Kutral";
    private static final String NALL = "<d>Nall";
    private static final Pos KEYPAD = new Pos(-677.5, 107.9, -735.5);
    private static final Pos ROOM = new Pos(-689.5, 102, -735.5, 90, 0);
    private static final Pos ROOM_EXIT = new Pos(-686.5, 102, -735.5);
    private static final Pos OUTSIDE = new Pos(-676.5, 107, -736.5, -90, 0);
    private static final Pos WINDOW = new Pos(-654.5, 107, -740.5);
    private static final double LISTEN_RANGE = 2.5;
    private static final int SECONDS_TO_OVERHEAR = 3;

    private static final Map<UUID, Listener> LISTENERS = new ConcurrentHashMap<>();

    private record Listener(Pos position, int seconds, long heardDay) {
    }

    private NuvianKeypad() {
    }

    public static String code(LocalDate day) {
        long mixed = day.toEpochDay() * 0x9E3779B97F4A7C15L;
        mixed ^= mixed >>> 31;
        return String.format("%04d", Math.floorMod(mixed, 10_000L));
    }

    public static String todaysCode() {
        return code(LocalDate.now(ZoneOffset.UTC));
    }

    public static void start() {
        Instance instance = HypixelConst.getInstanceContainer();
        new InteractionEntity(0.6f, 0.6f, (player, _) -> player.openView(new GUIKeypad(NuvianKeypad::submit)))
                .setInstance(instance, KEYPAD);
        new InteractionEntity(1f, 2f, (player, _) -> player.teleport(OUTSIDE)).setInstance(instance, ROOM_EXIT);
        ServerHolograms.addExternalHologram(ServerHolograms.ExternalHologram.builder()
                .instance(instance).pos(ROOM_EXIT.add(0, 2.1, 0)).text(new String[]{"<e>Exit", "<e><l>CLICK"}).build());
        MinecraftServer.getSchedulerManager().buildTask(NuvianKeypad::listen).repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static void submit(SkyBlockPlayer player, String entered) {
        if (!entered.equals(todaysCode())) {
            player.sendMessage("<c>Incorrect code!");
            return;
        }
        player.sendMessage("<a>The door clicks open.");
        player.teleport(ROOM);
    }

    private static void listen() {
        long today = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        for (var online : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (!(online instanceof SkyBlockPlayer player) || !player.isOnline() || player.getSkyblockDataHandler() == null) continue;
            Pos at = player.getPosition();
            if (at.distance(WINDOW) > LISTEN_RANGE) {
                LISTENERS.remove(player.getUuid());
                continue;
            }
            Listener last = LISTENERS.get(player.getUuid());
            if (last != null && last.heardDay() == today) continue;
            boolean still = last != null && last.position().samePoint(at);
            if (!player.isSneaking() || (last != null && !still)) {
                if (last != null && last.seconds() > 0 && !CrimsonScene.isPlaying(player)) {
                    CrimsonScene.create().say(KUTRAL, "Get away from us.").play(player);
                }
                LISTENERS.put(player.getUuid(), new Listener(at, 0, 0));
                continue;
            }
            int seconds = last == null ? 1 : last.seconds() + 1;
            if (seconds < SECONDS_TO_OVERHEAR || CrimsonScene.isPlaying(player)) {
                LISTENERS.put(player.getUuid(), new Listener(at, seconds, 0));
                continue;
            }
            LISTENERS.put(player.getUuid(), new Listener(at, seconds, today));
            CrimsonScene.create()
                    .say(NALL, "So do you know if they got the new Abiphones?")
                    .say(KUTRAL, "Ya, they got all of them. It's a bit expensive though.")
                    .say(NALL, "Incredible and do you know the code..?")
                    .say(KUTRAL, "Yep, but keep it to yourself.")
                    .say(NALL, "Of course.")
                    .say(KUTRAL, "Today's code is <a>" + todaysCode() + "<f>.")
                    .say(NALL, "Ok, got it.")
                    .play(player);
        }
    }
}
