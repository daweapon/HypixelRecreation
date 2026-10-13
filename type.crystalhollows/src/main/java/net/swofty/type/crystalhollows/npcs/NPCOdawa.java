package net.swofty.type.crystalhollows.npcs;

import net.swofty.type.crystalhollows.gui.GUIShopOdawa;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCOdawa extends AbstractStructureNPC {
    private static final String MET = "met_odawa";

    public NPCOdawa() {
        super("Odawa", "<2>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZiN2NhZjRiZDkzNjE4MWY2YmExMGIwYTZiZGExNDRhYWIwMzY0ZThjODZlZmNhZDYwN2ZlZjRiZDc3MDgxNyJ9fX0=", "odawa_hut", "odawa");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        if (!hasMet(player, MET)) {
            setDialogue(player, "hello").thenRun(() -> markMet(player, MET));
            return;
        }
        player.openView(new GUIShopOdawa());
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{DialogueSet.builder().key("hello").lines(
                "Outsider. The Kalhuiki tribe does not welcome visitors easily.",
                "Bring me Sludge Juice from the creatures of this jungle, and I will trade you what you need.",
                "A Jungle Key opens the temple door. A Kalhuiki Mask keeps the tribe from attacking you."
        ).build()};
    }
}
