package dev.daweapon.type.skyblockgeneric.minion.minions.fishing;

import net.minestom.server.color.Color;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;
import dev.daweapon.type.skyblockgeneric.minion.actions.MinionCropAction;
import dev.daweapon.type.skyblockgeneric.minion.crop.CropFieldExpectation;
import dev.daweapon.type.skyblockgeneric.minion.crop.MinionCrop;

import java.util.List;

public class MinionLilyPad extends SkyBlockMinion {
    @Override
    public List<MinionTier> getTiers() {
        return List.of(
                new MinionTier(1, 37.5f, 64,
                        "413cca52523499174e53d7719d116f2a85e5953e063e426f71b1bd9e66dc395e",
                        Material.FISHING_ROD, true),
                new MinionTier(2, 37.5f, 192,
                        "5cd8d275821b2717e0719262a4af605bd3210f26f85048dbf601a53f917e52d1",
                        Material.FISHING_ROD, true),
                new MinionTier(3, 33.5f, 192,
                        "4a422bb27c063a3ad9d7f79fc4799899d36e1be16f13d886f14f2a127338071d",
                        Material.FISHING_ROD, true),
                new MinionTier(4, 33.5f, 384,
                        "b0aaf25f80cc5546a67244f5c0746b72cc36a4dd04b438b58fd4cbcce2403aeb",
                        Material.FISHING_ROD, true),
                new MinionTier(5, 29.5f, 384,
                        "b033fe05ddf246a2c82e772b7e54d94e056dd288a3a83dff3264d2989c90cdac",
                        Material.FISHING_ROD, true),
                new MinionTier(6, 29.5f, 576,
                        "1696aee203057e944377595b901d02d07d34f92dba1310fbd5951d8b396033a7",
                        Material.FISHING_ROD, true),
                new MinionTier(7, 25.5f, 576,
                        "38261443eb0c0dddbb56f187b730ade7073c6c61e5e59c3820252891b07327c6",
                        Material.FISHING_ROD, true),
                new MinionTier(8, 25.5f, 768,
                        "a7fc6fbf43a5312f9d9b8de5bf2def6dc2360c5bf39010ef58334ba86a61169c",
                        Material.FISHING_ROD, true),
                new MinionTier(9, 21.5f, 768,
                        "f08e9e38c89e55e319ad035b809f48aa9fe392d7f67b6cacbab1282f2ab95f45",
                        Material.FISHING_ROD, true),
                new MinionTier(10, 21.5f, 960,
                        "5e49f551c5709a4a28b458fd46fd696bbc5534e46489ee99fd70328aab65379d",
                        Material.FISHING_ROD, true),
                new MinionTier(11, 17.5f, 960,
                        "d20f8db73e70fba2bbcb350e9636174c4542e5e627747c5213e7c8133559d409",
                        Material.FISHING_ROD, true),
                new MinionTier(12, 15, 960,
                        "e08f466277f29c9f1ce6bf5d78f1846b5bf86e4edd8042d4bc2162aa4dd64875",
                        Material.FISHING_ROD, false)
        );
    }

    @Override
    public Color getBootColour() {
        return new Color(58,140,60);
    }

    @Override
    public Color getLeggingsColour() {
        return new Color(58,140,60);
    }

    @Override
    public Color getChestplateColour() {
        return new Color(58,140,60);
    }

    @Override
    public List<MinionExpectation> getExpectations() {
        return List.of(new CropFieldExpectation(MinionCrop.LILY_PAD));
    }

    @Override
    public MinionAction getAction() {
        return new MinionCropAction(MinionCrop.LILY_PAD);
    }
}
