package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.AnimalConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class NPCShania extends HypixelNPC {
    public NPCShania() {
        super(new AnimalConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Shania", "<e><l>CLICK"};
            }

            @Override
            public EntityType entityType() {
                return EntityType.COW;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(59.5, 72, -144.5);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        sendNPCMessage(e.player(), "Moooooo!");
    }
}