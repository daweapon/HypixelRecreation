package dev.daweapon.type.galatea.gui;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import dev.daweapon.type.skyblockgeneric.fishing.torrhus.TorrhusSeaCreatures;
import dev.daweapon.type.skyblockgeneric.gui.inventories.fishing.GUI13SeaCreatureGuide;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.GUIBestiaryFishing;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class GUIHunterJean extends StatelessView {
    private static final int FISHING_LEVEL = 15;

    public record Entry(String id, int slot, String rarity, int weight, String head) {
    }

    public static final List<Entry> ENTRIES = List.of(
            new Entry(TorrhusSeaCreatures.HAGGARD, 10, "<f><l>COMMON", 5000, "837be7781a3ae4454f4670e1686dc17a07dd321d6f670ba80f91e391f0763321"),
            new Entry(TorrhusSeaCreatures.BRINELING, 11, "<a><l>UNCOMMON", 2875, "18ddf45f2a16e662c667541422cbbd944398b1a90c500b03308b1c99c10003d3"),
            new Entry(TorrhusSeaCreatures.SPRAWL, 12, "<9><l>RARE", 1500, "6bbe6a78b8b9b1bee4458fae633deca7dc9da31589b98903c9e871efcb416aea"),
            new Entry(TorrhusSeaCreatures.TORRID, 13, "<5><l>EPIC", 500, "9da145254a59efce57186e6c99b64ba0e86803a4496cdbe8c96cc13dda71aa24"),
            new Entry(TorrhusSeaCreatures.SILKBREEZE, 14, "<6><l>LEGENDARY", 100, "2630c3171c4028544ff979ba87fa03102aacbc1e917ace9984d8ee51a9bbd5ba"),
            new Entry(TorrhusSeaCreatures.GIANT_ISOPOD, 15, "<d><l>MYTHIC", 25, "3fcb203b029aac4958407daf4072a01a353105162373ce7e559c669d78325bcb"));

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Torrhus Canyon Sea Creatures", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        layout.slot(4, ItemStacks.item(Material.MANGROVE_LOG, """
                <a>Torrhus Canyon Sea Creatures
                <7>View all of the Sea Creatures that
                <7>can be caught on <6>Torrhus Canyon<7>."""));

        for (Entry entry : ENTRIES) {
            SeaCreatureProfile profile = profile(entry.id());
            layout.slot(entry.slot(), ItemStacks.head(entry.head(), describe(entry, profile)),
                    (_, c) -> c.push(new GUIBestiaryFishing()));
        }

        layout.slot(48, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Sea Creature Guide"""), (_, c) -> c.push(new GUI13SeaCreatureGuide()));

        layout.slot(50, ItemStacks.item(Material.HOPPER, """
                <a>Sort

                <b>▶ Fishing Level Requirement
                <7>  A to Z
                <7>  Z to A
                <7>  Mob Level
                <7>  Killed Most
                <7>  Lowest Rarity
                <7>  Highest Rarity

                <e>Click to switch sort!
                <b>Right-Click to go backwards!"""));

        layout.slot(51, ItemStacks.item(Material.ENDER_EYE, """
                <a>Filter

                <a>▶ None
                <7>  Has Level Requirement
                <7>  Has Never Killed

                <e>Click to switch filter!
                <b>Right-Click to go backwards!"""));

        layout.slot(52, ItemStacks.item(Material.OAK_SIGN, """
                <a>Search
                <7>Find Sea Creatures by name!

                <e>Click to search!"""));
    }

    public static String describe(Entry entry, SeaCreatureProfile profile) {
        String types = profile.mobTypes().stream().map(MobType::getFullDisplayName).collect(Collectors.joining("<7>, "));
        return "<7>[Lvl " + profile.level() + "] " + profile.displayName() + "<7> (" + entry.rarity() + "<7>)\n"
                + types + "\n\n"
                + "<7>Fishing Skill: <a>" + FISHING_LEVEL + "\n"
                + "<7>Spawn Chance: <a>" + new BigDecimal(entry.weight()).movePointLeft(2).stripTrailingZeros().toPlainString() + "% <8>(" + entry.weight() + " Weight)\n\n"
                + "<7>Categories:\n"
                + "<7>- Liquid: <b>Water\n"
                + "<7>- Island: <6>Torrhus Canyon\n\n"
                + "<e>Click to view Bestiary!";
    }

    private static SeaCreatureProfile profile(String id) {
        return TorrhusSeaCreatures.PROFILES.stream().filter(p -> p.id().equals(id)).findFirst().orElseThrow();
    }
}
