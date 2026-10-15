package dev.daweapon.type.generic.collectibles.bedwars;

import net.minestom.server.item.Material;
import dev.daweapon.type.generic.collectibles.CollectibleCategory;
import dev.daweapon.type.generic.collectibles.CollectibleDefinition;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.Optional;

// Resolves the wood/plank material a player has selected via the Wood Skins collectible category.
public final class BedWarsWoodSkinRuntimeService {

    private BedWarsWoodSkinRuntimeService() {
    }

    public static Material resolveSelectedMaterial(HypixelPlayer player) {
        Optional<CollectibleDefinition> selected = BedWarsCollectibleStateService.reconcileSelected(
            player,
            CollectibleCategory.WOOD_SKINS
        );
        if (selected.isEmpty()) {
            return Material.OAK_PLANKS;
        }

        Material selectedMaterial = BedWarsCollectibleCatalog.parseMaterial(selected.get().effectiveSelectionValue());
        if (selectedMaterial != null) {
            return selectedMaterial;
        }

        Material iconMaterial = selected.get().iconMaterial();
        return iconMaterial != null ? iconMaterial : Material.OAK_PLANKS;
    }
}
