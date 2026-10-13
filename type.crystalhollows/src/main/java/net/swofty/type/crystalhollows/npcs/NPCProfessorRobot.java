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
        super("Professor Robot", "<b>", DEFAULT_TEXTURE, "lost_precursor_city", "professor_robot");
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
            openStorage(player);
            return;
        }
        submitParts(player);
    }

    private void submitParts(SkyBlockPlayer player) {
        UUID id = player.getUuid();
        boolean duplicate = false;
        boolean submitted = false;
        for (ItemType part : CrystalHollowsProgress.AUTOMATON_PARTS) {
            if (player.getAmountAnywhere(part) <= 0) continue;
            if (CrystalHollowsProgress.partSubmitted(id, part)) {
                duplicate = true;
                continue;
            }
            player.removeItemFromPlayer(part, 1);
            CrystalHollowsProgress.submitPart(id, part);
            submitted = true;
        }
        int total = CrystalHollowsProgress.partsSubmitted(id);
        int needed = CrystalHollowsProgress.AUTOMATON_PARTS.size();
        if (total >= needed) {
            openStorage(player);
            return;
        }
        if (submitted) {
            sendNPCMessage(player, "<f>Thank you! I need " + (needed - total) + " more.");
        } else if (duplicate) {
            sendNPCMessage(player, "<f>I already have that one. I need a unique part!");
        } else {
            sendNPCMessage(player, "<f>I still need the " + missingParts(id) + ". Bring me any I'm missing, or a Precursor Apparatus!");
        }
    }

    private static String missingParts(UUID id) {
        StringBuilder missing = new StringBuilder();
        for (ItemType part : CrystalHollowsProgress.AUTOMATON_PARTS) {
            if (CrystalHollowsProgress.partSubmitted(id, part)) continue;
            if (!missing.isEmpty()) missing.append(", ");
            missing.append(part.getDisplayName());
        }
        return missing.toString();
    }

    private void openStorage(SkyBlockPlayer player) {
        CrystalHollowsProgress.mark(player.getUuid(), CrystalHollowsProgress.SAPPHIRE_STORAGE_OPEN);
        setDialogue(player, "open").thenRun(() -> giveCrystal(player));
    }

    private void giveCrystal(SkyBlockPlayer player) {
        UUID id = player.getUuid();
        CrystalHollowsProgress.mark(id, CrystalHollowsProgress.SAPPHIRE_CRYSTAL_CLAIMED);
        if (CrystalNucleus.claim(player, NucleusCrystal.SAPPHIRE)) {
            sendNPCMessage(player, "<f>Take the Sapphire Crystal, and place it at the Crystal Nucleus.");
            return;
        }
        sendNPCMessage(player, "<f>Don't forget to place the Sapphire Crystal at the Crystal Nucleus.");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "Ah, a visitor to the Lost Precursor City! This citadel was built by the Precursors long ago.",
                        "Its guardian, the Automaton Prime, is missing six components: an Electron Transmitter, an FTX 3070, a Robotron Reflector, a Superlite Motor, a Control Switch and a Synthetic Heart.",
                        "Bring me each of them, or a Precursor Apparatus that combines all six, and I can open the storage vault."
                ).build(),
                DialogueSet.builder().key("open").lines(
                        "That's every component! The vault is opening...",
                        "Perhaps the long-lost Sapphire Crystal is in there!"
                ).build()
        };
    }
}
