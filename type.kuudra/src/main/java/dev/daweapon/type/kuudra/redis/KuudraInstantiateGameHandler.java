package dev.daweapon.type.kuudra.redis;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol.Request;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol.Response;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.kuudra.run.KuudraRuns;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.queue.KuudraRoster;
import org.tinylog.Logger;

import java.util.List;
import java.util.UUID;

/** Creates a run when the orchestrator asks: the tier comes as the game type, the party as the map. */
public class KuudraInstantiateGameHandler implements RedisMessageHandler<Request, Response> {
    private static final InstantiateGamePushProtocol PROTOCOL = new InstantiateGamePushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request request, RedisMessageContext context) {
        try {
            KuudraTier tier = KuudraTier.valueOf(request.gameType());
            List<UUID> roster = KuudraRoster.decode(request.map());
            if (roster.isEmpty()) return Response.failure("No party was given for the Kuudra run");
            KuudraRun run = KuudraRuns.create(tier, roster);
            return Response.success(run.id().toString(), "kuudras_hollow", tier.name());
        } catch (Exception exception) {
            Logger.error(exception, "Couldn't create a Kuudra run for {}", request);
            return Response.failure("Failed to create a Kuudra run: " + exception.getMessage());
        }
    }
}
