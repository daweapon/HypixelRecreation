package dev.daweapon.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.bedwarsgame.shop.TrapId;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;

import java.util.List;

// Lucky Block reward that instantly fills the opener's team trap queue with a fixed set of traps.
public class InstantTrapQueueLuckyReward extends LuckyReward {
    public InstantTrapQueueLuckyReward() {
        super("Instant Trap Queue");
    }

    @Override
    public void apply(BedWarsPlayer player, Pos openedAt) {
        List<TrapId> traps = List.of(TrapId.BLINDNESS, TrapId.COUNTER_OFFENSIVE, TrapId.REVEAL);
        traps.forEach(trap -> player.getGame().addTeamTrap(player.getTeamKey(), trap));
        player.sendMessage("<a>Filled your trap queue.");
    }
}
