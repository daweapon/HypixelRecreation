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
        var bag = player.getAccessoryBag();
        for (SkyBlockItem item : bag.getAllAccessories()) {
            if (item.getAttributeHandler().getPotentialType() != ItemType.TRAPPER_CREST) continue;
            credit(player, item, pelts);
            AccessoryBag.save(player, bag);
            AccessoryOwnership.invalidate(player);
            return;
        }
        for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() != ItemType.TRAPPER_CREST) continue;
            credit(player, item, pelts);
            player.getInventory().setItemStack(slot, item.getItemStack());
            AccessoryOwnership.invalidate(player);
            return;
        }
    }

    private static void credit(SkyBlockPlayer player, SkyBlockItem crest, int pelts) {
        long collected = crest.getAttributeHandler().getTrapperCrestPelts() + takeLegacyCount(player) + pelts;
        crest.getAttributeHandler().setTrapperCrestPelts(collected);
        crest.getAttributeHandler().setRarity(crestTierFor(collected));
    }

    private static long takeLegacyCount(SkyBlockPlayer player) {
        var datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class);
        DatapointHunting.HuntingData data = datapoint.getValue();
        long legacy = data.counter(CREST_PELTS);
        if (legacy <= 0) return 0;
        data.setCounter(CREST_PELTS, 0);
        datapoint.setValue(data);
        return legacy;
    }

    public static long crestPelts(SkyBlockItem crest) {
        return crest.getAttributeHandler().getTrapperCrestPelts();
    }

    public static Rarity crestTierFor(long collected) {
        if (collected >= CREST_RARE_AT) return Rarity.RARE;
        if (collected >= CREST_UNCOMMON_AT) return Rarity.UNCOMMON;
        return Rarity.COMMON;
    }

}
