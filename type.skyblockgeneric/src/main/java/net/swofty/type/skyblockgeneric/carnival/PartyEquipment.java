package net.swofty.type.skyblockgeneric.carnival;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointInventory;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockInventory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The Party Equipment set (Party Top Hat, Necklace, Cloak, Belt and Gloves). Its Carnival Craze
 * tiered bonus gives +4% more Carnival Tokens per piece worn, up to +20% with all five.
 */
public final class PartyEquipment {
    private static final Set<ItemType> PIECES = Set.of(ItemType.PARTY_HAT, ItemType.PARTY_NECKLACE,
            ItemType.PARTY_CLOAK, ItemType.PARTY_BELT, ItemType.PARTY_GLOVES);
    public static final int TOTAL_PIECES = 5;
    private static final int PERCENT_PER_PIECE = 4;

    private PartyEquipment() {
    }

    public static int bonusPercent(int pieces) {
        return Math.min(pieces, TOTAL_PIECES) * PERCENT_PER_PIECE;
    }

    public static int boostedTokens(SkyBlockPlayer player, int tokens) {
        return (int) Math.round(tokens * (1 + bonusPercent(wornPieces(player)) / 100D));
    }

    public static int wornPieces(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        List<ItemType> worn = new ArrayList<>();
        if (!player.getHelmet().isAir()) worn.add(new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType());
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

    public static void registerLore() {
        LoreRegistry.register("PARTY_EQUIPMENT", new LoreConfig(PartyEquipment::pieceLore, null));
    }

    private static List<String> pieceLore(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        int worn = player == null ? 0 : wornPieces(player);
        return List.of(
                (worn > 0 ? "<6>" : "<8>") + "Tiered Bonus: Carnival Craze (" + worn + "/" + TOTAL_PIECES + ")",
                "<7>Gain <a>+" + bonusPercent(Math.max(worn, 1)) + "% <7>more <e>Carnival Tokens<7>.");
    }
}
