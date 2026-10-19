package dev.daweapon.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCTyzzo extends AbstractEndNPC {
    public NPCTyzzo() {
        super("<d><l>DRAGON ESSENCE SHOP", "Tyzzo", "<9>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYzYzRlZGQxYzI3ZGIzMTYyZTkyZjg3MDgxMGZjZTM2NDZkY2JmOGVmNWViYjQ2ZjJkZGFkNWQ4ODQyNWUyZWUifX19",
                new Pos(-596.5, 5, -271.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        setDialogue(player, "hello").thenRun(() -> player.openView(new dev.daweapon.type.theend.gui.GUITyzzo()));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "The <5>Void Slate <f>hums when a dragon falls. I am <9>Tyzzo<f>.",
                        "Bring me <d>Dragon Essence <f>and I will trade you power drawn from the dragons themselves."
                ).build()
        };
    }
}
