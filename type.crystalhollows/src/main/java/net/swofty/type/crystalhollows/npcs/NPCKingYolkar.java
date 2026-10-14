package net.swofty.type.crystalhollows.npcs;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
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
            if (met && CrystalHollowsAccess.data(player).getNucleusRuns() > 0) {
                setDialogue(player, "again");
                return;
            }
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
        String user = player.getUsername();
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "*rumble* *rumble*",
                        "Did ya hear that, " + user + "? That's the sound of an empty stomach.",
                        "I've been so hungry since the Goblin Queen tossed me aside.",
                        "Bring me back a Goblin Egg of any type and we can teach her a lesson!"
                ).build(),
                DialogueSet.builder().key("need-egg").lines(
                        "Bring me any type of Goblin Egg and we can show the Goblin Queen what it's like to lose something she loves?!",
                        "Dark, right?!"
                ).build(),
                DialogueSet.builder().key("accepted").lines(
                        "Well done, " + user + "!",
                        "These eggs will help me stomach my pain.",
                        "Now let's hit the Queen where it hurts - by stealing her <6>Amber Crystal<f>!",
                        "I'm covering you in my <a>foul stench <r>as we speak. It won't last long before it dissipates!",
                        "Get to the Queen before my stench goes away and you'll be able to sneak past her imbecile Guards!"
                ).build(),
                DialogueSet.builder().key("scented").lines(
                        "You must hurry! Get to the Goblin Queen before my stench escapes you!",
                        "And stay away from water!"
                ).build(),
                DialogueSet.builder().key("again").lines(
                        "You need my <a>foul stench <f>again? Bring me a <9>Goblin Egg <f>of any type!"
                ).build()
        };
    }
}
