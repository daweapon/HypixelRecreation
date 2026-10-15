package dev.daweapon.velocity.testflow.handlers;

import dev.daweapon.velocity.SkyBlockVelocity;
import dev.daweapon.velocity.testflow.ProxyTestFlowHandler;
import dev.daweapon.velocity.testflow.TestFlowManager;
import org.tinylog.Logger;

// Example/default test flow handler that just logs lifecycle and periodic status events.
public class SendToHubTestFlowHandler extends ProxyTestFlowHandler {

    @Override
    public void onTestFlowStart(TestFlowManager.ProxyTestFlowInstance instance) {

    }

    @Override
    public void onAllServersReady(TestFlowManager.ProxyTestFlowInstance instance) {

    }

    @Override
    public void onPlayerJoin(String playerName, TestFlowManager.ProxyTestFlowInstance instance) {

    }

    @Override
    public void onPlayerLeave(String playerName, TestFlowManager.ProxyTestFlowInstance instance) {

    }

    @Override
    public void onTestFlowEnd(TestFlowManager.ProxyTestFlowInstance instance) {
        Logger.info("Example test flow ended: {}", instance.getName());
    }

    @Override
    public void onPeriodicUpdate(TestFlowManager.ProxyTestFlowInstance instance) {
        if (instance.getUptime() % 300000 == 0) {
            long onlinePlayers = instance.getPlayers().stream()
                    .mapToLong(name -> SkyBlockVelocity.getServer().getPlayer(name).isPresent() ? 1 : 0)
                    .sum();
            Logger.info("Test flow {} status — runtime: {}s, active servers: {}, online test players: {}",
                    instance.getName(),
                    instance.getUptime() / 1000,
                    instance.getGameServers().size(),
                    onlinePlayers);
        }
    }
}