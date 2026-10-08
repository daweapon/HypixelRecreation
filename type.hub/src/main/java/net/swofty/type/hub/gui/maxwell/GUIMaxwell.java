package net.swofty.type.hub.gui.maxwell;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.swofty.type.skyblockgeneric.accessory.OwnedAccessory;
import net.swofty.type.skyblockgeneric.accessory.power.AccessoryPowerType;
import net.swofty.type.skyblockgeneric.accessory.power.AccessoryPowers;
import net.swofty.type.skyblockgeneric.accessory.power.PowerScaling;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers.PowerData;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUIAccessoryBag;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Maxwell's Accessory Bag Thaumaturgy: choose the bag's power, learn stone powers and tune stats. */
public class GUIMaxwell implements StatefulView<GUIMaxwell.Page> {
    private static final int[] POWER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    public record Page(int index) {
    }

    @Override
    public ViewConfiguration<Page> configuration() {
        return new ViewConfiguration<>("Accessory Bag Thaumaturgy", InventoryType.CHEST_6_ROW);
    }

    @Override
    public Page initialState() {
        return new Page(0);
    }

    @Override
    public void layout(ViewLayout<Page> layout, Page state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PowerData data = AccessoryPowers.data(player);
        int accessoryPower = player.getAccessoryPower();
        int combatLevel = AccessoryPowers.combatLevel(player);

        Components.fill(layout);
        Components.close(layout, 49);

        List<AccessoryPowerType> entries = new ArrayList<>();
        entries.add(null);
        for (AccessoryPowerType power : AccessoryPowerType.menuOrder()) {
            if (AccessoryPowers.isUnlocked(data, power)) entries.add(power);
        }
        int pages = Math.max(1, (entries.size() + POWER_SLOTS.length - 1) / POWER_SLOTS.length);
        int page = Math.min(state.index(), pages - 1);
        for (int i = 0; i < POWER_SLOTS.length; i++) {
            int index = page * POWER_SLOTS.length + i;
            if (index >= entries.size()) break;
            AccessoryPowerType power = entries.get(index);
            if (power == null) {
                layout.slot(POWER_SLOTS[i], (s, c) -> MaxwellItems.noPower(data.getSelected() == null && data.isPowerChosen()),
                        (click, c) -> select(c, null));
            } else {
                layout.slot(POWER_SLOTS[i], (s, c) -> powerItem(power, data, accessoryPower, combatLevel),
                        (click, c) -> select(c, power));
            }
        }
        if (page > 0) layout.slot(45, ItemStacks.item(Material.ARROW, "<a>Previous Page\n<e>Page " + page),
                (click, c) -> c.replace(new GUIMaxwell(), new Page(page - 1)));
        if (page < pages - 1) layout.slot(53, ItemStacks.item(Material.ARROW, "<a>Next Page\n<e>Page " + (page + 2)),
                (click, c) -> c.replace(new GUIMaxwell(), new Page(page + 1)));

        layout.slot(47, ItemStacks.head(MaxwellItems.ACCESSORY_BAG_TEXTURE, """
                        <a>Accessory Bag Shortcut
                        <7>Quickly access your accessory bag
                        <7>from right here!

                        <e>Click to open!"""),
                (click, c) -> c.push(new GUIAccessoryBag()));
        layout.slot(48, (s, c) -> breakdown(player));
        layout.slot(50, ItemStacks.item(Material.ENCHANTING_TABLE, """
                        <a>Learn Power From Stones
                        <7>Combine <a>9x <7>identical <9>Power Stones
                        <7>that you may find across <a>SkyBlock <7>to
                        <7>permanently unlock their <a>power<7>.

                        <e>Click to combine stones!"""),
                (click, c) -> c.push(new GUILearnPowers()));
        layout.slot(51, (s, c) -> ItemStacks.item(Material.COMPARATOR, "<a>Stats Tuning\n"
                        + "<7>Optimize your build to your liking by using\n<e>Tuning Points<7>.\n\n"
                        + "<7>Every <6>10 AP <7>grants <e>1 Tuning Point<7>.\n\n"
                        + "<7>Accessory Power: <6>" + StringUtility.commaify(accessoryPower) + "\n"
                        + "<7>Tuning Points: <e>" + PowerScaling.tuningPoints(accessoryPower)
                        + unassignedWarning(data, accessoryPower) + "\n\n<e>Click to tune your stats!"),
                (click, c) -> c.push(new GUIStatsTuning()));
    }

