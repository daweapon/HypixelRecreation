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
        SHINY_YELLOW_ROCK(ring(0), 1_000_000, (player) -> ringTier(player) == 0),
        YELLOW_ROCK_OF_LOVE(ring(1), 1_000_000, (player) -> ringTier(player) == 1),
        MEDIOCRE_RING_OF_LOVE(ring(2), 1_000_000, (player) -> ringTier(player) == 2),
        RUBBISH_RING_OF_LOVE(ring(3), 1_000_000, (player) -> ringTier(player) == 3),
        MODEST_RING_OF_LOVE(ring(4), 1_000_000, (player) -> ringTier(player) == 4),
        REFINED_RING_OF_LOVE(ring(5), 1_000_000, (player) -> ringTier(player) == 5),
        CLASSY_RING_OF_LOVE(ring(6), 1_000_000, (player) -> ringTier(player) == 6),
        EXQUISITE_RING_OF_LOVE(ring(7), 1_000_000, (player) -> ringTier(player) == 7),
        INVALUABLE_RING_OF_LOVE(ring(8), 1_000_000, (player) -> ringTier(player) == 8),
        LEGENDARY_RING_OF_LOVE(ring(9), 1_000_000, (player) -> ringTier(player) == 9),
        ;

        private static SkyBlockItem ring(int tier) {
            return new SkyBlockItem(dev.daweapon.type.skyblockgeneric.romero.RomeroRing.values()[tier].itemType());
        }

        private static int ringTier(SkyBlockPlayer player) {
            int step = dev.daweapon.type.skyblockgeneric.romero.RomeroJulietteQuest.step(player);
            return step - 2;
        }

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
