package net.swofty.type.skyblockgeneric.item.utility;

import net.swofty.type.skyblockgeneric.item.components.SackComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

public final class PocketSackInASack {
    public static final String HANDLER_ID = "POCKET_SACK_IN_A_SACK";

    private PocketSackInASack() {
    }

    public static void registerAnvilCombine() {
        AnvilCombineRegistry.register(HANDLER_ID, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler()
                        .setSackUpgrades(upgradeItem.getAttributeHandler().getSackUpgrades() + 1),
                (player, upgradeItem, sacrificeItem) -> upgradeItem.hasComponent(SackComponent.class)
                        && upgradeItem.getAttributeHandler().getSackUpgrades() < SackComponent.MAX_UPGRADES,
                (upgradeItem, sacrificeItem, player) -> 0));
    }
}
