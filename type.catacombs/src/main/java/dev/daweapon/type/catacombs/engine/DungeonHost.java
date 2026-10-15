package dev.daweapon.type.catacombs.engine;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import dev.daweapon.type.catacombs.arena.CatacombsArena;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.instance.Instance;

import java.util.List;

/** What the room, door, blood-room and boss engines need from the run that owns them. */
public interface DungeonHost {
    CatacombsArena arena();

    Instance instance();

    CatacombsRunState state();

    KeyRing keys();

    List<SkyBlockPlayer> alivePlayers();

    void broadcast(String message);

    void broadcast(Text message);

    boolean advanceTo(DungeonRunPhase phase);

    void win();

    void lose(String reason);

    void later(int ticks, Runnable action);
}
