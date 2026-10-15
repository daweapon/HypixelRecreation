package dev.daweapon.type.skyblockgeneric.crystalhollows;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;

import java.util.ArrayList;
import java.util.List;

public final class BundleTable {
    private static final List<String> GEMS = List.of("RUBY", "AMETHYST", "JADE", "AMBER", "SAPPHIRE", "TOPAZ");
    private static final List<BundleReward> REWARDS = build();

    private BundleTable() {
    }

    public static List<BundleReward> rewards() {
        return REWARDS;
    }

    public static List<BundleReward> meterRewards() {
        return REWARDS.stream().filter(BundleReward::hasMeter).toList();
    }

    public static BundleReward byId(String id) {
        return REWARDS.stream().filter(reward -> reward.id().equals(id)).findFirst().orElse(null);
    }

    public static double totalWeight() {
        return REWARDS.stream().mapToDouble(BundleReward::weight).sum();
    }

    private static List<BundleReward> build() {
        List<BundleReward> list = new ArrayList<>();
        list.add(BundleReward.ofItem("flawless_jasper", "Flawless Jasper Gemstone", 5, ItemType.FLAWLESS_JASPER_GEM, 1, 0));
        list.add(BundleReward.ofItem("divan_alloy", "Divan's Alloy", 17, ItemType.DIVAN_ALLOY, 1, 1_000_000));
        list.add(BundleReward.ofItem("quick_claw", "Quick Claw", 50, ItemType.PET_ITEM_QUICK_CLAW, 1, 450_000));
        list.add(BundleReward.ofItem("fine_jasper", "Fine Jasper Gemstone", 200, ItemType.FINE_JASPER_GEM, 10, 0));
        list.add(BundleReward.ofItem("treasurite_10", "Treasurite", 200, ItemType.TREASURITE, 10, 0));
        list.add(BundleReward.ofItem("recall_potion", "Recall Potion", 250, ItemType.RECALL_POTION, 1, 100_000));
        list.add(BundleReward.ofItem("wishing_compass", "Wishing Compass", 500, ItemType.WISHING_COMPASS, 3, 0));
        list.add(BundleReward.ofItem("precious_pearl", "Precious Pearl", 500, ItemType.PRECIOUS_PEARL, 1, 35_000));
        list.add(BundleReward.ofItem("claw_fossil", "Claw Fossil", 500, ItemType.CLAW_FOSSIL, 1, 50_000));
        list.add(BundleReward.ofItem("flawed_jasper_32", "Flawed Jasper Gemstone", 700, ItemType.FLAWED_JASPER_GEM, 32, 0));
        list.add(BundleReward.ofItem("gemstone_mixture", "Gemstone Mixture", 700, ItemType.GEMSTONE_MIXTURE, 1, 35_000));
        list.add(BundleReward.ofBook("fortune_iv", "Enchanted Book (Fortune IV)", 700, EnchantmentType.FORTUNE, 4, 35_000));
        list.add(BundleReward.ofEssence("gold_essence", "Gold Essence", 750, EssenceType.GOLD, 10));
        list.add(BundleReward.ofEssence("diamond_essence", "Diamond Essence", 750, EssenceType.DIAMOND, 10));
        list.add(BundleReward.ofItem("jaderald", "Jaderald", 1450, ItemType.JADERALD, 1, 20_000));
        list.add(BundleReward.ofItem("gemstone_grahams", "Dwarven O's Gemstone Grahams", 1450, ItemType.DWARVEN_OS_GEMSTONE_GRAHAMS, 1, 20_000));
        list.add(BundleReward.ofItem("flawed_jasper_24", "Flawed Jasper Gemstone", 2000, ItemType.FLAWED_JASPER_GEM, 24, 0));
        list.add(BundleReward.ofItem("treasurite_5", "Treasurite", 2000, ItemType.TREASURITE, 5, 0));
        list.add(BundleReward.ofItem("ruby_crystal", "Ruby Crystal", 2500, ItemType.RUBY_CRYSTAL, 1, 8_000));
        list.add(BundleReward.ofItem("jasper_crystal", "Jasper Crystal", 2500, ItemType.JASPER_CRYSTAL, 1, 10_000));
        list.add(BundleReward.ofItem("helix", "Helix Fossil", 4000, ItemType.HELIX, 1, 10_000));
        list.add(BundleReward.ofBook("lapidary_i", "Enchanted Book (Lapidary I)", 4000, EnchantmentType.LAPIDARY, 1, 10_000));
        list.add(BundleReward.ofItem("prehistoric_egg", "Prehistoric Egg", 5000, ItemType.PREHISTORIC_EGG, 1, 8_000));
        list.add(BundleReward.ofItem("flawed_jasper_12", "Flawed Jasper Gemstone", 5000, ItemType.FLAWED_JASPER_GEM, 12, 0));
        list.add(BundleReward.ofItem("divan_fragment", "Divan Fragment", 6000, ItemType.DIVAN_FRAGMENT, 1, 8_000));
        list.add(BundleReward.ofItem("oil_barrel", "Oil Barrel", 8000, ItemType.OIL_BARREL, 1, 0));
        list.add(BundleReward.ofItem("pickonimbus", "Pickonimbus 2000", 10_000, ItemType.PICKONIMBUS_2000, 1, 4_000));
        list.add(BundleReward.ofItem("bob_omb", "Bob-omb", 10_000, ItemType.BOB_OMB, 20, 0));
        list.add(BundleReward.ofItem("jade_dye", "Jade Dye", 0.622, ItemType.DYE_JADE, 1, 5_000_000));
        for (String gem : GEMS) {
            list.add(BundleReward.ofItem("flawless_" + gem.toLowerCase(), "Flawless " + title(gem) + " Gemstone", 15, ItemType.valueOf("FLAWLESS_" + gem + "_GEM"), 1, 0));
            list.add(BundleReward.ofItem("fine_" + gem.toLowerCase(), "Fine " + title(gem) + " Gemstone", 900, ItemType.valueOf("FINE_" + gem + "_GEM"), 1, 0));
            list.add(BundleReward.ofItem("flawed_" + gem.toLowerCase() + "_48", "Flawed " + title(gem) + " Gemstone", 2000, ItemType.valueOf("FLAWED_" + gem + "_GEM"), 48, 0));
            list.add(BundleReward.ofItem("flawed_" + gem.toLowerCase() + "_24", "Flawed " + title(gem) + " Gemstone", 5000, ItemType.valueOf("FLAWED_" + gem + "_GEM"), 24, 0));
            list.add(BundleReward.ofItem("flawed_" + gem.toLowerCase() + "_12", "Flawed " + title(gem) + " Gemstone", 10_000, ItemType.valueOf("FLAWED_" + gem + "_GEM"), 12, 0));
            list.add(BundleReward.ofItem("flawed_" + gem.toLowerCase() + "_6", "Flawed " + title(gem) + " Gemstone", 20_000, ItemType.valueOf("FLAWED_" + gem + "_GEM"), 6, 0));
        }
        return List.copyOf(list);
    }

    private static String title(String gem) {
        return gem.charAt(0) + gem.substring(1).toLowerCase();
    }
}
