package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseConfig;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIGreenhouseUpgrades extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Greenhouse Upgrades"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 31, ctx);
        entry(layout, 11, GreenhouseConfig.Upgrade.GROWTH_SPEED, Material.OAK_SAPLING, "Greenhouse Growth Speed");
        entry(layout, 13, GreenhouseConfig.Upgrade.PLANT_YIELD, Material.FLOWER_POT, "Greenhouse Plant Yield");
        entry(layout, 15, GreenhouseConfig.Upgrade.PLOT_LIMIT, Material.GRASS_BLOCK, "Greenhouse Plot Limit");
    }

    private void entry(ViewLayout<DefaultState> layout, int slot, GreenhouseConfig.Upgrade upgrade, Material icon, String noun) {
        layout.slot(slot, (_, c) -> {
            GreenhouseData data = GreenhouseService.data((SkyBlockPlayer) c.player());
            GreenhouseConfig config = GreenhouseConfig.get();
            int tier = GreenhouseService.tier(data, upgrade);
            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<7>Upgrade your {} tier to increase", upgrade.displayName()));
            lore.add(Text.of("<7>your {}.", noun));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Current Tier: <a>{}<7>/<a>{}", tier, config.maxTier(upgrade)));
            switch (upgrade) {
                case GROWTH_SPEED -> lore.add(Text.of("<7>Growth Speed: <a>{}%", trim(config.growthSpeedPercent(tier))));
                case PLANT_YIELD -> lore.add(Text.of("<7>Plant Yield: <a>{}%", trim(config.plantYieldPercent(tier))));
                case PLOT_LIMIT -> lore.add(Text.of("<7>Plot Limit: <a>+{}", tier));
            }
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to view!"));
            return ItemStacks.item(icon, Text.of("<a>" + upgrade.displayName()), lore);
        }, (_, c) -> c.push(new GUIGreenhouseUpgrade(upgrade)));
    }

    static String trim(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    static ItemStack.Builder tierItem(SkyBlockPlayer player, GreenhouseConfig.Upgrade upgrade, int index) {
        GreenhouseConfig config = GreenhouseConfig.get();
        GreenhouseData data = GreenhouseService.data(player);
        int tier = GreenhouseService.tier(data, upgrade);
        GreenhouseConfig.Cost cost = config.costs(upgrade).get(index);
        String roman = StringUtility.getAsRomanNumeral(index + 1);
        Material material = index < tier ? Material.LIME_STAINED_GLASS_PANE : index == tier ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        List<Text> lore = new ArrayList<>();
        switch (upgrade) {
            case GROWTH_SPEED -> lore.add(Text.of("<7>Growth Speed: <a>{}%", trim(config.growthSpeedPercent(index + 1))));
            case PLANT_YIELD -> lore.add(Text.of("<7>Plant Yield: <a>{}%", trim(config.plantYieldPercent(index + 1))));
            case PLOT_LIMIT -> lore.add(Text.of("<7>Plot Limit: <a>+{}", index + 1));
        }
        lore.add(Text.of("<8>+<b>5 SkyBlock XP"));
        lore.add(Text.empty());
        if (index < tier) {
            lore.add(Text.of("<a>Unlocked!"));
        } else {
            lore.add(Text.of("<7>Cost"));
            if (cost.copper() > 0) lore.add(Text.of("<6>{} Copper", StringUtility.commaify(cost.copper())));
            if (cost.bundles() > 0) lore.add(Text.of("<a>Compost Bundle <8>x{}", cost.bundles()));
            if (cost.vines() > 0) lore.add(Text.of("<5>Ethereal Vine <8>x{}", cost.vines()));
            lore.add(Text.empty());
            lore.add(index == tier ? Text.of("<e>Click to upgrade!") : Text.of("<c>You need to unlock the previous tier first!"));
        }
        return ItemStacks.item(material, index + 1, Text.of("<a>" + upgrade.displayName() + " " + roman), lore);
    }
}
