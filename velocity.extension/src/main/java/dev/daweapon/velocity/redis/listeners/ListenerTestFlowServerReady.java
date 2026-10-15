package dev.daweapon.velocity.redis.listeners;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.to.TestFlowServerReadyProtocol;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.velocity.testflow.TestFlowManager;
import org.tinylog.Logger;

import java.util.UUID;

// Redis handler that records a server's readiness signal for its assigned test flow.
public class ListenerTestFlowServerReady implements RedisMessageHandler<
        TestFlowServerReadyProtocol.Request,
        TestFlowServerReadyProtocol.Response> {

    @Override
    public RedisProtocol<TestFlowServerReadyProtocol.Request, TestFlowServerReadyProtocol.Response> protocol() {
        return new TestFlowServerReadyProtocol();
    }

    @Override
    public TestFlowServerReadyProtocol.Response handle(TestFlowServerReadyProtocol.Request message, RedisMessageContext context) {
        try {
            String testFlowName = message.testFlowName();
            ServerType serverType = ServerType.valueOf(message.serverType());
            int serverIndex = message.serverIndex();

            TestFlowManager.markServerReady(testFlowName, serverType, serverIndex, UUID.fromString(context.origin().id()));

            Logger.info("Server {} #{} is ready for test flow '{}' (UUID: {})",
                    serverType.name(), serverIndex, testFlowName, UUID.fromString(context.origin().id()));

            return new TestFlowServerReadyProtocol.Response(true, "Server ready status recorded", null);

        } catch (Exception e) {
            Logger.error(e, "Failed to process test flow server ready from server {}", UUID.fromString(context.origin().id()));
            return new TestFlowServerReadyProtocol.Response(false, null, e.getMessage());
        }
    }
}
