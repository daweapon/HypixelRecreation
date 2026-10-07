package net.swofty.type.skyblockgeneric.gui.inventories.spraycan;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.spray.Spray;
import net.swofty.type.skyblockgeneric.spray.SprayCanData;
import net.swofty.type.skyblockgeneric.spray.SprayCatalogue;
import net.swofty.type.skyblockgeneric.spray.SprayDirection;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Picks which way an arrow spray points, laid out like a compass around the spray. */
public final class GUISprayOrientation implements StatefulView<GUISprayOrientation.State> {
    private static final int CENTRE_SLOT = 22;
    private static final int BACK_SLOT = 40;
    private static final Map<SprayDirection, Integer> SLOTS = new EnumMap<>(Map.of(
            SprayDirection.UP_LEFT, 12, SprayDirection.UP, 13, SprayDirection.UP_RIGHT, 14,
            SprayDirection.LEFT, 21, SprayDirection.RIGHT, 23,
            SprayDirection.DOWN_LEFT, 30, SprayDirection.DOWN, 31, SprayDirection.DOWN_RIGHT, 32));

    @Override
    public State initialState() {
        return new State(-1, "");
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Select Orientation", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        Components.back(layout, BACK_SLOT, ctx);
        Spray spray = SprayCatalogue.byId(state.sprayId()).orElse(null);
        SprayCanData can = SprayCans.read(player, state.slot()).orElse(null);
        if (spray == null || can == null) return;

        List<Text> inkLore = new ArrayList<>();
        spray.lore().forEach(line -> inkLore.add(Text.of("<7>{}", line)));
        inkLore.add(Text.empty());
        inkLore.add(Text.of("<7>Required Ink"));
        spray.ink().forEach((dye, percent) -> inkLore.add(dye.loreLine(percent, can.inkOf(dye) >= percent)));
        layout.slot(CENTRE_SLOT, ItemStacks.item(Material.PAINTING, 1, Text.of("<a>{}", spray.name()), inkLore));

        for (SprayDirection direction : SprayDirection.offeredBy(spray)) {
            boolean selected = spray.id().equals(can.getSelectedSpray()) && can.getDirection() == direction;
            layout.slot(SLOTS.get(direction), (s, c) -> {
                var item = ItemStacks.item(Material.ARROW, selected
                        ? "<a>{}\n\n<e>Right-Click to preview!\n\n<a>Selected!"
                        : "<a>{}\n\n<e>Right-Click to preview!\n\n<e>Click to select!", direction.getDisplayName());
                return selected ? ItemStacks.enchanted(item) : item;
            }, (click, c) -> {
                if (click.click() instanceof Click.Right) {
                    GUISprayCan.preview(player, spray, direction);
                    return;
                }
                SprayCans.select(player, state.slot(), spray, direction);
                c.session(State.class).refresh();
            });
        }
    }

    public record State(int slot, String sprayId) {}
}
