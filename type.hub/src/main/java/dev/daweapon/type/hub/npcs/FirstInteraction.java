package dev.daweapon.type.hub.npcs;

import dev.daweapon.type.generic.data.datapoints.DatapointToggles.Toggles.ToggleType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.user.HypixelPlayer;

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
