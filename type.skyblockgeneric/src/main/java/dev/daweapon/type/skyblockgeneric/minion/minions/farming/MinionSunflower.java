package dev.daweapon.type.skyblockgeneric.minion.minions.farming;

import net.minestom.server.color.Color;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;
import dev.daweapon.type.skyblockgeneric.minion.actions.MinionCropAction;
import dev.daweapon.type.skyblockgeneric.minion.crop.CropFieldExpectation;
import dev.daweapon.type.skyblockgeneric.minion.crop.MinionCrop;

import java.util.List;

public class MinionSunflower extends SkyBlockMinion {
    @Override
    public List<MinionTier> getTiers() {
        return List.of(
                new MinionTier(1, 24, 128,
                        "1400c11dd48e403faa03f930b2134b8d8b0e7716c876aa1997f2eb4e548d8a10",
                        Material.WOODEN_HOE, true),
                new MinionTier(2, 23, 192,
                        "ab0c6e7cf074823c4fc671498e8342e84335b4629220a5fd60a57fa69267d11",
                        Material.WOODEN_HOE, true),
                new MinionTier(3, 22, 320,
                        "c2f526cdb367312685a3debe1fdbfb8cec2441c9ebd3e56352cfb89380ecfbb0",
                        Material.STONE_HOE, true),
                new MinionTier(4, 21, 384,
                        "16c16c8d093cbdbc58870edf1b1b2e8033614d780e0267b4766d0fff7f2c2ff2",
                        Material.STONE_HOE, true),
                new MinionTier(5, 20, 448,
                        "b005e21c6e2c5aae5d9837135cb31566e70a98d165eb27923a62f0798ddc1d80",
                        Material.STONE_HOE, true),
                new MinionTier(6, 19, 512,
                        "35f08da54ee757395b5f8e1b1bef53ffd70b9d5de8ef006cba1e31de75fbd81f",
                        Material.IRON_HOE, true),
                new MinionTier(7, 18, 576,
                        "bb871af7dcce83075f464bba026046ae629552328c4fd0ec12c4e34702e7dc60",
                        Material.IRON_HOE, true),
                new MinionTier(8, 17, 704,
                        "64635607d2c0f1e45f3a80811794249a15f0dc7d94a11fe60280224b2b568b67",
                        Material.IRON_HOE, true),
                new MinionTier(9, 16, 768,
                        "80eb24c3ad6a16a415a0129deb7490841622f302279d63b0aa38a63cbfd82c7c",
                        Material.GOLDEN_HOE, true),
                new MinionTier(10, 15, 896,
                        "68ef4e4003973166159fd837e83423c6adb0f0cebac8ada41d5478f10d81e9c0",
                        Material.GOLDEN_HOE, true),
                new MinionTier(11, 14, 960,
                        "adc8be040b3448a42ef34f47e8af305379ca639911b864e9fa36cdfe3e7aac7f",
                        Material.DIAMOND_HOE, true),
                new MinionTier(12, 13, 960,
                        "d0e80c28668bad1f81a165b67810ead1a8607c0721e0d0ec68fe8ae9f2133471",
                        Material.DIAMOND_HOE, false)
        );
    }

    @Override
    public Color getBootColour() {
        return new Color(250,200,30);
    }

    @Override
    public Color getLeggingsColour() {
        return new Color(250,200,30);
    }

    @Override
    public Color getChestplateColour() {
        return new Color(250,200,30);
    }

    @Override
    public List<MinionExpectation> getExpectations() {
        return List.of(new CropFieldExpectation(MinionCrop.SUNFLOWER));
    }

    @Override
    public MinionAction getAction() {
        return new MinionCropAction(MinionCrop.SUNFLOWER);
    }
}
