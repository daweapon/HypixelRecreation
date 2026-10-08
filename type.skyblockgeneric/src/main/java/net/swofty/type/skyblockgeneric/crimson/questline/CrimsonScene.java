package net.swofty.type.skyblockgeneric.crimson.questline;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.entity.npc.NPCConversations;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

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
        return play(player, null);
    }

    /** Plays the scene as a conversation with {@code speaker}, so clicking that NPC again is not refused. */
    public CompletableFuture<Void> play(HypixelPlayer player, @Nullable Object speaker) {
        long expected = Math.max(0, lines.size() - 1) * LINE_TICKS * 50;
        Playback playback = new Playback(player, new CompletableFuture<>(),
                NPCConversations.server().begin(player.getUuid(), speaker, expected));
        PLAYING.put(player.getUuid(), playback.future());
        sendFrom(playback, 0);
        return playback.future();
    }

    private void sendFrom(Playback playback, int index) {
        try {
            sendLine(playback, index);
        } catch (RuntimeException exception) {
            Logger.error(exception, "Crimson scene line {} failed for {}", index, playback.player().getUsername());
            playback.finish(exception);
        }
    }

    private void sendLine(Playback playback, int index) {
        HypixelPlayer player = playback.player();
        if (!player.isOnline() || playback.future().isDone() || index >= lines.size()) {
            playback.finish(null);
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
            playback.finish(null);
            return;
        }
        MinecraftServer.getSchedulerManager().buildTask(() -> sendFrom(playback, index + 1))
                .delay(TaskSchedule.tick((int) LINE_TICKS)).schedule();
    }

    private record Playback(HypixelPlayer player, CompletableFuture<Void> future, long token) {
        void finish(@Nullable Throwable failure) {
            PLAYING.remove(player.getUuid(), future);
            try {
                if (failure == null) future.complete(null);
                else future.completeExceptionally(failure);
            } finally {
                NPCConversations.server().end(player.getUuid(), token);
            }
        }
    }
}
