package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.GardenConfig;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.GardenProgress;
import dev.daweapon.type.skyblockgeneric.garden.GardenService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIConfigurePlots extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key("gui_garden_upgrades.plots.title"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        layout.slot(GardenGrids.BARN_SLOT, GardenMenus.item(Material.BARREL, Text.key("gui_garden_upgrades.plots.barn.name"), List.of(
                Text.key("gui_garden_upgrades.plots.barn.lore"),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.plots.unlocked.teleport"))),
                (_, c) -> dev.daweapon.type.skyblockgeneric.garden.GardenTeleport.toPlot((SkyBlockPlayer) c.player(), dev.daweapon.type.skyblockgeneric.garden.GardenLayout.BARN));

        for (int plot = 1; plot <= GardenProfile.PLOT_COUNT; plot++) {
            int number = plot;
            layout.slot(GardenGrids.slotOfPlot(plot),
                    (_, c) -> plotItem(number, (SkyBlockPlayer) c.player()),
                    (click, c) -> click(number, click, c));
        }
    }

    private ItemStack.Builder plotItem(int plot, SkyBlockPlayer player) {
        GardenProfile profile = GardenService.profile(player);
        List<Text> lore = new ArrayList<>();
        if (profile.isPlotUnlocked(plot)) {
            String preset = profile.plotPreset(plot, GardenGrids.DEFAULT_PRESET);
            boolean custom = GardenGrids.hasPreset(preset);
            if (custom) {
                lore.add(Text.key("gui_garden_upgrades.plots.unlocked.preset", preset));
            }
            lore.add(GardenMenus.blank());
            lore.add(Text.key("gui_garden_upgrades.plots.unlocked.modify"));
            lore.add(Text.key("gui_garden_upgrades.plots.unlocked.teleport"));
            Material icon = custom ? GardenGrids.presetIcon(preset) : Material.LIME_STAINED_GLASS_PANE;
            return GardenMenus.item(icon, Text.key("gui_garden_upgrades.plots.unlocked.name", plot), lore);
        }

        GardenProgress.PlotQuote quote = GardenProgress.quotePlot(profile, plot);
        int level = GardenProgress.level(profile);
        lore.add(Text.key("gui_garden_upgrades.plots.locked.requirement"));
        lore.add(Text.key(level >= quote.requiredLevel()
                ? "gui_garden_upgrades.tiers.requirement_met"
                : "gui_garden_upgrades.tiers.requirement_unmet", quote.requiredLevel()));
        lore.add(Text.key("gui_garden_upgrades.plots.locked.cost"));
        int bundles = GardenGrids.bundleCount(quote.compostCost(), GardenConfig.get().compostPerBundle());
        lore.add(bundles > 0
                ? Text.key("gui_garden_upgrades.plots.locked.compost_bundle", bundles)
                : Text.key("gui_garden_upgrades.plots.locked.compost", quote.compostCost()));
        lore.add(GardenMenus.blank());
        if (quote.status() == GardenProgress.PlotStatus.NOT_ADJACENT) {
            lore.add(Text.key("gui_garden_upgrades.plots.locked.need_adjacent"));
        } else if (level < quote.requiredLevel()) {
            lore.add(Text.key("gui_garden_upgrades.plots.locked.need_level"));
        } else if (!GardenService.canAffordCompost(player, quote.compostCost())) {
            lore.add(Text.key("gui_garden_upgrades.plots.locked.need_compost"));
        } else {
            lore.add(Text.key("gui_garden_upgrades.plots.locked.click"));
        }
        boolean purchasable = quote.status() != GardenProgress.PlotStatus.NOT_ADJACENT && level >= quote.requiredLevel();
        return GardenMenus.item(purchasable ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                Text.key("gui_garden_upgrades.plots.locked.name", plot), lore);
    }

    private void click(int plot, ClickContext<DefaultState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (GardenService.profile(player).isPlotUnlocked(plot)) {
            if (click.click() instanceof Click.Right) {
                dev.daweapon.type.skyblockgeneric.garden.GardenTeleport.toPlot(player, plot);
            } else {
                GardenMenus.open(ctx, new GUIPlotModification(plot));
            }
            return;
        }
        if (click.click() instanceof Click.Right) {
            return;
        }
        GardenProgress.PlotQuote result = GardenService.unlockPlot(player, plot);
        switch (result.status()) {
            case UNLOCKABLE -> player.sendMessage(Text.key("gui_garden_upgrades.plots.unlock.success", plot));
            case NOT_ADJACENT -> player.sendMessage(Text.key("gui_garden_upgrades.plots.unlock.adjacent"));
            case LEVEL_TOO_LOW -> player.sendMessage(Text.key("gui_garden_upgrades.plots.unlock.level", result.requiredLevel()));
            case ALREADY_UNLOCKED -> player.sendMessage(Text.key("gui_garden_upgrades.plots.unlock.already"));
            case INVALID -> player.sendMessage(Text.key(result.compostCost() > 0
                    ? "gui_garden_upgrades.plots.unlock.compost"
                    : "gui_garden_upgrades.plots.unlock.invalid"));
        }
        ctx.session(DefaultState.class).refresh();
    }
}
