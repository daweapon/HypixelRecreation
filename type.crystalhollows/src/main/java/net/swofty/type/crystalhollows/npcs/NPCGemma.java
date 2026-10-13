package net.swofty.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.gui.inventories.essence.GUIEssenceShop;

public class NPCGemma extends AbstractHollowsNPC {
    private static final String MET = "met_gemma";

    public NPCGemma() {
        super("Gemma", "<b>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhlNDIwMTVkMGJmYTg1NWY5OWQwOWFmMDZmMzI3OTBkOWUzOTM4Mzk2MTc2NmQ4ZTJjOGYxNjA0NTg1MjQ1YjkifX19", new Pos(475.5, 106, 513.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        if (isInDialogue(player)) return;
        if (!hasMet(skyBlock(player), MET)) {
            setDialogue(player, "hello").thenRun(() -> markMet(skyBlock(player), MET));
            return;
        }
        player.openView(new GUIEssenceShop(EssenceType.DIAMOND));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{DialogueSet.builder().key("hello").lines(
                "Welcome to the Nucleus! Diamond Essence is the currency down here.",
                "Bring me some and I'll trade it for permanent upgrades!"
        ).build()};
    }
}
