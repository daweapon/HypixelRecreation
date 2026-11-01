package dev.daweapon.type.skyblockgeneric.gui.inventories.fishing;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

import java.util.List;

/**
 * Shared chrome for the 3-page sea creature guide. Subclasses only need to
 * declare which page they are and which entries to show; layout, navigation,
 * sort/filter/category buttons all live here.
 *
 * Entries are modelled as a sealed {@link Entry} hierarchy: {@code Head} for
 * player heads (the common case — most creatures use one) and {@code Block}
 * for the handful that render as a regular item (dragon eggs, etc.). Each
 * entry carries one markup text block whose first line is the item name.
 */
public abstract sealed class SeaCreatureGuidePage extends StatelessView
    permits GUI13SeaCreatureGuide, GUI23SeaCreatureGuide, GUI33SeaCreatureGuide {

    private static final int TOTAL_PAGES = 3;

    protected abstract int pageNumber();

    protected abstract List<Entry> entries();

    @Override
    public final ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(
            Text.of("({}/{}) Sea Creature Guide", pageNumber(), TOTAL_PAGES),
            InventoryType.CHEST_6_ROW
        );
    }

    @Override
    public final void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(4, (_, c) -> ItemStacks.item(Material.BOOK, """
                <a>⚑ Sea Creature Guide
                <7>Your guide to the creatures of the
                <7>deep!

                <7>Beware, Sea Creatures spawn much
                <7>less often on your private island.

                <7>Your Fishing: <a>Level {}

                <8>Also accessible via /scg""", ((SkyBlockPlayer) c.player()).getSkills().getCurrentLevel(SkillCategories.FISHING)));

        for (Entry entry : entries()) {
            layout.slot(entry.slot(), entry.render());
        }

        if (pageNumber() > 1) {
            layout.slot(45, ItemStacks.item(Material.ARROW, """
                    <a>Previous Page
                    <e>Page {}""", pageNumber() - 1));
        }

        layout.slot(48, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Fishing Skill"""));

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

        if (pageNumber() < TOTAL_PAGES) {
            layout.slot(53, ItemStacks.item(Material.ARROW, """
                    <a>Next Page
                    <e>Page {}""", pageNumber() + 1));
        }
    }

    protected static Entry head(int slot, String texture, String textBlock) {
        return new Entry.Head(slot, texture, textBlock);
    }

    protected static Entry block(int slot, Material material, String textBlock) {
        return new Entry.Block(slot, material, textBlock);
    }

    public sealed interface Entry {
        int slot();

        ItemStack.Builder render();

        record Head(int slot, String texture, String textBlock) implements Entry {
            @Override
            public ItemStack.Builder render() {
                return ItemStacks.head(texture, textBlock);
            }
        }

        record Block(int slot, Material material, String textBlock) implements Entry {
            @Override
            public ItemStack.Builder render() {
                return ItemStacks.item(material, textBlock);
            }
        }
    }
}
