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
import net.swofty.type.skyblockgeneric.hotm.HotmAbilities;
import net.swofty.type.skyblockgeneric.hotm.HotmAbility;
import net.swofty.type.skyblockgeneric.hotm.HotmPerk;
import net.swofty.type.skyblockgeneric.hotm.HotmRegistry;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

public class GUIHotm extends StatelessView {
    private static final int VISIBLE_ROWS = 5;
    private static final int TOP_TIER = 10;
    private static final int SCROLL_STEP = 4;
    private static final int CORE_TIER = 5;
    private static final int CORE_COL = 4;
    private static final int SCROLL_UP_SLOT = 8;
    private static final int SCROLL_DOWN_SLOT = 53;
    private final int firstTier;

    public GUIHotm() {
        this(1);
    }

    public GUIHotm(int firstTier) {
        this.firstTier = Math.min(Math.max(firstTier, 1), TOP_TIER - VISIBLE_ROWS + 1);
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Heart of the Mountain", InventoryType.CHEST_6_ROW);
    }

    private static int slotOf(int bottom, int tier, int col) {
        return (bottom + VISIBLE_ROWS - 1 - tier) * 9 + col;
    }

    private static boolean visible(int bottom, int tier) {
        return tier >= bottom && tier < bottom + VISIBLE_ROWS;
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int bottom = firstTier;

        for (int tier = bottom; tier < bottom + VISIBLE_ROWS; tier++) {
            int shown = tier;
            layout.slot(slotOf(bottom, tier, 0), (_, _) -> tierLabel(player, shown));
        }
        for (HotmPerk perk : HotmRegistry.get().perks().values()) {
            if (!visible(bottom, perk.tier())) continue;
            layout.slot(slotOf(bottom, perk.tier(), perk.col()), (_, _) -> perkItem(player, perk), (click, c) -> {
                click(player, perk, click.click());
                c.session(Object.class).refresh();
            });
        }
        if (visible(bottom, CORE_TIER)) {
            layout.slot(slotOf(bottom, CORE_TIER, CORE_COL), (_, _) -> ItemStacks.item(Material.REDSTONE_BLOCK, HotmTooltips.core(player)), (_, c) -> {
                String error = HotmService.upgradeCore(player);
                player.sendMessage(error != null ? error : "<a>You upgraded the <5>Core of the Mountain<a>!");
                c.session(Object.class).refresh();
            });
        }

        if (bottom + VISIBLE_ROWS - 1 < TOP_TIER) {
            layout.slot(SCROLL_UP_SLOT, ItemStacks.item(Material.ARROW, "<a>Scroll Up\n<e>Left-click to scroll up!\n\n<e>Right-click to go to the top tier!"),
                    (click, c) -> c.replace(new GUIHotm(rightClick(click.click()) ? TOP_TIER : bottom + SCROLL_STEP)));
        }
        if (bottom > 1) {
            layout.slot(SCROLL_DOWN_SLOT, ItemStacks.item(Material.ARROW, "<a>Scroll Down\n<e>Left-click to scroll down!\n\n<e>Right-click to go to the bottom tier!"),
                    (click, c) -> c.replace(new GUIHotm(rightClick(click.click()) ? 1 : bottom - SCROLL_STEP)));
        }

        Components.close(layout, 45);
        layout.slot(47, ItemStacks.item(Material.CHEST, """
                <a>Heart of the Mountain Slot
                <7>Quickly swap between saved trees.

                <7>Current: <a>Heart of the Mountain 1
                <c>Swapping trees has a 10m cooldown!

                <8>Saved trees are not available yet."""));
        Components.back(layout, 48, ctx);
        layout.slot(49, (_, _) -> ItemStacks.item(Material.ENDER_EYE, HotmTooltips.heart(player)));
        layout.slot(50, ItemStacks.item(Material.ORANGE_DYE, """
                <d>Crystal Hollows Crystals
                <7>Crystals are used to forge Gems into <d>Perfect<7> Gems. They can be found hidden within the <5>Crystal Hollows<7>.

                <8>The Crystal Hollows is not available yet."""));
        layout.slot(51, ItemStacks.item(Material.HEART_OF_THE_SEA, """
                <d>Crystal Nucleus RNG Meter
                <7>Your <d>Crystal Nucleus RNG Meter<7> fills with <9>1,000 Nucleus XP<7> every time you complete the <5>Crystal Nucleus<7>!

                <8>The Crystal Hollows is not available yet."""));
        layout.slot(52, (_, _) -> ItemStacks.item(Material.CAULDRON, HotmTooltips.reset(player)), (_, c) -> c.push(new GUIHotmReset()));
    }

    private static boolean rightClick(Click click) {
        return click instanceof Click.Right || click instanceof Click.RightShift;
    }

    private static void click(SkyBlockPlayer player, HotmPerk perk, Click click) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        if (rightClick(click)) {
            if (!perk.isAbility() && HotmService.togglePerk(player, perk.id())) {
                player.sendMessage(HotmService.data(player).getDisabledPerks().contains(perk.id())
                        ? "<c>Disabled <6>" + perk.name() + "<c>." : "<a>Enabled <6>" + perk.name() + "<a>.");
            }
            return;
        }
        if (perk.isAbility() && data.perkLevel(perk.id()) > 0) {
            HotmAbilities.select(player, HotmAbility.valueOf(perk.id().toUpperCase(Locale.ROOT)));
            player.sendMessage("<a>You selected <6>" + perk.name() + "<a>.");
            return;
        }
        int count = click instanceof Click.LeftShift ? 10 : 1;
        HotmService.Purchase purchase = HotmService.buyPerkLevels(player, perk.id(), count);
        if (purchase.levels() > 0) {
            player.sendMessage("<a>You upgraded <6>" + perk.name() + "<a>!");
        } else if (purchase.error() != null) {
            player.sendMessage(purchase.error());
        }
    }

    private static ItemStack.Builder tierLabel(SkyBlockPlayer player, int tier) {
        boolean reached = HotmService.data(player).getTier() >= tier;
        return ItemStacks.item(reached ? Material.GREEN_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, HotmTooltips.tier(player, tier)).amount(tier);
    }

    private static ItemStack.Builder perkItem(SkyBlockPlayer player, HotmPerk perk) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        int level = data.perkLevel(perk.id());
        boolean maxed = level >= perk.maxLevel();
        boolean single = perk.maxLevel() == 1;
        Material material;
        if (perk.isAbility()) {
            boolean selected = level > 0 && HotmAbilities.selected(player) == HotmAbility.valueOf(perk.id().toUpperCase(Locale.ROOT));
            material = level == 0 ? Material.COAL_BLOCK : selected ? Material.DIAMOND_BLOCK : Material.REDSTONE_BLOCK;
        } else if (level > 0 && data.getDisabledPerks().contains(perk.id())) {
            material = Material.REDSTONE_BLOCK;
        } else {
            material = level == 0 ? Material.COAL : maxed ? Material.DIAMOND : Material.EMERALD;
        }
        if (single && !perk.isAbility() && level == 0) material = Material.COAL;
        return ItemStacks.item(material, HotmTooltips.perk(player, perk));
    }
}
