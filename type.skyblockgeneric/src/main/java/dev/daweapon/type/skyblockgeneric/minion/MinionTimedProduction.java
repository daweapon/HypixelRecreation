package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.MinionFuelComponent;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MinionTimedProduction {
    private static final long MAX_OFFLINE_ITEMS = 10_000;
    private static final double VOIDLING_BONUS_PER_TIER = 0.03;
    private static final String VOIDLING = "VOIDLING";

    public record Source(ItemType upgrade, ItemType produced, long onlineMillis, long offlineMillis,
                         boolean fuelMultiplied, boolean voidlingBonus) {
    }

    public static final List<Source> SOURCES = List.of(
            new Source(ItemType.LESSER_SOULFLOW_ENGINE, ItemType.RAW_SOULFLOW,
                    Duration.ofSeconds(180).toMillis(), Duration.ofSeconds(185).toMillis(), true, false),
            new Source(ItemType.SOULFLOW_ENGINE, ItemType.RAW_SOULFLOW,
                    Duration.ofSeconds(90).toMillis(), Duration.ofSeconds(105).toMillis(), true, true),
            new Source(ItemType.SUPER_JACOB_SYSTEM, ItemType.JACOBS_TICKET,
                    Duration.ofHours(3).toMillis(), Duration.ofHours(3).toMillis(), false, false));

    private static final Map<String, Long> NEXT_AT = new ConcurrentHashMap<>();

    private MinionTimedProduction() {
    }

    public static void tick(IslandMinionData.IslandMinion minion, long now) {
        for (Source source : SOURCES) {
            if (!minion.getExtensionData().hasMinionUpgrade(source.upgrade())) continue;
            String key = minion.getMinionUUID() + ":" + source.upgrade().name();
            Long next = NEXT_AT.get(key);
            if (next == null || now - next > source.onlineMillis()) {
                NEXT_AT.put(key, now + source.onlineMillis());
                continue;
            }
            if (now < next) continue;
            NEXT_AT.put(key, now + source.onlineMillis());
            produce(minion, source, 1);
        }
    }

    public static void creditOffline(IslandMinionData.IslandMinion minion, long elapsedMillis) {
        for (Source source : SOURCES) {
            if (!minion.getExtensionData().hasMinionUpgrade(source.upgrade())) continue;
            long count = Math.min(MAX_OFFLINE_ITEMS, elapsedMillis / source.offlineMillis());
            for (long i = 0; i < count; i++) {
                if (!produce(minion, source, 1)) break;
            }
        }
    }

    public static double multiplier(IslandMinionData.IslandMinion minion, Source source) {
        double multiplier = 1.0;
        if (source.fuelMultiplied()) {
            SkyBlockItem fuel = minion.getExtensionData().getFuel();
            if (fuel != null) multiplier *= fuel.getComponent(MinionFuelComponent.class).getOutputMultiplier();
        }
        if (source.voidlingBonus() && minion.getMinion().name().equals(VOIDLING)) {
            multiplier *= 1 + VOIDLING_BONUS_PER_TIER * minion.getTier();
        }
        return multiplier;
    }

    private static boolean produce(IslandMinionData.IslandMinion minion, Source source, int base) {
        int amount = MinionBonusDrops.scaledAmount(base, multiplier(minion, source), source.produced());
        if (amount <= 0) return true;
        boolean stored = minion.addItem(new SkyBlockItem(source.produced(), amount));
        if (stored && source.produced() == ItemType.RAW_SOULFLOW) compactSoulflow(minion);
        return stored;
    }

    private static void compactSoulflow(IslandMinionData.IslandMinion minion) {
        var upgrades = minion.getExtensionData();
        if (!upgrades.hasMinionUpgrade(ItemType.SUPER_COMPACTOR_3000) && !upgrades.hasMinionUpgrade(ItemType.DWARVEN_COMPACTOR)) return;
        List<ItemQuantifiable> stored = minion.getItemsInMinion();
        for (ItemQuantifiable entry : stored) {
            if (entry.getItem().getAttributeHandler().getPotentialType() != ItemType.RAW_SOULFLOW) continue;
            int crafted = MinionCompaction.enchantedCount(entry.getAmount());
            if (crafted == 0) return;
            entry.setAmount(entry.getAmount() - crafted * MinionCompaction.ENCHANTED_COST);
            ItemQuantifiable existing = stored.stream()
                    .filter(candidate -> candidate.getItem().getAttributeHandler().getPotentialType() == ItemType.SOULFLOW)
                    .findFirst().orElse(null);
            if (existing != null) existing.setAmount(existing.getAmount() + crafted);
            else stored.add(new ItemQuantifiable(ItemType.SOULFLOW, crafted));
            stored.removeIf(candidate -> candidate.getAmount() <= 0);
            return;
        }
    }
}
