package net.swofty.type.generic.entity.npc;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DialogueController {
    private static final long LINE_MILLIS = 2_000;
    private final HashMap<HypixelPlayer, Map.Entry<HypixelNPC.DialogueSet, CompletableFuture<String>>> activeDialogues = new HashMap<>();
    private final Map<HypixelPlayer, Long> conversationTokens = new HashMap<>();
    private final HypixelNPC npc;

    public DialogueController(HypixelNPC npc) {
        this.npc = npc;
    }

    /**
     * Checks if the player is currently in a dialogue with this NPC.
     *
     * @param player The player to check.
     * @return True if the player is in dialogue, false otherwise.
     */
    public boolean isInDialogue(HypixelPlayer player) {
        return activeDialogues.containsKey(player);
    }

    /**
     * Starts a dialogue with the player using the specified dialogue key.
     *
     * @param player The player to start the dialogue with.
     * @param key    The key of the dialogue set to play.
     * @return A CompletableFuture that completes when the dialogue finishes, with the dialogue key.
     */
    public CompletableFuture<String> setDialogue(HypixelPlayer player, String key) {
        CompletableFuture<String> future = new CompletableFuture<>();

        HypixelNPC.DialogueSet[] dialogueSets = npc.dialogues(player);
        for (HypixelNPC.DialogueSet dialogueSet : dialogueSets) {
            if (dialogueSet.key().equals(key)) {
                activeDialogues.put(player, Map.entry(dialogueSet, future));
                long expected = Math.max(0, dialogueSet.lines().length - 1) * LINE_MILLIS;
                conversationTokens.put(player, NPCConversations.server().begin(player.getUuid(), npc, expected));
                sendLines(player, dialogueSet);
                return future;
            }
        }

        future.completeExceptionally(new NullPointerException("Dialogue set with key '" + key + "' not found."));
        return future;
    }

    /**
     * Removes the player from the dialogue, completing the future with null.
     *
     * @param player The player to remove from dialogue.
     */
    public void cancelDialogue(HypixelPlayer player) {
        finish(player, future -> future.complete(null));
    }

    private void finish(HypixelPlayer player, Consumer<CompletableFuture<String>> completion) {
        Map.Entry<HypixelNPC.DialogueSet, CompletableFuture<String>> entry = activeDialogues.remove(player);
        Long token = conversationTokens.remove(player);
        try {
            if (entry != null) completion.accept(entry.getValue());
        } finally {
            if (token != null) NPCConversations.server().end(player.getUuid(), token);
        }
    }

    private void sendLines(HypixelPlayer player, HypixelNPC.DialogueSet dialogueSet) {
        try {
            handleLineSendingLoop(player, dialogueSet);
        } catch (RuntimeException exception) {
            finish(player, future -> future.completeExceptionally(exception));
            Logger.error(exception, "Dialogue {} of NPC {} failed", dialogueSet.key(), npc.getName());
        }
    }

    private void handleLineSendingLoop(HypixelPlayer player, HypixelNPC.DialogueSet dialogueSet) {
        if (dialogueSet.sound() != null) {
            npc.sendNPCMessage(player, dialogueSet.lines()[0], dialogueSet.sound());
        } else {
            npc.sendNPCMessage(player, dialogueSet.lines()[0]);
        }

        Text[] newLines = new Text[dialogueSet.lines().length - 1];
        System.arraycopy(dialogueSet.lines(), 1, newLines, 0, dialogueSet.lines().length - 1);

        if (newLines.length == 0) {
            finish(player, future -> future.complete(dialogueSet.key()));
            return;
        }

        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.buildTask(() -> {
            // Check if the player is still in dialogue (might have been canceled)
            if (activeDialogues.containsKey(player)) {
                sendLines(player, new HypixelNPC.DialogueSet(
                        dialogueSet.key(), newLines, dialogueSet.sound()));
            }
        }).delay(TaskSchedule.millis(LINE_MILLIS)).schedule();
    }

    public static void removeAllDialogues(HypixelPlayer player) {
        for (HypixelNPC npc : HypixelNPC.getRegisteredNPCs()) {
            npc.dialogue().cancelDialogue(player);
        }
        NPCConversations.server().release(player.getUuid());
    }
}
