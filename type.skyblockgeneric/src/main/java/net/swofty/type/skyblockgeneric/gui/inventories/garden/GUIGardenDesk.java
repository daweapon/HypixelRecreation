package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GUIGardenDesk extends StatelessView {
    private static final String KEY = "gui_garden_desk.";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key(KEY + "title"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        layout.slot(4, (_, c) -> sunflower((SkyBlockPlayer) c.player()), (_, c) -> GardenMenus.open(c, new GUIGardenLevels()));

        view(layout, 19, Material.GRASS_BLOCK, "plots", 2, () -> new GUIConfigurePlots());
        view(layout, 21, Material.MELON_SLICE, "upgrades", 2, () -> new GUIGardenUpgrades());
        view(layout, 23, Material.EMERALD, "skymart", 6, () -> new GUISkyMart());
        view(layout, 25, Material.GOLD_BLOCK, "milestones", 2, () -> new GUIGardenMilestones());
        view(layout, 31, Material.CLOCK, "speed", 2, () -> new GUICropSpeed());
        view(layout, 33, Material.PLAYER_HEAD, "chips", 2, () -> new GUIManageChips());
        view(layout, 48, Material.CHEST, "logbook", 2, () -> new GUIVisitorLogbook());
        pending(layout, 50, Material.DAYLIGHT_DETECTOR, "time", 1, "Garden Time");
        layout.slot(51, (_, _) -> GardenMenus.item(Material.DIAMOND_HOE, Text.key(KEY + "toolkit.name"), tooltip("toolkit", 3, false)),
                (_, c) -> openToolkit(c));
    }

    private void openToolkit(ViewContext c) {
        SkyBlockPlayer player = (SkyBlockPlayer) c.player();
        if (!GardenService.profile(player).toolkit().isUnlocked()) {
            player.sendMessage("<c>You haven't unlocked the Farming Toolkit yet!");
            return;
        }
        GardenMenus.open(c, new GUIFarmingToolkit());
    }

    private net.minestom.server.item.ItemStack.Builder sunflower(SkyBlockPlayer player) {
        var profile = GardenService.profile(player);
        var config = net.swofty.type.skyblockgeneric.garden.GardenConfig.get();
        int level = GardenProgress.level(profile);
        List<Text> lore = new java.util.ArrayList<>();
        lore.add(Text.of("<7>Earn Garden experience by accepting"));
        lore.add(Text.of("<7>visitors' offers and unlocking new"));
        lore.add(Text.of("<7>milestones!"));
        lore.add(GardenMenus.blank());
        if (level >= config.maxLevel()) {
            lore.add(Text.of("<a>Max level reached!"));
        } else {
            double base = config.level(level).cumulativeXp();
            double need = config.level(level + 1).cumulativeXp() - base;
            long into = (long) Math.max(0, profile.getGardenXp() - base);
            String roman = net.swofty.commons.StringUtility.getAsRomanNumeral(level + 1);
            lore.add(Text.of("<7>Progress to Level {}: <e>{}", roman, GardenMenus.percent(into, (long) need)));
            int filled = GardenMenus.filled(into, (long) need, GardenMenus.BAR_WIDTH);
            lore.add(Text.of("<dark_green><m>" + GardenMenus.spaces(filled) + "<white><m>" + GardenMenus.spaces(GardenMenus.BAR_WIDTH - filled)
                    + "<reset> <gold>" + GardenMenus.number(into) + "<yellow>/<yellow>" + GardenMenus.number((long) need)));
            lore.add(GardenMenus.blank());
            lore.add(Text.of("<7>Level {} Reward:", roman));
            lore.add(Text.of("<8>+<b>10 SkyBlock XP"));
        }
        lore.add(GardenMenus.blank());
        lore.add(Text.key(KEY + "click_to_view"));
        return GardenMenus.item(Material.SUNFLOWER, Text.of("<a>Garden Level " + net.swofty.commons.StringUtility.getAsRomanNumeral(level)), lore);
    }

    private void view(ViewLayout<DefaultState> layout, int slot, Material material, String id, int loreLines,
                      Supplier<net.swofty.type.generic.gui.v2.View<?>> target) {
        layout.slot(slot, (_, _) -> GardenMenus.item(material, Text.key(KEY + id + ".name"), tooltip(id, loreLines, false)),
                (_, c) -> GardenMenus.open(c, target.get()));
    }

    private void pending(ViewLayout<DefaultState> layout, int slot, Material material, String id, int loreLines, String label) {
        layout.slot(slot, (_, _) -> GardenMenus.item(material, Text.key(KEY + id + ".name"), tooltip(id, loreLines, id.equals("time"))),
                (_, c) -> GardenMenus.notAdded(c, label));
    }

    private List<Text> tooltip(String id, int loreLines, boolean modify) {
        List<Text> lore = new ArrayList<>(GardenMenus.lines(KEY + id + ".lore.", loreLines));
        lore.add(GardenMenus.blank());
        lore.add(Text.key(KEY + (modify ? "click_to_modify" : "click_to_view")));
        return lore;
    }
}
