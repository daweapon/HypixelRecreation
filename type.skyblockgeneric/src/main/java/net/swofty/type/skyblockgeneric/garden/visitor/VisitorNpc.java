package net.swofty.type.skyblockgeneric.garden.visitor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.npc.GardenGround;
import net.swofty.type.skyblockgeneric.garden.npc.GardenNpcSkin;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIVisitor;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;

public class VisitorNpc extends HypixelNPC {
    private final VisitorCatalogue.Visitor visitor;

    public VisitorNpc(VisitorCatalogue.Visitor visitor) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{visitor.coloured(), "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return GardenNpcSkin.SIGNATURE;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return GardenNpcSkin.TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                int slot = VisitorSlots.slotOf(player.getUuid(), visitor.id());
                Pos wanted = VisitorSlots.POSITIONS[Math.max(0, slot) % VisitorSlots.POSITIONS.length];
                return GardenGround.onGround(player.getInstance(), wanted);
            }

            @Override
            public Instance instance(HypixelPlayer player) {
                return player.getInstance();
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return VisitorSlots.slotOf(player.getUuid(), visitor.id()) >= 0
                        && player.getInstance() != null
                        && player.getInstance() == SkyBlockGarden.of((SkyBlockPlayer) player).getGardenInstance();
            }

            @Override
            public boolean dynamicPosition() {
                return true;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return visitor.coloured();
            }
        });
        this.visitor = visitor;
    }

    @Override
    public String getName() {
        return visitor.name();
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (!visitor.offerLines().isEmpty()) {
            sendNPCMessage(player, VisitorText.fill(visitor.offerLines().getFirst(), player));
        }
        player.openView(new GUIVisitor(visitor.id()));
    }
}
