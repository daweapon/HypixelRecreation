package net.swofty.type.deepcaverns.npcs;

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

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.Map;

/**
 * The Kuudra Loremaster's past self on the old Blazing Fortress across the Deep Caverns void. He is
 * there all of Tuesday (UTC-5) for players who quoted a Kuudra line to him in Scarleton, and trades
 * the Follower Boots for 1,024 Enchanted Magma Cream and Enchanted Netherrack plus 1M of each
 * collection. Whether the stacks are taken is not documented; they are taken here.
 */
public class NPCKuudraLoremasterPast extends KuudraFollowerNPC {
    private static final int STACK = 1_024;
    private static final int COLLECTION = 1_000_000;

    public NPCKuudraLoremasterPast() {
        super("<5>Kuudra Loremaster", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MwY2Q2MjZiYWYxNjNjZmQ0ZmU3NjhmNDc1M2Y4MGQ1MGRkMTE5NzcwMGU0YzNhMmQ1ODUwY2Y3OWJlYjA5NDcifX19",
                new Pos(-256.5, 146, 120.5), NPCKuudraLoremasterPast::isHere);
    }

    private static boolean isHere(SkyBlockPlayer player) {
        return Instant.now().atOffset(KuudraFollowerRules.QUEST_ZONE).getDayOfWeek() == DayOfWeek.TUESDAY
                && KuudraFollowerProgress.has(player, FollowerStep.LOREMASTER_TRIGGER)
                && !KuudraFollowerProgress.has(player, FollowerStep.BOOTS_CLAIMED);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player) || !isHere(player)) return;
        if (!hasStacks(player)) {
            setDialogue(player, "dejavu");
            return;
        }
        if (player.getCollection().get(ItemType.NETHERRACK) < COLLECTION
                || player.getCollection().get(ItemType.MAGMA_CREAM) < COLLECTION) {
            setDialogue(player, "not_collected");
            return;
        }
        setDialogue(player, "gift").thenRun(() -> player.openView(
                new GUIKuudraClaimReward(ItemType.KUUDRA_FOLLOWER_BOOTS, NPCKuudraLoremasterPast::claimBoots)));
    }

    private static boolean hasStacks(SkyBlockPlayer player) {
        return player.countItem(ItemType.ENCHANTED_MAGMA_CREAM) >= STACK
                && player.countItem(ItemType.ENCHANTED_NETHERRACK) >= STACK;
    }

    private static void claimBoots(SkyBlockPlayer player) {
        if (!isHere(player) || !hasStacks(player)) return;
        player.removeItemsFromPlayer(Map.of(ItemType.ENCHANTED_MAGMA_CREAM, STACK, ItemType.ENCHANTED_NETHERRACK, STACK));
        KuudraFollowerProgress.complete(player, FollowerStep.BOOTS_CLAIMED);
        player.addAndUpdateItem(ItemType.KUUDRA_FOLLOWER_BOOTS);
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("dejavu").lines("Have I met you before? I am getting some dejavu.").build(),
                DialogueSet.builder().key("not_collected").lines(
                        "Wow that's a lot of resources!",
                        "Too bad you didn't put in the effort to collect them yourselves."
                ).build(),
                DialogueSet.builder().key("gift").lines(
                        "Wow how did you know I liked these items!",
                        "I'm lucky to have met you.",
                        "Here, take this to commemorate our meeting!"
                ).build()
        };
    }
}
