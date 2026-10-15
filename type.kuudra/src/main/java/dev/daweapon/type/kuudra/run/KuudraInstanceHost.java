package dev.daweapon.type.kuudra.run;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.game.game.GameObject;
import dev.daweapon.type.game.instance.InstanceHost;

import java.util.ArrayList;
import java.util.List;

public final class KuudraInstanceHost implements InstanceHost {
    @Override
    public ServerType type() {
        return ServerType.SKYBLOCK_KUUDRA;
    }

    @Override
    public List<GameObject> instances() {
        List<GameObject> games = new ArrayList<>();
        for (KuudraRun run : KuudraRuns.all()) {
            GameObject game = new GameObject();
            game.setGameId(run.id());
            game.setType(ServerType.SKYBLOCK_KUUDRA);
            game.setMap("kuudras_hollow");
            game.setGameTypeName(run.tier().name());
            game.setAcceptingJoins(false);
            game.setInvolvedPlayers(new ArrayList<>(run.roster()));
            game.setDisconnectedPlayers(run.disconnectedMembers());
            games.add(game);
        }
        return games;
    }

    @Override
    public int remainingSlots() {
        return KuudraRuns.remainingSlots();
    }
}
