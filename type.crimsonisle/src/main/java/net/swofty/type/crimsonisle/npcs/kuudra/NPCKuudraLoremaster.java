package net.swofty.type.crimsonisle.npcs.kuudra;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.FollowerStep;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import net.swofty.type.skyblockgeneric.crimson.kuudra.npc.KuudraFollowerNPC;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Kuudra Loremaster in Scarleton. Quoting a Kuudra fight line to him in chat sends you looking
 * for his past self in the Deep Caverns, and his "real lair" line decays the Teeth Plaque.
 */
public class NPCKuudraLoremaster extends KuudraFollowerNPC {
    public static final Pos POSITION = new Pos(-106.5, 126, -817.5);
    private static final double HEARING_RANGE = 10;
    private static final List<String> TRIGGER_LINES = List.of(
            "THE BRIDGE IS COLLAPSING, GET TO THE CENTER!",
            "Those orbs strengthen Kuudra! Shoot them down with your cannons!",
            "A Dropship! Take it down before it's too late!");
    private static final String LAIR_LINE = "What just happened!? Is this Kuudra's real lair?";
    private static volatile NPCKuudraLoremaster instance;

    public NPCKuudraLoremaster() {
        super("<5>Kuudra Loremaster", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MwY2Q2MjZiYWYxNjNjZmQ0ZmU3NjhmNDc1M2Y4MGQ1MGRkMTE5NzcwMGU0YzNhMmQ1ODUwY2Y3OWJlYjA5NDcifX19",
                POSITION);
        instance = this;
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        setDialogue(player, "idle_" + ThreadLocalRandom.current().nextInt(3));
    }

    /** Chat from a player near the Loremaster; the trigger lines work even while muted on Hypixel. */
    public static void hear(SkyBlockPlayer player, String message) {
        NPCKuudraLoremaster loremaster = instance;
        if (loremaster == null || !KuudraFollowerRules.npcsVisible(player)) return;
        if (player.getPosition().distance(POSITION) > HEARING_RANGE || loremaster.isInDialogue(player)) return;
        String said = message.trim();
        if (TRIGGER_LINES.contains(said)) {
            loremaster.setDialogue(player, "classic")
                    .thenRun(() -> KuudraFollowerProgress.complete(player, FollowerStep.LOREMASTER_TRIGGER));
            return;
        }
        if (said.equals(LAIR_LINE)) {
            KuudraFollowerProgress.complete(player, FollowerStep.LOREMASTER_LAIR_LINE);
            if (KuudraFollowerRules.holding(player, "KUUDRA_CAVITY") && KuudraFollowerProgress.plaqueStage(player) != null
                    && !KuudraFollowerProgress.has(player, FollowerStep.DECAY_LOREMASTER)) {
                loremaster.setDialogue(player, "plaque")
                        .thenRun(() -> KuudraFollowerProgress.decay(player, FollowerStep.DECAY_LOREMASTER));
            }
        }
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("classic").lines(
                        "Wow! That is a classic. It's great to meet a fellow follower of Kuudra.",
                        "I wish I could give you something to commemorate meeting you, but I don't have that much anymore.",
                        "Maybe if we met in the past I would have something for you."
                ).build(),
                DialogueSet.builder().key("plaque").lines(
                        "I don't know, adventurer, is it his real lair???",
                        "Speaking of layers, I see you have <a>Repulsive Kuudra Teeth Plaque<f>!",
                        "You can definitely use that to take some layers off Kuudra's teeth!",
                        "There, I'll help you get it broken down!"
                ).build(),
                DialogueSet.builder().key("idle_0").lines(
                        "I know so many things about Kuudra.",
                        "They are quite interesting."
                ).build(),
                DialogueSet.builder().key("idle_1").lines(
                        "I've been studying the ancient texts of Kuudra.",
                        "They are quite interesting."
                ).build(),
                DialogueSet.builder().key("idle_2").lines(
                        "When I was much younger, I used to collect so much Magma Cream and Netherrack.",
                        "Unfortunately, those days are behind me.",
                        "I now spend my days learning about Kuudra."
                ).build()
        };
    }
}
