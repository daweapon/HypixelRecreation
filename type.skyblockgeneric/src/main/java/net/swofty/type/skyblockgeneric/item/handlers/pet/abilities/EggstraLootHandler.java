package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.entity.drop.ItemDrops;
import net.swofty.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.loottable.ForcedDrops;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;

/**
 * Chicken's Eggstra Loot: chickens always drop an Egg, and {0}% of animal kills roll their loot
 * table a second time.
 */
public final class EggstraLootHandler implements PetAbilityHandler {
    private static final Set<EntityType> ANIMALS = Set.of(
            EntityType.CHICKEN, EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.RABBIT);

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.Kill kill)) return;
        SkyBlockMob mob = kill.mob();
        if (mob.getEntityType() == EntityType.CHICKEN) give(kill.player(), new SkyBlockItem(ItemType.EGG), mob);
        if (ANIMALS.contains(mob.getEntityType())) extraLoot(kill.player(), mob, context.value(0));
    }

    private static void extraLoot(SkyBlockPlayer player, SkyBlockMob mob, double chance) {
        SkyBlockLootTable table = mob.getLootTable();
        if (table == null) return;
        boolean forced = ForcedDrops.isForced(player.getUuid(), table.getLootTableItems());
        if (!forced && !LootRoll.roll(null, null, chance)) return;

        for (Map.Entry<ItemType, SkyBlockLootTable.LootRecord> entry : table.runChances(player).entrySet()) {
            SkyBlockLootTable.LootRecord record = entry.getValue();
            if (SkyBlockLootTable.LootRecord.isNone(record)) continue;
            give(player, new SkyBlockItem(entry.getKey(), record.getAmount()), mob);
        }
    }

    private static void give(SkyBlockPlayer player, SkyBlockItem item, SkyBlockMob mob) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type != null && player.canInsertItemIntoSacks(type, item.getAmount())) {
            player.getSackItems().increase(type, item.getAmount());
        } else if (player.getSkyBlockExperience().getLevel().asInt() >= 6) {
            player.addAndUpdateItem(item);
        } else {
            ItemDrops.dropAt(new DroppedItemEntityImpl(item, player), mob.getInstance(), mob.getPosition().add(0, 0.5, 0));
        }
    }
}
