package dev.daweapon.type.skyblockgeneric.minion.minions.mining;

import net.minestom.server.color.Color;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;
import dev.daweapon.type.skyblockgeneric.minion.actions.MinionMineAction;

import java.util.List;

public class MinionRedSand extends SkyBlockMinion {
    @Override
    public List<MinionTier> getTiers() {
        return List.of(
                new MinionTier(1, 26, 64,
                        "9d24991435e4e7fb1a9ad23db75c80aec300d003ec0c5963e0ed658634027889",
                        Material.WOODEN_SHOVEL, true),
                new MinionTier(2, 25, 128,
                        "d61dbdb2056dc998126d6c289d9a6023602be5dad5083f10e3c58d10fae3339f",
                        Material.WOODEN_SHOVEL, true),
                new MinionTier(3, 24, 192,
                        "cb8b223ba4a85a598f58d825c3b58ef1a2e1c0995880e291002730aa9cbd3e10",
                        Material.STONE_SHOVEL, true),
                new MinionTier(4, 23, 320,
                        "f3cfa0a6d31d0de2badf94a6934188159289e7ac8b75b1df4e82d3dcf35993c7",
                        Material.STONE_SHOVEL, true),
                new MinionTier(5, 22, 384,
                        "26af8bef4d12cc45cc5ff927dbe7d72ca025bbf3f7ae71c3d6c4aaf062bb3cd9",
                        Material.STONE_SHOVEL, true),
                new MinionTier(6, 21, 512,
                        "24a9cb73ca09e5838feccae3cd2fa719327d743f1891305b6c0a8538c4fe9c41",
                        Material.IRON_SHOVEL, true),
                new MinionTier(7, 20, 576,
                        "56cafdedeb9b7b708b99c675be6d3e32dd48a628c9aa0222458fa0d71e187813",
                        Material.IRON_SHOVEL, true),
                new MinionTier(8, 19, 704,
                        "7e1f9c552b66b368c4f0433fbf95da5e47fdb224100a79bd63d0411583578360",
                        Material.IRON_SHOVEL, true),
                new MinionTier(9, 18, 768,
                        "15f9be6eaea4c6c43329ab512b17f5bfbcc453e11b5351ddb572df35f597dd9a",
                        Material.GOLDEN_SHOVEL, true),
                new MinionTier(10, 16, 896,
                        "dbf7060ca6f41076ffbb869b7c05b386f296820ef7aecd0f908f120b8317a077",
                        Material.GOLDEN_SHOVEL, true),
                new MinionTier(11, 13, 960,
                        "859fb3c281dffba855fcf58b837749fb6142d785d6d8875d86ffb1920f103d20",
                        Material.DIAMOND_SHOVEL, true),
                new MinionTier(12, 11, 960,
                        "99fd71cefb5ad4ec2a239f04e6fef0d8838f3100fa775006b8d7bed4c20b7cc7",
                        Material.DIAMOND_SHOVEL, false)
        );
    }

    @Override
    public Color getBootColour() {
        return new Color(190, 102, 33);
    }

    @Override
    public Color getLeggingsColour() {
        return new Color(190, 102, 33);
    }

    @Override
    public Color getChestplateColour() {
        return new Color(190, 102, 33);
    }

    @Override
    public List<MinionExpectation> getExpectations() {
        return List.of(
                new BlockExpectation(-1, Block.RED_SAND, Block.AIR)
        );
    }

    @Override
    public MinionAction getAction() {
        return new MinionMineAction(Block.RED_SAND);
    }
}
