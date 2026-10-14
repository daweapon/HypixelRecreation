package net.swofty.type.crystalhollows.npcs;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress.Keeper;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;
import java.util.UUID;

public abstract class AbstractKeeper extends AbstractStructureNPC {
    private final Keeper keeper;

    protected AbstractKeeper(Keeper keeper) {
        super("Keeper of " + keeper.displayName, keeper.color, DEFAULT_TEXTURE, "mines_of_divan",
                "keeper_" + keeper.name().toLowerCase(Locale.ROOT));
        this.keeper = keeper;
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        UUID id = player.getUuid();
        if (!CrystalHollowsProgress.has(id, CrystalHollowsProgress.METAL_DETECTOR_GIVEN)) {
            CrystalHollowsProgress.mark(id, CrystalHollowsProgress.METAL_DETECTOR_GIVEN);
            if (player.getAmountAnywhere(ItemType.DWARVEN_METAL_DETECTOR) == 0) {
                player.addAndUpdateItem(ItemType.DWARVEN_METAL_DETECTOR);
            }
            setDialogue(player, "detector");
            return;
        }
        if (CrystalHollowsProgress.has(id, CrystalHollowsProgress.JADE_CRYSTAL_CLAIMED)) {
            if (CrystalNucleus.claim(player, NucleusCrystal.JADE)) {
                setDialogue(player, "complete");
                return;
            }
            setDialogue(player, CrystalNucleus.state(player, NucleusCrystal.JADE) == CrystalNucleus.State.NOT_PLACED ? "unplaced" : "placed");
            return;
        }
        if (CrystalHollowsProgress.turnedIn(id, keeper)) {
            setDialogue(player, "restored");
            return;
        }
        ItemType scavenged = CrystalHollowsProgress.scavengedItem(keeper);
        if (scavenged == null || player.getAmountAnywhere(scavenged) <= 0) {
            setDialogue(player, "missing");
            return;
        }
        player.removeItemFromPlayer(scavenged, 1);
        CrystalHollowsProgress.turnIn(id, keeper);
        if (CrystalHollowsProgress.allKeepersSatisfied(id)) {
            CrystalHollowsProgress.mark(id, CrystalHollowsProgress.JADE_CRYSTAL_CLAIMED);
            CrystalNucleus.claim(player, NucleusCrystal.JADE);
            setDialogue(player, "complete");
            return;
        }
        setDialogue(player, "thanks");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        String item = "<c>Scavenged " + keeper.scavengedName;
        return new DialogueSet[]{
                DialogueSet.builder().key("detector").lines(
                        "Take this <c>Metal Detector<f> to scavenge the ground for lost items!",
                        "Return the items to their rightful owners to gain a reward!"
                ).build(),
                DialogueSet.builder().key("missing").lines(
                        "Talk to me when you have found a " + item + "<f>."
                ).build(),
                DialogueSet.builder().key("thanks").lines(
                        "Excellent! You have returned the " + item + " <f>to it's rightful place!",
                        "Use your <c>Metal Detector<f> to find the remaining items!"
                ).build(),
                DialogueSet.builder().key("restored").lines(
                        "You have already restored this Dwarf's " + item + "<f>!",
                        "Use your <c>Metal Detector<f> to find the rest of the items!"
                ).build(),
                DialogueSet.builder().key("complete").lines(
                        "You found all of the items! Behold... the <a>Jade Crystal<f>!"
                ).build(),
                DialogueSet.builder().key("unplaced").lines(
                        "You haven't placed the <a>Jade Crystal <f>yet!",
                        "Go to the <5>Crystal Nucleus <f>to place it!"
                ).build(),
                DialogueSet.builder().key("placed").lines(
                        "You have already placed the <a>Jade Crystal<f>!",
                        "Come back when you need to collect it again!"
                ).build()
        };
    }
}
