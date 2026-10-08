package net.swofty.type.skyblockgeneric.gifts;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointInventory;
import net.swofty.type.skyblockgeneric.user.SkyBlockInventory;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The Snow Suit and its equipment (stats doubled on Jerry's Workshop, +5% Gift Attack gifts per
 * piece) and the Krampus Helmet (+1 Health per 50 gifts given, up to 500).
 */
public final class SnowSuit {
    private static final Set<ItemType> PIECES = Set.of(
            ItemType.SNOW_SUIT_HELMET, ItemType.SNOW_SUIT_CHESTPLATE, ItemType.SNOW_SUIT_LEGGINGS, ItemType.SNOW_SUIT_BOOTS,
            ItemType.SNOW_NECKLACE, ItemType.SNOW_CLOAK, ItemType.SNOW_BELT, ItemType.SNOW_GLOVES);
    private static final int GIFTS_PER_KRAMPUS_HEALTH = 50;
    private static final int KRAMPUS_MAX_HEALTH = 500;

    private SnowSuit() {
    }

    public static ItemStatistics extraItemStatistics(SkyBlockItem item, @Nullable SkyBlockPlayer wearer) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type == null) return ItemStatistics.empty();
        if (PIECES.contains(type)) {
            return HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_JERRYS_WORKSHOP
                    ? item.getAttributeHandler().getStatistics() : ItemStatistics.empty();
        }
        if (type == ItemType.KRAMPUS_HELMET && wearer != null && wearer.getSkyblockDataHandler() != null) {
            return ItemStatistics.builder().withBase(ItemStatistic.HEALTH, (double) krampusHealth(giftsGiven(wearer))).build();
        }
        return ItemStatistics.empty();
    }

    public static int krampusHealth(int giftsGiven) {
        return Math.min(KRAMPUS_MAX_HEALTH, giftsGiven / GIFTS_PER_KRAMPUS_HEALTH);
    }

    public static int giftsGiven(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GIFTS_GIVEN, DatapointInteger.class).getValue();
    }

    public static void addGiftGiven(SkyBlockPlayer player) {
        DatapointInteger given = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GIFTS_GIVEN, DatapointInteger.class);
        given.setValue(given.getValue() + 1);
    }

    public static int wornPieces(SkyBlockPlayer player) {
        int worn = 0;
        for (ItemStack armor : List.of(player.getHelmet(), player.getChestplate(), player.getLeggings(), player.getBoots())) {
            if (!armor.isAir() && PIECES.contains(new SkyBlockItem(armor).getAttributeHandler().getPotentialType())) worn++;
        }
        SkyBlockInventory inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        for (UnderstandableSkyBlockItem equipment : Arrays.asList(inventory.getNecklace(), inventory.getCloak(),
                inventory.getBelt(), inventory.getGloves())) {
            if (equipment == null) continue;
            SkyBlockItem item = new SkyBlockItem(equipment);
            if (!item.isAir() && PIECES.contains(item.getAttributeHandler().getPotentialType())) worn++;
        }
        return worn;
    }
}
