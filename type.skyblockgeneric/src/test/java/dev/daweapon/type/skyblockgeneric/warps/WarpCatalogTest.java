package dev.daweapon.type.skyblockgeneric.warps;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarpCatalogTest {
    @Test
    void everyScrollBelongsToExactlyOneIsland() {
        for (TravelScrollType scroll : TravelScrollType.values()) {
            long owners = Arrays.stream(TravelScrollIslands.values())
                    .filter(island -> island.getAssociatedScrolls().contains(scroll) || island.getUnlockScroll() == scroll)
                    .count();
            assertEquals(1, owners, scroll + " must belong to exactly one island");
        }
    }

    @Test
    void scrollWarpNamesNeverShadowAnotherIsland() {
        for (TravelScrollType scroll : TravelScrollType.values()) {
            TravelScrollIslands named = TravelScrollIslands.getFromInternalName(scroll.getInternalName());
            assertTrue(named == null || named.getUnlockScroll() == scroll,
                    scroll + " shares its warp name with " + named);
        }
    }

    @Test
    void warpNamesAreUnique() {
        Set<String> islandNames = new HashSet<>();
        for (TravelScrollIslands island : TravelScrollIslands.values()) {
            assertTrue(islandNames.add(island.getInternalName()), "duplicate island warp " + island.getInternalName());
        }
        Set<String> scrollNames = new HashSet<>();
        for (TravelScrollType scroll : TravelScrollType.values()) {
            assertTrue(scrollNames.add(scroll.getInternalName()), "duplicate scroll warp " + scroll.getInternalName());
        }
    }

    @Test
    void everyAliasResolvesToARealWarp() {
        for (String alias : List.of("ci", "isle", "galatea", "dh", "mound", "gt", "wiz", "spring", "atoll", "trees")) {
            String warp = TravelScrollIslands.resolveAlias(alias);
            boolean known = TravelScrollIslands.getFromInternalName(warp) != null || TravelScrollType.getFromInternalName(warp) != null;
            assertTrue(known, alias + " resolves to nothing");
        }
    }
}
