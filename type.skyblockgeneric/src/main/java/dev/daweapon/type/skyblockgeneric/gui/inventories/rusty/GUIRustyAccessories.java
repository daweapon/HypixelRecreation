package dev.daweapon.type.skyblockgeneric.gui.inventories.rusty;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.chickenrace.ChickenRaceProgress;
import dev.daweapon.type.skyblockgeneric.chickenrace.Cluckpoint;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.Function;

public class GUIRustyAccessories extends GUIRustySubMenu<GUIRustyAccessories.RustyItem> {
    public GUIRustyAccessories() {
        super(
                "gui_rusty.accessories.title",
                () -> List.of(RustyItem.values())
        );
    }

    public enum RustyItem implements ShopEntry {
        FROZEN_CHICKEN(new SkyBlockItem(ItemType.FROZEN_CHICKEN), 1_000_000, (player) -> ChickenRaceProgress.hasClaimed(player, Cluckpoint.THREE)),
        ;

        private final SkyBlockItem item;
        private final int price;
        private final Function<SkyBlockPlayer, Boolean> unlocked;

        RustyItem(
                SkyBlockItem item,
                int price,
                Function<SkyBlockPlayer, Boolean> unlocked
        ) {
            this.item = item;
            this.price = price;
            this.unlocked = unlocked;
        }

        @Override public SkyBlockItem item() { return item; }
        @Override public int price() { return price; }
        @Override public Function<SkyBlockPlayer, Boolean> hasUnlocked() { return unlocked; }
    }
}
