package net.swofty.type.skyblockgeneric.crimson;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.Map;
import java.util.Set;

public final class CrimsonItemHandlers {
    private static final Set<Material> LEATHER_ARMOR = Set.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
    private static final Map<String, String> DYES = Map.of(
            "DYE_CYCLAMEN", "#F56FA1",
            "DYE_BONE", "#E3DAC9");
    private static final int MATCH_STICK_SECONDS = 10;

    private CrimsonItemHandlers() {
    }

    public static void registerAnvilCombines() {
        DYES.forEach((handlerId, color) -> AnvilCombineRegistry.register(handlerId, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler().setDyeColor(color),
                (player, upgradeItem, sacrificeItem) -> LEATHER_ARMOR.contains(upgradeItem.getMaterial())
                        && !color.equals(upgradeItem.getAttributeHandler().getDyeColor()),
                (upgradeItem, sacrificeItem, player) -> 0,
                null)));
    }

    public static void registerInteractables() {
        InteractableRegistry.register("MATCH_STICKS", InteractableItemConfig.builder()
                .rightClickHandler(CrimsonItemHandlers::lightMatchSticks).build());
    }

    private static void lightMatchSticks(SkyBlockPlayer player, SkyBlockItem item) {
        player.setFireTicks(MATCH_STICK_SECONDS * 20);
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withBase(ItemStatistic.STRENGTH, 10D).build())
                .withExpirationInMs(MATCH_STICK_SECONDS * 1000L)
                .withDisplayName("Match-Sticks")
                .build());
        SkyBlockItem remaining = item.clone();
        remaining.setAmount(item.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
        player.sendMessage("<6>You lit yourself on fire! <c>+10❁ Strength <6>for 10 seconds.");
    }
}
