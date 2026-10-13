package net.swofty.type.skyblockgeneric.garden.pest;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import net.swofty.type.skyblockgeneric.crimson.mob.MobDrop;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.garden.farm.GardenFarm;
import net.swofty.type.skyblockgeneric.garden.feast.FeastService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class PestRewards {
    private PestRewards() {
    }

    public static int guaranteedAmount(PestCatalogue.Pest pest, double fortune) {
        return pest.base() + (int) Math.floor(Math.max(0, fortune) / pest.perFortune());
    }

    public static void award(SkyBlockPlayer player, GardenProfile profile, PestCatalogue.Pest pest) {
        PestCatalogue.Universal universal = PestCatalogue.get().universal();
        if (pest.crop() != null) {
            String itemId = pest.altItem() != null && ThreadLocalRandom.current().nextBoolean() ? pest.altItem() : pest.item();
            ItemType item = ItemType.get(itemId);
            if (item != null) {
                int amount = guaranteedAmount(pest, GardenFarm.fortune(player, profile, pest.crop()));
                give(player, new SkyBlockItem(item, amount));
                player.sendMessage(Text.of("<7>You received <a>{}x {} <7>for killing a <2>{}<7>!",
                        StringUtility.commaify(amount), new SkyBlockItem(item).getDisplayNameText(), pest.name()));
            }
        } else if (pest.id().equals("FIELD_MOUSE")) {
            awardFieldMouse(player, profile);
        }
        if (!pest.id().equals("FIELD_MOUSE")) {
            player.addCoins(universal.coins());
            player.getSkills().increase(player, SkillCategories.FARMING, (double) universal.farmingXp());
            GardenService.addGardenXp(player, universal.gardenXp());
            player.addBits(universal.bits());
            rollMaterial(player, universal);
        }
        rollRare(player, pest);
        FeastService.onPestKill(player, pest.crop(), pest.id().equals("FIELD_MOUSE"));
    }

    private static void awardFieldMouse(SkyBlockPlayer player, GardenProfile profile) {
        player.addCoins(10_000);
        player.getSkills().increase(player, SkillCategories.FARMING, 6000D);
        GardenService.addGardenXp(player, 50);
        player.addBits(PestCatalogue.get().universal().bits());
        for (String id : PestCatalogue.get().universal().oneOf()) {
            ItemType type = ItemType.get(id);
            if (type != null) give(player, new SkyBlockItem(type, 1));
        }
        List<PestCatalogue.Pest> crops = PestCatalogue.get().natural();
        PestCatalogue.Pest pick = crops.get(ThreadLocalRandom.current().nextInt(crops.size()));
        ItemType item = ItemType.get(pick.item());
        if (item != null) {
            int amount = (int) Math.floor(guaranteedAmount(pick, GardenFarm.fortune(player, profile, pick.crop())) * 1.5);
            give(player, new SkyBlockItem(item, amount));
        }
    }

    private static void rollMaterial(SkyBlockPlayer player, PestCatalogue.Universal universal) {
        List<ItemType> items = universal.oneOf().stream().map(ItemType::get).toList();
        if (items.stream().anyMatch(java.util.Objects::isNull)) items = items.stream().filter(java.util.Objects::nonNull).toList();
        ItemType forced = LootRoll.claim(player.getUuid(), items);
        ItemType won = forced;
        if (won == null) {
            double roll = ThreadLocalRandom.current().nextDouble() * 100;
            int index = (int) Math.floor(roll / universal.oneOfChance());
            if (index < items.size()) won = items.get(index);
        }
        if (won != null) give(player, new SkyBlockItem(won, 1));
    }

    private static void rollRare(SkyBlockPlayer player, PestCatalogue.Pest pest) {
        for (MobDrop drop : pest.rare()) {
            List<ItemType> items = drop.availableItems();
            if (items.isEmpty()) continue;
            ItemType won = CrimsonMobLoot.decide(player.getUuid(), items, drop.chancePercent(), ThreadLocalRandom.current());
            if (won == null) continue;
            SkyBlockItem item = create(won, drop);
            give(player, item);
            player.sendMessage(Text.of("<6><l>RARE DROP!</l> {}", item.getDisplayNameText()));
        }
    }

    private static SkyBlockItem create(ItemType type, MobDrop drop) {
        SkyBlockItem item = new SkyBlockItem(type, drop.rollAmount());
        if (drop.rarity() != null) item.getAttributeHandler().setRarity(drop.rarity());
        EnchantmentType enchantment = drop.enchantmentType();
        if (enchantment != null) {
            item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(enchantment, drop.enchantmentLevel()));
        }
        return item;
    }

    public static void give(SkyBlockPlayer player, SkyBlockItem item) {
        @Nullable ItemType type = item.getAttributeHandler().getPotentialType();
        if (type != null && player.canInsertItemIntoSacks(type, item.getAmount())) {
            player.getSackItems().increase(type, item.getAmount());
        } else {
            player.addAndUpdateItem(item);
        }
    }
}
