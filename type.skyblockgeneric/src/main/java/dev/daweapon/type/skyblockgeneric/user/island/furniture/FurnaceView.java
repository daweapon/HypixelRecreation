package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

public class FurnaceView extends StatelessView {
    private static final int INPUT = 0;
    private static final int FUEL = 1;
    private static final int OUTPUT = 2;

    private final IslandFurniture owner;
    private final PlacedFurniture furniture;
    private Task ticker;

    public FurnaceView(IslandFurniture owner, PlacedFurniture furniture) {
        this.owner = owner;
        this.furniture = furniture;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Furnace", InventoryType.FURNACE);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        FurnaceState furnace = load();
        furnace.advanceTo(System.currentTimeMillis());
        owner.setStorage(furniture, furnace.serialize());
        layout.editable(INPUT, furnace.input.isAir() ? ItemStack.builder(net.minestom.server.item.Material.AIR) : furnace.input.builder(), (_, _, _, _) -> {
        });
        layout.editable(FUEL, furnace.fuel.isAir() ? ItemStack.builder(net.minestom.server.item.Material.AIR) : furnace.fuel.builder(), (_, _, _, _) -> {
        });
        layout.editable(OUTPUT, furnace.output.isAir() ? ItemStack.builder(net.minestom.server.item.Material.AIR) : furnace.output.builder(), (_, _, _, _) -> {
        });
    }

    @Override
    public void onOpen(DefaultState state, ViewContext ctx) {
        ticker = MinecraftServer.getSchedulerManager().buildTask(() -> tick(ctx)).repeat(TaskSchedule.seconds(1)).schedule();
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        if (ticker != null) ticker.cancel();
        FurnaceState furnace = read(ctx);
        furnace.touchedAt = System.currentTimeMillis();
        owner.setStorage(furniture, furnace.serialize());
    }

    private void tick(ViewContext ctx) {
        FurnaceState furnace = read(ctx);
        furnace.advance(20);
        furnace.touchedAt = System.currentTimeMillis();
        owner.setStorage(furniture, furnace.serialize());
        ctx.inventory().setItemStack(INPUT, furnace.input);
        ctx.inventory().setItemStack(FUEL, furnace.fuel);
        ctx.inventory().setItemStack(OUTPUT, furnace.output);
    }

    private FurnaceState load() {
        return FurnaceState.parse(owner.storageOf(furniture));
    }

    private FurnaceState read(ViewContext ctx) {
        FurnaceState furnace = new FurnaceState();
        furnace.input = ctx.inventory().getItemStack(INPUT);
        furnace.fuel = ctx.inventory().getItemStack(FUEL);
        furnace.output = ctx.inventory().getItemStack(OUTPUT);
        FurnaceState stored = load();
        furnace.burnLeft = stored.burnLeft;
        furnace.burnTotal = stored.burnTotal;
        furnace.cookProgress = stored.cookProgress;
        return furnace;
    }
}
