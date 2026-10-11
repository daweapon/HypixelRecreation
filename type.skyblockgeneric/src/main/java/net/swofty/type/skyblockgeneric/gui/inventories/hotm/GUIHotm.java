package net.swofty.type.skyblockgeneric.gui.inventories.hotm;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.hotm.HotmCoreLevel;
import net.swofty.type.skyblockgeneric.hotm.HotmPerk;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.hotm.HotmRegistry;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

public class GUIHotm extends StatelessView {
    private static final int CORE_SLOT = 22;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Heart of the Mountain", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        for (HotmPerk perk : HotmRegistry.get().perks().values()) {
            layout.slot(perk.slot(), (_, _) -> perkItem(player, perk), (_, c) -> {
                String error = HotmService.buyPerk(player, perk.id());
                player.sendMessage(error != null ? error : "<a>You upgraded <6>" + perk.name() + "<a>!");
                c.session(Object.class).refresh();
            });
        }

        layout.slot(CORE_SLOT, (_, _) -> coreItem(player), (_, c) -> {
            String error = HotmService.upgradeCore(player);
            player.sendMessage(error != null ? error : "<a>You upgraded the <5>Core of the Mountain<a>!");
            c.session(Object.class).refresh();
        });
        layout.slot(45, ItemStacks.item(Material.REDSTONE_BLOCK, """
                <c>Reset the Heart of the Mountain
                <7>Resets all perk levels and refunds the
                <7>Powder and Tokens spent on them.

                <7>Your Tier and Core of the Mountain
                <7>levels are kept, and Powder spent on the
                <7>Core is not refunded.

                <e>Click to reset!"""), (_, c) -> c.push(new GUIHotmReset()));
        layout.slot(46, (_, _) -> abilitiesItem(player), (_, c) -> c.push(new GUIHotmAbilities()));
        layout.slot(47, (_, _) -> tierItem(player));
        layout.slot(51, (_, _) -> powderItem(player));
    }

    private static ItemStack.Builder tierItem(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        StringBuilder text = new StringBuilder("<5>Heart of the Mountain Tier " + data.getTier() + "\n");
        if (data.isMaxed()) {
            text.append("<a>Maximum tier reached!\n");
        } else {
            text.append("<7>Progress to Tier ").append(data.getTier() + 1).append(": <e>").append(data.getProgressPercent()).append("%\n");
            text.append("<7>").append(String.format("%,d", data.getExperience())).append("<8>/<7>").append(String.format("%,d", data.getNextTierXp())).append(" HotM Exp\n");
        }
        text.append("\n<7>Tokens of the Mountain: <5>").append(data.getAvailableTokens());
        text.append("\n<7>Forge slots: <a>").append(HotmService.forgeSlots(player));
        return ItemStacks.item(Material.PRISMARINE_CRYSTALS, text.toString());
    }

    private static ItemStack.Builder powderItem(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        StringBuilder text = new StringBuilder("<a>Your Powder\n");
        for (HotmPowder powder : HotmPowder.values()) {
            text.append("<7>").append(powder.getDisplayName()).append(": ").append(powder.getColour())
                    .append(String.format("%,d", data.powder(powder))).append("\n");
        }
        return ItemStacks.item(Material.GUNPOWDER, text.toString());
    }

    private static ItemStack.Builder abilitiesItem(SkyBlockPlayer player) {
        var selected = net.swofty.type.skyblockgeneric.hotm.HotmAbilities.selected(player);
        return ItemStacks.item(Material.GOLDEN_PICKAXE, "<6>Pickaxe Abilities\n<7>Unlock and choose the ability your\n<7>pickaxe uses on right-click.\n\n<7>Selected: "
                + (selected == null ? "<c>None" : "<a>" + selected.getDisplayName()) + "\n\n<e>Click to view!");
    }

    private static ItemStack.Builder coreItem(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        HotmRegistry registry = HotmRegistry.get();
        StringBuilder text = new StringBuilder("<5>Core of the Mountain " + data.getCoreLevel() + "<7>/<5>" + registry.maxCoreLevel() + "\n");
        for (HotmCoreLevel level : registry.coreLevels()) {
            boolean owned = level.level() <= data.getCoreLevel();
            text.append(owned ? "<a>" : "<8>").append(owned ? "✔ " : "✖ ").append("Level ").append(level.level()).append(": ").append(level.reward()).append("\n");
        }
        HotmCoreLevel next = registry.coreLevel(data.getCoreLevel() + 1);
        if (next == null) {
            text.append("\n<a>MAXED");
        } else {
            text.append("\n<7>Cost: ").append(next.cost() == 0 ? "<a>Free" : next.powder().getColour() + String.format("%,d", next.cost()) + " " + next.powder().getDisplayName());
            text.append("\n\n<e>Click to upgrade!");
        }
        return ItemStacks.item(Material.NETHER_STAR, text.toString());
    }

    private static ItemStack.Builder perkItem(SkyBlockPlayer player, HotmPerk perk) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        int level = data.perkLevel(perk.id());
        boolean locked = perk.tier() > data.getTier();
        boolean maxed = level >= perk.maxLevel();
        String colour = locked ? "<c>" : level > 0 ? "<a>" : "<e>";
        StringBuilder text = new StringBuilder(colour + perk.name());
        if (perk.maxLevel() > 1) text.append(" <7>(").append(colour).append(level).append("<7>/").append(colour).append(perk.maxLevel()).append("<7>)");
        text.append("\n").append(describe(perk, Math.max(level, 1))).append("\n");
        if (perk.maxLevel() > 1 && level > 0 && !maxed) {
            text.append("\n<7>Next level: ").append(describe(perk, level + 1));
        }
        text.append("\n");
        if (!perk.available()) text.append("\n<8>The effect applies once the required content is added.");
        if (locked) {
            text.append("\n<c>Requires Heart of the Mountain Tier ").append(perk.tier());
        } else if (maxed) {
            text.append("\n<a>MAXED");
        } else if (level == 0) {
            text.append("\n<7>Cost: <5>").append(perk.tokenCost()).append(" Token of the Mountain");
            text.append("\n\n<e>Click to unlock!");
        } else if (perk.usesPowder()) {
            text.append("\n<7>Cost: ").append(perk.powder().getColour()).append(String.format("%,d", perk.costToReach(level + 1))).append(" ").append(perk.powder().getDisplayName());
            text.append("\n\n<e>Click to upgrade!");
        }
        ItemStack.Builder builder = ItemStacks.item(perk.icon(), text.toString());
        return level > 0 ? ItemStacks.enchanted(builder) : builder;
    }

    private static String describe(HotmPerk perk, int level) {
        return "<7>" + perk.description()
                .replace("{value2}", format(perk.value("second", level)))
                .replace("{value}", format(perk.value(level)));
    }

    private static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) return String.format(Locale.ROOT, "%d", Math.round(value));
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
