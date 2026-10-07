package net.swofty.type.hub.npcs;

import net.swofty.type.generic.data.datapoints.DatapointToggles.Toggles.ToggleType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.user.HypixelPlayer;

public final class FirstInteraction {
    private FirstInteraction() {
    }

    public static void then(HypixelNPC npc, HypixelPlayer player, ToggleType spokenToggle, String dialogueKey, Runnable action) {
        if (npc.isInDialogue(player)) return;
        if (player.getToggles().get(spokenToggle)) {
            action.run();
            return;
        }
        npc.setDialogue(player, dialogueKey).thenRun(() -> {
            player.getToggles().set(spokenToggle, true);
            action.run();
        });
    }
}
