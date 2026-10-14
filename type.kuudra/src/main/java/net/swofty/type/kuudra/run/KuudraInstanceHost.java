package net.swofty.type.kuudra.run;

import net.swofty.commons.ServerType;
import net.swofty.type.game.game.GameObject;
import net.swofty.type.game.instance.InstanceHost;

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
