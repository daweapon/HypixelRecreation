package net.swofty.type.skyblockgeneric.crimson.questline;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A scripted conversation between several NPCs, sent to one player two seconds a line. Speaker
 * names and lines are markup; {@code <player>} in a line becomes the player's grey name.
 */
public final class CrimsonScene {
    public static final String ELLE = "<c>Elle";
    public static final String UGO = "<f>Ugo";
    public static final String CHIEF_SCORN = "<4><l>Chief Scorn";
    public static final String CAPTAIN_AHONE = "<5>Captain Ahone";
    public static final String QUEEN_NYX = "<d><l>Queen Nyx";
    public static final String DEAN = "<5>Dean";
    public static final String KREVIUS = "<e>Krevius";
    public static final String GRELIUS = "<e>Grelius";

    private static final long LINE_TICKS = 40;
    private static final Map<UUID, CompletableFuture<Void>> PLAYING = new ConcurrentHashMap<>();

    private final List<String[]> lines = new ArrayList<>();
    private boolean phone;

    public static CrimsonScene create() {
        return new CrimsonScene();
    }

    public static boolean isPlaying(HypixelPlayer player) {
        CompletableFuture<Void> future = PLAYING.get(player.getUuid());
        return future != null && !future.isDone();
    }

    public CrimsonScene phone() {
        this.phone = true;
        return this;
    }

    public CrimsonScene say(String speaker, String... text) {
        for (String line : text) lines.add(new String[]{speaker, line});
        return this;
    }

    /** A line that is not spoken by an NPC, such as a system message or an item change. */
    public CrimsonScene raw(String markup) {
        lines.add(new String[]{null, markup});
        return this;
    }

    public CompletableFuture<Void> play(HypixelPlayer player) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        PLAYING.put(player.getUuid(), future);
        sendFrom(player, 0, future);
        return future;
    }

    private void sendFrom(HypixelPlayer player, int index, CompletableFuture<Void> future) {
        if (!player.isOnline()) {
            future.complete(null);
            return;
        }
        if (index >= lines.size()) {
            future.complete(null);
            return;
        }
        String[] line = lines.get(index);
        String text = line[1].replace("<player>", "<7>" + player.getUsername() + "<f>");
        if (line[0] == null) {
            player.sendMessage(text);
        } else {
            player.sendMessage("<e>[NPC] " + line[0] + "<f>: " + (phone ? "<b>✆ <f>" : "") + text);
            player.playSound(Sound.sound(Key.key("entity.villager.celebrate"), Sound.Source.NEUTRAL, 1f, 1f));
        }
        if (index + 1 >= lines.size()) {
            future.complete(null);
            return;
        }
        MinecraftServer.getSchedulerManager().buildTask(() -> sendFrom(player, index + 1, future))
                .delay(TaskSchedule.tick((int) LINE_TICKS)).schedule();
    }
}
