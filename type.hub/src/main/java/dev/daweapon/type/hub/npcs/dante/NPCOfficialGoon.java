package dev.daweapon.type.hub.npcs.dante;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.dante.DanteAccess;
import dev.daweapon.type.skyblockgeneric.dante.DanteSkin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public abstract class NPCOfficialGoon extends HypixelNPC {
    private final String building;

    protected NPCOfficialGoon(String building, Pos position) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Goon", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return DanteSkin.SIGNATURE;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return DanteSkin.TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return position;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return DanteAccess.securityActive();
            }
        });
        this.building = building;
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (DanteAccess.canEnter(player)) {
            player.sendMessage("<e>[NPC] Goon<f>: Carry on. The <e>" + building + " <f>is open to supporters of Dante.");
            return;
        }
        player.sendMessage("<e>[NPC] Goon<f>: Move along. The <e>" + building
                + " <f>is for supporters of Dante only. Buy a <a>Happy Mask <f>or <6>Dante's Talisman <f>from Dante in the Community Center.");
    }
}
