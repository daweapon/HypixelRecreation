package net.swofty.proxyapi;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

// Wraps a list of player UUIDs known to the proxy, exposing them as ProxyPlayer handles.
@Getter
public class ProxyPlayerSet {
    private final List<UUID> players;

    public ProxyPlayerSet(List<UUID> players) {
        this.players = players;
    }

    public List<ProxyPlayer> asProxyPlayers() {
        return players.stream().map(ProxyPlayer::new).toList();
    }
}
