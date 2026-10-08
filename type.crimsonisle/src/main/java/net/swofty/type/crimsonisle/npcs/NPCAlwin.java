package net.swofty.type.crimsonisle.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.essence.GUICrimsonBlacksmith;

// Alwin, the Scarleton blacksmith: essence upgrades, salvaging and the Kuudra Armor Type Transfer.
public class NPCAlwin extends HypixelNPC {
    public NPCAlwin() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<5>Alwin", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q4ZWQ4YWVhMjRhNThkOWUyYzBlOGQ3NGQ3MDQ5MmZmOTA4NDkxNjMxNTE0NTMzZWM4MTRkNTFkZWI3N2I3YzcifX19";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<5>Alwin");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-82.5, 92, -734.5);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (isInDialogue(event.player())) return;
        event.player().openView(new GUICrimsonBlacksmith("Alwin"));
    }
}
