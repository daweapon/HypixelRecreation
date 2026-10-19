package dev.daweapon.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class NPCGuber extends AbstractEndNPC {
    public NPCGuber() {
        super(null, "Guber", "<9>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhmY2M1M2E4NjBjNGQ2MjFiYjc1ZmM4NmI5N2IxMmNiM2Q2ZDk3N2Y4ZWI5ODNlNzQ1OGY1ZTI2MWIzZDEyNzkifX19",
                new Pos(-494.5, 121, -241.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (isInDialogue(event.player())) return;
        setDialogue(event.player(), "race");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("race").lines(
                        "Hey you! Over here! Come talk to me! I'll teach you about racing!",
                        "<c>The End Race isn't open yet. Check back soon!"
                ).build()
        };
    }
}
