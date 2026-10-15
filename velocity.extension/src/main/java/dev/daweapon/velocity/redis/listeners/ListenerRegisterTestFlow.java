package dev.daweapon.velocity.redis.listeners;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.to.RegisterTestFlowProtocol;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.velocity.testflow.TestFlowManager;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Redis handler that registers a new isolated test flow and its expected server configs with the TestFlowManager.
public class ListenerRegisterTestFlow implements RedisMessageHandler<
        RegisterTestFlowProtocol.Request,
        RegisterTestFlowProtocol.Response> {

    @Override
    public RedisProtocol<RegisterTestFlowProtocol.Request, RegisterTestFlowProtocol.Response> protocol() {
        return new RegisterTestFlowProtocol();
    }

    @Override
    public RegisterTestFlowProtocol.Response handle(RegisterTestFlowProtocol.Request message, RedisMessageContext context) {
        try {
            String testFlowName = message.testFlowName();
            String handler = message.handler();
            List<String> players = message.players();

            List<TestFlowManager.ServerConfig> serverConfigs = new ArrayList<>();
            for (Map<String, Object> configMap : message.serverConfigs()) {
                JSONObject configJson = new JSONObject(configMap);
                serverConfigs.add(TestFlowManager.ServerConfig.fromJson(configJson));
            }

            TestFlowManager.registerTestFlow(testFlowName, handler, players, serverConfigs);

            Logger.info("Registered test flow '{}' from server {}", testFlowName, UUID.fromString(context.origin().id()));

            return new RegisterTestFlowProtocol.Response(true, "Test flow registered successfully", null);

        } catch (Exception e) {
            Logger.error(e, "Failed to register test flow from server {}", UUID.fromString(context.origin().id()));
            return new RegisterTestFlowProtocol.Response(false, null, e.getMessage());
        }
    }
}
