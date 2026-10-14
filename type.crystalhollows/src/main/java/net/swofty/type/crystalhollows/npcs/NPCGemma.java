package net.swofty.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.gui.inventories.essence.GUIGemma;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCGemma extends AbstractHollowsNPC {
    private static final String MET = "met_gemma";
    private static final String WELCOMED = "gemma_welcomed";
    private static final long WELCOME_ESSENCE = 25;

    public NPCGemma() {
        super("<b><l>DIAMOND ESSENCE SHOP", "Gemma", "<b>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhlNDIwMTVkMGJmYTg1NWY5OWQwOWFmMDZmMzI3OTBkOWUzOTM4Mzk2MTc2NmQ4ZTJjOGYxNjA0NTg1MjQ1YjkifX19", new Pos(475.5, 106, 513.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        if (CrystalHollowsAccess.data(player).getNucleusRuns() == 0) {
            boolean met = hasMet(player, MET);
            setDialogue(player, met ? "locked" : "hello").thenRun(() -> markMet(player, MET));
            return;
        }
        if (!hasMet(player, WELCOMED)) {
            setDialogue(player, "unlocked").thenRun(() -> {
                markMet(player, MET);
                markMet(player, WELCOMED);
                Essences.add(player, EssenceType.DIAMOND, WELCOME_ESSENCE);
                player.openView(new GUIGemma());
            });
            return;
        }
        player.openView(new GUIGemma());
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Hello there! That's strange... <o>you don't look like a dwarf, <r>but you also don't look like a <c>Goblin<f>!",
                        "I've not seen a human down here since... well, nevermind.",
                        "The name's <b>Gemma<f>, and I run the <b>Diamond Essence Shop<f>, where you can trade in <b>Diamond Essence<f> for useful perks and upgrades!",
                        "Show me that you know your way around. Bring back all five Crystals found throughout the <5>Crystal Hollows<f> and complete the <d>Crystal Nucleus<f>, I'll let you use my shop.",
                        "If you're having trouble finding any of those Crystals, go take a look at the <5>Nucleus Vault<f> over there.",
                        "That'll tell you where each Crystal can be found in the <2>Dwarven Mines<f>."
                ).build(),
                DialogueSet.builder().key("locked").lines(
                        "Stuck? Go take a look at the <5>Nucleus Vault<f>.",
                        "That'll tell you where each Crystal can be found in the <5>Crystal Hollows<f>."
                ).build(),
                DialogueSet.builder().key("unlocked").lines(
                        "<a>Awesome! <f>You really completed the <d>Crystal Nucleus<f>?",
                        "I'll give you <b>25 Diamond Essence <f>to get you started- you can find more all around the Crystal Hollows!",
                        "Let me show you the <b>Diamond Essence Shop<f>!"
                ).build()
        };
    }
}
