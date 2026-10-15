package dev.daweapon.type.skyblockgeneric.minion.minions.mining;

import net.minestom.server.color.Color;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;
import dev.daweapon.type.skyblockgeneric.minion.actions.MinionMineAction;

import java.util.List;

public class MinionMycelium extends SkyBlockMinion {
    @Override
    public List<MinionTier> getTiers() {
        return List.of(
                new MinionTier(1, 26, 64,
                        "fc8ebad72b77df3990e07bc869a99a8f8962d3c19c76e39d99553cae4131cc8",
                        Material.WOODEN_SHOVEL, true),
                new MinionTier(2, 25, 128,
                        "7a41cf5a575f98d2686d35471d164afd76e907387362435af5175dcf8af9fbb0",
                        Material.WOODEN_SHOVEL, true),
                new MinionTier(3, 24, 192,
                        "5e857bddca9f14d66ae4c3a909447d5978f443db97e32efb6fde29b4a41d4d40",
                        Material.STONE_SHOVEL, true),
                new MinionTier(4, 23, 320,
                        "583d7d5e52342091add5b0ae4992c65d599ad56f9d979f73b10d68f09d20ddbc",
                        Material.STONE_SHOVEL, true),
                new MinionTier(5, 22, 384,
                        "4b8778057129a9d56632f07800854d5982b9132bd193a6f1517c288dd85d9ff3",
                        Material.STONE_SHOVEL, true),
                new MinionTier(6, 21, 512,
                        "341e6f7533482d8c4b46ca5b9747819ab61f34d7d1442e638b65cd706e225bd9",
                        Material.IRON_SHOVEL, true),
                new MinionTier(7, 20, 576,
                        "2472a4d11bc93f2246250116b46900def5c4731182b374f4444c49b33928e2e",
                        Material.IRON_SHOVEL, true),
                new MinionTier(8, 19, 704,
                        "3160fc6b6ca8a735a62c4918596f1f6a10f81933f5e83586ad46158aaeca8fe",
                        Material.IRON_SHOVEL, true),
                new MinionTier(9, 18, 768,
                        "e0124bfa3865dd94e09a8637db591c2caa1ab51fa06946f04b261e9a6c63ff68",
                        Material.GOLDEN_SHOVEL, true),
                new MinionTier(10, 16, 896,
                        "5dd19195d650240187b5874a0080a7734c28e31bb7e1aedd347ae9c634640e54",
                        Material.GOLDEN_SHOVEL, true),
                new MinionTier(11, 13, 960,
                        "f379623e4f66deca3f5365986c39a6183c851cb20efb5d6121ef4206c9e8d11c",
                        Material.DIAMOND_SHOVEL, true),
                new MinionTier(12, 11, 960,
                        "d2290d9564a5a5702260128fe8f56532cdee1f15ef99f18b1528e2ec17719946",
                        Material.DIAMOND_SHOVEL, false)
        );
    }

    @Override
    public Color getBootColour() {
        return new Color(102, 77, 107);
    }

    @Override
    public Color getLeggingsColour() {
        return new Color(102, 77, 107);
    }

    @Override
    public Color getChestplateColour() {
        return new Color(102, 77, 107);
    }

    @Override
    public List<MinionExpectation> getExpectations() {
        return List.of(
                new BlockExpectation(-1, Block.MYCELIUM, Block.AIR)
        );
    }

    @Override
    public MinionAction getAction() {
        return new MinionMineAction(Block.MYCELIUM);
    }
}
