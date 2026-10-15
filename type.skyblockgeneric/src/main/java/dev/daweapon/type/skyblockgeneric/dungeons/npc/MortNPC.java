package dev.daweapon.type.skyblockgeneric.dungeons.npc;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.GUICatacombsGate;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Mort, the Catacombs gate keeper: stands at the Hub's Catacombs entrance and in the Dungeon Hub, opening the same Gate. */
public abstract class MortNPC extends HypixelNPC {
    private static final String SIGNATURE = "VEyyNaHlOvqAGUk+r9tWnKp09JSL6jrCdphYgxbldPga6YCvWv8w8Lluebx6gX8R+TLRpg2EWpSUPRBgwEMKBolUxzV9gFQBxQD1IaUDdbVTucNCqp8tPnBEVazM9HOcje4XTwm47yucZbjnEQFndHwyPyFBORCd5vbO6JfzopR0ZD00A0lZn07JGwJz/2WTGjqM8CtP8Yi7RHykaJaso2xfYKcIBaLLE1iMm5G4ZIQZEtfgstCQ58/W9R+FBegqGfgDccwqXP/zOTDl110BE77cRTufeAjjCXZfmSjegF6ctbcA+SxJYgXpQdHlFaWO8fQhJmuauhCMBcKMzL3nP/EMlDFvnFTYlnQBTz7dUqolLqY8fX6jbr1F9eCH8Rb8CBrVsMx4kAX/G5QGKeyzoWRQtDJxAJuHp5U8Gw9c1zDW4Yapse1Gp/0Gj4r1XfWoPjEGDv7FQgdBRMlggBKxtrctbFvDCfHHExZh6y/PQfP6U9BdIy2TRbPCD2tb9r86mvYSmFd1PXV8POElLLaIJTvqqYNewe2pwgdVt4aj2JLBGrP7my8ditkR5q7bWHFTzl+8DbhF4ect5g6I3MhRjq/61InzNwky6lObsRYVHsDAj4FYc6eT5CLfLZnePzeM/RBTCf+K3Jx6eCF7XWgO1OZGr5W4y5/daWTZpKXz/yw=";
    private static final String TEXTURE = "eyJ0aW1lc3RhbXAiOjE1Nzg0MDk0MTMxNjksInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzliNTY4OTViOTY1OTg5NmFkNjQ3ZjU4NTk5MjM4YWY1MzJkNDZkYjljMWIwMzg5YjhiYmViNzA5OTlkYWIzM2QiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQ==";

    protected MortNPC(Pos position) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Mort", "<e><l>GATE KEEPER"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return SIGNATURE;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return position;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<b>Mort";
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        DatapointToggles.Toggles.ToggleType spoken = DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MORT;
        if (player.getToggles().get(spoken)) {
            openGate(player);
            return;
        }
        setDialogue(player, "first-interaction").thenRun(() -> {
            player.getToggles().set(spoken, true);
            openGate(player);
        });
    }

    private void openGate(SkyBlockPlayer player) {
        player.openView(new GUICatacombsGate(p -> setDialogue(p, "low-combat")));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.ofTranslation("first-interaction", "npcs_hub.mort.dialogue.first_interaction",
                player == null ? "" : player.getUsername()),
            DialogueSet.ofTranslation("low-combat", "npcs_hub.mort.dialogue.low_combat")
        };
    }
}
