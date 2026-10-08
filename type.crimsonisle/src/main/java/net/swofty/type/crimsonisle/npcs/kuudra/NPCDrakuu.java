package net.swofty.type.crimsonisle.npcs.kuudra;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.FollowerStep;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import net.swofty.type.skyblockgeneric.crimson.kuudra.npc.KuudraFollowerNPC;
import net.swofty.type.skyblockgeneric.gui.inventories.kuudra.GUIConfirmDrakuuTrade;
import net.swofty.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Drakuu, high in The Wasteland, only deals with players in full Kuudra Follower Armor: during a
 * Cataclysmic eruption he sells the Follower Artifact, and he swaps a Golden Ball for the
 * Exquisite Kuudra Teeth Plaque.
 */
public class NPCDrakuu extends KuudraFollowerNPC {

    public NPCDrakuu() {
        super("<c>Drakuu", "ewogICJ0aW1lc3RhbXAiIDogMTY0MjM1NDA1MDAwNywKICAicHJvZmlsZUlkIiA6ICIwMGZiNTRiOWI4NDA0YTA0YTViMmJhMzBlYzBlYTAxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJrbGxveWQ3MCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMGNkNjI2YmFmMTYzY2ZkNGZlNzY4ZjQ3NTNmODBkNTBkZDExOTc3MDBlNGMzYTJkNTg1MGNmNzliZWIwOTQ3IgogICAgfQogIH0KfQ==",
                new Pos(-324.5, 191, -1015.5));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!KuudraFollowerRules.wearingFollowerArmor(player)) {
            setDialogue(player, "stranger_" + ThreadLocalRandom.current().nextInt(3));
            return;
        }
        if (!KuudraFollowerProgress.has(player, FollowerStep.PLAQUE_RECEIVED) && player.countItem(ItemType.GOLDEN_BALL) > 0) {
            if (KuudraFollowerRules.holding(player, "GOLDEN_BALL")) {
                setDialogue(player, "golden_ball").thenRun(() -> player.openView(new GUIConfirmDrakuuTrade(this::trade)));
            } else {
                setDialogue(player, "inventory");
            }
            return;
        }
        if (!KuudraFollowerRules.isCataclysmicEruption()) {
            setDialogue(player, "condition");
            return;
        }
        setDialogue(player, "shop").thenRun(() -> player.openView(new CatalogueShopView("drakuu")));
    }

    private void trade(SkyBlockPlayer player) {
        if (KuudraFollowerProgress.has(player, FollowerStep.PLAQUE_RECEIVED)
                || !player.removeItemFromPlayer(ItemType.GOLDEN_BALL, 1)) return;
        KuudraFollowerProgress.complete(player, FollowerStep.PLAQUE_RECEIVED);
        player.addAndUpdateItem(ItemType.KUUDRA_CAVITY);
        setDialogue(player, "traded");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("stranger_0").lines("Do not talk to me, we are not the same.").build(),
                DialogueSet.builder().key("stranger_1").lines(
                        "I have seen many adventurers challenge Kuudra, but they don't understand what they're doing.").build(),
                DialogueSet.builder().key("stranger_2").lines(
                        "There are many ways to find Kuudra, you just have to think.").build(),
                DialogueSet.builder().key("condition").lines(
                        "You might be someone worth taking to, but the condition isn't right.").build(),
                DialogueSet.builder().key("shop").lines(
                        "You seem like someone of similar interests.",
                        "I have a shop of some unique items that may interest you.",
                        "Have a look and see if theres anything you like."
                ).build(),
                DialogueSet.builder().key("inventory").lines(
                        "Adventurer!",
                        "I see you have something in your inventory...",
                        "I feel as though I've seen it before..."
                ).build(),
                DialogueSet.builder().key("golden_ball").lines(
                        "Ah yes, I can see it clearer in your hand.",
                        "I definitely seem to recognize this item!",
                        "Do you mind if you give it to me so I can inspect it?",
                        "Don't worry! I'll give you something in return of course!"
                ).build(),
                DialogueSet.builder().key("traded").lines(
                        "You should find this item I give you of value.",
                        "It may seem uninteresting, but I'm sure you can find a way to use it for good."
                ).build()
        };
    }
}
