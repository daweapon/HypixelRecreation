package dev.daweapon.type.skyblockgeneric.trapper;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryBag;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.type.skyblockgeneric.accessory.OwnedAccessory;
import dev.daweapon.type.skyblockgeneric.accessory.handler.UsableAccessory;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.equipment.EquipmentBonuses;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

import java.util.Optional;

public final class TrapperRewards {
    public static final int CREST_UNCOMMON_AT = 100;
    public static final int CREST_RARE_AT = 500;
    public static final double CREST_ODDS_BONUS = 1.1;
    private static final String CREST_PELTS = "crestPelts";

    private TrapperRewards() {
    }

    public static int multiplierHundredths(Rarity crestRarity) {
        return switch (crestRarity) {
            case COMMON -> 110;
            case UNCOMMON -> 125;
            default -> 150;
        };
    }

    public static boolean hasBelt(SkyBlockPlayer player) {
        return EquipmentBonuses.wornItems(player).containsKey("PELT_BELT");
    }

    public static boolean hasPlushie(SkyBlockPlayer player) {
        return UsableAccessory.usable(player, ItemType.JAKE_PLUSHIE);
    }

    public static Optional<OwnedAccessory> crest(SkyBlockPlayer player) {
        return AccessoryOwnership.active(player).stream().filter(owned -> owned.type() == ItemType.TRAPPER_CREST).findFirst();
    }

    public static boolean crestAtTopTier(SkyBlockPlayer player) {
        return crest(player).map(owned -> owned.item().getAttributeHandler().getRarity().ordinal() >= Rarity.RARE.ordinal()).orElse(false);
    }

    public static int peltsFor(SkyBlockPlayer player, TrapperRarity rarity) {
        int flat = rarity.getPelts() + (hasBelt(player) ? 1 : 0) + (hasPlushie(player) ? 1 : 0);
        int multiplier = crest(player).map(owned -> multiplierHundredths(owned.item().getAttributeHandler().getEffectiveRarity())).orElse(100);
        return (flat * multiplier + 99) / 100;
    }

    public static void creditCrest(SkyBlockPlayer player, int pelts) {
        if (crest(player).isEmpty()) return;
        var datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class);
        DatapointHunting.HuntingData data = datapoint.getValue();
        data.setCounter(CREST_PELTS, data.counter(CREST_PELTS) + pelts);
        datapoint.setValue(data);
        syncCrest(player, data.counter(CREST_PELTS));
    }

    public static long crestPelts(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class).getValue().counter(CREST_PELTS);
    }

    public static Rarity crestTierFor(long collected) {
        if (collected >= CREST_RARE_AT) return Rarity.RARE;
        if (collected >= CREST_UNCOMMON_AT) return Rarity.UNCOMMON;
        return Rarity.COMMON;
    }

    public static void syncCrest(SkyBlockPlayer player, long collected) {
        Rarity target = crestTierFor(collected);
        var bag = player.getAccessoryBag();
        boolean bagChanged = false;
        for (SkyBlockItem item : bag.getAllAccessories()) {
            if (item.getAttributeHandler().getPotentialType() != ItemType.TRAPPER_CREST) continue;
            if (item.getAttributeHandler().getRarity() == target) continue;
            item.getAttributeHandler().setRarity(target);
            bagChanged = true;
        }
        if (bagChanged) AccessoryBag.save(player, bag);
        for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() != ItemType.TRAPPER_CREST) continue;
            if (item.getAttributeHandler().getRarity() == target) continue;
            item.getAttributeHandler().setRarity(target);
            player.getInventory().setItemStack(slot, item.getItemStack());
        }
        AccessoryOwnership.invalidate(player);
    }
}
