package dev.daweapon.type.skyblockgeneric.gui.inventories.moonglade;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconCooldowns;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconRules;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.MoongladeBeacon;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class GUIMoongladeBeacon extends StatelessView {
    private static final int BEACON_SLOT = 13;
    private static final int TUNE_SLOT = 30;
    private static final int UPGRADE_SLOT = 32;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withText((_, ctx) ->
                Text.of("<9>" + MoongladeBeacon.site().displayName() + " {}", title((SkyBlockPlayer) ctx.player())), InventoryType.CHEST_6_ROW);
    }

    private static String title(SkyBlockPlayer player) {
        int level = MoongladeBeacon.level(player);
        return level == 0 ? "0" : StringUtility.getAsRomanNumeral(level);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);
        layout.slot(BEACON_SLOT, (_, c) -> beacon((SkyBlockPlayer) c.player()));
        layout.slot(TUNE_SLOT, (_, c) -> tune((SkyBlockPlayer) c.player()),
                (_, c) -> {
                    SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                    long wait = BeaconCooldowns.remainingMillis(clicker.getUuid(), System.currentTimeMillis());
                    if (wait > 0) {
                        clicker.sendMessage("<c>You can tune the beacon again in <b>{}<c>!", StringUtility.formatTimeLeft(wait));
                        return;
                    }
                    c.push(new GUIBeaconTuning(false));
                });
        layout.slot(UPGRADE_SLOT, (_, c) -> upgrade((SkyBlockPlayer) c.player()), (_, c) -> {
            SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
            if (MoongladeBeacon.level(clicker) >= BeaconRules.MAX_LEVEL) return;
            if (clicker.countItem(ItemType.SIGNAL_ENHANCER) < 1) {
                clicker.sendMessage("<c>You need a Signal Enhancer to improve the signal strength!");
                return;
            }
            c.push(new GUIBeaconTuning(true));
        });
    }

    private static ItemStack.Builder beacon(SkyBlockPlayer player) {
        int stacks = MoongladeBeacon.activeStacks();
        double fortune = MoongladeBeacon.fortune(player);
        return ItemStacks.item(Material.BEACON, """
                <9>{} {}
                <8>Island-Wide Effect

                <7>This beacon currently gives you:
{}
                <7>...because it has <b>{} Stack{}<7>.
                <7>(Stack limit: <a>{}<7>)

                <7>Successfully tuning the frequency
                <7>grants <b>+1 Stack <7>to all players on
                <7>the island for <a>20min<7>.""",
                MoongladeBeacon.site().displayName(), title(player), fortuneLines(fortune), stacks, stacks == 1 ? "" : "s",
                BeaconRules.stackLimit(MoongladeBeacon.level(player)));
    }

    private static String fortuneLines(double fortune) {
        StringBuilder lines = new StringBuilder();
        for (ItemStatistic statistic : MoongladeBeacon.site().statistics()) {
            if (!lines.isEmpty()) lines.append('\n');
            lines.append("                <6> +").append(BeaconRules.format(fortune)).append("☘ ").append(statistic.getDisplayName());
        }
        return lines.toString();
    }

    private static ItemStack.Builder tune(SkyBlockPlayer player) {
        return ItemStacks.item(Material.COMPARATOR, """
                <9>Tune Frequency
                <7>Successfully tuning the frequency
                <7>grants <b>+1 Stack <7>to all players on
                <7>the island for <a>20min<7>.

                <7>Current Stacks: <b>{}

                <e>Click to start tuning!""", MoongladeBeacon.activeStacks());
    }

    private static ItemStack.Builder upgrade(SkyBlockPlayer player) {
        int level = MoongladeBeacon.level(player);
        if (level >= BeaconRules.MAX_LEVEL) {
            return ItemStacks.item(Material.REPEATER, """
                    <6>Upgrade Signal Strength
                    <8>Beacon Upgrade

                    <b>{} {}
                    <8>Max level reached!""", MoongladeBeacon.site().displayName(), StringUtility.getAsRomanNumeral(level));
        }
        int next = level + 1;
        boolean owns = player.countItem(ItemType.SIGNAL_ENHANCER) > 0;
        return ItemStacks.item(Material.REPEATER, """
                <6>Improve Signal Strength
                <8>Beacon Upgrade

                <b>{} {}
                <7>Stack Limit: <a>{}
                <7>Fortune Per Stack: <6>+{}☘

                <7>Cost
                <6>Signal Enhancer
                """ + (owns ? "\n<e>Click to upgrade!" : "\n<c>You don't have a Signal Enhancer!"),
                MoongladeBeacon.site().displayName(), StringUtility.getAsRomanNumeral(next), BeaconRules.stackLimit(next),
                BeaconRules.format(BeaconRules.fortunePerStack(next)));
    }
}
