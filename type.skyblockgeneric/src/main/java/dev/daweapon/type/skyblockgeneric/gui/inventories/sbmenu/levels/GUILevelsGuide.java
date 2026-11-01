package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels;

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
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.rewards.GUILevelRewards;
import dev.daweapon.type.skyblockgeneric.levels.LevelsGuide;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUILevelsGuide extends StatelessView {
    private static final double UNLOCK_THRESHOLD = 0.5;
    private static final String[] TIER_COLORS = {"<a>", "<b>", "<dark_aqua>", "<9>", "<5>", "<6>", "<d>"};
    private static final int[] TIER_SLOTS = {1, 2, 3, 4, 5, 6, 7};

    private static final int[] TASK_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final LevelsGuide guide;

    public GUILevelsGuide(LevelsGuide guide) {
        this.guide = guide;
    }

    public static LevelsGuide currentTier(SkyBlockPlayer player) {
        LevelsGuide current = LevelsGuide.STARTER;
        for (LevelsGuide candidate : LevelsGuide.values()) {
            if (candidate.getTasksSets().isEmpty() || !isUnlocked(player, candidate)) break;
            current = candidate;
            if (progress(player, candidate) < 1) break;
        }
        return current;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withText(
            (state, ctx) -> Text.key("gui_sbmenu.levels.guide.title", StringUtility.toNormalCase(guide.name())),
                InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(50, (s, c) -> ItemStacks.item(Material.REDSTONE_TORCH, 1, Text.key("gui_sbmenu.levels.guide.info"),
                Text.keyLines("gui_sbmenu.levels.guide.info.lore")));

        LevelsGuide[] tiers = LevelsGuide.values();
        for (int i = 0; i < tiers.length && i < TIER_SLOTS.length; i++) {
            LevelsGuide tier = tiers[i];
            layout.slot(TIER_SLOTS[i], (s, c) -> tierTab((SkyBlockPlayer) c.player(), tier), (click, c) -> {
                if (tier != guide && !tier.getTasksSets().isEmpty() && isUnlocked((SkyBlockPlayer) c.player(), tier)) {
                    c.replace(new GUILevelsGuide(tier));
                }
            });
        }

        LevelsGuide.TasksSet[] tasks = guide.getTasksSets().toArray(new LevelsGuide.TasksSet[0]);
        for (int i = 0; i < TASK_SLOTS.length && i < tasks.length; i++) {
            LevelsGuide.TasksSet task = tasks[i];

            layout.slot(TASK_SLOTS[i], (s, c) -> taskTile((SkyBlockPlayer) c.player(), task),
                    (click, c) -> c.push(task.getGuiToOpen()));
        }
    }

    private ItemStack.Builder tierTab(SkyBlockPlayer player, LevelsGuide tier) {
        String color = TIER_COLORS[tier.ordinal()];
        String name = StringUtility.toNormalCase(tier.name());
        List<Text> lore = new ArrayList<>();
        lore.add(tier.getTitle());
        lore.add(Text.empty());
        if (isUnlocked(player, tier) && !tier.getTasksSets().isEmpty()) {
            lore.add(tier.getDescription());
            lore.add(Text.empty());
            double progress = progress(player, tier);
            lore.add(Text.of("<7>Total Progress: <a>{}%", percent(progress)));
            lore.add(GUILevelRewards.progressBar(progress, 1, causesDone(player, tier), causes(tier).size()));
            lore.add(Text.empty());
            lore.add(tier == guide ? Text.of("<a>SELECTED") : Text.of("<e>Click to view!"));
            Material material = tier == guide ? Material.MAP : tier.getGlassMaterial();
            return ItemStacks.item(material, 1, Text.of(color + "{}", name), lore);
        }
        String previousName = tier.ordinal() == 0 ? name
                : StringUtility.toNormalCase(LevelsGuide.values()[tier.ordinal() - 1].name());
        String previousColor = TIER_COLORS[Math.max(0, tier.ordinal() - 1)];
        lore.add(Text.of("<7>Reach <a>{}% <7>completion of", Math.round(UNLOCK_THRESHOLD * 100)));
        lore.add(Text.of(previousColor + "{} <7>to unlock this", previousName));
        lore.add(Text.of("<7>stage!"));
        lore.add(Text.empty());
        lore.add(Text.of("<c>LOCKED"));
        return ItemStacks.item(Material.GRAY_DYE, 1, Text.of(color + "{}", name), lore);
    }

    private static ItemStack.Builder taskTile(SkyBlockPlayer player, LevelsGuide.TasksSet task) {
        List<SkyBlockLevelCauseAbstr> causes = new ArrayList<>(task.getCauses().keySet());
        long done = causes.stream().filter(cause -> player.getSkyBlockExperience().hasExperienceFor(cause)).count();
        double worth = causes.stream().mapToDouble(SkyBlockLevelCauseAbstr::xpReward).sum();
        double progress = causes.isEmpty() ? 0 : done / (double) causes.size();
        List<Text> lore = new ArrayList<>();

        if (causes.size() > 1) {
            lore.add(Text.key("gui_sbmenu.levels.guide.tasks", causes.size()));
            lore.add(Text.empty());
        }
        task.getDisplay().apply(player).forEach(line -> lore.add(Text.parse(line)));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Total Worth: <b>+{} XP", StringUtility.commaify(Math.round(worth))));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Total Progress: <a>{}%", percent(progress)));
        lore.add(GUILevelRewards.progressBar(progress, 1, done, causes.size()));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to view more!"));

        String status = progress >= 1 ? "<a>✔ " : "<c>✖ ";
        Text name = Text.of(status + "<a>{}", ItemStacks.nameText(task.getMaterial()));
        return ItemStacks.text(ItemStacks.copy(task.getMaterial().build()), name, lore);
    }

    private static long causesDone(SkyBlockPlayer player, LevelsGuide tier) {
        return causes(tier).stream().filter(cause -> player.getSkyBlockExperience().hasExperienceFor(cause)).count();
    }

    private static String percent(double progress) {
        return String.valueOf(Math.round(progress * 100));
    }

    private static double progress(SkyBlockPlayer player, LevelsGuide tier) {
        List<SkyBlockLevelCauseAbstr> causes = causes(tier);
        if (causes.isEmpty()) return 0;
        long done = causes.stream().filter(cause -> player.getSkyBlockExperience().hasExperienceFor(cause)).count();
        return done / (double) causes.size();
    }

    private static List<SkyBlockLevelCauseAbstr> causes(LevelsGuide tier) {
        List<SkyBlockLevelCauseAbstr> causes = new ArrayList<>();
        tier.getTasksSets().forEach(set -> causes.addAll(set.getCauses().keySet()));
        return causes;
    }

    private static boolean isUnlocked(SkyBlockPlayer player, LevelsGuide tier) {
        if (tier.ordinal() == 0) return true;
        LevelsGuide previous = LevelsGuide.values()[tier.ordinal() - 1];
        return !previous.getTasksSets().isEmpty() && progress(player, previous) >= UNLOCK_THRESHOLD;
    }
}
