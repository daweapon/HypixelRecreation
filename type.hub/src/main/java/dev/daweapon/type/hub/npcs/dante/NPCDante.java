package dev.daweapon.type.hub.npcs.dante;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.gui.GUIShopDante;
import dev.daweapon.type.skyblockgeneric.dante.DanteAccess;
import dev.daweapon.type.skyblockgeneric.dante.DanteSkin;

public class NPCDante extends HypixelNPC {
    private static final String[] GREETING = {
            "<d>[NPC] Dante<f>: Values. Progress. Justice",
            "<d>[NPC] Dante<f>: I will stabilize this rapidly declining system, permanently.",
            "<d>[NPC] Dante<f>: I will fix the declining economy, but above all things, safety is my priority."
    };

    public NPCDante() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Dante", "<e><l>CLICK"};
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
                return new Pos(5.5, 79, 17.5, 180, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return DanteAccess.mayorIsDante();
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        for (String line : GREETING) player.sendMessage(line);
        player.openView(new GUIShopDante());
    }
}
