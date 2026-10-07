package net.swofty.type.skyblockgeneric.utility;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class WarpPortal {

    @Getter
    private static ArrayList<WarpPortalData> warpPortals = new ArrayList<>();

    public static void create(Instance instance, BlockVec vector, Component text, Pos pos) {
        create(instance, vector, text, pos, () -> null);
    }

    /** {@code closedReason} returns why the portal is shut right now, or null while it is open. */
    public static void create(Instance instance, BlockVec vector, Component text, Pos pos, Supplier<String> closedReason) {
        createFor(instance, vector, text, pos, player -> closedReason.get());
    }

    /** {@code closedReason} returns why the portal is shut to this player, or null while it lets them through. */
    public static void createFor(Instance instance, BlockVec vector, Component text, Pos pos, Function<SkyBlockPlayer, String> closedReason) {
        ServerHolograms.addExternalHologram(ServerHolograms.ExternalHologram.builder().pos(vector.asPos().add(0.5, 2, 0.5)).instance(instance).text(new String[]{
            Text.of("<5>✦ <d>Warp To <b>{}", text).serialize()
        }).build());
        warpPortals.add(new WarpPortalData(vector, text, pos, closedReason));
    }

    public record WarpPortalData(BlockVec vector, Component text, Pos pos, Function<SkyBlockPlayer, String> closedReason) {
    }

}
