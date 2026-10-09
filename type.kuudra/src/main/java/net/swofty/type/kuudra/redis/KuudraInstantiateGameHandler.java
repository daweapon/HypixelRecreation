package net.swofty.type.kuudra.redis;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.game.InstantiateGamePushProtocol;
import net.swofty.commons.protocol.objects.game.InstantiateGamePushProtocol.Request;
import net.swofty.commons.protocol.objects.game.InstantiateGamePushProtocol.Response;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.kuudra.run.KuudraRun;
import net.swofty.type.kuudra.run.KuudraRuns;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.queue.KuudraRoster;
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