    private static String unassignedWarning(PowerData data, int accessoryPower) {
        int unassigned = AccessoryPowers.unassignedPoints(data, accessoryPower);
        return unassigned == 0 ? "" : "\n<7>Unassigned Points: <c>" + unassigned + "!!!";
    }

    private static ItemStack.Builder powerItem(AccessoryPowerType power, PowerData data, int accessoryPower, int combatLevel) {
        boolean selected = data.getSelected() == power;
        String action;
        if (selected) action = "<a>Power is selected!";
        else if (!AccessoryPowers.meetsRequirement(power, combatLevel))
            action = "<c>Requires <a>Combat Skill " + power.getCombatLevel() + "<c>.";
        else action = "<e>Click to select power!";
        ItemStack.Builder builder = MaxwellItems.withText(MaxwellItems.icon(power),
                "<a>" + power.getDisplayName() + "\n<8>" + power.getTier().getDisplayName() + "\n\n"
                        + MaxwellItems.stats(power, accessoryPower) + "\n"
                        + MaxwellItems.accessoryPower(accessoryPower) + "\n\n" + action);
        return selected ? ItemStacks.enchanted(builder) : builder;
    }

    private static void select(ViewContext ctx, AccessoryPowerType power) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PowerData data = AccessoryPowers.data(player);
        if (power != null && !AccessoryPowers.canSelect(data, power, AccessoryPowers.combatLevel(player))) {
            player.sendMessage("<c>You need <a>Combat Skill " + power.getCombatLevel() + " <c>to use this power!");
            return;
        }
        if (data.isPowerChosen() && data.getSelected() == power) return;
        AccessoryPowers.select(data, power);
        AccessoryPowers.save(player, data);
        player.sendMessage(power == null
                ? "<a>You no longer have a power selected for your <a>Accessory Bag<a>."
                : "<a>Selected the <e>" + power.getDisplayName() + " <a>power for your <a>Accessory Bag<a>!");
        ctx.session(Page.class).refresh();
    }

    private static ItemStack.Builder breakdown(SkyBlockPlayer player) {
        Map<Rarity, Integer> counts = new EnumMap<>(Rarity.class);
        for (OwnedAccessory owned : AccessoryOwnership.active(player)) {
            if (!owned.inBag() || !owned.definition().getRealm().countsForPower()) continue;
            counts.merge(owned.item().getAttributeHandler().getEffectiveRarity(), 1, Integer::sum);
        }
        int fromRarity = 0;
        StringBuilder lines = new StringBuilder();
        for (Rarity rarity : MaxwellItems.breakdownRarities()) {
            int count = counts.getOrDefault(rarity, 0);
            int power = rarity.getAccessoryPower();
            fromRarity += count * power;
            lines.append("<6>").append(power).append(" AP <7>x <color:").append(rarity.getColor().asHexString()).append(">")
                    .append(count).append(" Accs. <7>= <6>").append(count * power).append(" AP\n");
        }
        int total = player.getAccessoryPower();
        return ItemStacks.item(Material.FILLED_MAP, "<a>Accessories Breakdown\n<8>From your bag\n\n"
                + lines + "<6>+" + (total - fromRarity) + " AP <7>from special bonuses\n\n"
                + "<7>Total: <6>" + StringUtility.commaify(total) + " Accessory Power");
    }
}
