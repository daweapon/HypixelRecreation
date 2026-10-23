package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.AnimalConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.technoblade.ShrineDialogue;
import dev.daweapon.type.skyblockgeneric.technoblade.TechnobladeShrine;
import dev.daweapon.type.skyblockgeneric.technoblade.TechnoshopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCTechnoshopPig extends HypixelNPC {
    public NPCTechnoshopPig() {
        super(new AnimalConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<d>Technoshop", "<e><l>CLICK"};
            }

            @Override
            public EntityType entityType() {
                return EntityType.PIG;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return TechnobladeShrine.config().technoshopPig();
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return TechnobladeShrine.technoshopOpen();
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        ShrineDialogue.technoshopPig().forEach(player::sendMessage);
        TechnoshopView.open(player);
    }
}
