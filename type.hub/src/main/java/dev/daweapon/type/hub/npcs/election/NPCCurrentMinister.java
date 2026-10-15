package dev.daweapon.type.hub.npcs.election;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.gui.inventories.election.MinisterMenuView;

public class NPCCurrentMinister extends HypixelNPC {

    public NPCCurrentMinister() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                SkyBlockMayor minister = ElectionManager.getCurrentMinister();
                if (minister == null) return new String[]{
                    "<key:'npcs_hub.election.minister_unknown'>",
                    "<key:'npcs_hub.election.click'>"
                };
                return new String[]{
                    Text.of("Minister {}", minister.getDisplayName()).serialize(),
                    "<key:'npcs_hub.election.click'>"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                SkyBlockMayor minister = ElectionManager.getCurrentMinister();
                return minister != null ? minister.getSignature() : "";
            }

            @Override
            public String texture(HypixelPlayer player) {
                SkyBlockMayor minister = ElectionManager.getCurrentMinister();
                return minister != null ? minister.getTexture() : "";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(8.5, 79, 17.5, 135, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockMayor minister = ElectionManager.getCurrentMinister();
        if (minister == null) {
            event.player().sendMessage(Text.key("npcs_hub.election.hello"));
            return;
        }
        event.player().openView(new MinisterMenuView());
    }
}
