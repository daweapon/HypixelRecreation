package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIVisitorMilestones extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Visitor Milestones"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        if (!Components.back(layout, 48, ctx)) {
            Components.close(layout, 48);
        }
        Components.close(layout, 49);
        layout.slot(21, (_, c) -> track((SkyBlockPlayer) c.player(), true));
        layout.slot(23, (_, c) -> track((SkyBlockPlayer) c.player(), false));
        layout.slot(50, (_, _) -> ItemStacks.item(Material.BOOK, Text.of("<a>Visitor's Logbook"), List.of(
                        Text.of("<7>Browse and filter through all of the"),
                        Text.of("<7>visitors on the Garden."),
                        Text.empty(),
                        Text.of("<e>Click to browse!"))),
                (_, c) -> c.push(new GUIVisitorLogbook()));
    }

    private ItemStack.Builder track(SkyBlockPlayer player, boolean unique) {
        VisitorCatalogue catalogue = VisitorCatalogue.get();
        List<VisitorCatalogue.MilestoneTier> tiers = unique ? catalogue.uniqueMilestones() : catalogue.acceptedMilestones();
        VisitorData data = Gardens.profile(player).visitors();
        int progress = unique ? data.uniqueServed() : data.totalAccepted();
        VisitorCatalogue.MilestoneTier next = tiers.stream().filter(tier -> progress < tier.cumulative()).findFirst().orElse(null);
        List<Text> lore = new ArrayList<>();
        if (unique) {
            lore.add(Text.of("<7>Unlock unique visitors and"));
            lore.add(Text.of("<7>complete their offer on your"));
            lore.add(Text.of("<7>island to increase this"));
            lore.add(Text.of("<7>milestone."));
        } else {
            lore.add(Text.of("<7>Accept offers from visitors to"));
            lore.add(Text.of("<7>increase this milestone."));
        }
        lore.add(Text.empty());
        if (next == null) {
            lore.add(Text.of("<a>Max tier reached!"));
        } else {
            long base = tiers.indexOf(next) == 0 ? 0 : tiers.get(tiers.indexOf(next) - 1).cumulative();
            long into = Math.max(0, progress - base);
            long need = next.cumulative() - base;
            lore.add(Text.of("<7>Progress to Tier {}: <e>{}%", StringUtility.getAsRomanNumeral(next.tier()), GardenMenus.percent(into, need)));
            int filled = GardenMenus.filled(into, need, GardenMenus.BAR_WIDTH);
            lore.add(Text.of("<dark_green><m>" + GardenMenus.spaces(filled) + "<white><m>" + GardenMenus.spaces(GardenMenus.BAR_WIDTH - filled)
                    + "<reset> <gold>" + GardenMenus.number(into) + "<yellow>/<yellow>" + GardenMenus.number(need)));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Rewards:"));
            lore.add(Text.of("　<8>+<2>{}<7> Garden Experience", next.gardenXp()));
            lore.add(Text.of("　<8>+<3>{}<7> Farming Experience", StringUtility.commaify(next.farmingXp())));
            lore.add(Text.of("　<8>+<b>{} SkyBlock XP", next.skyblockXp()));
        }
        return ItemStacks.item(Material.PLAYER_HEAD, Text.of(unique ? "<a>Unique Visitors Served" : "<a>Offers Accepted"), lore);
    }
}
