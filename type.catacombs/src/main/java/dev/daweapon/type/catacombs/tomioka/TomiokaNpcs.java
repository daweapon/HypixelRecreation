package dev.daweapon.type.catacombs.tomioka;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.type.catacombs.arena.RoomFeatures;
import dev.daweapon.type.catacombs.puzzle.PuzzleNpc;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Tomioka standing on his ledge in every real Tomioka room of a run; clicking him opens his shop. */
public final class TomiokaNpcs {
    private final List<PuzzleNpc> npcs = new ArrayList<>();
    private TomiokaConfig config;

    public void start(Instance instance, FloorLayout layout, Predicate<PlacedRoom> real) {
        for (PlacedRoom room : layout.rooms()) {
            if (!real.test(room)) continue;
            RoomFeatures.shared().tomioka(room).ifPresent(spot -> spawn(instance, spot));
        }
    }

    private void spawn(Instance instance, RoomFeatures.Spot spot) {
        if (config == null) {
            try {
                config = TomiokaConfig.load(TomiokaConfig.FILE);
            } catch (IOException | RuntimeException exception) {
                Logger.error(exception, "Could not read Tomioka from {}", TomiokaConfig.FILE);
                return;
            }
        }
        PuzzleNpc npc = new PuzzleNpc(config.skin().playerSkin(), config.nameLines());
        npc.spawn(instance, new Pos(spot.x(), spot.y(), spot.z(), spot.yaw(), 0));
        npcs.add(npc);
    }

    public boolean interact(SkyBlockPlayer player, Entity entity) {
        if (npcs.stream().noneMatch(npc -> npc.is(entity))) return false;
        player.openView(new GUITomioka(config));
        return true;
    }

    public void dispose() {
        npcs.forEach(PuzzleNpc::remove);
        npcs.clear();
    }
}
