package net.swofty.type.skyblockgeneric.gui.inventories.slayer;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.slayer.SlayerDefinition;
import net.swofty.type.skyblockgeneric.slayer.SlayerRegistry;
import net.swofty.type.skyblockgeneric.slayer.SlayerService;
import net.swofty.type.skyblockgeneric.slayer.SlayerTier;
import net.swofty.type.skyblockgeneric.slayer.SlayerTierDefinition;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GUISlayerBoss extends StatelessView {
    private static final int FIRST_TIER_SLOT = 11;

    private final SlayerMenuStyle style;

    public GUISlayerBoss(SlayerMenuStyle style) {
        this.style = style;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(style.type().displayName(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SlayerDefinition definition = SlayerRegistry.get(style.type());
        if (definition == null) return;

        for (SlayerTier tier : SlayerTier.values()) {
            SlayerTierDefinition tierDefinition = definition.tiers().get(tier);
            if (tierDefinition == null) continue;
            layout.slot(FIRST_TIER_SLOT + tier.ordinal(),
                    (s, c) -> tierItem((SkyBlockPlayer) c.player(), definition, tierDefinition),
                    (click, c) -> {
                        SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                        Optional<String> lock = SlayerService.tierLock(player, definition, tierDefinition);
                        if (lock.isPresent()) {
                            player.sendMessage(Text.of("<c>{}", lock.get()));
                            return;
                        }
                        c.push(GUISlayerConfirm.startQuest(style, tierDefinition));
                    });
        }

        layout.slot(29, (s, c) -> levelRewardsItem((SkyBlockPlayer) c.player(), definition),
                (click, c) -> c.push(new GUISlayerLevels(style)));
        if (definition.hasDropTable()) {
            layout.slot(31, ItemStacks.item(Material.GOLD_NUGGET, """
                    <6>Boss Drops
                    <8>{}

                    <7>Usually, the boss will drop
                    <7>its main token which is used
                    <7>for slayer crafts.

                    <7>Rarer drops unlock as your
                    <7>slayer level grows.

                    <e>Click to view drops!""", style.type().displayName()),
                    (click, c) -> c.push(new GUISlayerDrops(style)));
        } else {
            layout.slot(31, ItemStacks.item(Material.GOLD_NUGGET, """
                    <6>Boss Drops
                    <8>{}

                    <7>Usually, the boss will drop
                    <7>its main token which is used
                    <7>for slayer crafts.

                    <c>Drop previews are not available yet!""", style.type().displayName()));
        }
        if (definition.drops().stream().anyMatch(net.swofty.type.skyblockgeneric.slayer.SlayerDrop::hasRngMeter)) {
            layout.slot(35, ItemStacks.item(Material.PAINTING, """
                    <d>RNG Meter
                    <8>{}

                    <7>Fill the meter with Slayer XP
                    <7>from Tier III+ kills to guarantee
                    <7>a drop of your choice.

                    <e>Click to open!""", style.type().displayName()),
                    (click, c) -> c.push(new GUISlayerRngMeter(style)));
        }
        layout.slot(33, ItemStacks.item(Material.BOOK, """
                <a>Slayer Recipes
                <8>{}

                <7>Unlock recipes and collect rare
                <7>drops in order to craft powerful
                <7>items.

                <7>View them in your <a>Recipe Book<7>!""", style.type().displayName()));
    }

    private ItemStack.Builder tierItem(SkyBlockPlayer player, SlayerDefinition definition, SlayerTierDefinition tierDefinition) {
        SlayerTier tier = tierDefinition.tier();
        Text name = Text.of(SlayerMenuStyle.tierColour(tier) + "{}", tierDefinition.displayName(style.type()));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{}", SlayerMenuStyle.tierDifficulty(tier)));
        lore.add(Text.empty());

        Optional<String> lock = SlayerService.tierLock(player, definition, tierDefinition);
        if (lock.isPresent()) {
            lore.add(Text.of("<c>{}", lock.get()));
            return ItemStacks.item(style.material(), tier.number(), name, lore);
        }

        lore.add(Text.of("<7>Health: <c>{:,}❤", (long) tierDefinition.bossHealth()));
        lore.add(Text.of("<7>Damage: <c>{:,} <7>per second", (long) tierDefinition.bossDamage()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Reward: <d>{} {} Slayer XP", SlayerService.slayerXpReward(tierDefinition), style.type().categoryName()));
        lore.add(Text.of("<8>  + Boss drops"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Cost to start: <6>{:,} coins", SlayerService.questCost(player, tierDefinition)));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to slay!"));
        return ItemStacks.item(style.material(), tier.number(), name, lore);
    }

    private ItemStack.Builder levelRewardsItem(SkyBlockPlayer player, SlayerDefinition definition) {
        int xp = player.getSlayerData().progress(style.type()).getXp();
        int level = definition.levelForXp(xp);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{} Slayer LVL", style.type().categoryName()));
        lore.add(Text.empty());
        lore.add(Text.of("<5>1. <7>Kill boss to get XP"));
        lore.add(Text.of("<5>2. <7>Gain LVL from XP"));
        lore.add(Text.of("<5>3. <7>Unlock rewards per LVL"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Current LVL: <e>{}", level));
        definition.levels().stream()
                .filter(reward -> reward.level() == level + 1)
                .findFirst()
                .ifPresent(next -> {
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>{} Slayer XP to LVL {}:", style.type().categoryName(), next.level()));
                    lore.add(Text.of("<d>{:,}<5>/<d>{:,}", xp, next.requiredXp()));
                });
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to view levels!"));
        return ItemStacks.item(Material.GOLD_BLOCK, Text.of("<5>Boss Leveling Rewards"), lore);
    }
}
