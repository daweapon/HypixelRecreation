package dev.daweapon.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Predicate;

public abstract class AbstractEndNPC extends HypixelNPC {
    protected AbstractEndNPC(String header, String name, String color, String texture, Pos position) {
        this(header, name, color, texture, position, _ -> true);
    }

    protected AbstractEndNPC(String header, String name, String color, String texture, Pos position,
                             Predicate<SkyBlockPlayer> visibleTo) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return header == null
                        ? new String[]{color + name, "<e><l>CLICK"}
                        : new String[]{header, color + name, "<e><l>CLICK"};
            }

            @Override
            public String texture(HypixelPlayer player) {
                return texture;
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return position;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return visibleTo.test((SkyBlockPlayer) player);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return color + name;
            }
        });
    }

    protected static SkyBlockPlayer skyBlock(HypixelPlayer player) {
        return (SkyBlockPlayer) player;
    }
}
