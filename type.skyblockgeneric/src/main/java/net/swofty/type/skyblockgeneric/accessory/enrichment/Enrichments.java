package net.swofty.type.skyblockgeneric.accessory.enrichment;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeEnrichment;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.accessory.AccessoryBag;
import net.swofty.type.skyblockgeneric.accessory.power.StatText;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.gui.inventories.accessory.GUIEnrichmentSwapper;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Accessory Enrichments: one per accessory, on Legendary, Mythic, Divine, Special and Very Special
 * accessories (by effective rarity, so a recombobulated Epic counts), applied in an Anvil. The
 * Accessory Enrichment Swapper changes every enriched accessory in the bag at once.
 */
public final class Enrichments {
    public static final String ANVIL_HANDLER = "ACCESSORY_ENRICHMENT";
    public static final String SWAPPER_HANDLER = "ACCESSORY_ENRICHMENT_SWAPPER";
    public static final Set<Rarity> ENRICHABLE = Set.of(Rarity.LEGENDARY, Rarity.MYTHIC, Rarity.DIVINE,
            Rarity.SPECIAL, Rarity.VERY_SPECIAL);

    private Enrichments() {
    }

    public static boolean canEnrich(SkyBlockItem item) {
        if (item.isNA() || item.isAir() || !item.hasComponent(AccessoryComponent.class)) return false;
        return ENRICHABLE.contains(item.getAttributeHandler().getEffectiveRarity());
    }

    public static @Nullable Enrichment of(SkyBlockItem item) {
        ItemAttributeEnrichment attribute = (ItemAttributeEnrichment) item.getAttribute("enrichment");
        return attribute == null ? null : Enrichment.byName(attribute.getValue());
    }

    public static void set(SkyBlockItem item, @Nullable Enrichment enrichment) {
        ((ItemAttributeEnrichment) item.getAttribute("enrichment")).setValue(enrichment == null ? "" : enrichment.name());
    }

    public static boolean canApply(SkyBlockItem accessory, @Nullable Enrichment enrichment) {
        return enrichment != null && canEnrich(accessory) && of(accessory) != enrichment;
    }

    public static ItemStatistics statistics(SkyBlockItem item) {
        Enrichment enrichment = of(item);
        if (enrichment == null) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(enrichment.getStatistic(), enrichment.getAmount()).build();
    }

    public static @Nullable String loreLine(SkyBlockItem item) {
        Enrichment enrichment = of(item);
        return enrichment == null ? null
                : "<7>Enrichment: " + StatText.line(enrichment.getStatistic(), enrichment.getAmount());
    }

    /** Re-enriches every enriched accessory in the bag; returns how many changed. */
    public static int swapAll(DatapointAccessoryBag.PlayerAccessoryBag bag, Enrichment enrichment) {
        int swapped = 0;
        for (SkyBlockItem item : bag.getAllAccessories()) {
            Enrichment current = of(item);
            if (current == null || current == enrichment) continue;
            set(item, enrichment);
            swapped++;
        }
        return swapped;
    }

    public static int enrichedInBag(DatapointAccessoryBag.PlayerAccessoryBag bag) {
        return (int) bag.getAllAccessories().stream().filter(item -> of(item) != null).count();
    }

    public static boolean useSwapper(SkyBlockPlayer player, Enrichment enrichment) {
        DatapointAccessoryBag.PlayerAccessoryBag bag = player.getAccessoryBag();
        if (enrichedInBag(bag) == 0) {
            player.sendMessage("<c>You don't have any enriched accessories in your Accessory Bag!");
            return false;
        }
        if (player.takeItem(ItemType.ACCESSORY_ENRICHMENT_SWAPPER, 1) == null) {
            player.sendMessage("<c>You need an Accessory Enrichment Swapper to do that!");
            return false;
        }
        int swapped = swapAll(bag, enrichment);
        AccessoryBag.save(player, bag);
        player.getStatistics().updateAccessoryStatistics();
        player.sendMessage("<a>Swapped the enrichment on <e>" + swapped + " <a>accessories to "
                + StatText.line(enrichment.getStatistic(), enrichment.getAmount()) + "<a>!");
        return true;
    }

    public static void registerAnvilCombine() {
        AnvilCombineRegistry.register(ANVIL_HANDLER, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> set(upgradeItem, Enrichment.byItem(sacrificeItem.getAttributeHandler().getPotentialType())),
                (player, upgradeItem, sacrificeItem) -> canApply(upgradeItem,
                        Enrichment.byItem(sacrificeItem.getAttributeHandler().getPotentialType())),
                (upgradeItem, sacrificeItem, player) -> 0,
                null));
    }

    public static void registerInteractables() {
        InteractableRegistry.register(SWAPPER_HANDLER, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> player.openView(new GUIEnrichmentSwapper()))
                .build());
    }
}
