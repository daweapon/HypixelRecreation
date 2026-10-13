package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterConfig;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterEngine;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterService;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterState;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterUpgrade;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIComposterUpgrades extends StatelessView {
    private static final int[] SLOTS = {20, 21, 22, 23, 24};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Composter Upgrades"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        for (ComposterUpgrade upgrade : ComposterUpgrade.values()) {
            layout.slot(SLOTS[upgrade.ordinal()], (_, c) -> item((SkyBlockPlayer) c.player(), upgrade), (_, c) -> buy(c, upgrade));
        }
    }

    private ItemStack.Builder item(SkyBlockPlayer player, ComposterUpgrade upgrade) {
        ComposterConfig config = ComposterConfig.get();
        ComposterState state = ComposterService.state(player);
        int tier = state.tier(upgrade);
        boolean maxed = tier >= ComposterUpgrade.MAX_TIER;
        List<Text> lore = new ArrayList<>();
        for (String line : description(upgrade, state, tier).split("\n")) {
            lore.add(Text.of("<7>" + line));
        }
        lore.add(Text.empty());
        if (maxed) {
            lore.add(Text.of("<7>Max tier reached!"));
        } else {
            ComposterConfig.Tier next = config.tier(upgrade, tier + 1);
            lore.add(Text.of("<7>Next Tier: <a>" + nextValue(upgrade, state, tier)));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Upgrade Cost:"));
            boolean affordable = true;
            for (ComposterConfig.Item cost : ComposterService.required(next)) {
                ItemType type = ItemType.get(cost.item());
                int have = type == null ? 0 : player.getAmountInInventory(type) + player.getSackItems().getAmount(type);
                affordable &= have >= cost.amount();
                lore.add(Text.of(" <a>{} <8>x<a>{}", type == null ? cost.item() : new SkyBlockItem(type).getDisplayName(), StringUtility.commaify(cost.amount())));
            }
            affordable &= Gardens.profile(player).getCopper() >= next.copper();
            lore.add(Text.of(" <6>{} Copper", StringUtility.commaify(next.copper())));
            lore.add(Text.empty());
            lore.add(Text.of("<b>+{} SkyBlock XP", next.skyblockXp()));
            lore.add(Text.empty());
            int required = config.levelRequired(upgrade);
            if (GardenProgress.level(Gardens.profile(player)) < required) {
                lore.add(Text.of("<c>Requires Garden Level " + required + "!"));
            } else {
                lore.add(Text.of(affordable ? "<e>Click to upgrade!" : "<c>Missing required items!"));
            }
        }
        String title = upgrade.getDisplayName() + " " + (maxed ? StringUtility.getAsRomanNumeral(tier) : tier);
        return ItemStacks.item(icon(upgrade), Math.max(1, tier), Text.of("<a>" + title), lore);
    }

    private static Material icon(ComposterUpgrade upgrade) {
        return switch (upgrade) {
            case SPEED -> Material.SUGAR;
            case MULTI_DROP -> Material.GOLDEN_HOE;
            case FUEL_CAP -> Material.GREEN_DYE;
            case ORGANIC_MATTER_CAP -> Material.POTATO;
            case COST_REDUCTION -> Material.GOLD_INGOT;
        };
    }

    private static String description(ComposterUpgrade upgrade, ComposterState state, int tier) {
        ComposterConfig config = ComposterConfig.get();
        return switch (upgrade) {
            case SPEED -> "Increases composter production\nspeed by " + (20 * tier) + "%.";
            case MULTI_DROP -> "When compost is produced, grants a\n" + (3 * tier) + "% chance to produce an extra one.";
            case FUEL_CAP -> "Increase the maximum machine fuel\nthe composter can hold to " + StringUtility.commaify((long) ComposterEngine.fuelCap(state, config)) + ".";
            case ORGANIC_MATTER_CAP -> "Increase the maximum amount of\norganic matter that can be stored to\n" + StringUtility.commaify((long) ComposterEngine.organicMatterCap(state, config)) + ".";
            case COST_REDUCTION -> "Reduces the amount of organic\nmatter and fuel required to produce\ncompost by " + tier + "%.";
        };
    }

    private static String nextValue(ComposterUpgrade upgrade, ComposterState state, int tier) {
        ComposterConfig config = ComposterConfig.get();
        return switch (upgrade) {
            case SPEED -> (20 * (tier + 1)) + "%";
            case MULTI_DROP -> (3 * (tier + 1)) + "%";
            case FUEL_CAP -> StringUtility.commaify((long) (config.baseFuelCap() + 30_000 * (tier + 1)));
            case ORGANIC_MATTER_CAP -> StringUtility.commaify((long) (config.baseOrganicMatterCap() + ComposterEngine.organicMatterBonus(tier + 1)));
            case COST_REDUCTION -> (tier + 1) + "%";
        };
    }

    private void buy(ViewContext ctx, ComposterUpgrade upgrade) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        switch (ComposterService.upgrade(player, upgrade)) {
            case UPGRADED -> ctx.session(DefaultState.class).refresh();
            case MAXED -> player.sendMessage(Text.of("<c>This upgrade is already maxed!"));
            case LEVEL_TOO_LOW -> player.sendMessage(Text.of("<c>You need Garden Level " + ComposterConfig.get().levelRequired(upgrade) + " for this upgrade!"));
            case MISSING_COPPER -> player.sendMessage(Text.of("<c>You don't have enough Copper!"));
            case MISSING_ITEMS -> player.sendMessage(Text.of("<c>Missing required items!"));
        }
    }
}
