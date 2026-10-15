package dev.daweapon.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Supplier;

public abstract class AbstractHollowsNPC extends HypixelNPC {
    protected static final String DEFAULT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY3NGMxMjRhMmQ1NDM1OWMyZmRkMDg2Mjk4ODcwNjYwODkzNWU2MGVmNzQ1ODg2N2E5MGJlMDEyMjM3ZjVkZjgifX19";
    protected static final String GOBLIN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZiOTcyZTMyZDc2MWIxOTI2MjZlNWQ2ZDAxZWRjMDk0OTQwOTEwMTAzY2VhNWUyZTJkMWYyMzFhZGI3NTVkNSJ9fX0=";
    protected static final String KALHUIKI_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY1ODcwNjE5YjBjMmRiODIyNDc5ZDJjYjM4MjEyNmJkNzk5YzU4MjM3OGI1YjFkZjkzZTc1YjdkMDE1NTFiNyJ9fX0=";
    protected static final String SLUDGE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0=";
    private static final Pos UNPLACED = new Pos(0, 0, 0);

    protected AbstractHollowsNPC(String name, String color, String texture, Pos fixed) {
        this(null, name, color, texture, () -> fixed);
    }

    protected AbstractHollowsNPC(String header, String name, String color, String texture, Pos fixed) {
        this(header, name, color, texture, () -> fixed);
    }

    protected AbstractHollowsNPC(String name, String color, String texture, Supplier<Pos> position) {
        this(null, name, color, texture, position);
    }

    protected AbstractHollowsNPC(String header, String name, String color, String texture, Supplier<Pos> position) {
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
