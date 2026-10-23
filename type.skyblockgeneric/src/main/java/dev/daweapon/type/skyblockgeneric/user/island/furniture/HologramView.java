package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class HologramView implements StatefulView<HologramView.State> {
    private static final int[] LINE_SLOTS = {10, 11, 12};

    private final IslandFurniture owner;
    private final PlacedFurniture initial;

    public HologramView(IslandFurniture owner, PlacedFurniture initial) {
        this.owner = owner;
        this.initial = initial;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Hologram", InventoryType.CHEST_3_ROW);
    }

    @Override
    public State initialState() {
        return new State(initial);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        HologramSettings settings = HologramSettings.decode(state.hologram().data());
        Components.fill(layout);
        for (int line = 0; line < HologramSettings.MAX_LINES; line++) {
            int index = line;
            String current = settings.line(line);
            layout.slot(LINE_SLOTS[line], (_, _) -> ItemStacks.item(Material.OAK_SIGN, """
                            <a>Line %d
                            <7>Current: %s

                            <e>Click to edit!""".formatted(index + 1, current.isBlank() ? "<8>empty" : "<f>" + current.replace('&', '§'))),
                    (c, _) -> editLine((SkyBlockPlayer) c.player(), c.state().hologram(), index));
        }
        layout.slot(14, (_, _) -> ItemStacks.item(Material.FIREWORK_STAR, """
                        <a>Particle Colour
                        <7>Current: <f>%s

                        <e>Click to cycle!""".formatted(settings.particle() == null ? "None" : HologramColours.nameOf(settings.particle()))),
                (c, v) -> update(c.state(), v, HologramSettings.decode(c.state().hologram().data()).nextParticle()));
        layout.slot(15, (_, _) -> ItemStacks.item(Material.ARMOR_STAND, """
                        <a>Model
                        <7>Currently: %s
                        <8>The model can only be hidden while the
                        <8>Hologram has text.

                        <e>Click to toggle!""".formatted(settings.modelVisible() ? "<a>Shown" : "<c>Hidden")),
                (c, v) -> {
                    HologramSettings current = HologramSettings.decode(c.state().hologram().data());
                    if (!current.hasText()) {
                        v.player().sendMessage("<c>Write at least one line of text before hiding the model!");
                        return;
                    }
                    update(c.state(), v, current.withModelVisible(!current.modelVisible()));
                });
        layout.slot(16, (_, _) -> ItemStacks.item(Material.BARRIER, """
                        <c>Pickup Hologram
                        <7>Put this Hologram back in your
                        <7>inventory.

                        <e>Click to pick up!"""),
                (c, _) -> {
                    c.player().closeInventory();
                    FurnitureActions.pickUp(owner, (SkyBlockPlayer) c.player(), c.state().hologram());
                });
        Components.close(layout, 22);
    }

    private void editLine(SkyBlockPlayer player, PlacedFurniture hologram, int line) {
        HologramEditing.select(player, hologram, line);
        player.closeInventory();
        player.sendMessage("<a>Editing line {}. <7>Type <e>/hgl <text> <7>to set it, or <e>/hgl clear <7>to remove it.", line + 1);
        player.sendMessage("<7>Use <e>&a<7>-<e>&f<7>, <e>&0<7>-<e>&9<7> for colours and <e>&l &o &n &m &k <7>for formatting. Up to {} characters per line.", HologramSettings.MAX_LINE_LENGTH);
    }

    private void update(State state, ViewContext ctx, HologramSettings settings) {
        PlacedFurniture current = state.hologram();
        PlacedFurniture updated = owner.replace(ctx.player().getInstance(), current, current.withData(settings.encode()));
        ctx.session(State.class).setState(new State(updated));
    }

    public record State(PlacedFurniture hologram) {
    }
}
