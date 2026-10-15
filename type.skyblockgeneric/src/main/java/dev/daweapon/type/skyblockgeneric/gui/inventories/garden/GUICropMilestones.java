package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
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
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class GUICropMilestones extends StatelessView {
    private static final String KEY = "gui_garden_desk.crops.";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key(KEY + "title"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        for (int i = 0; i < GardenGrids.CROP_ORDER.size(); i++) {
            GardenCrop crop = GardenGrids.CROP_ORDER.get(i);
            layout.slot(GardenGrids.CROP_SLOTS.get(i), (_, c) -> cropItem(crop, GardenService.profile((SkyBlockPlayer) c.player())));
        }
    }

    private ItemStack.Builder cropItem(GardenCrop crop, GardenProfile profile) {
        GardenConfig config = GardenConfig.get();
        Material icon = GardenMenus.icon(crop);
        String name = crop.getDisplayName();
        if (!GardenProgress.cropUnlocked(profile, crop)) {
            return GardenMenus.item(icon, Text.key(KEY + "name_locked", name), List.of(
                    Text.key(KEY + "locked", GardenMenus.unlockLevel(crop))));
        }

        int tier = profile.milestoneTier(crop);
        long count = profile.cropCount(crop);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.key(KEY + "harvest", name));
        lore.add(Text.key(KEY + "harvest.2", name));
        lore.add(GardenMenus.blank());

        if (tier >= config.maxMilestoneTier()) {
            long beyond = Math.max(0, count - config.cumulativeThreshold(crop, config.maxMilestoneTier() - 1));
            lore.add(Text.key(KEY + "total", GardenMenus.number(count)));
            lore.add(GardenMenus.blank());
            lore.add(Text.key(KEY + "max"));
            lore.add(Text.key(KEY + "max_bar", GardenMenus.spaces(GardenMenus.BAR_WIDTH), GardenMenus.number(beyond)));
            lore.add(GardenMenus.blank());
            lore.add(Text.key(KEY + "max_note"));
            return GardenMenus.item(icon, Text.key(KEY + "name_tier", name, StringUtility.getAsRomanNumeral(tier)), lore);
        }

        int next = tier + 1;
        long base = tier == 0 ? 0 : config.cumulativeThreshold(crop, tier);
        long into = Math.max(0, count - base);
        long target = config.tierThreshold(crop, next);
        int filled = GardenMenus.filled(into, target, GardenMenus.BAR_WIDTH);
        GardenConfig.MilestoneTier reward = config.milestoneTiers().get(tier);

        lore.add(Text.key(KEY + "progress", StringUtility.getAsRomanNumeral(next), GardenMenus.percent(into, target)));
        lore.add(Text.key(KEY + "bar", GardenMenus.spaces(filled), GardenMenus.spaces(GardenMenus.BAR_WIDTH - filled),
                GardenMenus.number(Math.min(into, target)), GardenMenus.number(target)));
        lore.add(GardenMenus.blank());
        lore.add(Text.key(KEY + "rewards"));
        lore.add(Text.key(KEY + "reward.garden_xp", GardenMenus.number(reward.gardenXp())));
        lore.add(Text.key(KEY + "reward.farming_xp", GardenMenus.number(reward.farmingXp())));
        lore.add(Text.key(KEY + "reward.skyblock_xp", GardenMenus.number(reward.skyblockXp())));

        Text title = tier == 0
                ? Text.key(KEY + "name", name)
                : Text.key(KEY + "name_tier", name, StringUtility.getAsRomanNumeral(tier));
        return GardenMenus.item(icon, title, lore);
    }
}
