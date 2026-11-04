package dev.daweapon.type.generic.redis.service;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminPlayerActionProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminPlayerActionProtocol.ActionRequest;
import dev.daweapon.commons.protocol.objects.admin.AdminPlayerActionProtocol.ActionResponse;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointRank;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;
import net.minestom.server.MinecraftServer;
import org.tinylog.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

// A rank change has to land in the live session of an online player: their quit save writes every
// account datapoint back, so a Redis-only write would be overwritten the moment they leave.
public class AdminPlayerActionHandler implements RedisMessageHandler<ActionRequest, ActionResponse> {
    private static final AdminPlayerActionProtocol PROTOCOL = new AdminPlayerActionProtocol();

    @Override
    public RedisProtocol<ActionRequest, ActionResponse> protocol() {
        return PROTOCOL;
    }

    @Override
    public ActionResponse handle(ActionRequest message, RedisMessageContext context) {
        HypixelPlayer player = HypixelGenericLoader.getFromUUID(message.uuid());
        if (player == null || !player.isOnline()) return new ActionResponse(false, "not on this server");

        try {
            return switch (message.action()) {
                case AdminPlayerActionProtocol.SET_RANK -> onTick(() -> setRank(player, message.value()));
                case AdminPlayerActionProtocol.KICK -> onTick(() -> kick(player, message.value()));
                default -> new ActionResponse(true, "unknown action " + message.action());
            };
        } catch (Exception e) {
            Logger.error(e, "Admin action {} failed for {}", message.action(), message.uuid());
            return new ActionResponse(true, "failed: " + e.getMessage());
        }
    }

    private static ActionResponse setRank(HypixelPlayer player, String value) {
        Rank rank = Rank.valueOf(value);
        HypixelDataHandler.getUser(player.getUuid()).get(HypixelDataHandler.Data.RANK, DatapointRank.class).setValue(rank);
        player.sendMessage("<a>Your rank is now <f>{}<a>.", rank.name());
        return new ActionResponse(true, "rank set to " + rank.name() + " in the live session");
    }

    private static ActionResponse kick(HypixelPlayer player, String reason) {
        player.kick(reason == null || reason.isBlank() ? "<c>You were kicked by staff." : "<c>" + reason);
        return new ActionResponse(true, "kicked");
    }

    private static ActionResponse onTick(Supplier<ActionResponse> work) throws Exception {
        CompletableFuture<ActionResponse> result = new CompletableFuture<>();
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            try {
                result.complete(work.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(5, TimeUnit.SECONDS);
    }
}
