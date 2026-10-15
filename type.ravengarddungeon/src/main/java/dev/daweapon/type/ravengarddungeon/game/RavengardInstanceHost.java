package dev.daweapon.type.ravengarddungeon.game;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.game.game.GameObject;
import dev.daweapon.type.game.instance.InstanceHost;

import java.util.ArrayList;
import java.util.List;

public final class RavengardInstanceHost implements InstanceHost {
    @Override
    public ServerType type() {
        return ServerType.RAVENGARD_DUNGEON;
    }

    @Override
    public List<GameObject> instances() {
        List<GameObject> games = new ArrayList<>();
        for (var instance : DungeonInstanceRegistry.all()) {
            GameObject game = new GameObject();
            game.setGameId(instance.getGameId());
            game.setType(ServerType.RAVENGARD_DUNGEON);
            game.setMap(instance.getPlayers().isEmpty()
                    ? "empty:" + instance.getRemainingLifeSeconds() : "generated");
            game.setGameTypeName(instance.getMode());
            game.setAcceptingJoins(instance.isAcceptingJoins());
            game.setInvolvedPlayers(new ArrayList<>(instance.getPlayers()));
            game.setDisconnectedPlayers(List.of());
            games.add(game);
        }
        return games;
    }

    @Override
    public int remainingSlots() {
        return DungeonInstanceRegistry.remainingSlots();
    }
}
