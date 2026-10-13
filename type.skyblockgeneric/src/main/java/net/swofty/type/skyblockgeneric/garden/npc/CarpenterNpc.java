package net.swofty.type.skyblockgeneric.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseLayout;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUICarpenter;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;

import java.util.List;

public class CarpenterNpc extends HypixelNPC {
    private final int index;

    public CarpenterNpc(int index) {
        super(configuration(index));
        this.index = index;
    }

    private static Integer plot(SkyBlockPlayer player, int index) {
        List<Integer> plots = Gardens.profile(player).greenhouse().plots();
        return index < plots.size() ? plots.get(index) : null;
    }

    private static HumanConfiguration configuration(int index) {
        return new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Carpenter", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return GardenNpcSkins.of("CARPENTER").signature();
            }

            @Override
            public String texture(HypixelPlayer player) {
                return GardenNpcSkins.of("CARPENTER").texture();
            }

            @Override
            public Pos position(HypixelPlayer player) {
                Integer plot = plot((SkyBlockPlayer) player, index);
                return plot == null ? new Pos(0, 100, 0) : GreenhouseLayout.carpenter(plot);
            }

            @Override
            public Instance instance(HypixelPlayer player) {
                return player.getInstance();
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                return player.getInstance() != null
                        && player.getInstance() == SkyBlockGarden.of(skyBlockPlayer).getGardenInstance()
                        && plot(skyBlockPlayer, index) != null;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<b>Carpenter";
            }
        };
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        Integer plot = plot(player, index);
        if (plot != null) player.openView(new GUICarpenter(plot));
    }
}
