package dev.daweapon.type.bedwarslobby.gui.cosmetics;

import dev.daweapon.type.generic.collectibles.CollectibleCategory;
import dev.daweapon.type.generic.collectibles.CollectibleDefinition;
import dev.daweapon.type.generic.collectibles.CollectibleSelectionCheck;
import dev.daweapon.type.generic.collectibles.bedwars.BedWarsCollectibleCatalog;
import dev.daweapon.type.generic.collectibles.bedwars.BedWarsCollectibleStateService;
import dev.daweapon.type.generic.data.datapoints.DatapointLeaderboardLong;
import dev.daweapon.type.generic.data.handlers.BedWarsDataHandler;
import dev.daweapon.type.generic.gui.impl.collectibles.CollectibleSelectionView;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.List;

// Collectible selection GUI for choosing an unlocked wood-block cosmetic skin.
public class GUIWoodSkins extends CollectibleSelectionView {

    public GUIWoodSkins() {
        super("Wood Skins ");
    }

    @Override
    protected List<CollectibleDefinition> loadItems(HypixelPlayer player) {
        BedWarsCollectibleCatalog.initialize();
        BedWarsCollectibleStateService.reconcileSelected(player, CollectibleCategory.WOOD_SKINS);
        return BedWarsCollectibleCatalog.getCategoryItems(CollectibleCategory.WOOD_SKINS);
    }

    @Override
    protected CollectibleSelectionCheck selectionCheck(HypixelPlayer player, CollectibleDefinition definition) {
        return BedWarsCollectibleStateService.checkSelectable(player, definition);
    }

    @Override
    protected boolean isSelected(HypixelPlayer player, CollectibleDefinition definition) {
        return BedWarsCollectibleStateService.isSelected(player, CollectibleCategory.WOOD_SKINS, definition.id());
    }

    @Override
    protected SelectionOutcome selectItem(HypixelPlayer player, ViewContext ctx, CollectibleDefinition definition) {
        BedWarsCollectibleStateService.SelectionResult result = BedWarsCollectibleStateService.select(player, definition);
        return result.success()
            ? SelectionOutcome.success(result.message())
            : SelectionOutcome.failure(result.message());
    }

    @Override
    protected boolean isPinnedDefault(HypixelPlayer player, CollectibleDefinition definition) {
        String pinned = BedWarsCollectibleCatalog.pinnedDefaultId(CollectibleCategory.WOOD_SKINS);
        return pinned != null && pinned.equals(definition.id());
    }

    @Override
    protected long tokenBalance(HypixelPlayer player) {
        BedWarsDataHandler dataHandler = BedWarsDataHandler.getUser(player);
        if (dataHandler == null) {
            return 0L;
        }
        return dataHandler.get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class).getValue();
    }
}
