package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.drill.DrillFuel;
import net.swofty.type.skyblockgeneric.gui.inventories.drill.GUIDrillAnvil;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCJotraelineGreatforge extends HypixelNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODUzNjJiNjFmMjk4ZGVjMmRmNzY1NDU3ZDZiNDE3ZTkxN2QyMzA5ZWJjOGY2OTFmMDg1NTQ4OTY4MmRkZWM1OSJ9fX0=";

    public NPCJotraelineGreatforge() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Jotraeline Greatforge", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<6>Jotraeline Greatforge";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-6.5, 145, -18.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!DrillFuel.hasDrill(player)) {
            setDialogue(player, "no-drill");
            return;
        }
        player.openView(new GUIDrillAnvil());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{DialogueSet.builder().key("no-drill").lines(
                "Sup? I am Jotraeline Greatforge, your one stop shop for refueling and upgrading Drills.",
                "You don't have a Drill on you! You can get one from the Forge."
        ).build()};
    }
}
