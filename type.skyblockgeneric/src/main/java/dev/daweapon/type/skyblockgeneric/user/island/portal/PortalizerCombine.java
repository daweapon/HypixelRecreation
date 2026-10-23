package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.IslandTravelScrollComponent;
import dev.daweapon.type.skyblockgeneric.item.components.TravelScrollComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
import org.jetbrains.annotations.Nullable;

public final class PortalizerCombine {
    public static final String HANDLER_ID = "PORTALIZER";

    private PortalizerCombine() {
    }

    public static void registerAnvilCombine() {
        AnvilCombineRegistry.register(HANDLER_ID, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> {
                    ItemType portal = portalFor(upgradeItem);
                    if (portal != null) upgradeItem.convertTo(portal);
                },
                (player, upgradeItem, sacrificeItem) -> portalFor(upgradeItem) != null,
                (upgradeItem, sacrificeItem, player) -> 0,
                null,
                (upgradeItem, sacrificeItem) -> isScroll(upgradeItem) && portalFor(upgradeItem) == null ? MISSING_PORTAL_LORE : null));
    }

    public static final String MISSING_PORTAL_LORE = """
            <c>Error!
            <c>This travel scroll doesn't
            <c>have a portal item. Ask an admin
            <c>to add one immediately!""";

    private static boolean isScroll(SkyBlockItem item) {
        return item.hasComponent(TravelScrollComponent.class) || item.hasComponent(IslandTravelScrollComponent.class);
    }

    public static @Nullable ItemType portalFor(SkyBlockItem scroll) {
        for (PortalDestination destination : PortalCatalog.values()) {
            if (matches(destination, scroll)) return ItemType.get(destination.itemId());
        }
        return null;
    }

    private static boolean matches(PortalDestination destination, SkyBlockItem scroll) {
        if (scroll.hasComponent(TravelScrollComponent.class)) {
            return destination.scroll() == scroll.getComponent(TravelScrollComponent.class).getTravelScrollType();
        }
        if (scroll.hasComponent(IslandTravelScrollComponent.class)) {
            return destination.island() == scroll.getComponent(IslandTravelScrollComponent.class).getIsland();
        }
        return false;
    }
}
