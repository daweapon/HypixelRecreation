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
import net.minestom.server.item.Material;

import java.util.List;

public class BeaconStatsView extends StatelessView {
    private static final List<Integer> SLOTS = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23);
    private static final int NONE_SLOT = 30;
    private static final int BACK_SLOT = 31;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Profile Stats", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        BeaconStat[] stats = BeaconStat.values();
        for (int index = 0; index < stats.length; index++) {
            BeaconStat stat = stats[index];
            layout.slot(SLOTS.get(index), (_, c) -> render(stat, data(c)), (_, c) -> choose(stat, (SkyBlockPlayer) c.player(), c));
        }
        layout.slot(NONE_SLOT, (_, c) -> ItemStacks.item(Material.RED_DYE, "<c>No Stat\n<7>Stop granting a profile stat.\n\n<e>Click to clear!"),
                (_, c) -> choose(null, (SkyBlockPlayer) c.player(), c));
        if (!Components.back(layout, BACK_SLOT, ctx)) Components.close(layout, BACK_SLOT);
    }

    private static BeaconData data(ViewContext ctx) {
        return BeaconService.get(((SkyBlockPlayer) ctx.player()).getSkyBlockIsland());
    }

    private static ItemStack.Builder render(BeaconStat stat, BeaconData data) {
        if (data == null) return ItemStacks.item(Material.BARRIER, "<c>No Beacon");
        boolean selected = data.stat() == stat;
        String scorched = "<7>With a <6>Scorched Power Crystal<7>: <a>+" + BeaconMenuView.number(stat.valueAtTier(data.tier()) * BeaconData.SCORCHED_STAT_MULTIPLIER);
        return ItemStacks.item(stat.icon(), stat.label() + "\n"
                + "<7>Granted EVERYWHERE in SkyBlock\n<7>while the Beacon is powered.\n\n"
                + "<7>Bonus: <a>+" + BeaconMenuView.number(stat.valueAtTier(data.tier())) + "\n"
                + scorched + "\n\n"
                + (selected ? "<a>Currently selected!" : "<e>Click to select!"));
    }

    private static void choose(BeaconStat stat, SkyBlockPlayer player, ViewContext ctx) {
        BeaconData data = BeaconService.get(player.getSkyBlockIsland());
        if (data == null) return;
        BeaconService.save(player.getSkyBlockIsland().getIslandID(), data.withStat(stat));
        player.sendMessage(stat == null ? "<a>Your Beacon no longer grants a profile stat." : "<a>Your Beacon now grants " + stat.label() + "<a>!");
        ctx.session(DefaultState.class).refresh();
    }
}
