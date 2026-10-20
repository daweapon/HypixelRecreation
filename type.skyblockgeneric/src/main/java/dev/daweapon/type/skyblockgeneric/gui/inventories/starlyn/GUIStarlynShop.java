package dev.daweapon.type.skyblockgeneric.gui.inventories.starlyn;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.CapTier;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.Fortune;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.Item;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.LevelCap;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.PersonalBest;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.Power;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.PowerTier;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.ShopEntry;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynCost;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynProfile;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynShop;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GUIStarlynShop extends StatelessView {
    private static final int FORAGING_BASE_CAP = 50;
    private final StarlynSister sister;

    public GUIStarlynShop(StarlynSister sister) {
        this.sister = sister;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(sister.shopName(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        List<ShopEntry> entries = StarlynConfig.get(sister).shop();
        for (int index = 0; index < entries.size() && index < StarlynGui.GRID.length; index++) {
            ShopEntry entry = entries.get(index);
            layout.slot(StarlynGui.GRID[index], (_, c) -> render((SkyBlockPlayer) c.player(), entry), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                String error = StarlynShop.purchase(player, sister, entry.id());
                if (error != null) player.sendMessage(error);
                c.session(DefaultState.class).refresh();
            });
        }
    }

    private ItemStack.Builder render(SkyBlockPlayer player, ShopEntry entry) {
        return switch (entry) {
            case LevelCap cap -> cap(player, cap);
            case Power power -> power(player, power);
            case Fortune fortune -> fortune(player, fortune);
            case PersonalBest best -> personalBest(player, best);
            case Item item -> item(player, item);
        };
    }

    private String state(SkyBlockPlayer player, int requiredMilestone, StarlynCost cost, String action) {
        int milestone = StarlynContests.data(player, sister).getMilestoneGranted();
        if (milestone < requiredMilestone) return "<c>Requires " + sister.milestoneName() + " " + StarlynShop.roman(requiredMilestone) + "!";
        return StarlynShop.affordable(player, sister, cost) ? "<e>Click to " + action + "!" : "<c>You can't afford this!";
    }

    private ItemStack.Builder cap(SkyBlockPlayer player, LevelCap entry) {
        CapTier tier = StarlynShop.nextCap(player, sister, entry);
        int owned = 0;
        for (CustomCollectionAward award : new CustomCollectionAward[]{CustomCollectionAward.FIG_FORAGING_LEVEL_CAP,
                CustomCollectionAward.MANGROVE_FORAGING_LEVEL_CAP, CustomCollectionAward.HELIX_FORAGING_LEVEL_CAP,
                CustomCollectionAward.AGATHA_MILESTONE_4_FORAGING_LEVEL_CAP, CustomCollectionAward.AGATHA_MILESTONE_5_FORAGING_LEVEL_CAP,
                CustomCollectionAward.MIRIA_MILESTONE_4_FORAGING_LEVEL_CAP, CustomCollectionAward.MIRIA_MILESTONE_5_FORAGING_LEVEL_CAP}) {
            if (player.hasCustomCollectionAward(award)) owned++;
        }
        List<String> lines = new ArrayList<>(List.of("<e>+1 Foraging Level Cap", "<7>Raise your Foraging Skill level cap", "<7>by one.", "",
                "<7>Your cap: <e>Foraging " + StarlynShop.roman(FORAGING_BASE_CAP + owned), ""));
        if (tier == null) {
            lines.add("<a>Maxed out!");
        } else {
            lines.addAll(StarlynGui.costLines(sister, tier.cost(), false));
            lines.add("");
            lines.add(state(player, tier.milestone(), tier.cost(), "unlock"));
        }
        return ItemStacks.item(entry.icon(), String.join("\n", lines));
    }

    private ItemStack.Builder power(SkyBlockPlayer player, Power entry) {
        PowerTier tier = StarlynShop.nextPower(player, sister, entry);
        int owned = StarlynContests.data(player, sister).purchased(entry.id());
        List<String> lines = new ArrayList<>();
        if (tier == null) {
            lines.add("<a>" + entry.name() + " " + StarlynShop.roman(owned));
            lines.add("<7>Grants <2>+" + (int) StarlynShop.sweepFromPower(player, sister, entry) + "∮ Sweep<7>.");
            lines.add("");
            lines.add("<a>Maxed out!");
        } else {
            StarlynCost cost = StarlynCost.coupons(tier.coupons());
            lines.add("<a>" + entry.name() + " " + StarlynShop.roman(owned + 1));
            lines.add("<7>Grants <2>+" + (int) tier.sweep() + "∮ Sweep<7>.");
            lines.add("");
            lines.addAll(StarlynGui.costLines(sister, cost, false));
            lines.add("");
            lines.add(state(player, tier.milestone(), cost, "unlock"));
        }
        return ItemStacks.item(entry.icon(), String.join("\n", lines));
    }

    private ItemStack.Builder fortune(SkyBlockPlayer player, Fortune entry) {
        int owned = StarlynContests.data(player, sister).purchased(entry.id());
        List<String> lines = new ArrayList<>();
        if (owned >= entry.maxLevel()) {
            lines.add("<a>" + entry.name() + " " + StarlynShop.roman(owned));
            lines.add("<7>Grants <6>+" + entry.fortune(owned) + " ☘ " + entry.name() + "<7>.");
            lines.add("");
            lines.add("<a>Maxed out!");
        } else {
            StarlynCost cost = entry.cost(owned + 1);
            lines.add("<a>" + entry.name() + " " + StarlynShop.roman(owned + 1));
            lines.add("<7>Grants <6>+" + entry.perLevel() + " ☘ " + entry.name() + "<7>.");
            lines.add("");
            lines.add("<7>Current: <6>+" + entry.fortune(owned) + " ☘ " + entry.name());
            lines.add("");
            lines.addAll(StarlynGui.costLines(sister, cost, true));
            lines.add("");
            lines.add(state(player, entry.milestone(), cost, "unlock"));
        }
        return ItemStacks.item(new dev.daweapon.type.skyblockgeneric.item.SkyBlockItem(entry.icon()).getMaterial(), String.join("\n", lines));
    }

    private ItemStack.Builder personalBest(SkyBlockPlayer player, PersonalBest entry) {
        StarlynProfile.SisterData data = StarlynContests.data(player, sister);
        String tree = entry.tree().getDisplayName();
        List<String> lines = new ArrayList<>(List.of("<a>" + entry.name() + " I", "<7>You gain <2>0.01% ∮ Sweep <7>for every",
                "<7>100 Extra " + tree + " Collection during her", "<7>contest. <8>(Max 100k collection).", ""));
        if (data.purchased(entry.id()) > 0) {
            lines.add("<a>Unlocked!");
        } else {
            StarlynCost cost = StarlynCost.coupons(entry.coupons());
            lines.addAll(StarlynGui.costLines(sister, cost, false));
            lines.add("");
            lines.add(state(player, entry.milestone(), cost, "unlock"));
        }
        return ItemStacks.item(entry.icon(), String.join("\n", lines));
    }

    private ItemStack.Builder item(SkyBlockPlayer player, Item entry) {
        ItemStack.Builder builder = PlayerItemUpdater.playerUpdate(player, StarlynShop.create(entry.give(), 1).getItemStack());
        List<String> lines = new ArrayList<>(List.of(""));
        lines.addAll(StarlynGui.costLines(sister, entry.cost(), false));
        lines.add("");
        lines.add(state(player, entry.milestone(), entry.cost(), "trade"));
        return ItemStacks.appendLore(builder, String.join("\n", lines));
    }
}
