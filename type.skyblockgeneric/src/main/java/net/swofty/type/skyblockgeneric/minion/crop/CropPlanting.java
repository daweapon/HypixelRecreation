package net.swofty.type.skyblockgeneric.minion.crop;

import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.minion.drops.MinionDropTable;

public record CropPlanting(Block grown, MinionDropTable drops) {
    public boolean matches(Block block) {
        return block.id() == grown.id();
    }
}
