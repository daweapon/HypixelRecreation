package dev.daweapon.velocity.redis.listeners;

import com.velocitypowered.api.proxy.Player;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.to.FinishedWithPlayerProtocol;
import dev.daweapon.velocity.SkyBlockVelocity;
import dev.daweapon.velocity.gamemanager.TransferHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.Optional;
import java.util.UUID;

// Redis handler that resumes a player's pending limbo transfer once their previous server confirms it has finished with them.
public class ListenerServerFinishedWithPlayer implements RedisMessageHandler<
        FinishedWithPlayerProtocol.Request,
        FinishedWithPlayerProtocol.Response> {

    @Override
    public RedisProtocol<FinishedWithPlayerProtocol.Request, FinishedWithPlayerProtocol.Response> protocol() {
        return new FinishedWithPlayerProtocol();
    }

    @Override
    public FinishedWithPlayerProtocol.Response handle(FinishedWithPlayerProtocol.Request message, RedisMessageContext context) {
        UUID playerUUID = UUID.fromString(message.uuid());

        Optional<Player> potentialPlayer = SkyBlockVelocity.getServer().getPlayer(playerUUID);
        if (potentialPlayer.isEmpty()) {
            return new FinishedWithPlayerProtocol.Response();
        }

        Player player = potentialPlayer.get();
        TransferHandler handler = new TransferHandler(player);
        if (!handler.isInLimbo()) {
            return new FinishedWithPlayerProtocol.Response();
        }
        handler.previousServerIsFinished();

        return new FinishedWithPlayerProtocol.Response();
    }
}
