package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.AnimalConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.gui.GUIShopAlchemist;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCAlchemist extends HypixelNPC {
    public NPCAlchemist() {
        super(new AnimalConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Alchemist", "<e><l>CLICK"};
            }

            @Override
            public EntityType entityType() {
                return EntityType.WITCH;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(80.5, 72, -90.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_ALCHEMIST);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_ALCHEMIST, true);
            });
            return;
        }

        player.openView(new GUIShopAlchemist());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
            DialogueSet.ofTranslation("hello", "npcs_hub.alchemist.dialogue.hello", player.getUsername())
        };
    }
}
