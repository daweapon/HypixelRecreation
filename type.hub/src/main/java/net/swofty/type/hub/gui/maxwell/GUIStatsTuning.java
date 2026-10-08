package net.swofty.type.hub.gui.maxwell;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.accessory.power.AccessoryPowers;
import net.swofty.type.skyblockgeneric.accessory.power.PowerScaling;
import net.swofty.type.skyblockgeneric.accessory.power.StatText;
import net.swofty.type.skyblockgeneric.accessory.power.TuningStat;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers.PowerData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

/** Stats Tuning: spend Tuning Points (1 per 10 Accessory Power) on stats, and save or load templates. */
public class GUIStatsTuning extends StatelessView {
    private static final Map<TuningStat, Integer> STAT_SLOTS = Map.of(
            TuningStat.HEALTH, 19, TuningStat.DEFENSE, 20, TuningStat.SPEED, 21, TuningStat.STRENGTH, 22,
            TuningStat.CRIT_DAMAGE, 28, TuningStat.CRIT_CHANCE, 29, TuningStat.ATTACK_SPEED, 30, TuningStat.INTELLIGENCE, 31);
    private static final int[] TEMPLATE_ROWS = {15, 24, 33, 42};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Stats Tuning", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PowerData data = AccessoryPowers.data(player);
        int accessoryPower = player.getAccessoryPower();

        Components.fill(layout);
        if (!Components.back(layout, 49, ctx)) Components.close(layout, 49);
        layout.slot(4, (s, c) -> ItemStacks.item(Material.COMPARATOR, "<a>Stats Tuning\n"
                + "<7>Optimize your build to your liking by using\n<e>Tuning Points<7>.\n\n"
                + "<7>Every <6>10 AP <7>grants <e>1 Tuning Point<7>.\n\n"
                + "<7>Accessory Power: <6>" + StringUtility.commaify(accessoryPower) + "\n"
                + "<7>Tuning Points: <e>" + PowerScaling.tuningPoints(accessoryPower) + "\n"
                + "<7>Unassigned Points: <e>" + AccessoryPowers.unassignedPoints(data, accessoryPower)));

        ItemStatistics total = player.getStatistics().allStatistics();
        STAT_SLOTS.forEach((stat, slot) -> layout.slot(slot,
                (s, c) -> statItem(stat, data, accessoryPower, total.getOverall(stat.getStatistic())),
                (click, c) -> tune(click, c, stat)));

