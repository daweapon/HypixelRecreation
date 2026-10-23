package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeArtOfWar;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AxeComponent;
import dev.daweapon.type.skyblockgeneric.item.components.BowComponent;
import dev.daweapon.type.skyblockgeneric.item.components.ShortBowComponent;
import dev.daweapon.type.skyblockgeneric.item.components.StandardItemComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

public final class ArtOfWar {
    public static final String HANDLER = "THE_ART_OF_WAR";
    public static final double STRENGTH = 5;
    private static final String ATTRIBUTE = "art_of_war";

    private ArtOfWar() {
    }

    public static void registerAnvilCombine() {
        AnvilCombineRegistry.register(HANDLER, new AnvilCombineHandler(
                (upgrade, sacrifice) -> setApplied(upgrade),
                (player, upgrade, sacrifice) -> acceptsArt(upgrade) && !applied(upgrade),
                (upgrade, sacrifice, player) -> 0));
    }

    public static boolean acceptsArt(SkyBlockItem item) {
        if (item.hasComponent(AxeComponent.class) || item.hasComponent(BowComponent.class) || item.hasComponent(ShortBowComponent.class)) return true;
        if (!item.hasComponent(StandardItemComponent.class)) return false;
        StandardItemComponent.StandardItemType type = item.getComponent(StandardItemComponent.class).getType();
        return type == StandardItemComponent.StandardItemType.SWORD || type == StandardItemComponent.StandardItemType.BOW;
    }

    public static boolean applied(SkyBlockItem item) {
        ItemAttributeArtOfWar attribute = (ItemAttributeArtOfWar) item.getAttribute(ATTRIBUTE);
        return attribute != null && attribute.getValue() > 0;
    }

    public static void setApplied(SkyBlockItem item) {
        ((ItemAttributeArtOfWar) item.getAttribute(ATTRIBUTE)).setValue(1);
    }

    public static ItemStatistics statistics(SkyBlockItem item) {
        if (item.isAir() || item.isNA() || !applied(item)) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.STRENGTH, STRENGTH).build();
    }
}
