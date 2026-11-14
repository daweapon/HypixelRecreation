package dev.daweapon.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.bedwarsgame.item.impl.LuckyBlockTrap;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;

// Lucky Block reward that places a hidden trap of the configured type at the opener's location.
public class TrapLuckyReward extends LuckyReward {
    private final String trap;

    public TrapLuckyReward(String name, String trap) {
        super(name);
        this.trap = trap;
    }

    @Override
    public void apply(BedWarsPlayer player, Pos openedAt) {
        LuckyBlockTrap.place(player, openedAt, trap);
    }
}
