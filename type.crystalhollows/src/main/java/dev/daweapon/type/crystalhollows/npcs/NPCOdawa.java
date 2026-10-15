package dev.daweapon.type.crystalhollows.npcs;

import dev.daweapon.type.crystalhollows.gui.GUIShopOdawa;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
                "Welcome traveler to our village!",
                "We have much to offer and explore - but be careful.",
                "The Kalhuiki tribe are a friendly people, but protective.",
                "Do not attack our young, else we will put you down."
        ).build()};
    }
}
