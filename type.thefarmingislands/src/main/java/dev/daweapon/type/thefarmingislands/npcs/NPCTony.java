package dev.daweapon.type.thefarmingislands.npcs;

import net.kyori.adventure.text.Component;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingNpcCatalogueView;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;

public class NPCTony extends HypixelNPC implements NPCAbiphoneTrait {
    public NPCTony() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Tony", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQzNGZjZjc3MjYzMWI4MThkMmM2YWZiODc4ZTM5MzM3NjcwN2EyYjU3MDE5YjVmZjg3MGQ2OTUyZmRkZDExYSIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiIwM2Q0MTY3YzVmYWQyZTI3OGI2NTUzZDFkYmZmMGIyZSIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzgyMDcxNzk4NjI4fQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<e>Tony");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(278.5, 104, -544.5, -160, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        player.openView(new HuntingNpcCatalogueView("tony", null));
    }

    @Override
    public String getAbiphoneKey() {
        return "tony";
    }
}
