package dev.daweapon.type.skyblockgeneric.beacon;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

public class BeaconEffectsView extends StatelessView {
    private static final int FIRST_SLOT = 10;
    private static final int BACK_SLOT = 31;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Beacon Effect", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        BeaconEffect[] effects = BeaconEffect.values();
        for (int index = 0; index < effects.length; index++) {
            BeaconEffect effect = effects[index];
            layout.slot(FIRST_SLOT + index, (_, c) -> render(effect, data(c)), (_, c) -> toggle(effect, (SkyBlockPlayer) c.player(), c));
        }
        if (!Components.back(layout, BACK_SLOT, ctx)) Components.close(layout, BACK_SLOT);
    }

    private static BeaconData data(ViewContext ctx) {
        return BeaconService.get(((SkyBlockPlayer) ctx.player()).getSkyBlockIsland());
    }

    private static ItemStack.Builder render(BeaconEffect effect, BeaconData data) {
        if (data == null) return ItemStacks.item(net.minestom.server.item.Material.BARRIER, "<c>No Beacon");
        boolean unlocked = effect.unlockedAt(data.tier());
        boolean enabled = data.effects().contains(effect);
        String status = !unlocked ? "<c>Requires Beacon " + BeaconMenuView.roman(effect.unlockTier()) + "!"
                : enabled ? "<a>ON\n\n<e>Click to turn off!" : "<c>OFF\n\n<e>Click to turn on!";
        return ItemStacks.item(effect.icon(), effect.label() + "\n<7>Applied to everyone while ON\n<7>your island.\n\n<7>Status: " + status);
    }

    private static void toggle(BeaconEffect effect, SkyBlockPlayer player, ViewContext ctx) {
        BeaconData data = BeaconService.get(player.getSkyBlockIsland());
        if (data == null || !effect.unlockedAt(data.tier())) {
            player.sendMessage("<c>Your Beacon is not strong enough for that effect!");
            return;
        }
        BeaconService.save(player.getSkyBlockIsland().getIslandID(), data.withEffectToggled(effect));
        ctx.session(DefaultState.class).refresh();
    }
}
