package dev.daweapon.velocity.gamemanager;

import com.velocitypowered.api.proxy.Player;

import java.util.List;

// Strategy for picking which candidate game server a player should be balanced onto.
public abstract class BalanceConfiguration {
    public abstract GameManager.GameServer getServer(Player player, List<GameManager.GameServer> servers);
}
