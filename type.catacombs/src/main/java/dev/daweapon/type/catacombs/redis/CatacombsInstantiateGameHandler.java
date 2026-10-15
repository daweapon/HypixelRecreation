package dev.daweapon.type.catacombs.redis;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol.Request;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol.Response;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.catacombs.run.CatacombsFloorId;
import dev.daweapon.type.catacombs.run.CatacombsInstanceHost;
import dev.daweapon.type.catacombs.run.CatacombsRoster;
import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.queue.KuudraRoster;
import org.tinylog.Logger;

import java.util.List;
import java.util.UUID;

/** Creates a run when the orchestrator asks: the floor comes as the game type, the party as the map. */
public class CatacombsInstantiateGameHandler implements RedisMessageHandler<Request, Response> {
    private static final InstantiateGamePushProtocol PROTOCOL = new InstantiateGamePushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request request, RedisMessageContext context) {
        try {
            CatacombsFloorId floor = CatacombsFloorId.parse(request.gameType());
            List<UUID> roster = CatacombsRoster.validated(KuudraRoster.decode(request.map()));
            CatacombsRun run = CatacombsRuns.create(floor, roster);
            return Response.success(run.id().toString(), CatacombsInstanceHost.MAP, floor.encode());
        } catch (Exception exception) {
            Logger.error(exception, "Couldn't create a Catacombs run for {}", request);
            return Response.failure("Failed to create a Catacombs run: " + exception.getMessage());
        }
    }
}
