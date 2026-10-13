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
import net.swofty.type.skyblockgeneric.garden.GardenConfig;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICropTierUpgrades extends StatelessView {
    private static final int CROP_SLOT = 4;
    private static final int FIRST_TIER_SLOT = 18;

    private final GardenCrop crop;

    public GUICropTierUpgrades(GardenCrop crop) {
        this.crop = crop;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key("gui_garden_upgrades.tiers.title", crop.getDisplayName()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        layout.slot(CROP_SLOT, (_, c) -> cropItem(GardenService.profile((SkyBlockPlayer) c.player())));

        for (GardenConfig.UpgradeTier tier : GardenConfig.get().upgradeTiers()) {
            layout.slot(FIRST_TIER_SLOT + tier.tier() - 1,
                    (_, c) -> tierItem(tier, GardenService.profile((SkyBlockPlayer) c.player())),
                    (_, c) -> click(tier, c));
        }
    }

    private ItemStack.Builder cropItem(GardenProfile profile) {
        String name = crop.getDisplayName();
        return GardenMenus.item(GardenMenus.icon(crop), Text.key("gui_garden_upgrades.tiers.crop.name", name), List.of(
                Text.key("gui_garden_upgrades.tiers.crop.lore.1", name, profile.cropUpgradeTier(crop), GardenConfig.get().maxUpgradeTier()),
                Text.key("gui_garden_upgrades.tiers.crop.lore.2", name, profile.cropUpgradeTier(crop), GardenConfig.get().maxUpgradeTier(),
                        GardenProgress.cropFortune(profile, crop))));
    }

    private ItemStack.Builder tierItem(GardenConfig.UpgradeTier tier, GardenProfile profile) {
        GardenConfig config = GardenConfig.get();
        String name = crop.getDisplayName();
        String roman = StringUtility.getAsRomanNumeral(tier.tier());
        int current = profile.cropUpgradeTier(crop);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.key("gui_garden_upgrades.tiers.fortune", name, config.fortunePerUpgradeTier()));
        lore.add(Text.key("gui_garden_upgrades.tiers.skyblock_xp", 1));
        lore.add(GardenMenus.blank());

        if (tier.tier() <= current) {
            lore.add(Text.key("gui_garden_upgrades.tiers.unlocked"));
            return stack(Material.LIME_STAINED_GLASS_PANE, tier, Text.key("gui_garden_upgrades.tiers.name", name, roman), lore);
        }

        int level = GardenProgress.level(profile);
        lore.add(Text.key("gui_garden_upgrades.tiers.requirement_header"));
        lore.add(Text.key(level >= tier.gardenLevel()
                ? "gui_garden_upgrades.tiers.requirement_met"
                : "gui_garden_upgrades.tiers.requirement_unmet", tier.gardenLevel()));
        lore.add(Text.key("gui_garden_upgrades.tiers.cost_header"));
        lore.add(Text.key("gui_garden_upgrades.tiers.cost", GardenMenus.number(tier.copper())));
        lore.add(GardenMenus.blank());

        if (tier.tier() != current + 1) {
            lore.add(Text.key("gui_garden_upgrades.tiers.need_previous"));
            return stack(Material.RED_STAINED_GLASS_PANE, tier, Text.key("gui_garden_upgrades.tiers.name_locked", name, roman), lore);
        }
        if (level < tier.gardenLevel()) {
            lore.add(Text.key("gui_garden_upgrades.tiers.need_level"));
        } else if (profile.getCopper() < tier.copper()) {
            lore.add(Text.key("gui_garden_upgrades.tiers.need_copper"));
        } else {
            lore.add(Text.key("gui_garden_upgrades.tiers.click_to_upgrade"));
        }
        return stack(Material.YELLOW_STAINED_GLASS_PANE, tier, Text.key("gui_garden_upgrades.tiers.name_next", name, roman), lore);
    }

    private ItemStack.Builder stack(Material material, GardenConfig.UpgradeTier tier, Text name, List<Text> lore) {
        return ItemStacks.item(material, tier.tier(), name, lore);
    }

    private void click(GardenConfig.UpgradeTier tier, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int current = GardenService.profile(player).cropUpgradeTier(crop);
        if (tier.tier() <= current) {
            return;
        }
        if (tier.tier() != current + 1) {
            player.sendMessage(Text.key("gui_garden_upgrades.tiers.result.previous"));
            return;
        }
        GardenProgress.UpgradeResult result = GardenService.upgradeCrop(player, crop);
        switch (result) {
            case UPGRADED -> player.sendMessage(Text.key("gui_garden_upgrades.tiers.result.upgraded",
                    crop.getDisplayName(), StringUtility.getAsRomanNumeral(tier.tier())));
            case MAX_TIER -> player.sendMessage(Text.key("gui_garden_upgrades.tiers.result.max"));
            case LEVEL_TOO_LOW -> player.sendMessage(Text.key("gui_garden_upgrades.tiers.result.level"));
            case NOT_ENOUGH_COPPER -> player.sendMessage(Text.key("gui_garden_upgrades.tiers.result.copper"));
        }
        ctx.session(DefaultState.class).refresh();
    }
}
