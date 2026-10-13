package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
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
import net.swofty.type.skyblockgeneric.garden.analyzer.AnalyzerService;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIDnaMilestones extends StatelessView {
    private static final int[] SLOTS = {19, 20, 21, 23, 24, 25};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Mutation Analysis Rewards"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 31, ctx);
        for (int tier = 0; tier < AnalyzerService.MILESTONE_TARGETS.length; tier++) {
            int index = tier;
            layout.slot(SLOTS[tier], (_, c) -> item((SkyBlockPlayer) c.player(), index), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (!AnalyzerService.claimMilestone(player)) player.sendMessage(Text.of("<c>Nothing to claim yet!"));
                c.session(DefaultState.class).refresh();
            });
        }
    }

    private net.minestom.server.item.ItemStack.Builder item(SkyBlockPlayer player, int tier) {
        GreenhouseData data = GreenhouseService.data(player);
        int target = AnalyzerService.MILESTONE_TARGETS[tier];
        int progress = AnalyzerService.milestoneProgress(data);
        boolean claimed = data.getDnaClaimed() > tier;
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Progress: <a>{}<7>/<a>{}", Math.min(progress, target), target));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards:"));
        lore.add(Text.of("<8>+<a>{} Farming Fortune", AnalyzerService.FORTUNE_PER_MILESTONE));
        lore.add(Text.of("<8>+<b>{} SkyBlock XP", AnalyzerService.SKYBLOCK_XP_PER_MILESTONE));
        for (String id : AnalyzerService.milestoneItems(tier)) {
            ItemType type = ItemType.get(id);
            lore.add(Text.of("<8>• <f>{}", type == null ? StringUtility.toNormalCase(id) + " <8>(not added yet)" : new SkyBlockItem(type).getDisplayName()));
        }
        lore.add(Text.empty());
        boolean claimable = data.getDnaClaimed() == tier && progress >= target;
        lore.add(claimed ? Text.of("<a>Claimed!") : claimable ? Text.of("<e>Click to claim!") : Text.of("<c>Analyze more mutations!"));
        Material material = claimed ? Material.LIME_STAINED_GLASS_PANE : claimable ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        return ItemStacks.item(material, tier + 1, Text.of("<a>DNA Analysis Milestone " + StringUtility.getAsRomanNumeral(tier + 1)), lore);
    }
}
