package dev.daweapon.type.skyblockgeneric.gui.inventories.fishing;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.fishing.item.FishingItemSupport;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// GUI listing available fishing baits and their effects
public class GUIBaitGuide extends StatelessView {
    private static final int[] BAIT_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final Map<String, List<String>> SOURCES = new LinkedHashMap<>();

    static {
        SOURCES.put("SPOOKY_BAIT", List.of("<a>Lily Pad Collection I"));
        SOURCES.put("CORRUPTED_BAIT", List.of("<a>Magmafish Collection V"));
        SOURCES.put("MINNOW_BAIT", List.of("<a>Raw Cod Collection I"));
        SOURCES.put("SPIKED_BAIT", List.of("<a>Pufferfish Collection V"));
        SOURCES.put("CARROT_BAIT", List.of("<a>Carrot Collection III"));
        SOURCES.put("FISH_BAIT", List.of("<a>Raw Salmon Collection V"));
        SOURCES.put("LIGHT_BAIT", List.of("<a>Prismarine Crystals Collection II"));
        SOURCES.put("DARK_BAIT", List.of("<a>Ink Sac Collection II"));
        SOURCES.put("WORM_BAIT", List.of("<a>Crafting Table"));
        SOURCES.put("SHARK_BAIT", List.of("<a>Crafting Table"));
        SOURCES.put("ICE_BAIT", List.of("<a>Ice Collection II"));
        SOURCES.put("GOLDEN_BAIT", List.of("<a>Seal's Treat Bag"));
        SOURCES.put("GLOWY_CHUM_BAIT", List.of("<2>Moby"));
        SOURCES.put("WOODEN_BAIT", List.of("<c>Unknown"));
        SOURCES.put("HOT_BAIT", List.of("<a>Magmafish Collection II"));
        SOURCES.put("BLESSED_BAIT", List.of("<a>Prismarine Crystals Collection VI"));
        SOURCES.put("HOTSPOT_BAIT", List.of("<a>Pufferfish Collection VIII"));
        SOURCES.put("FROZEN_BAIT", List.of("<6>Generow's Gift Milestones", "<b>Frozen Treasure"));
        SOURCES.put("TREASURE_BAIT", List.of("<2>Junker Joel"));
        SOURCES.put("WHALE_BAIT", List.of("<a>Lily Pad Collection VI"));
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Bait Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(4, ItemStacks.item(Material.BOOK, """
                <6>Bait Guide
                <7>View <6>Baits<7>, their uses, and how to
                <7>obtain them! Can also be accessed
                <7>with <a>/bait<7>!"""));
        layout.slot(50, ItemStacks.item(Material.REDSTONE_TORCH, """
                <6>Bait Guide
                <6>Bait <7>can be stored in your inventory
                <7>or <9>Fishing Bag<7>, which is unlocked at
                <a>Raw Cod Collection III<7>.

                <7>When you cast your rod, Bait is
                <7>automatically consumed and its buff
                <7>is applied.

                <7>Some Baits, like <f>Fish Bait<7>, increase
                <7>your <b>☂ Fishing Speed<7>, which makes it
                <7>quicker to catch fish.

                <7>Other Baits, like <f>Spiked Bait<7>, make you
                <7>more likely to catch <3>Sea Creatures<7>.

                <7>Fishing with <6>Bait <7>makes fishing easier,
                <7>so keep plenty on hand!"""));

        Map<String, SkyBlockItem> baits = new LinkedHashMap<>();
        FishingItemSupport.getBaits().forEach(bait -> baits.put(bait.getAttributeHandler().getPotentialType().name(), bait));
        List<SkyBlockItem> ordered = new ArrayList<>();
        SOURCES.keySet().forEach(id -> {
            SkyBlockItem bait = baits.remove(id);
            if (bait != null) ordered.add(bait);
        });
        ordered.addAll(baits.values());

        int index = 0;
        for (var bait : ordered) {
            if (index >= BAIT_SLOTS.length) {
                break;
            }
            String id = bait.getAttributeHandler().getPotentialType().name();
            layout.slot(BAIT_SLOTS[index++], FishingGuideStackFactory.buildBaitStack(bait, SOURCES.getOrDefault(id, List.of())));
        }
    }
}
