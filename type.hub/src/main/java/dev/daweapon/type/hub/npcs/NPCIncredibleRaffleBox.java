package dev.daweapon.type.hub.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.AnimalConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.century.raffle.CenturyEvent;
import dev.daweapon.type.skyblockgeneric.century.raffle.RaffleMenus;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;

public class NPCIncredibleRaffleBox extends HypixelNPC {
    public NPCIncredibleRaffleBox() {
        super(new AnimalConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Incredible Raffle Box", "<e><l>CLICK"};
            }

            @Override
            public EntityType entityType() {
                return EntityType.SHULKER;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-9.5, 76, 3.5, 90, 0);
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return CenturyEvent.latest() != null;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (CenturyEvent.latest() == null) {
            player.sendMessage("<e>[NPC] <6>Raffle Box<f>: Hey it's me, the raffle box.");
            player.sendMessage("<e>[NPC] <6>Raffle Box<f>: The event hasn't started yet.");
            player.sendMessage("<e>[NPC] <6>Raffle Box<f>: Don't tell anyone I can talk.");
            return;
        }
        RaffleMenus.openBox(player);
    }
}