        for (int template = 0; template < AccessoryPowers.TEMPLATE_SLOTS; template++) {
            int index = template;
            int loadSlot = TEMPLATE_ROWS[template];
            if (AccessoryPowers.isTemplateUnlocked(data, index)) {
                boolean stored = data.getTemplates().containsKey(index);
                layout.slot(loadSlot, ItemStacks.item(Material.BOOK, "<a>Load\n<8>Tuning Template #" + (index + 1) + "\n\n"
                                + (stored ? "<e>Click to load!" : "<7>There is no template stored in this\n<7>slot.\n\n<c>Nothing to load!")),
                        (click, c) -> loadTemplate(c, index));
                layout.slot(loadSlot + 1, ItemStacks.item(Material.WRITABLE_BOOK, "<a>Save\n<8>Tuning Template #" + (index + 1) + "\n\n"
                                + (stored ? "<7>A template is stored in this slot.\n\n<e>Click to overwrite!" : "<7>There is no template stored in this\n<7>slot.\n\n<e>Click to save!")),
                        (click, c) -> saveTemplate(c, index));
            } else {
                ItemStack.Builder locked = ItemStacks.item(Material.GRAY_DYE, "<c>Tuning Template\n<8>Tuning Template #" + (index + 1) + "\n\n"
                        + "<7>Save and load your stats tuning\n<7>in this convenient template\n<7>slot!\n\n"
                        + "<7>Cost: <b>" + StringUtility.commaify(AccessoryPowers.TEMPLATE_BITS_COST) + " Bits\n\n"
                        + (index == data.getTemplatesUnlocked() ? "<e>Click to unlock!" : "<c>Unlock the previous template first!"));
                layout.slot(loadSlot, locked, (click, c) -> unlockTemplate(c, index));
                layout.slot(loadSlot + 1, locked, (click, c) -> unlockTemplate(c, index));
            }
        }
    }

    private static ItemStack.Builder statItem(TuningStat stat, PowerData data, int accessoryPower, double current) {
        String colour = "<color:" + stat.getStatistic().getDisplayColor().asHexString() + ">";
        return ItemStacks.item(stat.getIcon(), colour + stat.getStatistic().getDisplayName() + "\n"
                + "<7>You have: " + colour + StatText.number(current) + (stat.getStatistic().getIsPercentage() ? "%" : "") + "\n"
                + "<8><m>------------------------</m>\n"
                + "<7>Stat has: <c>" + data.pointsIn(stat) + " points\n"
                + "<7>Per point: " + StatText.line(stat.getStatistic(), stat.getPerPoint()) + "\n"
                + "<7>Points left: " + AccessoryPowers.unassignedPoints(data, accessoryPower) + "\n\n"
                + "<e>Right-Click to add a point!\n<e>Left-Click to remove a point!\n<8>Hold shift for 10 points!");
    }

    private static void tune(ClickContext<DefaultState> click, ViewContext ctx, TuningStat stat) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PowerData data = AccessoryPowers.data(player);
        int accessoryPower = player.getAccessoryPower();
        boolean changed = switch (click.click()) {
            case Click.Right ignored -> AccessoryPowers.addPoints(data, stat, 1, accessoryPower) > 0;
            case Click.RightShift ignored -> AccessoryPowers.addPoints(data, stat, 10, accessoryPower) > 0;
            case Click.Left ignored -> AccessoryPowers.removePoints(data, stat, 1) > 0;
            case Click.LeftShift ignored -> AccessoryPowers.removePoints(data, stat, 10) > 0;
            default -> false;
        };
        if (!changed) {
            if (click.click() instanceof Click.Right || click.click() instanceof Click.RightShift)
                player.sendMessage("<c>You don't have any unassigned Tuning Points!");
            return;
        }
        AccessoryPowers.save(player, data);
        ctx.session(DefaultState.class).refresh();
    }

    private static void saveTemplate(ViewContext ctx, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PowerData data = AccessoryPowers.data(player);
        AccessoryPowers.saveTemplate(data, index);
        AccessoryPowers.save(player, data);
        player.sendMessage("<a>Saved your stats tuning to <e>Tuning Template #" + (index + 1) + "<a>!");
        ctx.session(DefaultState.class).refresh();
    }

    private static void loadTemplate(ViewContext ctx, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PowerData data = AccessoryPowers.data(player);
        if (!AccessoryPowers.loadTemplate(data, index)) {
            player.sendMessage("<c>Nothing to load!");
            return;
        }
        AccessoryPowers.save(player, data);
        player.sendMessage("<a>Loaded <e>Tuning Template #" + (index + 1) + "<a>!");
        ctx.session(DefaultState.class).refresh();
    }

    private static void unlockTemplate(ViewContext ctx, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PowerData data = AccessoryPowers.data(player);
        if (index != data.getTemplatesUnlocked()) {
            player.sendMessage("<c>Unlock the previous template first!");
            return;
        }
        if (player.getBits() < AccessoryPowers.TEMPLATE_BITS_COST) {
            player.sendMessage("<c>You don't have enough Bits!");
            return;
        }
        player.removeBits(AccessoryPowers.TEMPLATE_BITS_COST);
        data.setTemplatesUnlocked(index + 1);
        AccessoryPowers.save(player, data);
        player.sendMessage("<a>Unlocked <e>Tuning Template #" + (index + 1) + "<a>!");
        ctx.session(DefaultState.class).refresh();
    }
}
