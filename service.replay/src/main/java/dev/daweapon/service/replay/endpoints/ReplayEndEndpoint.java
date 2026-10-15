package dev.daweapon.service.replay.endpoints;

import dev.daweapon.commons.protocol.objects.replay.ReplayEndProtocolObject;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.service.replay.ReplayService;
import dev.daweapon.service.replay.session.ReplaySessionManager;
import org.tinylog.Logger;

import java.util.concurrent.TimeUnit;

// Redis endpoint that ends a replay recording session and waits for it to be finalized and persisted.
public class ReplayEndEndpoint implements RedisMessageHandler<
        ReplayEndProtocolObject.EndMessage,
        ReplayEndProtocolObject.EndResponse> {

    @Override
    public ReplayEndProtocolObject protocol() {
        return new ReplayEndProtocolObject();
    }

    @Override
    public ReplayEndProtocolObject.EndResponse handle(ReplayEndProtocolObject.EndMessage msg, RedisMessageContext context) {

        try {
            Logger.info("Ending replay session {} (duration: {} ticks)", msg.replayId(), msg.durationTicks());
            ReplaySessionManager.EndResult result = ReplayService.getSessionManager()
                    .endSession(
                            msg.replayId(),
                            msg.endTime(),
                            msg.durationTicks()
                    )
                    .get(30, TimeUnit.SECONDS); // Wait up to 30 seconds

            Logger.info("Replay {} finalized: {} -> {} bytes",
                    msg.replayId(), result.totalBytes(), result.compressedBytes());

            return new ReplayEndProtocolObject.EndResponse(
                    result.success(),
                    result.totalBytes(),
                    result.compressedBytes(),
                    result.success()
            );

        } catch (Exception e) {
            Logger.error(e, "Failed to end replay session");
            return new ReplayEndProtocolObject.EndResponse(false, 0, 0, false);
        }
    }
}
