package dev.daweapon.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.bedwarsgame.TypeBedWarsGameLoader;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;

// Lucky Block reward that places a configured block at the spot the block was opened.
public class BlockLuckyReward extends LuckyReward {
    private final Block block;

    public BlockLuckyReward(String name, Block block) {
        super(name);
        this.block = block;
    }

    @Override
    public void apply(BedWarsPlayer player, Pos openedAt) {
        player.getInstance().setBlock(openedAt, block.withTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG, true));
    }
}
