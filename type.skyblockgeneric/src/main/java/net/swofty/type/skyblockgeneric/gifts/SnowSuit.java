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
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockInventory;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The Snow Suit and its equipment (stats doubled on Jerry's Workshop, +5% Gift Attack gifts per
 * piece, and with all eight worn the Cold Thumb bonus: Frosty's cannons never use ammo) and the
 * Krampus Helmet (+1 Health per 50 gifts given, up to 500).
 */
public final class SnowSuit {
    private static final Set<ItemType> PIECES = Set.of(
            ItemType.SNOW_SUIT_HELMET, ItemType.SNOW_SUIT_CHESTPLATE, ItemType.SNOW_SUIT_LEGGINGS, ItemType.SNOW_SUIT_BOOTS,
            ItemType.SNOW_NECKLACE, ItemType.SNOW_CLOAK, ItemType.SNOW_BELT, ItemType.SNOW_GLOVES);
    public static final int TOTAL_PIECES = 8;
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
        List<ItemType> worn = new ArrayList<>();
        for (ItemStack armor : List.of(player.getHelmet(), player.getChestplate(), player.getLeggings(), player.getBoots())) {
            if (!armor.isAir()) worn.add(new SkyBlockItem(armor).getAttributeHandler().getPotentialType());
        }
        SkyBlockInventory inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        for (UnderstandableSkyBlockItem equipment : Arrays.asList(inventory.getNecklace(), inventory.getCloak(),
                inventory.getBelt(), inventory.getGloves())) {
            if (equipment == null) continue;
            SkyBlockItem item = new SkyBlockItem(equipment);
            if (!item.isAir()) worn.add(item.getAttributeHandler().getPotentialType());
        }
        return countPieces(worn);
    }

    public static int countPieces(Collection<ItemType> worn) {
        return (int) worn.stream().filter(Objects::nonNull).filter(PIECES::contains).distinct().count();
    }

    public static boolean hasColdThumb(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler() != null
                && (wornPieces(player) >= TOTAL_PIECES
                || net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityEffects.nutcrackerPieces(player) >= 4);
    }

    public static void registerLore() {
        LoreRegistry.register("SNOW_SUIT", new LoreConfig(SnowSuit::pieceLore, null));
    }

    private static List<String> pieceLore(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        int worn = player == null || player.getSkyblockDataHandler() == null ? 0 : wornPieces(player);
        return List.of(
                "<6>Full Set Bonus: Cold Thumb <7>(" + worn + "/" + TOTAL_PIECES + ")",
                "<7>Allows the wearer to shoot unlimited",
                "<7>snowballs when using <9>Frosty the",
                "<9>Snow Cannon<7>.",
                "",
                "<7>Each piece grants a <a>5% <7>bonus gift",
                "<7>chance for every present you earn",
                "<7>from the <c>Gift Attack <7>minigame!",
                "",
                "<7>All stats of this armor piece are",
                "<7>doubled while on <c>Jerry's Workshop<7>!");
    }
}
