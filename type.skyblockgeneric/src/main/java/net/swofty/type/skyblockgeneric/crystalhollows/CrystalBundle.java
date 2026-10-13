package net.swofty.type.skyblockgeneric.crystalhollows;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.essence.EssencePerks;
import net.swofty.type.skyblockgeneric.essence.EssenceShops;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class CrystalBundle {
    public static final int MIN_ROLLS = 14;
    public static final int MAX_ROLLS = 19;
    public static final long HOTM_XP = 800;
    public static final int HOTM_XP_TIER_CAP = 10;
    public static final long POWDER_MIN = 2_000;
    public static final long POWDER_MAX = 7_000;
    private static final AttributeId BIGGER_BOX = AttributeId.parse("U7");

    private CrystalBundle() {
    }

    public static void award(SkyBlockPlayer player) {
        player.sendMessage(Text.of("<a><l>NUCLEUS COMPLETE! <a>You've earned a Crystal Loot Bundle!"));
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, BundleReward> seen = new LinkedHashMap<>();
        for (NucleusCrystal crystal : NucleusCrystal.values()) {
            player.addAndUpdateItem(new SkyBlockItem(crystal.getFineGem(), 1));
        }
        player.addAndUpdateItem(new SkyBlockItem(ItemType.FINE_RUBY_GEM, 1));
        player.addAndUpdateItem(new SkyBlockItem(ItemType.FINE_JASPER_GEM, 1));

        int rolls = ThreadLocalRandom.current().nextInt(MIN_ROLLS, MAX_ROLLS + 1) + extraRolls(player);
        List<BundleReward> table = BundleTable.rewards();
        BundleReward target = NucleusRngMeter.selected(player);
        boolean hitTarget = false;
        for (int i = 0; i < rolls; i++) {
            BundleReward reward = LootRoll.pick(player.getUuid(), table, BundleReward::item,
                    entry -> NucleusRngMeter.weight(player, entry), ThreadLocalRandom.current());
            reward.give(player);
            counts.merge(reward.id(), 1, Integer::sum);
            seen.putIfAbsent(reward.id(), reward);
            if (target != null && reward.id().equals(target.id())) hitTarget = true;
        }

        player.sendMessage(Text.of("<7>Fine Ruby, Amethyst, Jade, Amber, Sapphire, Topaz and Jasper Gemstones"));
        seen.forEach((id, reward) -> player.sendMessage(Text.of("  <8>{}x <f>{}", counts.get(id) * reward.amount(), reward.name())));
        if (hitTarget) {
            NucleusRngMeter.reset(player);
            player.sendMessage(Text.of("<d><l>RNG METER! <7>You received your selected drop: <f>{}", target.name()));
        }
        awardProgress(player);
    }

    private static void awardProgress(SkyBlockPlayer player) {
        if (HotmService.data(player).getTier() >= HOTM_XP_TIER_CAP) {
            long powder = ThreadLocalRandom.current().nextLong(POWDER_MIN, POWDER_MAX + 1);
            long gained = HotmService.gainPowder(player, HotmPowder.GEMSTONE, powder);
            player.sendMessage(Text.of("<7>You also earned <d>{} Gemstone Powder<7>.", String.format("%,d", gained)));
        } else {
            HotmService.addExperience(player, HOTM_XP);
            player.sendMessage(Text.of("<7>You also earned <5>{} Heart of the Mountain XP<7>.", HOTM_XP));
        }
    }

    public static int extraRolls(SkyBlockPlayer player) {
        int extra = highRoller(player);
        double mole = PetAbilities.value(player, ItemType.MOLE_PET, "Nucleic Explorer", 0);
        if (mole > 0 && LootRoll.roll(player.getUuid(), null, mole)) extra++;
        double box = AttributeEffectService.value(player.getHuntingData(), BIGGER_BOX);
        if (box > 0 && LootRoll.roll(player.getUuid(), null, box)) extra++;
        return extra;
    }

    private static int highRoller(SkyBlockPlayer player) {
        return EssenceShops.shop(EssenceType.DIAMOND)
                .flatMap(shop -> shop.perks().stream().filter(perk -> perk.id().equals("high_roller")).findFirst())
                .map(perk -> (int) EssencePerks.value(player, perk))
                .orElse(0);
    }
}
