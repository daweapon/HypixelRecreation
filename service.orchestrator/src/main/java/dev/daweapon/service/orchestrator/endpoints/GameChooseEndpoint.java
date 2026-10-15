package dev.daweapon.service.orchestrator.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.game.GameInformationPushProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that forwards a player's chosen game selection on to the hosting server.
public class GameChooseEndpoint implements RedisMessageHandler
		<ChooseGameProtocol.ChooseGameMessage,
				ChooseGameProtocol.ChooseGameResponse> {

	private static final long PUSH_TIMEOUT_SECONDS = 3;

	@Override
	public RedisProtocol<ChooseGameProtocol.ChooseGameMessage, ChooseGameProtocol.ChooseGameResponse> protocol() {
		return new ChooseGameProtocol();
	}

	@Override
	public ChooseGameProtocol.ChooseGameResponse handle(ChooseGameProtocol.ChooseGameMessage body, RedisMessageContext context) {
		try {
			RedisClient.requestServerFromService(body.server().uuid(), new GameInformationPushProtocol(),
					new GameInformationPushProtocol.Request(body.player(), body.gameId()))
					.get(PUSH_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
			return new ChooseGameProtocol.ChooseGameResponse(true, null);
		} catch (Exception e) {
			return new ChooseGameProtocol.ChooseGameResponse(false, "The game server did not confirm the assignment");
		}
	}

}
