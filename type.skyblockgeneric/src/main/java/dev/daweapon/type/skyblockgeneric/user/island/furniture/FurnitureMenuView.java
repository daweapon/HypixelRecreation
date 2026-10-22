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

public class FurnitureMenuView implements StatefulView<FurnitureMenuView.State> {
    private final IslandFurniture owner;
    private final PlacedFurniture initial;

    public FurnitureMenuView(IslandFurniture owner, PlacedFurniture initial) {
        this.owner = owner;
        this.initial = initial;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Furniture", InventoryType.CHEST_3_ROW);
    }

    @Override
    public State initialState() {
        return new State(initial);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (_, _) -> ItemStacks.item(Material.ARROW, """
                        <a>Rotate Left
                        <7>Turn this furniture %d degrees anticlockwise.

                        <e>Click to rotate!""".formatted(PlacedFurniture.YAW_STEP)),
                (c, v) -> rotate(c.state(), v, -PlacedFurniture.YAW_STEP));
        layout.slot(13, (_, _) -> ItemStacks.item(Material.BARRIER, """
                        <c>Pickup Furniture
                        <7>Put this furniture back in your
                        <7>inventory.

                        <e>Click to pick up!"""),
                (c, _) -> {
                    c.player().closeInventory();
                    FurnitureActions.pickUp(owner, (SkyBlockPlayer) c.player(), c.state().furniture());
                });
        layout.slot(15, (_, _) -> ItemStacks.item(Material.ARROW, """
                        <a>Rotate Right
                        <7>Turn this furniture %d degrees clockwise.

                        <e>Click to rotate!""".formatted(PlacedFurniture.YAW_STEP)),
                (c, v) -> rotate(c.state(), v, PlacedFurniture.YAW_STEP));
        Components.close(layout, 22);
    }

    private void rotate(State state, ViewContext ctx, int degrees) {
        PlacedFurniture current = state.furniture();
        PlacedFurniture rotated = owner.replace(ctx.player().getInstance(), current, current.withYaw(current.yaw() + degrees));
        ctx.session(State.class).setState(new State(rotated));
    }

    public record State(PlacedFurniture furniture) {
    }
}
