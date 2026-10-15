package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MiningOffCamera {
    private MiningOffCamera() {
    }

    public static List<ItemType> enchantedMiningItems() {
        return Arrays.stream(CollectionCategories.MINING.getCategory().getCollections())
                .map(CollectionCategory.ItemCollection::type)
                .map(type -> ItemType.get("ENCHANTED_" + type.name()))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public static void collectionProgressed(SkyBlockPlayer player, ItemType collected, int amount) {
        if (amount <= 0 || !isMining(collected)) return;
        double chance = PetPerkMath.chancePerUnit(
                PetAbilities.value(player, ItemType.PRECURSOR_DRONE_PET, "Mining Off Camera", 0), amount);
        if (chance <= 0) return;
        List<ItemType> pool = enchantedMiningItems();
        if (pool.isEmpty()) return;
        ItemType forced = LootRoll.claim(player.getUuid(), pool);
        if (forced == null && !LootRoll.roll(null, null, chance)) return;
        ItemType dropped = forced != null ? forced : pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        player.addAndUpdateItem(dropped, 1);
    }

    private static boolean isMining(ItemType type) {
        return Arrays.stream(CollectionCategories.MINING.getCategory().getCollections())
                .anyMatch(collection -> collection.type() == type);
    }
}
