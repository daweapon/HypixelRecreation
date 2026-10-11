package net.swofty.type.skyblockgeneric.gui.inventories.hotm;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
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
import net.swofty.type.skyblockgeneric.hotm.HotmTierReward;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

public class GUIHotm extends StatelessView {
    private static final int VISIBLE_ROWS = 5;
    private static final int TOP_TIER = 10;
    private final int firstTier;

    public GUIHotm() {
        this(-1);
    }

    public GUIHotm(int firstTier) {
        this.firstTier = firstTier;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Heart of the Mountain", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int bottom = firstTier > 0 ? firstTier : Math.max(1, HotmService.data(player).getTier() - 2);
        bottom = Math.min(bottom, TOP_TIER - VISIBLE_ROWS + 1);

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int tier = bottom + VISIBLE_ROWS - 1 - row;
            layout.slot(row * 9, (_, _) -> tierLabel(player, tier));
        }
        for (HotmPerk perk : HotmRegistry.get().perks().values()) {
            if (perk.tier() < bottom || perk.tier() >= bottom + VISIBLE_ROWS) continue;
            int slot = (bottom + VISIBLE_ROWS - 1 - perk.tier()) * 9 + perk.col();
            layout.slot(slot, (_, _) -> perkItem(player, perk), (click, c) -> {
                if (click.click() instanceof Click.Right) {
                    if (HotmService.togglePerk(player, perk.id())) {
                        player.sendMessage(HotmService.data(player).getDisabledPerks().contains(perk.id())
                                ? "<c>Disabled <6>" + perk.name() + "<c>." : "<a>Enabled <6>" + perk.name() + "<a>.");
                    }
                } else {
                    String error = HotmService.buyPerk(player, perk.id());
                    player.sendMessage(error != null ? error : "<a>You upgraded <6>" + perk.name() + "<a>!");
                }
                c.session(Object.class).refresh();
            });
        }

        int scrollUp = bottom + VISIBLE_ROWS - 1;
        if (scrollUp < TOP_TIER) {
            int target = bottom + 2;
            layout.slot(8, ItemStacks.item(Material.ARROW, "<a>Scroll Up\n<7>Show higher tiers."), (_, c) -> c.replace(new GUIHotm(target)));
        }
        if (bottom > 1) {
            int target = bottom - 2;
            layout.slot(44, ItemStacks.item(Material.ARROW, "<a>Scroll Down\n<7>Show lower tiers."), (_, c) -> c.replace(new GUIHotm(Math.max(1, target))));
        }

        Components.close(layout, 45);
        layout.slot(47, (_, _) -> powderItem(player));
        Components.back(layout, 48, ctx);
        layout.slot(49, (_, _) -> coreItem(player), (_, c) -> {
            String error = HotmService.upgradeCore(player);
            player.sendMessage(error != null ? error : "<a>You upgraded the <5>Core of the Mountain<a>!");
            c.session(Object.class).refresh();
        });
        layout.slot(50, (_, _) -> abilitiesItem(player), (_, c) -> c.push(new GUIHotmAbilities()));
        layout.slot(51, (_, _) -> tierItem(player));
        layout.slot(52, ItemStacks.item(Material.CAULDRON, """
                <c>Reset the Heart of the Mountain
                <7>Resets all perk levels and refunds the
                <7>Powder and Tokens spent on them.

                <7>Your Tier and Core of the Mountain
                <7>levels are kept, and Powder spent on the
                <7>Core is not refunded.

                <e>Click to reset!"""), (_, c) -> c.push(new GUIHotmReset()));
    }

    private static ItemStack.Builder tierLabel(SkyBlockPlayer player, int tier) {
        boolean reached = HotmService.data(player).getTier() >= tier;
        HotmTierReward reward = HotmRegistry.get().tierReward(tier);
        String text = (reached ? "<a>" : "<c>") + "Tier " + tier + "\n<7>Tokens: <5>+" + reward.tokens()
                + "\n<7>SkyBlock XP: <b>+" + reward.skyBlockXp()
                + (reward.forgeSlots() > 0 ? "\n<7>Forge slots: <a>" + reward.forgeSlots() : "")
                + (reached ? "\n\n<a>Reached" : "\n\n<c>Not reached yet");
        return ItemStacks.item(reached ? Material.LIME_BANNER : Material.RED_BANNER, text).amount(tier);
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
        return ItemStacks.item(Material.HEART_OF_THE_SEA, text.toString());
    }

    private static ItemStack.Builder powderItem(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        StringBuilder text = new StringBuilder("<a>Your Powder\n");
        for (HotmPowder powder : HotmPowder.values()) {
            text.append("<7>").append(powder.getDisplayName()).append(": ").append(powder.getColour())
                    .append(String.format("%,d", data.powder(powder))).append("\n");
        }
        return ItemStacks.item(Material.CHEST, text.toString());
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
        return ItemStacks.item(Material.ENDER_EYE, text.toString());
    }

    private static ItemStack.Builder perkItem(SkyBlockPlayer player, HotmPerk perk) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        int level = data.perkLevel(perk.id());
        boolean locked = perk.tier() > data.getTier();
        boolean maxed = level >= perk.maxLevel();
        boolean disabled = level > 0 && data.getDisabledPerks().contains(perk.id());
        boolean single = perk.maxLevel() == 1;
        String colour = locked ? "<c>" : level > 0 ? "<a>" : "<e>";
        StringBuilder text = new StringBuilder(colour + perk.name());
        if (!single) text.append(" <7>(").append(colour).append(level).append("<7>/").append(colour).append(perk.maxLevel()).append("<7>)");
        text.append("\n").append(describe(perk, Math.max(level, 1))).append("\n");
        if (!single && level > 0 && !maxed) {
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
        if (level > 0) text.append(disabled ? "\n<c>DISABLED <8>(right-click to enable)" : "\n<8>Right-click to disable");
        Material material = disabled ? Material.REDSTONE_BLOCK
                : level == 0 ? (single ? Material.COAL_BLOCK : Material.COAL)
                : maxed ? (single ? Material.DIAMOND_BLOCK : Material.DIAMOND)
                : Material.EMERALD;
        return ItemStacks.item(material, text.toString());
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
