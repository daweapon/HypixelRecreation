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
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import net.swofty.type.skyblockgeneric.slayer.SlayerDefinition;
import net.swofty.type.skyblockgeneric.slayer.SlayerRegistry;
import net.swofty.type.skyblockgeneric.slayer.SlayerService;
import net.swofty.type.skyblockgeneric.slayer.SlayerTier;
import net.swofty.type.skyblockgeneric.slayer.SlayerTierDefinition;
import net.swofty.type.skyblockgeneric.slayer.SlayerType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GUISlayer extends StatelessView {
    private static final int[] BOSS_SLOTS = {10, 11, 12, 13, 14, 15};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Slayer", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);

        SlayerMenuStyle[] styles = SlayerMenuStyle.values();
        for (int i = 0; i < styles.length; i++) {
            SlayerMenuStyle style = styles[i];
            layout.slot(BOSS_SLOTS[i], (s, c) -> bossItem((SkyBlockPlayer) c.player(), style), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                Optional<String> lock = bossLock(player, style);
                if (lock.isPresent()) {
                    player.sendMessage(Text.of("<c>{}", lock.get()));
                    return;
                }
                c.push(new GUISlayerBoss(style));
            });
        }

        layout.slot(16, ItemStacks.item(Material.COAL_BLOCK, """
                <c>Not released yet!
                <7>This boss is still in
                <7>development!"""));

        layout.slot(28, ItemStacks.item(Material.GRAY_DYE, """
                <b>Auto-Slayer
                <7>Upon defeating a boss,
                <a>automatically <7>completes the
                <7>quest and starts another of the
                <7>same type if you have enough
                <6>coins <7>in your purse or bank.

                <c>Not available yet!"""));

        layout.slot(29, (s, c) -> leaderboardItem());
        layout.slot(30, (s, c) -> activeQuestItem((SkyBlockPlayer) c.player()), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (!SlayerService.status(player).active()) return;
            c.push(GUISlayerConfirm.cancelQuest());
        });
        layout.slot(32, (s, c) -> combatWisdomItem((SkyBlockPlayer) c.player()));
        layout.slot(33, (s, c) -> bonusRewardsItem((SkyBlockPlayer) c.player()));
        layout.slot(35, ItemStacks.item(Material.PAINTING, """
                <d>RNG Meter

                <7>Your <d>RNG Meter <7>fills with
                <d>Slayer XP <7>every time you
                <7>defeat a <a>Tier III <7>or higher!

                <7>When your meter is full, your
                <7>selected drop will be guaranteed
                <7>to drop the next time you defeat
                <7>the boss!"""));
    }

    static Optional<String> bossLock(SkyBlockPlayer player, SlayerMenuStyle style) {
        if (style.riftOnly()) {
            return Optional.of("Only inside The Rift!");
        }
        SlayerDefinition definition = SlayerRegistry.get(style.type());
        if (definition == null || !definition.enabled()) {
            return Optional.of("This boss is not available yet!");
        }
        if (!SlayerService.isBossUnlocked(player, definition)) {
            return definition.unlockRequirement()
                    .map(requirement -> "Slay " + requirement.type().displayName() + " " + requirement.tier().numeral() + "!");
        }
        return Optional.empty();
    }

    private ItemStack.Builder bossItem(SkyBlockPlayer player, SlayerMenuStyle style) {
        Optional<String> lock = bossLock(player, style);
        List<Text> lore = new ArrayList<>(Text.of(style.description()).lines());
        lore.add(Text.empty());
        String colour = lock.isPresent() ? "<c>" : "<e>";
        lore.add(lock.map(reason -> Text.of("<c>{}", reason)).orElse(Text.of("<e>Click to view boss!")));
        return ItemStacks.item(style.material(), Text.of("<c>☠ " + colour + "{}", style.type().displayName()), lore);
    }

    private ItemStack.Builder leaderboardItem() {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>Ultimate bragging rights"));
        lore.add(Text.empty());
        for (SlayerMenuStyle style : SlayerMenuStyle.values()) {
            lore.add(Text.of("<7>{}: <8>N/A", style.type().displayName()));
        }
        return ItemStacks.item(Material.EXPERIENCE_BOTTLE, Text.of("<b>Slayer Leaderboards"), lore);
    }

    private ItemStack.Builder activeQuestItem(SkyBlockPlayer player) {
        SlayerService.QuestStatus status = SlayerService.status(player);
        if (!status.active() || status.tier() == null) {
            return Components.asFiller(Material.BLACK_STAINED_GLASS_PANE);
        }
        SlayerTierDefinition tier = status.tier();
        SlayerType type = status.quest().type();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Boss: " + SlayerMenuStyle.tierColour(tier.tier()) + "{}", tier.displayName(type)));
        if (status.quest().bossSpawned()) {
            lore.add(Text.of("<7>Status: <c>Slay the boss!"));
        } else {
            lore.add(Text.of("<7>Progress: <e>{:,}<7>/<c>{:,} Combat XP", status.quest().combatXp(), tier.requiredCombatXp()));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to cancel quest!"));
        return ItemStacks.item(SlayerMenuStyle.of(type).material(), Text.of("<5>Ongoing Slayer Quest"), lore);
    }

    private ItemStack.Builder combatWisdomItem(SkyBlockPlayer player) {
        DatapointSlayer.SlayerData data = player.getSlayerData();
        List<Text> tiers = new ArrayList<>();
        int total = 0;
        for (SlayerMenuStyle style : SlayerMenuStyle.values()) {
            Optional<SlayerTier> highest = highestSlain(data.progress(style.type()));
            total += highest.map(tier -> tier.number() >= 4 ? 2 : 1).orElse(0);
            tiers.add(highest
                    .map(tier -> Text.of("<7>{}: " + SlayerMenuStyle.tierColour(tier) + "Tier {}", style.type().displayName(), tier.numeral()))
                    .orElse(Text.of("<7>{}: <8>No kills", style.type().displayName())));
        }

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>Slayer Bonus"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Total Buff: <3>+{} ☯ Combat Wisdom", total));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Earn extra Combat EXP based on"));
        lore.add(Text.of("<7>your unique slayer boss kills."));
        lore.add(Text.empty());
        lore.add(Text.of("<8>Highest slain tiers"));
        lore.addAll(tiers);
        lore.add(Text.empty());
        lore.add(Text.of("<7>Tier I, II, III grant <3>+1 ☯ Combat Wisdom<7>."));
        lore.add(Text.of("<7>Tier IV grants <3>+2 ☯ Combat Wisdom<7>."));
        return ItemStacks.item(Material.WHEAT, Text.of("<a>Global Combat Wisdom <7>Buff"), lore);
    }

    private ItemStack.Builder bonusRewardsItem(SkyBlockPlayer player) {
        boolean levelSix = SlayerService.firstThreeBossesAtLevel(player, 6);
        boolean levelSeven = SlayerService.firstThreeBossesAtLevel(player, 7);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Unlock bonuses by reaching a"));
        lore.add(Text.of("<7>LVL on the first 3 bosses."));
        lore.add(Text.empty());
        lore.add(Text.of((levelSix ? "<a>✔" : "<c>✖") + " LVL 6"));
        lore.add(Text.of("<7>Earn <a>+3 <7>of any boss's main"));
        lore.add(Text.of("<7>token drop when slaying its"));
        lore.add(Text.of("<7>mini-bosses."));
        lore.add(Text.empty());
        lore.add(Text.of((levelSeven ? "<a>✔" : "<c>✖") + " LVL 7"));
        lore.add(Text.of("<7>Slayers are <6>4% cheaper<7>."));
        return ItemStacks.item(Material.POWERED_RAIL, Text.of("<a>Slayer Bonus Rewards"), lore);
    }

    static Optional<SlayerTier> highestSlain(DatapointSlayer.SlayerProgress progress) {
        SlayerTier[] tiers = SlayerTier.values();
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (progress.completions(tiers[i]) > 0) return Optional.of(tiers[i]);
        }
        return Optional.empty();
    }
}
