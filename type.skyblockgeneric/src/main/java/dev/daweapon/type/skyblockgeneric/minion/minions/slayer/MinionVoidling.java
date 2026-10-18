package dev.daweapon.type.skyblockgeneric.minion.minions.slayer;

import net.minestom.server.color.Color;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.minionMobs.MobMinionVoidling;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;
import dev.daweapon.type.skyblockgeneric.minion.actions.MinionKillMobAction;
import dev.daweapon.type.skyblockgeneric.minion.drops.MinionDrop;
import dev.daweapon.type.skyblockgeneric.minion.drops.MinionDropTable;

import java.util.List;

public class MinionVoidling extends SkyBlockMinion {
    public static final MinionDropTable DROPS = MinionDropTable.of(
            MinionDrop.of(ItemType.OBSIDIAN, 2),
            MinionDrop.chance(ItemType.OBSIDIAN, 1, 50),
            MinionDrop.chance(ItemType.QUARTZ, 1, 40),
            MinionDrop.chance(ItemType.ENCHANTED_ENDER_PEARL, 1, 0.0625)
    );

    @Override
    public List<MinionTier> getTiers() {
        return List.of(
                new MinionTier(1, 45, 64,
                        "3a851ed2ce5c2c0523af772d206d9555e2e1383ec87946e6ff4c51186e29ef7f",
                        Material.WOODEN_SWORD, true),
                new MinionTier(2, 45, 192,
                        "bc494af6b45980cb2b035007c25e1b4e169371c410fe5d2708daa4f695668fd2",
                        Material.WOODEN_SWORD, true),
                new MinionTier(3, 42, 192,
                        "4092f0e0e390f1d2382c908b0a8a90f988458ba8cfa13773f81faa6fc0c906f2",
                        Material.STONE_SWORD, true),
                new MinionTier(4, 42, 384,
                        "be983af1e4fd62b3ad436b00782a3faebb2e392827097eb96c652e2e5521e438",
                        Material.STONE_SWORD, true),
                new MinionTier(5, 39, 384,
                        "527465fd028df45d67de911870cabbde33ee4c4f3ba2057c15465e94b495e22f",
                        Material.STONE_SWORD, true),
                new MinionTier(6, 39, 576,
                        "5936fa9d8f9319ff6b20e2d25520ba97e2bbe858d579a34392631515d49cd7eb",
                        Material.IRON_SWORD, true),
                new MinionTier(7, 35, 576,
                        "6ed0ad0130e99a36cf7d78cae759965476f98025b1833cde5542f7dc42829400",
                        Material.IRON_SWORD, true),
                new MinionTier(8, 35, 768,
                        "56140d9598bef25911b283649278ac1213697609be939676b8d944e9d63577bc",
                        Material.IRON_SWORD, true),
                new MinionTier(9, 30, 768,
                        "76a551138ea5378f71f3e6440a00dab112e60ff8b9784cd2010b3ac36123d5a6",
                        Material.GOLDEN_SWORD, true),
                new MinionTier(10, 30, 960,
                        "7d63d05501d38fac8b0969deb78b68a58fe03c3e0d78cdb84ef1a9a7513ee326",
                        Material.GOLDEN_SWORD, true),
                new MinionTier(11, 24, 960,
                        "7334fc47a50b46ac9df7bf2ed712309ff0b029981368b22db0d539c88f83144f",
                        Material.DIAMOND_SWORD, true)
        );
    }

    @Override
    public Color getBootColour() {
        return new Color(34,7,45);
    }

    @Override
    public Color getLeggingsColour() {
        return new Color(34,7,45);
    }

    @Override
    public Color getChestplateColour() {
        return new Color(34,7,45);
    }

    @Override
    public List<MinionExpectation> getExpectations() {
        return List.of(
                new MobGapExpectation(3)
        );
    }

    @Override
    public MinionAction getAction() {
        return new MinionKillMobAction(MobMinionVoidling.class, MobMinionVoidling::new, DROPS);
    }
}
