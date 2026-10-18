package dev.daweapon.type.skyblockgeneric.minion.minions.slayer;

import net.minestom.server.color.Color;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.minionMobs.MobMinionRevenant;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;
import dev.daweapon.type.skyblockgeneric.minion.actions.MinionKillMobAction;
import dev.daweapon.type.skyblockgeneric.minion.drops.MinionDrop;
import dev.daweapon.type.skyblockgeneric.minion.drops.MinionDropTable;

import java.util.List;

public class MinionRevenant extends SkyBlockMinion {
    public static final MinionDropTable DROPS = MinionDropTable.of(
            MinionDrop.of(ItemType.ROTTEN_FLESH, 2),
            MinionDrop.chance(ItemType.ROTTEN_FLESH, 1, 50),
            MinionDrop.chance(ItemType.ROTTEN_FLESH, 2, 100.0 / 3),
            MinionDrop.chance(ItemType.DIAMOND, 1, 20)
    );

    @Override
    public List<MinionTier> getTiers() {
        return List.of(
                new MinionTier(1, 29, 64,
                        "a3dce8555923558d8d74c2a2b261b2b2d630559db54ef97ed3f9c30e9a20aba",
                        Material.WOODEN_SWORD, true),
                new MinionTier(2, 29, 192,
                        "c5aff1b4f533bb1e1cf5ea96caea7349d5efe9e9a982ec8051ac32910e3ae68c",
                        Material.WOODEN_SWORD, true),
                new MinionTier(3, 26, 192,
                        "d3865482377fb54bc07dc7633a5a25bbaaecc3e9978c04bf608776da1f8a154a",
                        Material.STONE_SWORD, true),
                new MinionTier(4, 26, 384,
                        "e138071b15709fd98c89597abddeefb70bee370fdcce7da9cbfa7275f2421557",
                        Material.STONE_SWORD, true),
                new MinionTier(5, 23, 384,
                        "73de056cedbd88c61cff93c1f97e4cc69f0dafcdac3ca62013e3c0527fb5245",
                        Material.STONE_SWORD, true),
                new MinionTier(6, 23, 576,
                        "d51616207cb10c6414aa7812e56e3f4b408eac7c5ddc9011fe794b41b7ae7c24",
                        Material.IRON_SWORD, true),
                new MinionTier(7, 19, 576,
                        "4097b94aecc2b187fcc251b2d2273554f66853436fa8ecc8e5156b148004c804",
                        Material.IRON_SWORD, true),
                new MinionTier(8, 19, 768,
                        "57228dedbff9e114d5e4ce7f8e39c3bfb07f2c2f121545a8dc7803dfc0484786",
                        Material.IRON_SWORD, true),
                new MinionTier(9, 14.5f, 768,
                        "e7eb574b6ab8b394c6b4a112ae18d5a672ffa414ec4dff3d65c9950523c19e0a",
                        Material.GOLDEN_SWORD, true),
                new MinionTier(10, 14.5f, 960,
                        "d0197c8a4eaca2e5cc1b287ac84c62ef8c9f63068218105292dd89c3f7e64596",
                        Material.GOLDEN_SWORD, true),
                new MinionTier(11, 10, 960,
                        "9cf6f95308bedb182b434aa73058aa8d69818b48900396cebc127c1bf7df6790",
                        Material.DIAMOND_SWORD, true),
                new MinionTier(12, 8, 960,
                        "10243a93beca837c15839e938575a5ceb90d14a745686a9a0c4456e3698e32f8",
                        Material.DIAMOND_SWORD, false)
        );
    }

    @Override
    public Color getBootColour() {
        return new Color(32,178,122);
    }

    @Override
    public Color getLeggingsColour() {
        return new Color(32,178,122);
    }

    @Override
    public Color getChestplateColour() {
        return new Color(32,178,122);
    }

    @Override
    public List<MinionExpectation> getExpectations() {
        return List.of(
                new MobGapExpectation(2)
        );
    }

    @Override
    public MinionAction getAction() {
        return new MinionKillMobAction(MobMinionRevenant.class, MobMinionRevenant::new, DROPS);
    }
}
