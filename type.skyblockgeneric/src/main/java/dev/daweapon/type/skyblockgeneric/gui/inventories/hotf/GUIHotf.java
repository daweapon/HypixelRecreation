package dev.daweapon.type.skyblockgeneric.gui.inventories.hotf;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.hotf.HotfAbilities;
import dev.daweapon.type.skyblockgeneric.hotf.HotfAbility;
import dev.daweapon.type.skyblockgeneric.hotf.HotfPerk;
import dev.daweapon.type.skyblockgeneric.hotf.HotfRegistry;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Locale;

public class GUIHotf extends StatelessView {
    private static final int VISIBLE_ROWS = 5;
    private static final int SCROLL_STEP = 2;
    private static final int CORE_TIER = 5;
    private static final int CORE_COL = 4;
    private static final int SCROLL_UP_SLOT = 8;
    private static final int SCROLL_DOWN_SLOT = 53;
    private final int firstTier;

    public GUIHotf() {
        this(1);
    }

    public GUIHotf(int firstTier) {
        this.firstTier = Math.min(Math.max(firstTier, 1), topTier() - VISIBLE_ROWS + 1);
    }

    private static int topTier() {
        return HotfRegistry.get().maxTier();
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Heart of the Forest", InventoryType.CHEST_6_ROW);
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
        Components.close(layout, 45);
        Components.back(layout, 48, ctx);
        if (!HotfService.isUnlocked(player)) {
            layout.slot(22, ItemStacks.item(Material.BARRIER, lockedTooltip()));
            return;
        }
        int bottom = firstTier;

        for (int tier = bottom; tier < bottom + VISIBLE_ROWS; tier++) {
            int shown = tier;
            layout.slot(slotOf(bottom, tier, 0), (_, _) -> tierLabel(player, shown));
        }
        for (HotfPerk perk : HotfRegistry.get().perks().values()) {
            if (!visible(bottom, perk.tier())) continue;
            layout.slot(slotOf(bottom, perk.tier(), perk.col()), (_, _) -> perkItem(player, perk), (click, c) -> {
                click(player, perk, click.click());
                c.session(Object.class).refresh();
            });
        }
        if (visible(bottom, CORE_TIER)) {
            layout.slot(slotOf(bottom, CORE_TIER, CORE_COL), (_, _) -> coreItem(player), (_, c) -> {
                String error = HotfService.upgradeCore(player);
                player.sendMessage(error != null ? error : "<a>You upgraded the <2>Center of the Forest<a>!");
                c.session(Object.class).refresh();
            });
        }

        if (bottom + VISIBLE_ROWS - 1 < topTier()) {
            layout.slot(SCROLL_UP_SLOT, ItemStacks.item(Material.ARROW, "<a>Scroll Up\n<e>Left-click to scroll up!\n\n<e>Right-click to go to the top tier!"),
                    (click, c) -> c.replace(new GUIHotf(rightClick(click.click()) ? topTier() : bottom + SCROLL_STEP)));
        }
        if (bottom > 1) {
            layout.slot(SCROLL_DOWN_SLOT, ItemStacks.item(Material.ARROW, "<a>Scroll Down\n<e>Left-click to scroll down!\n\n<e>Right-click to go to the bottom tier!"),
                    (click, c) -> c.replace(new GUIHotf(rightClick(click.click()) ? 1 : bottom - SCROLL_STEP)));
        }

        layout.slot(49, (_, _) -> ItemStacks.item(Material.JUNGLE_SAPLING, HotfTooltips.heart(player)));
        layout.slot(52, (_, _) -> ItemStacks.item(Material.CAULDRON, HotfTooltips.reset(player)), (_, c) -> c.push(new GUIHotfReset()));
    }

    private static String lockedTooltip() {
        return "<c>Heart of the Forest Locked\n<7>Talk to <b>Hina<7> in Tangleburg's Path\n<7>on <2>Galatea<7> to unlock your own\n<2>Heart of the Forest<7>.";
    }

    private static boolean rightClick(Click click) {
        return click instanceof Click.Right || click instanceof Click.RightShift;
    }

    private static void click(SkyBlockPlayer player, HotfPerk perk, Click click) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        if (rightClick(click)) {
            if (!perk.isAbility() && HotfService.togglePerk(player, perk.id())) {
                player.sendMessage(HotfService.data(player).getDisabledPerks().contains(perk.id())
                        ? "<c>Disabled <6>" + perk.name() + "<c>." : "<a>Enabled <6>" + perk.name() + "<a>.");
            }
            return;
        }
        if (perk.isAbility() && perk.available() && data.perkLevel(perk.id()) > 0) {
            HotfAbilities.select(player, HotfAbility.valueOf(perk.id().toUpperCase(Locale.ROOT)));
            player.sendMessage("<a>You selected <6>" + perk.name() + "<a>.");
            return;
        }
        int count = click instanceof Click.LeftShift ? 10 : 1;
        HotfService.Purchase purchase = HotfService.buyPerkLevels(player, perk.id(), count);
        if (purchase.levels() > 0) {
            player.sendMessage("<a>You upgraded <6>" + perk.name() + "<a>!");
        } else if (purchase.error() != null) {
            player.sendMessage(purchase.error());
        }
    }

    private static ItemStack.Builder tierLabel(SkyBlockPlayer player, int tier) {
        boolean reached = HotfService.tier(player) >= tier;
        return ItemStacks.item(reached ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, HotfTooltips.tier(player, tier)).amount(tier);
    }

    private static ItemStack.Builder coreItem(SkyBlockPlayer player) {
        boolean owned = HotfService.data(player).getCoreLevel() > 0;
        return ItemStacks.item(owned ? Material.DARK_OAK_LOG : Material.WHITE_DYE, HotfTooltips.core(player));
    }

    private static ItemStack.Builder perkItem(SkyBlockPlayer player, HotfPerk perk) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        int level = data.perkLevel(perk.id());
        Material material;
        if (level == 0) {
            material = Material.WHITE_DYE;
        } else if (perk.isAbility()) {
            material = HotfAbility.valueOf(perk.id().toUpperCase(Locale.ROOT)).getIcon();
        } else if (data.getDisabledPerks().contains(perk.id())) {
            material = Material.REDSTONE_BLOCK;
        } else {
            material = Material.OAK_LOG;
        }
        return ItemStacks.item(material, HotfTooltips.perk(player, perk));
    }
}
