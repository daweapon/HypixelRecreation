package dev.daweapon.type.galatea.support;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public abstract class TalkNPC extends HypixelNPC {
    public record Spec(String name, String colour, String texture, String signature, Pos position) {
    }

    protected TalkNPC(Spec spec) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<" + spec.colour() + ">" + spec.name(), "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return spec.signature();
            }

            @Override
            public String texture(HypixelPlayer player) {
                return spec.texture();
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<" + spec.colour() + ">" + spec.name());
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return spec.position();
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }
}
