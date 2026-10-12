package net.swofty.type.hub;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import net.swofty.type.skyblockgeneric.shop.catalogue.ShopCatalogue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TaylorDisplayCases {
    private static final List<Pos> CASES = List.of(
            new Pos(34.5, 75.4, -41.5, 135, 0),
            new Pos(36.5, 75.4, -42.5, 135, 0),
            new Pos(39.5, 75.4, -42.5, 135, 0),
            new Pos(41.5, 75.4, -41.5, 135, 0)
    );

    private TaylorDisplayCases() {
    }

    public static void spawn(Instance instance) {
        List<CatalogueEntry> candidates = new ArrayList<>(ShopCatalogue.get("taylor_collection").entries().stream()
                .filter(entry -> entry.unavailableReason() == null && entry.itemType() != null)
                .toList());
        Collections.shuffle(candidates);
        for (int i = 0; i < Math.min(CASES.size(), candidates.size()); i++) {
            Entity display = new Entity(EntityType.ITEM_DISPLAY);
            SkyBlockItem item = new SkyBlockItem(candidates.get(i).itemType());
            display.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setItemStack(item.getItemStack());
                meta.setDisplayContext(ItemDisplayMeta.DisplayContext.HEAD);
                meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
                meta.setScale(new Vec(1.5, 1.5, 1.5));
                meta.setHasNoGravity(true);
            });
            display.setNoGravity(true);
            display.setInstance(instance, CASES.get(i));
        }
    }
}
