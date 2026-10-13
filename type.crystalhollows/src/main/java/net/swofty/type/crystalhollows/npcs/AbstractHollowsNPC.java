package net.swofty.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Supplier;

public abstract class AbstractHollowsNPC extends HypixelNPC {
    protected static final String DEFAULT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY3NGMxMjRhMmQ1NDM1OWMyZmRkMDg2Mjk4ODcwNjYwODkzNWU2MGVmNzQ1ODg2N2E5MGJlMDEyMjM3ZjVkZjgifX19";
    private static final Pos UNPLACED = new Pos(0, 0, 0);

    protected AbstractHollowsNPC(String name, String color, String texture, Pos fixed) {
        this(name, color, texture, () -> fixed);
    }

    protected AbstractHollowsNPC(String name, String color, String texture, Supplier<Pos> position) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{color + name, "<e><l>CLICK"};
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
                Pos pos = position.get();
                return pos == null ? UNPLACED : pos;
            }

            @Override
            public boolean dynamicPosition() {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return position.get() != null;
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

    protected static boolean hasMet(SkyBlockPlayer player, String flag) {
        return CrystalHollowsAccess.data(player).hasFlag(flag);
    }

    protected static void markMet(SkyBlockPlayer player, String flag) {
        var data = CrystalHollowsAccess.data(player);
        data.getFlags().add(flag);
        CrystalHollowsAccess.save(player, data);
    }

    protected static SkyBlockPlayer skyBlock(HypixelPlayer player) {
        return (SkyBlockPlayer) player;
    }
}
