package net.swofty.type.skyblockgeneric.gui.inventories.rusty;

import net.swofty.type.skyblockgeneric.crimson.dojo.DojoBelt;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoProgress;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.Function;

public class GUIRustyWeaponsAndGear extends GUIRustySubMenu<GUIRustyWeaponsAndGear.RustyItem> {
    public GUIRustyWeaponsAndGear() {
        super(
                "gui_rusty.weapons.title",
                () -> List.of(RustyItem.values())
        );
    }

    public enum RustyItem implements ShopEntry {
        WHITE_BELT(DojoBelt.WHITE),
        YELLOW_BELT(DojoBelt.YELLOW),
        GREEN_BELT(DojoBelt.GREEN),
        BLUE_BELT(DojoBelt.BLUE),
        BROWN_BELT(DojoBelt.BROWN),
        BLACK_BELT(DojoBelt.BLACK);

        /** Rusty re-sells the player's highest Dojo belt (wiki Rusty, Jul 2022). The price is not documented. */
        private static final int BELT_PRICE = 10_000;

        private final SkyBlockItem item;
        private final int price;
        private final Function<SkyBlockPlayer, Boolean> unlocked;

        RustyItem(DojoBelt belt) {
            this(new SkyBlockItem(belt.getItemType()), BELT_PRICE, player -> DojoProgress.highestBelt(player) == belt);
        }

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
