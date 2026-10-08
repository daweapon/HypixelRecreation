package net.swofty.type.crimsonisle.npcs.kuudra;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.FollowerStep;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import net.swofty.type.skyblockgeneric.crimson.kuudra.npc.KuudraFollowerNPC;
import net.swofty.type.skyblockgeneric.gui.inventories.kuudra.GUIKuudraClaimReward;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * The Kuudra Archeologist in Dragontail gives the integral clue, trades the Follower Leggings for a
 * look at the Kuudra Relic (which you keep), and decays the Teeth Plaque at 4,500 Intelligence.
 */
public class NPCKuudraArcheologist extends KuudraFollowerNPC {

    public NPCKuudraArcheologist() {
        super("<c>Kuudra Archeologist", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYzNjMwZDliMDIwODhlYTE5MjRhODcyMjQyYWJjNzViNjI2MmMyYWNhOTJhZWNmODcxNGE1N2E0MWVlYTBiOWQifX19",
                new Pos(-570.5, 111, -642.5));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (KuudraFollowerRules.holding(player, "KUUDRA_CAVITY") && KuudraFollowerProgress.plaqueStage(player) != null
                && !KuudraFollowerProgress.has(player, FollowerStep.DECAY_ARCHEOLOGIST)
                && KuudraFollowerRules.intelligence(player) >= KuudraFollowerRules.DECAY_INTELLIGENCE) {
            setDialogue(player, "plaque").thenRun(() -> KuudraFollowerProgress.decay(player, FollowerStep.DECAY_ARCHEOLOGIST));
            return;
        }
        if (KuudraFollowerProgress.has(player, FollowerStep.LEGGINGS_CLAIMED)) {
            setDialogue(player, "after");
            return;
        }
        if (player.countItem(ItemType.KUUDRA_RELIC) > 0) {
            setDialogue(player, "relic").thenRun(() -> player.openView(
                    new GUIKuudraClaimReward(ItemType.KUUDRA_FOLLOWER_LEGGINGS, NPCKuudraArcheologist::claimLeggings)));
            return;
        }
        setDialogue(player, "clue").thenRun(() -> KuudraFollowerProgress.complete(player, FollowerStep.ARCHEOLOGIST_CLUE));
    }

    private static void claimLeggings(SkyBlockPlayer player) {
        if (KuudraFollowerProgress.has(player, FollowerStep.LEGGINGS_CLAIMED) || player.countItem(ItemType.KUUDRA_RELIC) < 1) return;
        KuudraFollowerProgress.complete(player, FollowerStep.LEGGINGS_CLAIMED);
        player.addAndUpdateItem(ItemType.KUUDRA_FOLLOWER_LEGGINGS);
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("clue").lines(
                        "I've been studying the ancient artifacts of Kuudra.",
                        "I found this artifact that had some weird writing on it.",
                        "It seems to be missing some information so I can't figure out what it means.",
                        "There has to be information somewhere that can fill in the blanks.",
                        "If you want to take a look here is what it says.",
                        "∫ 2x - 2x^5 + 7x^3 dx"
                ).build(),
                DialogueSet.builder().key("relic").lines(
                        "Is that a Kuudra relic?",
                        "Where did you find it?",
                        "Whatever, it doesn't matter.",
                        "Since you brought this to me, I'll give you something to show my appreciation."
                ).build(),
                DialogueSet.builder().key("after").lines(
                        "Thanks for showing me the Kuudra relic!",
                        "...",
                        "You never did tell me how you found it..."
                ).build(),
                DialogueSet.builder().key("plaque").lines(
                        "Ah yes the <6>Exquisite Kuudra Teeth Plaque<f>!",
                        "I'll go ahead and help break that down for you!",
                        "You know... this item can be very helpful to unlock something special!"
                ).build()
        };
    }
}
