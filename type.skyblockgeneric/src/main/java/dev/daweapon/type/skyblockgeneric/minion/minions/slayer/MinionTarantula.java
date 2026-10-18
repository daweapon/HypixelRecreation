package dev.daweapon.type.skyblockgeneric.minion.minions.slayer;

import net.minestom.server.color.Color;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.minionMobs.MobMinionTarantula;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;
import dev.daweapon.type.skyblockgeneric.minion.actions.MinionKillMobAction;
import dev.daweapon.type.skyblockgeneric.minion.drops.MinionDrop;
import dev.daweapon.type.skyblockgeneric.minion.drops.MinionDropTable;

import java.util.List;

public class MinionTarantula extends SkyBlockMinion {
    public static final MinionDropTable DROPS = MinionDropTable.of(
            MinionDrop.of(ItemType.STRING, 2),
            MinionDrop.chance(ItemType.STRING, 1, 50),
            MinionDrop.chance(ItemType.STRING, 2, 100.0 / 3),
            MinionDrop.of(ItemType.SPIDER_EYE, 1),
            MinionDrop.chance(ItemType.IRON_INGOT, 1, 20)
    );

    @Override
    public List<MinionTier> getTiers() {
        return List.of(
                new MinionTier(1, 29, 64,
                        "97e86007064c9ce26eb4bad8ac9aa30aac309e70a9e0b615936318dea40a721",
                        Material.WOODEN_SWORD, true),
                new MinionTier(2, 29, 192,
                        "578fea239bb3881ae53d6c735b8af69d8c6b477c0f5c34bc7cbed5792869ca67",
                        Material.WOODEN_SWORD, true),
                new MinionTier(3, 26, 192,
                        "fc398914acb7fce5d93c2002a258f23b795d2f20d8e5fc555acd5070662efa0b",
                        Material.STONE_SWORD, true),
                new MinionTier(4, 26, 384,
                        "bafde429ffcd5141f42b3d754d75a0ad3528594509c09be0083bc2c98d38fdce",
                        Material.STONE_SWORD, true),
                new MinionTier(5, 23, 384,
                        "f78b57faf9b4935932b749e10a2fff66532fecdede5a4e58f80d6f6ace2ed7ed",
                        Material.STONE_SWORD, true),
                new MinionTier(6, 23, 576,
                        "ba4c2f24b79f98133f9fd66760685a18d4c29e415cef0b62e67e957085b3875b",
                        Material.IRON_SWORD, true),
                new MinionTier(7, 19, 576,
                        "7affad96dbfb4d5bfd4dc73d4dc1295db0062cbbd0c967b7da39bcc6e051b2e",
                        Material.IRON_SWORD, true),
                new MinionTier(8, 19, 768,
                        "cb0bdd9de5c6d56f3f3341ed7bc07d17f372d5f21b32fbb9cdc67ec7096a7cf0",
                        Material.IRON_SWORD, true),
                new MinionTier(9, 14.5f, 768,
                        "13cb3afa7d81b71751a246278d4f8f3a406a80a1302291ac620fc42c6cf2c179",
                        Material.GOLDEN_SWORD, true),
                new MinionTier(10, 14.5f, 960,
                        "535cc5773ffb461bc491270af45aa14cda6d7d92a4cc8c12b2b188620a2a44e4",
                        Material.GOLDEN_SWORD, true),
                new MinionTier(11, 10, 960,
                        "9c4d0dfb09516a79b286a9e8c67c4e981f245ff6221f470f6452fdafc0a92749",
                        Material.DIAMOND_SWORD, true),
                new MinionTier(12, 8, 960,
                        "84beb7c90f87d54b8a2e9a25d762aef2960f563758c5f0b3d4cce0c86a22d85c",
                        Material.DIAMOND_SWORD, false)
        );
    }

    @Override
    public Color getBootColour() {
        return new Color(137,103,11);
    }

    @Override
    public Color getLeggingsColour() {
        return new Color(137,103,11);
    }

    @Override
    public Color getChestplateColour() {
        return new Color(137,103,11);
    }

    @Override
    public List<MinionExpectation> getExpectations() {
        return List.of(
                new MobGapExpectation(1)
        );
    }

    @Override
    public MinionAction getAction() {
        return new MinionKillMobAction(MobMinionTarantula.class, MobMinionTarantula::new, DROPS);
    }
}
