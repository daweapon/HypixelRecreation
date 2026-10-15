package dev.daweapon.service.replay.endpoints;

import dev.daweapon.commons.protocol.objects.replay.ReplayMapUploadProtocolObject;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.service.replay.ReplayService;
import org.tinylog.Logger;

// Redis endpoint that stores an uploaded map's compressed data, skipping the write if that map hash already exists.
public class ReplayMapUploadEndpoint implements RedisMessageHandler<
        ReplayMapUploadProtocolObject.MapUploadMessage,
        ReplayMapUploadProtocolObject.MapUploadResponse> {

    @Override
    public ReplayMapUploadProtocolObject protocol() {
        return new ReplayMapUploadProtocolObject();
    }

    @Override
    public ReplayMapUploadProtocolObject.MapUploadResponse handle(ReplayMapUploadProtocolObject.MapUploadMessage msg, RedisMessageContext context) {

        try {
            // Check if map already exists
            if (ReplayService.getDatabase().hasMap(msg.mapHash())) {
                Logger.debug("Map {} already exists", msg.mapHash());
                return new ReplayMapUploadProtocolObject.MapUploadResponse(true, true);
            }

            // Save map
            ReplayService.getDatabase().saveMap(msg.mapHash(), msg.mapName(), msg.compressedData());
            Logger.info("Saved map {} ({}) - {} bytes", msg.mapName(), msg.mapHash(), msg.compressedData().length);

            return new ReplayMapUploadProtocolObject.MapUploadResponse(true, false);

        } catch (Exception e) {
            Logger.error(e, "Failed to upload map");
            return new ReplayMapUploadProtocolObject.MapUploadResponse(false, false);
        }
    }
}
