package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.GardenConfig;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.GardenProgress;
import dev.daweapon.type.skyblockgeneric.garden.GardenService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIGardenLevels extends StatelessView {
    private static final String KEY = "gui_garden_desk.levels_menu.";
    private static final int[] LEVEL_SLOTS = {9, 18, 27, 28, 29, 20, 11, 2, 3, 4, 13, 22, 31, 32, 33};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key(KEY + "title"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        if (!Components.back(layout, 48, ctx)) {
            Components.close(layout, 48);
        }
        Components.close(layout, 49);

        layout.slot(0, (_, c) -> summary(GardenService.profile((SkyBlockPlayer) c.player())));

        int count = Math.min(LEVEL_SLOTS.length, GardenConfig.get().maxLevel());
        for (int i = 0; i < count; i++) {
            int level = i + 1;
            layout.slot(LEVEL_SLOTS[i], (_, c) -> levelItem(level, GardenService.profile((SkyBlockPlayer) c.player())));
        }
    }

    private ItemStack.Builder summary(GardenProfile profile) {
        GardenConfig config = GardenConfig.get();
        int level = GardenProgress.level(profile);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.key(KEY + "summary.xp", GardenMenus.number((long) profile.getGardenXp())));
        if (level >= config.maxLevel()) {
            lore.add(Text.key(KEY + "summary.max"));
        } else {
            double base = config.level(level).cumulativeXp();
            double next = config.level(level + 1).cumulativeXp();
            long into = (long) Math.max(0, profile.getGardenXp() - base);
            lore.add(Text.key(KEY + "summary.next", StringUtility.getAsRomanNumeral(level + 1), GardenMenus.percent(into, (long) (next - base))));
        }
        return GardenMenus.item(Material.SUNFLOWER, Text.key(KEY + "summary.name", StringUtility.getAsRomanNumeral(level)), lore);
    }

    private ItemStack.Builder levelItem(int level, GardenProfile profile) {
        GardenConfig.Level entry = GardenConfig.get().level(level);
        boolean unlocked = GardenProgress.level(profile) >= level;
        String roman = StringUtility.getAsRomanNumeral(level);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.key(KEY + "rewards"));
        lore.add(Text.key(KEY + "reward.visitors", entry.newVisitors()));
        for (GardenCrop crop : entry.crops()) {
            lore.add(Text.key(KEY + "reward.crop", crop == GardenCrop.MELON ? "Melon Slice" : crop.getDisplayName()));
        }
        for (String pest : entry.pests()) {
            lore.add(Text.key(KEY + "reward.pest", pest));
        }
        if (entry.cropUpgradeTier() > 0) {
            lore.add(Text.key(KEY + "reward.upgrades", StringUtility.getAsRomanNumeral(entry.cropUpgradeTier())));
        }
        if (!entry.barnSkin().isEmpty()) {
            lore.add(Text.key(KEY + "reward.skin." + skinColor(entry.barnSkin()), entry.barnSkin()));
        }
        for (String extra : entry.extras()) {
            lore.add(Text.key(KEY + "reward.extra", extra));
        }
        if (entry.skyblockXp() > 0) {
            lore.add(Text.key(KEY + "reward.skyblock_xp", entry.skyblockXp()));
        }
        if (entry.cropGrowth() > 0) {
            lore.add(Text.key(KEY + "reward.crop_growth", entry.cropGrowth()));
        }
        lore.add(GardenMenus.blank());
        lore.add(unlocked
                ? Text.key(KEY + "unlocked")
                : Text.key(KEY + "progress", GardenMenus.number((long) profile.getGardenXp()), GardenMenus.number((long) entry.cumulativeXp())));

        Text name = Text.key(KEY + (unlocked ? "level_name" : "level_name_locked"), roman);
        return stack(level, unlocked, name, lore);
    }

    private ItemStack.Builder stack(int level, boolean unlocked, Text name, List<Text> lore) {
        Material material = switch (level) {
            case 5 -> Material.STONE_HOE;
            case 10 -> Material.IRON_HOE;
            case 15 -> Material.DIAMOND_HOE;
            default -> unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        };
        return ItemStacks.item(material, level, name, lore);
    }

    private String skinColor(String skin) {
        if (skin.startsWith("Mansion")) {
            return "purple";
        }
        return skin.startsWith("Cabin") ? "blue" : "green";
    }
}
