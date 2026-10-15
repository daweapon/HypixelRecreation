package dev.daweapon.type.catacombs.run;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.game.game.GameObject;
import dev.daweapon.type.game.instance.InstanceHost;

import java.util.ArrayList;
import java.util.List;

public final class CatacombsInstanceHost implements InstanceHost {
    public static final String MAP = "catacombs";

    @Override
    public ServerType type() {
        return ServerType.SKYBLOCK_CATACOMBS;
    }

    @Override
    public List<GameObject> instances() {
        List<GameObject> games = new ArrayList<>();
        for (CatacombsRun run : CatacombsRuns.all()) {
            GameObject game = new GameObject();
            game.setGameId(run.id());
            game.setType(ServerType.SKYBLOCK_CATACOMBS);
            game.setMap(MAP);
            game.setGameTypeName(run.floorId().encode());
            game.setAcceptingJoins(false);
            game.setInvolvedPlayers(new ArrayList<>(run.roster()));
            game.setDisconnectedPlayers(run.disconnectedMembers());
            games.add(game);
        }
        return games;
    }

    @Override
    public int remainingSlots() {
        return CatacombsRuns.remainingSlots();
    }
}
