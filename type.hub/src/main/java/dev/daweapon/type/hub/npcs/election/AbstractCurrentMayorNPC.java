package dev.daweapon.type.hub.npcs.election;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.entity.npc.configuration.NPCConfiguration;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.gui.inventories.election.MayorMenuView;

public abstract class AbstractCurrentMayorNPC extends HypixelNPC {
    private final NPCConfiguration humanConfig;
    private final NPCConfiguration villagerConfig;

    protected AbstractCurrentMayorNPC(Pos mayorPosition) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                SkyBlockMayor mayor = ElectionManager.getCurrentMayor();
                if (mayor == null) return new String[]{
                    "<key:'npcs_hub.election.mayor_unknown'>",
                    "<key:'npcs_hub.election.click'>"
                };
                return new String[]{
                    Text.of("Mayor {}", mayor.getDisplayName()).serialize(),
                    "<key:'npcs_hub.election.click'>"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                SkyBlockMayor mayor = ElectionManager.getCurrentMayor();
                return mayor != null ? mayor.getSignature() : "";
            }

            @Override
            public String texture(HypixelPlayer player) {
                SkyBlockMayor mayor = ElectionManager.getCurrentMayor();
                return mayor != null ? mayor.getTexture() : "";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return mayorPosition;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });

        this.humanConfig = super.getParameters();
        this.villagerConfig = new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Mayor Jerry", "<key:'npcs_hub.election.click'>"};
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.NITWIT;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return mayorPosition;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        };
    }

    @Override
    public NPCConfiguration getParameters() {
        SkyBlockMayor mayor = ElectionManager.getCurrentMayor();
        return mayor == SkyBlockMayor.JERRY ? villagerConfig : humanConfig;
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockMayor mayor = ElectionManager.getCurrentMayor();
        if (mayor == null) {
            event.player().sendMessage(Text.key("npcs_hub.election.hello"));
            return;
        }
        event.player().openView(new MayorMenuView());
    }
}
