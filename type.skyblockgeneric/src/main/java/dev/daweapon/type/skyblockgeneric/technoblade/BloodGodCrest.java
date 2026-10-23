package dev.daweapon.type.skyblockgeneric.technoblade;

import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeCrestKills;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.type.skyblockgeneric.accessory.OwnedAccessory;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class BloodGodCrest {
    public static final String HANDLER = "BLOOD_GOD_CREST";
    public static final int MAX_STRENGTH = 7;
    private static final String ATTRIBUTE = "crest_kills";
    private static final int MAX_BAG_SLOTS = 400;

    private BloodGodCrest() {
    }

    public static void registerLore() {
        LoreRegistry.register(HANDLER, new LoreConfig((item, player) -> lore(item), null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    public static int strengthFor(long kills) {
        if (kills <= 0) return 0;
        return Math.min(MAX_STRENGTH, Long.toString(kills).length());
    }

    public static long kills(SkyBlockItem item) {
        ItemAttributeCrestKills attribute = (ItemAttributeCrestKills) item.getAttribute(ATTRIBUTE);
        return attribute == null ? 0 : attribute.getValue();
    }

    private static void setKills(SkyBlockItem item, long kills) {
        ((ItemAttributeCrestKills) item.getAttribute(ATTRIBUTE)).setValue(kills);
    }

    private static boolean isCrest(SkyBlockItem item) {
        return !item.isAir() && !item.isNA() && item.getAttributeHandler().getPotentialType() == ItemType.BLOOD_GOD_CREST;
    }

    public static ItemStatistics statistics(SkyBlockItem item) {
        if (!isCrest(item)) return ItemStatistics.empty();
        int strength = strengthFor(kills(item));
        if (strength == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.STRENGTH, (double) strength).build();
    }

    static List<String> lore(SkyBlockItem item) {
        long kills = kills(item);
        return List.of("", "<7>Counter: <c>" + (kills == 0 ? "None yet!" : StringUtility.commaify(kills)));
    }

    public static void onKill(SkyBlockPlayer player) {
        for (OwnedAccessory owned : AccessoryOwnership.active(player)) {
            if (owned.type() != ItemType.BLOOD_GOD_CREST) continue;
            if (owned.inBag() ? countInBag(player) : countInInventory(player)) AccessoryOwnership.invalidate(player);
            return;
        }
    }

    private static boolean countInBag(SkyBlockPlayer player) {
        DatapointAccessoryBag.PlayerAccessoryBag bag = player.getAccessoryBag();
        for (int slot = 0; slot < MAX_BAG_SLOTS; slot++) {
            SkyBlockItem item = bag.getInSlot(slot);
            if (item == null || !isCrest(item)) continue;
            setKills(item, kills(item) + 1);
            bag.setInSlot(slot, item);
            player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ACCESSORY_BAG, DatapointAccessoryBag.class).setValue(bag);
            return true;
        }
        return false;
    }

    private static boolean countInInventory(SkyBlockPlayer player) {
        for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            if (!isCrest(new SkyBlockItem(stack))) continue;
            player.updateItemInSlot(slot, item -> setKills(item, kills(item) + 1));
            return true;
        }
        return false;
    }
}
