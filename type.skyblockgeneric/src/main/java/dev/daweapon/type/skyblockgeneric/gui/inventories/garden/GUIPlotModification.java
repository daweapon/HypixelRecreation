package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import dev.daweapon.type.skyblockgeneric.garden.farm.GardenPresetCost;
import dev.daweapon.type.skyblockgeneric.garden.farm.GardenPresets;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.garden.GardenService;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GUIPlotModification extends StatelessView {
    private final int plot;

    public GUIPlotModification(int plot) {
        this.plot = plot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key("gui_garden_upgrades.modify.title", plot), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        for (GardenGrids.Preset preset : GardenGrids.PRESETS) {
            layout.slot(preset.slot(),
                    (_, c) -> presetItem((SkyBlockPlayer) c.player(), preset, GardenService.profile((SkyBlockPlayer) c.player())),
                    (_, c) -> select(preset, c));
        }

        layout.slot(47, (_, c) -> convertItem((SkyBlockPlayer) c.player()), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            GardenProfile profile = Gardens.profile(player);
            if (GreenhouseService.isGreenhouse(profile, plot)) return;
            if (!GreenhouseService.canConvert(profile)) {
                player.sendMessage(Text.of(convertReason(profile)));
                return;
            }
            c.push(new GUIGreenhouseConvert(plot));
        });

        layout.slot(50, GardenMenus.item(Material.COMMAND_BLOCK, Text.key("gui_garden_upgrades.modify.save.name"), List.of(
                Text.key("gui_garden_upgrades.modify.save.lore.1"),
                Text.key("gui_garden_upgrades.modify.save.lore.2"),
                Text.key("gui_garden_upgrades.modify.save.lore.3"),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.modify.save.click"))),
                (_, c) -> c.player().sendMessage(Text.key("gui_garden_upgrades.modify.save.unavailable")));

        layout.slot(51, GardenMenus.item(Material.OAK_SIGN, Text.key("gui_garden_upgrades.modify.name.name"), List.of(
                Text.key("gui_garden_upgrades.modify.name.lore"),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.modify.name.current", plot),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.modify.name.click"))),
                (_, c) -> c.player().sendMessage(Text.key("gui_garden_upgrades.modify.name.unavailable")));
    }

    private ItemStack.Builder convertItem(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        boolean converted = GreenhouseService.isGreenhouse(profile, plot);
        return GardenMenus.item(Material.WHITE_CONCRETE, Text.key("gui_garden_upgrades.modify.greenhouse.name"), List.of(
                Text.key("gui_garden_upgrades.modify.greenhouse.lore.1"),
                Text.key("gui_garden_upgrades.modify.greenhouse.lore.2"),
                Text.key("gui_garden_upgrades.modify.greenhouse.lore.3"),
                GardenMenus.blank(),
                converted ? Text.of("<a>This plot is a Greenhouse!")
                        : GreenhouseService.canConvert(profile) ? Text.of("<e>Click to convert!") : Text.of(convertReason(profile))));
    }

    private static String convertReason(GardenProfile profile) {
        return profile.greenhouse().getBeds().size() >= profile.greenhouse().greenhouseLimit() && profile.greenhouse().isBlueprintDelivered()
                ? "<c>Already at max Greenhouse plots!" : "<c>Finish Sam's Greenhouse quest to unlock this!";
    }

    private ItemStack.Builder presetItem(SkyBlockPlayer player, GardenGrids.Preset preset, GardenProfile profile) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.key("gui_garden_upgrades.modify.preset.lore.1"));
        lore.add(Text.key("gui_garden_upgrades.modify.preset.lore.2"));
        lore.add(GardenMenus.blank());
        if (preset.name().equals(profile.plotPreset(plot, GardenGrids.DEFAULT_PRESET))) {
            lore.add(Text.key("gui_garden_upgrades.modify.preset.selected"));
            lore.add(GardenMenus.blank());
        }
        GardenCrop crop = GardenPresets.cropOf(preset.name());
        if (crop != null) {
            lore.add(Text.of("<7>Cost"));
            lore.add(Text.of("<8> - <a>{}x <7>{}", NumberFormat.getIntegerInstance(Locale.US).format(GardenPresetCost.required(crop)),
                    new SkyBlockItem(GardenPresetCost.seed(crop)).getDisplayNameText()));
            long have = GardenPresets.available(player, crop);
            lore.add(Text.of(have >= GardenPresetCost.required(crop) ? "<8> - <7>You have <a>{} <7>(counting compacted items)"
                    : "<8> - <7>You have <c>{} <7>(counting compacted items)",
                    NumberFormat.getIntegerInstance(Locale.US).format(have)));
            lore.add(GardenMenus.blank());
        }
        lore.add(GardenPresets.isPastable(preset.name()) ? Text.key("gui_garden_upgrades.modify.preset.click")
                : Text.key("gui_garden_upgrades.modify.preset.unavailable"));
        return GardenMenus.item(preset.icon(), Text.key("gui_garden_upgrades.modify.preset.name", preset.name()), lore);
    }

    private void select(GardenGrids.Preset preset, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GardenProfile profile = GardenService.profile(player);
        if (!profile.isPlotUnlocked(plot)) {
            return;
        }
        if (!GardenPresets.isPastable(preset.name())) {
            player.sendMessage(Text.key("gui_garden_upgrades.modify.preset.unavailable"));
            return;
        }
        player.closeInventory();
        GardenPresets.paste(player, plot, preset.name());
    }
}
