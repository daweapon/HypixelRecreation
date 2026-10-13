package net.swofty.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCEmissarySisko extends AbstractHollowsNPC {
    public NPCEmissarySisko() {
        super("Emissary Sisko", "<6>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhlYmY5Y2FjNWFiNDc5MDNlMWQyZDRjMjRmNjg1YTY2N2YyMDVmOWE2MTc3ZjZhNzYyMWY4NDcxOWU0ODc0NjUifX19", new Pos(495.5, 106, 556.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        if (isInDialogue(player)) return;
        setDialogue(player, "commissions");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{DialogueSet.builder().key("commissions").lines(
                "The King sent me to keep the Crystal Hollows commissions running at the Nucleus.",
                "Every commission you finish down here earns you extra Heart of the Mountain experience.",
                "The commission board is not ready yet. Check back with me soon!"
        ).build()};
    }
}
