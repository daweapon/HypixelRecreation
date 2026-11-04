package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.gui.GUITyashoiExperiments;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookEvent;
import dev.daweapon.type.skyblockgeneric.greatspook.TyashoiService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCTyashoiAlchemist extends HypixelNPC {

    public NPCTyashoiAlchemist() {
        super(new HumanConfiguration() {
            @Override
            public boolean visible(HypixelPlayer player) {
                return GreatSpookEvent.isActive();
            }

            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<5>Tyashoi Alchemist", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzdiNDA2MDc0YjA2YmUzYWM5MWY1ZTk5Nzc0NDE2NzEwYWFiYzIyMTBhMmZiNjdhNzYyMDUxZDViNTYwYWQ4NCIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiI1ZGM5YWIwZGJmYzYyZmFiOGQ5OWYyODgzMTQyNGZlMSIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzkxOTkzOTM4Mzg2fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(41.5, 68, -54.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (!GreatSpookEvent.isActive()) return;
        TyashoiService.greet((SkyBlockPlayer) event.player(),
                player -> player.openView(new GUITyashoiExperiments(), GUITyashoiExperiments.State.of(player)));
    }
}
