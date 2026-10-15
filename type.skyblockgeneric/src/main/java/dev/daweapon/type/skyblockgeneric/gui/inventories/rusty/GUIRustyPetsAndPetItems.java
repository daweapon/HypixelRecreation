package dev.daweapon.type.skyblockgeneric.gui.inventories.rusty;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.Function;

public class GUIRustyPetsAndPetItems extends GUIRustySubMenu<GUIRustyPetsAndPetItems.RustyItem> {
    public GUIRustyPetsAndPetItems() {
        super(
                "gui_rusty.pets.title",
                () -> List.of(RustyItem.values())
        );
    }

    public enum RustyItem implements ShopEntry {
        REAPER_GEM(new SkyBlockItem(ItemType.REAPER_GEM), 20_000_000,
                player -> SlayerRequirement.level(player, SlayerType.REVENANT_HORROR) >= 9);

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
