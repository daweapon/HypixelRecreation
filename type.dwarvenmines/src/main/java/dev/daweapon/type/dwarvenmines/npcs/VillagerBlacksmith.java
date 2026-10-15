package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIReforge;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;

public class VillagerBlacksmith extends HypixelNPC {
    public VillagerBlacksmith() {
        super(new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Blacksmith", "<e><l>CLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-6.5, 201.00, -154.50, 0f, 0f);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.WEAPONSMITH;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        new GUIReforge().open(player);
    }
}

