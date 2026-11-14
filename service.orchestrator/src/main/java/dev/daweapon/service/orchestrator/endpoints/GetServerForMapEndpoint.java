package dev.daweapon.service.orchestrator.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.orchestrator.instance.InstancedActivities;
import dev.daweapon.service.orchestrator.instance.QueuePositionNotifier;

// Redis endpoint that resolves a matchmaking request into a specific server to join, for the instanced SkyBlock activities (Kuudra, Catacombs).
public class GetServerForMapEndpoint implements RedisMessageHandler
		<GetServerForMapProtocol.GetServerForMapMessage,
				GetServerForMapProtocol.GetServerForMapResponse> {

	@Override
	public RedisProtocol<GetServerForMapProtocol.GetServerForMapMessage, GetServerForMapProtocol.GetServerForMapResponse> protocol() {
		return new GetServerForMapProtocol();
	}

	@Override
	public GetServerForMapProtocol.GetServerForMapResponse handle(GetServerForMapProtocol.GetServerForMapMessage body, RedisMessageContext context) {
		return switch (body.type()) {
			case SKYBLOCK_KUUDRA -> InstancedActivities.kuudra(body, QueuePositionNotifier.waiterFor(body, context));
			case SKYBLOCK_CATACOMBS -> InstancedActivities.catacombs(body, QueuePositionNotifier.waiterFor(body, context));
			default -> new GetServerForMapProtocol.GetServerForMapResponse(null, null, true, null);
		};
	}
}
