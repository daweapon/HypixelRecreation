package net.swofty.type.crimsonisle.gui.town;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;

/**
 * Vesuvius's chest storage (wiki Vesuvius/UI). Unopened Kuudra reward chests would wait here for
 * 72 hours; the fight and its chests are not built yet, so the storage is always empty.
 */
public class GUIVesuvius extends StatelessView {
    private static final String HEAD = "11443decbade0555c53938fdf69fb4cfe408a0a600ea8555a69433b3415aa823";

    public enum Filter {
        ALL("All"), OPENED("Opened"), UNOPENED("Unopened");

        private final String label;

        Filter(String label) {
            this.label = label;
        }

        Filter next() {
            return values()[(ordinal() + 1) % values().length];
        }

        Filter previous() {
            return values()[(ordinal() + values().length - 1) % values().length];
        }
    }

    private final Filter filter;

    public GUIVesuvius() {
        this(Filter.ALL);
    }

    public GUIVesuvius(Filter filter) {
        this.filter = filter;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Vesuvius", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(4, ItemStacks.head(HEAD, """
                <e>Vesuvius
                <7>After completing a <a>Catacombs <7>or
                <c>Kuudra <7>run, if you don't open a
                <7>chest, I'll save it here for you!

                <7>But beware, I can only hold <660 runs
                <7>worth of chests, and will discard
                <7>them after <a>72 hours<7>!"""));
        layout.slot(22, ItemStacks.item(Material.SKELETON_SKULL, """
                <c>No treasures!
                <7>Chests from <c>Kuudra <7>runs where you
                <7>didn't open the end treasure will
                <7>accumulate here.

                {}""", KuudraAvailability.UNAVAILABLE_LORE));
        layout.slot(51, (_, _) -> ItemStacks.item(Material.ENDER_EYE, filterLore()),
                (click, c) -> c.player().openView(new GUIVesuvius(click.click() instanceof Click.Right ? filter.previous() : filter.next())));
    }

    private String filterLore() {
        StringBuilder lore = new StringBuilder("<a>Filter\n\n");
        for (Filter each : Filter.values()) {
            lore.append(each == filter ? "<b>▶ " : "<7>  ").append(each.label).append('\n');
        }
        return lore.append("\n<b>Right-click to go backwards!\n<e>Click to switch!").toString();
    }
}
