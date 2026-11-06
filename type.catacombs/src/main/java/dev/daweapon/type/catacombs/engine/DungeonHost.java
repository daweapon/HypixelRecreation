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

    void recordSecret();

    void recordCrypt();

    void later(int ticks, Runnable action);

    /** Revives the killer if they are a ghost, otherwise the first ghost in the party; false when nobody is dead. */
    boolean reviveGhost(SkyBlockPlayer killer);
}
