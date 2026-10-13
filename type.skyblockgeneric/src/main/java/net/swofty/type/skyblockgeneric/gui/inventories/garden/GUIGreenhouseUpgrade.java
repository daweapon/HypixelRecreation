package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseConfig;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIGreenhouseUpgrade extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20};
    private final GreenhouseConfig.Upgrade upgrade;

    public GUIGreenhouseUpgrade(GreenhouseConfig.Upgrade upgrade) {
        this.upgrade = upgrade;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of(upgrade.displayName() + " Upgrades"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 31, ctx);
        int tiers = GreenhouseConfig.get().maxTier(upgrade);
        for (int i = 0; i < tiers; i++) {
            int index = i;
            layout.slot(SLOTS[i], (_, c) -> GUIGreenhouseUpgrades.tierItem((SkyBlockPlayer) c.player(), upgrade, index), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (GreenhouseService.tier(GreenhouseService.data(player), upgrade) != index) return;
                GreenhouseService.UpgradeResult result = GreenhouseService.upgrade(player, upgrade);
                player.sendMessage(switch (result) {
                    case UPGRADED -> Text.of("<a>You upgraded your Greenhouse {}<a>!", upgrade.displayName());
                    case MAX_TIER -> Text.of("<c>This upgrade is already maxed!");
                    case NOT_ENOUGH_COPPER -> Text.of("<c>You need more Copper!");
                    case NOT_ENOUGH_BUNDLES -> Text.of("<c>You need more Compost Bundles!");
                    case NOT_ENOUGH_VINES -> Text.of("<c>You need more Ethereal Vines!");
                    case SLOTS_LOCKED -> Text.of("<c>Unlock all Greenhouse crop slots to use this!");
                });
                c.session(DefaultState.class).refresh();
            });
        }
    }
}
