package dev.daweapon.type.skyblockgeneric.gui.inventories.rusty;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoBelt;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.miningevent.CultOfTheFallenStar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
        WHITE_BELT(DojoBelt.WHITE, 1_000_000),
        YELLOW_BELT(DojoBelt.YELLOW, 2_000_000),
        GREEN_BELT(DojoBelt.GREEN, 3_000_000),
        BLUE_BELT(DojoBelt.BLUE, 4_000_000),
        BROWN_BELT(DojoBelt.BROWN, 5_000_000),
        BLACK_BELT(DojoBelt.BLACK, 6_000_000),
        FALLEN_STAR_HELMET(new SkyBlockItem(ItemType.valueOf("FALLEN_STAR_HAT")), 500_000, CultOfTheFallenStar::joined);

        private final SkyBlockItem item;
        private final int price;
        private final Function<SkyBlockPlayer, Boolean> unlocked;

        RustyItem(DojoBelt belt, int price) {
            this(new SkyBlockItem(belt.getItemType()), price, player -> DojoProgress.highestBelt(player) == belt);
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
