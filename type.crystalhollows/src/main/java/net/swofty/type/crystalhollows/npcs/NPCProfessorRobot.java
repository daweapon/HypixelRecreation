package net.swofty.type.crystalhollows.npcs;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public class NPCProfessorRobot extends AbstractStructureNPC {
    private static final String MET = "met_professor_robot";

    public NPCProfessorRobot() {
        super("Professor Robot", "<e>", DEFAULT_TEXTURE, "lost_precursor_city", "professor_robot");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        UUID id = player.getUuid();
        if (CrystalHollowsProgress.has(id, CrystalHollowsProgress.SAPPHIRE_STORAGE_OPEN)) {
            giveCrystal(player);
            return;
        }
        if (!hasMet(player, MET)) {
            setDialogue(player, "intro").thenRun(() -> markMet(player, MET));
            return;
        }
        if (player.getAmountAnywhere(ItemType.PRECURSOR_APPARATUS) > 0) {
            player.removeItemFromPlayer(ItemType.PRECURSOR_APPARATUS, 1);
            openStorage(player, "apparatus");
            return;
        }
        submitParts(player);
    }

    private void submitParts(SkyBlockPlayer player) {
        UUID id = player.getUuid();
        int needed = CrystalHollowsProgress.AUTOMATON_PARTS.size();
        boolean held = false;
        for (ItemType part : CrystalHollowsProgress.AUTOMATON_PARTS) {
            if (player.getAmountAnywhere(part) <= 0) continue;
            held = true;
            if (CrystalHollowsProgress.partSubmitted(id, part)) {
                sendNPCMessage(player, "You have already delivered that part to me! Bring me a unique robot part!");
                continue;
            }
            player.removeItemFromPlayer(part, 1);
            CrystalHollowsProgress.submitPart(id, part);
            int total = CrystalHollowsProgress.partsSubmitted(id);
            if (total >= needed) break;
            sendNPCMessage(player, "Thanks for bringing me the <9>" + part.getDisplayName() + "<f>! Bring me " + (needed - total) + " more components to fix the giant!");
        }
        if (CrystalHollowsProgress.partsSubmitted(id) >= needed) {
            openStorage(player, "open");
            return;
        }
        if (!held) {
            sendNPCMessage(player, "That's not one of the components I need! Bring me one of the missing components:\n" + missingParts(id));
        }
    }

    private static String missingParts(UUID id) {
        StringBuilder missing = new StringBuilder();
        for (ItemType part : CrystalHollowsProgress.AUTOMATON_PARTS) {
            if (CrystalHollowsProgress.partSubmitted(id, part)) continue;
            if (!missing.isEmpty()) missing.append('\n');
            missing.append("<9>").append(part.getDisplayName());
        }
        return missing.toString();
    }

    private void openStorage(SkyBlockPlayer player, String dialogue) {
        CrystalHollowsProgress.mark(player.getUuid(), CrystalHollowsProgress.SAPPHIRE_STORAGE_OPEN);
        setDialogue(player, dialogue).thenRun(() -> giveCrystal(player));
    }

    private void giveCrystal(SkyBlockPlayer player) {
        CrystalHollowsProgress.mark(player.getUuid(), CrystalHollowsProgress.SAPPHIRE_CRYSTAL_CLAIMED);
        if (CrystalNucleus.claim(player, NucleusCrystal.SAPPHIRE)) return;
        if (CrystalNucleus.state(player, NucleusCrystal.SAPPHIRE) == CrystalNucleus.State.NOT_PLACED) {
            setDialogue(player, "unplaced");
            return;
        }
        sendNPCMessage(player, "Don't forget to place the Sapphire Crystal at the Crystal Nucleus.");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "This citadel used to be the epicenter of fructuous engineering.",
                        "The inhabitants, <b>Precursors<f>, as they were once called, laid the foundations for the future.",
                        "However, they have been extinct for eons - only leaving the remnants of the <c>Automatons<f> behind.",
                        "One rather peculiar discovery is the <b>Automaton Prime<f> - the giant robot towering above me. The exact purpose of it is unknown, although it appears to be guarding something.",
                        "There seems to be <a>6<f> key components to the giant that are missing.",
                        "Each part - the <9>Electron Transmitter<f>, <9>FTX 3070<f>, <9>Robotron Reflector<f>, <9>Superlite Motor<f>, <9>Control Switch<f>, and a <9>Synthetic Heart<f> plays a distinctive role in the creation of this behemoth.",
                        "I think the Automatons nearby are built with similar components. Find them and bring them to me!"
                ).build(),
                DialogueSet.builder().key("open").lines(
                        "You've brought me all of the components!",
                        "I have uploaded them to the giant below...it appears it has opened its storage compartment!",
                        "Perhaps the long-lost <b>Sapphire Crystal<f> is in there!"
                ).build(),
                DialogueSet.builder().key("apparatus").lines(
                        "That's not one of the compon-",
                        "Wait a minute. This will work just fine.",
                        "I have uploaded this to the giant below...it appears it has opened its storage compartment!",
                        "Perhaps the long-lost <b>Sapphire Crystal<f> is in there!"
                ).build(),
                DialogueSet.builder().key("unplaced").lines(
                        "You haven't placed the <b>Sapphire Crystal<f> yet!",
                        "Go to the <5>Crystal Nucleus<f> to place it!"
                ).build()
        };
    }
}
