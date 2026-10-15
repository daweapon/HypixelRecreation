package dev.daweapon.type.skyblockgeneric.accessory;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

/** Maps the Accessory Bag's absolute slots onto 45-slot pages. */
public final class AccessoryBagPages {
    public static final int SLOTS_PER_PAGE = 45;

    private AccessoryBagPages() {
    }

    public static int pages(int totalSlots) {
        return Math.max(1, (totalSlots + SLOTS_PER_PAGE - 1) / SLOTS_PER_PAGE);
    }

    public static int firstSlot(int page) {
        return page * SLOTS_PER_PAGE;
    }

    public static int slotsOnPage(int page, int totalSlots) {
        return Math.max(0, Math.min(SLOTS_PER_PAGE, totalSlots - firstSlot(page)));
    }

    /** Writes what the page's open slots now hold back into the bag; other pages are untouched. */
    public static void writePage(DatapointAccessoryBag.PlayerAccessoryBag bag, int page, int totalSlots,
                                 IntFunction<@Nullable SkyBlockItem> pageSlot) {
        int start = firstSlot(page);
        for (int i = 0; i < slotsOnPage(page, totalSlots); i++) {
            SkyBlockItem item = pageSlot.apply(i);
            if (item == null || item.isNA() || item.isAir() || item.getMaterial() == Material.AIR) {
                bag.removeFromSlot(start + i);
            } else {
                bag.setInSlot(start + i, item);
            }
        }
    }
}
