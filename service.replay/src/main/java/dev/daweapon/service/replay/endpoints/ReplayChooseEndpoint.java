package dev.daweapon.service.replay.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.game.ViewReplayPushProtocol;
import dev.daweapon.commons.protocol.objects.replay.ChooseReplayProtocolObject;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;

// Redis endpoint that broadcasts a player's request to view a chosen replay to all servers.
public class ReplayChooseEndpoint implements RedisMessageHandler
		<ChooseReplayProtocolObject.ChooseReplayMessage,
			ChooseReplayProtocolObject.ChooseReplayResponse> {

	@Override
	public RedisProtocol<ChooseReplayProtocolObject.ChooseReplayMessage, ChooseReplayProtocolObject.ChooseReplayResponse> protocol() {
		return new ChooseReplayProtocolObject();
	}

	@Override
	public ChooseReplayProtocolObject.ChooseReplayResponse handle(ChooseReplayProtocolObject.ChooseReplayMessage body, RedisMessageContext context) {
		RedisClient.requestAllServersFromService(
			new ViewReplayPushProtocol(),
			new ViewReplayPushProtocol.Request(body.player(), body.replayId(), body.shareCode()),
			300);
		return new ChooseReplayProtocolObject.ChooseReplayResponse(false);
	}

}
