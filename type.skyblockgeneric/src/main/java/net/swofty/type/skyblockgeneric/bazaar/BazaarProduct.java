package net.swofty.type.skyblockgeneric.bazaar;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

// Something the Bazaar trades. Orders are keyed by id(); how the product is held, taken and delivered
// is the product's own business (an item, an enchantment book, or an essence balance).
public interface BazaarProduct {
    String id();

    String name();

    Rarity rarity();

    Material material();

    @Nullable ItemType iconType();

    @Nullable String headTexture();

    @Nullable SkyBlockItem item();

    @Nullable ItemType itemType();

    int held(SkyBlockPlayer player);

    int room(SkyBlockPlayer player);

    @Nullable Runnable take(SkyBlockPlayer player, int amount);

    void give(SkyBlockPlayer player, int amount);
}
