package net.swofty.type.crystalhollows.npcs;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public class NPCKingYolkar extends AbstractStructureNPC {
    public NPCKingYolkar() {
        super("King Yolkar", "<6>", DEFAULT_TEXTURE, "king_yolkars_tower", "king_yolkar");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        UUID id = player.getUuid();
        if (CrystalHollowsProgress.hasKingsScent(id)) {
            setDialogue(player, "scented");
            return;
        }
        ItemType egg = heldEgg(player);
        if (egg == null) {
            boolean met = !CrystalHollowsProgress.mark(id, CrystalHollowsProgress.MET_KING_YOLKAR);
            setDialogue(player, met ? "need-egg" : "intro");
            return;
        }
        player.removeItemFromPlayer(egg, 1);
        CrystalHollowsProgress.startKingsScent(id);
        setDialogue(player, "accepted");
    }

    private static ItemType heldEgg(SkyBlockPlayer player) {
        for (ItemType egg : CrystalHollowsProgress.GOBLIN_EGGS) {
            if (player.getAmountAnywhere(egg) > 0) return egg;
        }
        return null;
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "I am King Yolkar, and I am hungry! The Goblin Queen cast me aside.",
                        "She loves her Goblin Eggs more than anything. I have a plan to teach her a lesson."
                ).build(),
                DialogueSet.builder().key("need-egg").lines(
                        "Bring me a Goblin Egg, any colour, and I will help you get into the Queen's den."
                ).build(),
                DialogueSet.builder().key("accepted").lines(
                        "A Goblin Egg! Thank you, thank you!",
                        "Now I will cover you in my scent. Reach the Goblin Queen's den before it fades, and avoid the water!"
                ).build(),
                DialogueSet.builder().key("scented").lines(
                        "Go! Reach the Queen's den before my scent fades, and stay out of the water!"
                ).build()
        };
    }
}
