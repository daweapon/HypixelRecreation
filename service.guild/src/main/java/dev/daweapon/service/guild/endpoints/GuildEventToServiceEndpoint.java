package dev.daweapon.service.guild.endpoints;

import dev.daweapon.commons.guild.GuildEvent;
import dev.daweapon.commons.guild.events.*;
import dev.daweapon.commons.protocol.objects.guild.SendGuildEventToServiceProtocolObject;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.service.guild.GuildCache;

// Redis endpoint that deserializes an incoming GuildEvent and dispatches it to the matching GuildCache handler.
public class GuildEventToServiceEndpoint implements RedisMessageHandler<
    SendGuildEventToServiceProtocolObject.SendGuildEventToServiceMessage,
    SendGuildEventToServiceProtocolObject.SendGuildEventToServiceResponse> {

    @Override
    public SendGuildEventToServiceProtocolObject protocol() {
        return new SendGuildEventToServiceProtocolObject();
    }

    @Override
    public SendGuildEventToServiceProtocolObject.SendGuildEventToServiceResponse handle(SendGuildEventToServiceProtocolObject.SendGuildEventToServiceMessage messageObject, RedisMessageContext context) {
        GuildEvent template = GuildEvent.findFromType(messageObject.eventType());
        GuildEvent event = template.getSerializer().deserialize(messageObject.eventData());

        switch (event) {
            case GuildCreateRequestEvent e -> GuildCache.handleCreateRequest(e);
            case GuildInviteRequestEvent e -> GuildCache.handleInviteRequest(e);
            case GuildAcceptInviteRequestEvent e -> GuildCache.handleAcceptInvite(e);
            case GuildJoinRequestEvent e -> GuildCache.handleJoinRequest(e);
            case GuildLeaveRequestEvent e -> GuildCache.handleLeaveRequest(e);
            case GuildKickRequestEvent e -> GuildCache.handleKickRequest(e);
            case GuildDisbandRequestEvent e -> GuildCache.handleDisbandRequest(e);
            case GuildPromoteRequestEvent e -> GuildCache.handlePromoteRequest(e);
            case GuildDemoteRequestEvent e -> GuildCache.handleDemoteRequest(e);
            case GuildTransferRequestEvent e -> GuildCache.handleTransferRequest(e);
            case GuildChatRequestEvent e -> GuildCache.handleChatRequest(e);
            case GuildSettingRequestEvent e -> GuildCache.handleSettingRequest(e);
            case GuildMuteRequestEvent e -> GuildCache.handleMuteRequest(e);
            case GuildUnmuteRequestEvent e -> GuildCache.handleUnmuteRequest(e);
            case GuildSetRankRequestEvent e -> GuildCache.handleSetRankRequest(e);
            case GuildProgressRequestEvent e -> GuildCache.handleProgressRequest(e);
            default ->
                throw new IllegalArgumentException("Unknown guild event type: " + event.getClass().getSimpleName());
        }

        return new SendGuildEventToServiceProtocolObject.SendGuildEventToServiceResponse(true);
    }
}
