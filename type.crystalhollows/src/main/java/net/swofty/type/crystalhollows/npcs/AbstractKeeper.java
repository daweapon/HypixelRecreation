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
            setDialogue(player, CrystalNucleus.claim(player, NucleusCrystal.JADE) ? "complete" : "again");
            return;
        }
        if (!CrystalHollowsProgress.turnedIn(id, keeper)) {
            ItemType scavenged = CrystalHollowsProgress.scavengedItem(keeper);
            if (scavenged == null || player.getAmountAnywhere(scavenged) <= 0) {
                setDialogue(player, "missing");
                return;
            }
            player.removeItemFromPlayer(scavenged, 1);
            CrystalHollowsProgress.turnIn(id, keeper);
        }
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
        return new DialogueSet[]{
                DialogueSet.builder().key("detector").lines(
                        "Take this Metal Detector to scavenge the ground for lost items!",
                        "Bring me the Scavenged " + keeper.scavengedName + " if you find it. The other Keepers are waiting on theirs too."
                ).build(),
                DialogueSet.builder().key("missing").lines(
                        "I am the Keeper of " + keeper.displayName + ". I am looking for the Scavenged " + keeper.scavengedName + ".",
                        "Use the Metal Detector to dig it up!"
                ).build(),
                DialogueSet.builder().key("thanks").lines(
                        "You found the Scavenged " + keeper.scavengedName + "! Thank you.",
                        "Bring the rest to the other Keepers."
                ).build(),
                DialogueSet.builder().key("complete").lines(
                        "You found all of the items! Behold... the Jade Crystal!"
                ).build(),
                DialogueSet.builder().key("again").lines(
                        "Come back when you need to collect it again."
                ).build()
        };
    }
}
