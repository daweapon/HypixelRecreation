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

        layout.slot(4, (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            var profile = GardenService.profile(player);
            List<Text> lore = GardenMenus.lines(KEY + "sunflower.lore.", 1);
            lore.add(Text.key(KEY + "sunflower.level", GardenProgress.level(profile)));
            lore.add(Text.key(KEY + "sunflower.xp", GardenMenus.number((long) profile.getGardenXp())));
            return GardenMenus.item(Material.SUNFLOWER, Text.key(KEY + "sunflower.name"), lore);
        });

        view(layout, 19, Material.GRASS_BLOCK, "plots", 2, () -> new GUIConfigurePlots());
        view(layout, 21, Material.MELON_SLICE, "upgrades", 2, () -> new GUIGardenUpgrades());
        view(layout, 23, Material.EMERALD, "skymart", 6, () -> new GUISkyMart());
        view(layout, 25, Material.GOLD_BLOCK, "milestones", 2, () -> new GUIGardenMilestones());
        view(layout, 31, Material.CLOCK, "speed", 2, () -> new GUICropSpeed());
        layout.slot(29, (_, c) -> levelsItem(c), (_, c) -> GardenMenus.open(c, new GUIGardenLevels()));
        view(layout, 48, Material.CHEST, "logbook", 2, () -> new GUIVisitorLogbook());
        pending(layout, 50, Material.DAYLIGHT_DETECTOR, "time", 1, "Garden Time");
        pending(layout, 51, Material.DIAMOND_HOE, "toolkit", 3, "Farming Toolkit");
    }

    private net.minestom.server.item.ItemStack.Builder levelsItem(ViewContext c) {
        List<Text> lore = GardenMenus.lines(KEY + "levels.lore.", 2);
        lore.add(Text.key(KEY + "levels.current", GardenProgress.level(GardenService.profile((SkyBlockPlayer) c.player()))));
        lore.add(GardenMenus.blank());
        lore.add(Text.key(KEY + "click_to_view"));
        return GardenMenus.item(Material.EXPERIENCE_BOTTLE, Text.key(KEY + "levels.name"), lore);
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
