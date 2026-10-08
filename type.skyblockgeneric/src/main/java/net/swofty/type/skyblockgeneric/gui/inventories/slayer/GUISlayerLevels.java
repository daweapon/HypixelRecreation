package net.swofty.type.skyblockgeneric.gui.inventories.slayer;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.slayer.SlayerDefinition;
import net.swofty.type.skyblockgeneric.slayer.SlayerLevelReward;
import net.swofty.type.skyblockgeneric.slayer.SlayerRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUISlayerLevels extends StatelessView {
    private static final int[] LEVEL_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    private final SlayerMenuStyle style;

    public GUISlayerLevels(SlayerMenuStyle style) {
        this.style = style;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("{} Slayer LVL Rewards", InventoryType.CHEST_4_ROW, style.type().categoryName());
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 31, ctx);

        SlayerDefinition definition = SlayerRegistry.get(style.type());
        if (definition == null) return;

        List<SlayerLevelReward> levels = definition.levels();
        for (int i = 0; i < levels.size() && i < LEVEL_SLOTS.length; i++) {
            SlayerLevelReward reward = levels.get(i);
            layout.slot(LEVEL_SLOTS[i], (s, c) -> levelItem((SkyBlockPlayer) c.player(), definition, reward));
        }
    }

    private static List<Text> rewardLines(SlayerLevelReward reward) {
        List<Text> lines = new ArrayList<>();
        reward.stats().forEach((stat, value) -> lines.add(Text.of(
            stat == ItemStatistic.TRUE_DEFENSE ? " <f>+{} {}" : " <c>+{} {}", value.intValue(), stat.getDisplayName())));
        reward.perks().forEach(perk -> lines.add(Text.of(" <8>+ " + perk)));
        reward.unlocks().forEach(item -> lines.add(Text.of(" <8>+ <a>{} <7>Recipe", item.getDisplayName())));
        reward.catalogued().forEach(entry -> lines.add(Text.of(" <8>+ " + entry + " <8>(not available yet)")));
        if (reward.skyBlockXp() > 0) lines.add(Text.of(" <8>+<b>{} SkyBlock XP", reward.skyBlockXp()));
        return lines;
    }

    private ItemStack.Builder levelItem(SkyBlockPlayer player, SlayerDefinition definition, SlayerLevelReward reward) {
        int xp = player.getSlayerData().progress(style.type()).getXp();
        int level = definition.levelForXp(xp);
        Text name = Text.of("<6>{} Slayer LVL {}", style.type().categoryName(), reward.level());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{}", reward.title()));
        lore.add(Text.empty());

        if (reward.level() > Math.max(3, level + 1)) {
            lore.add(Text.of("<7>Reward is secret!"));
            lore.add(Text.of("<e>Reach LVL {} to reveal!", reward.level() - 1));
            return ItemStacks.item(Material.COAL_BLOCK, name, lore);
        }

        lore.add(Text.of("<7>Progress:"));
        lore.add(Text.of("<d>{:,}<5>/<d>{:,}", Math.min(xp, reward.requiredXp()), reward.requiredXp()));
        lore.add(Text.of("<7>Kill the boss to gain XP."));
        lore.add(Text.of("<7>Higher tiers reward more XP."));
        lore.add(Text.empty());
        List<Text> rewards = rewardLines(reward);
        if (!rewards.isEmpty()) {
            lore.add(Text.of("<7>Rewards:"));
            lore.addAll(rewards);
            lore.add(Text.empty());
        }
        boolean reached = reward.level() <= level;
        lore.add(reached ? Text.of("<a>Level reached!") : Text.of("<c>Level not reached!"));
        return ItemStacks.item(reached ? Material.LIME_STAINED_GLASS_PANE : style.material(), name, lore);
    }
}
