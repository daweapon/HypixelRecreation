package dev.daweapon.type.skyblockgeneric.crimson.kuudra.npc;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Predicate;

/** A Kuudra Follower questline NPC: shown once {@link KuudraFollowerRules#npcsVisible} allows it. */
public abstract class KuudraFollowerNPC extends HypixelNPC {

    protected KuudraFollowerNPC(String colouredName, String texture, Pos position) {
        this(colouredName, texture, position, _ -> true);
    }

    protected KuudraFollowerNPC(String colouredName, String texture, Pos position, Predicate<SkyBlockPlayer> extraVisibility) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{colouredName, "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return texture;
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of(colouredName);
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
                return player instanceof SkyBlockPlayer skyBlockPlayer
                        && KuudraFollowerRules.npcsVisible(skyBlockPlayer)
                        && extraVisibility.test(skyBlockPlayer);
            }
        });
    }
}
