package dev.daweapon.type.skyblockgeneric.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseLayout;
import dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUICropAnalyzer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.garden.SkyBlockGarden;

import java.util.List;

public class JakeNpc extends HypixelNPC {
    private final int index;

    public JakeNpc(int index) {
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
                return new String[]{"Jake", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return GardenNpcSkins.of("JAKE").signature();
            }

            @Override
            public String texture(HypixelPlayer player) {
                return GardenNpcSkins.of("JAKE").texture();
            }

            @Override
            public Pos position(HypixelPlayer player) {
                Integer plot = plot((SkyBlockPlayer) player, index);
                return plot == null ? new Pos(0, 100, 0) : GreenhouseLayout.jake(plot);
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
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<b>Jake";
            }
        };
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        dev.daweapon.type.skyblockgeneric.farming.lab.CropAnalyzerAccess.open((SkyBlockPlayer) e.player());
    }
}
