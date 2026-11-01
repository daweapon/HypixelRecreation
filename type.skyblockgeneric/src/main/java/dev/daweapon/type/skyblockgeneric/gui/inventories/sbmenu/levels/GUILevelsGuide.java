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
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.guide.GUIGuideTask;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.rewards.GUILevelRewards;
import dev.daweapon.type.skyblockgeneric.levels.LevelsGuide;
import dev.daweapon.type.skyblockgeneric.levels.guide.GuideEntry;
import dev.daweapon.type.skyblockgeneric.levels.guide.GuideTask;
import dev.daweapon.type.skyblockgeneric.levels.guide.SkyBlockGuide;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUILevelsGuide extends StatelessView {
    private static final int LISTED_ENTRIES = 12;
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
        return SkyBlockGuide.currentTier(player);
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
                if (tier != guide && SkyBlockGuide.unlocked((SkyBlockPlayer) c.player(), tier)) {
                    c.replace(new GUILevelsGuide(tier));
                }
            });
        }

        List<GuideTask> tasks = guide.tasks();
        for (int i = 0; i < TASK_SLOTS.length && i < tasks.size(); i++) {
            GuideTask task = tasks.get(i);
            layout.slot(TASK_SLOTS[i], (s, c) -> taskTile((SkyBlockPlayer) c.player(), task),
                    (click, c) -> c.push(new GUIGuideTask(), new GUIGuideTask.State(guide, task, task.entries(), 0)));
        }
    }

    private ItemStack.Builder tierTab(SkyBlockPlayer player, LevelsGuide tier) {
        String color = TIER_COLORS[tier.ordinal()];
        String name = StringUtility.toNormalCase(tier.name());
        List<Text> lore = new ArrayList<>();
        lore.add(tier.getTitle());
        lore.add(Text.empty());
        if (SkyBlockGuide.unlocked(player, tier)) {
            lore.add(tier.getDescription());
            lore.add(Text.empty());
            double progress = SkyBlockGuide.progress(player, tier);
            lore.add(Text.of("<7>Total Progress: <a>{}%", percent(progress)));
            lore.add(GUILevelRewards.progressBar(progress, 1, SkyBlockGuide.completed(player, tier), SkyBlockGuide.entries(tier).size()));
            lore.add(Text.empty());
            lore.add(tier == guide ? Text.of("<a>SELECTED") : Text.of("<e>Click to view!"));
            Material material = tier == guide ? Material.MAP : tier.getGlassMaterial();
            return ItemStacks.item(material, 1, Text.of(color + "{}", name), lore);
        }
        String previousName = tier.ordinal() == 0 ? name
                : StringUtility.toNormalCase(LevelsGuide.values()[tier.ordinal() - 1].name());
        String previousColor = TIER_COLORS[Math.max(0, tier.ordinal() - 1)];
        lore.add(Text.of("<7>Reach <a>{}% <7>completion of", Math.round(SkyBlockGuide.UNLOCK_THRESHOLD * 100)));
        lore.add(Text.of(previousColor + "{} <7>to unlock this", previousName));
        lore.add(Text.of("<7>stage!"));
        lore.add(Text.empty());
        lore.add(Text.of("<c>LOCKED"));
        return ItemStacks.item(Material.GRAY_DYE, 1, Text.of(color + "{}", name), lore);
    }

    private static ItemStack.Builder taskTile(SkyBlockPlayer player, GuideTask task) {
        long done = task.completed(player);
        double progress = task.progress(player);
        List<Text> lore = new ArrayList<>();

        if (task.entries().size() > 1) {
            lore.add(Text.key("gui_sbmenu.levels.guide.tasks", task.entries().size()));
            lore.add(Text.empty());
        }
        lore.add(Text.of("<7>{}", task.description()));
        if (task.entries().size() > 1) {
            lore.add(Text.empty());
            List<GuideEntry> entries = task.entries();
            for (int i = 0; i < Math.min(LISTED_ENTRIES, entries.size()); i++) {
                lore.add(entryLine(player, entries.get(i)));
            }
            if (entries.size() > LISTED_ENTRIES) lore.add(Text.of("<8>...and {} more", entries.size() - LISTED_ENTRIES));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Total Worth: <b>+{} XP", StringUtility.commaify(task.xp())));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Total Progress: <a>{}%", percent(progress)));
        lore.add(GUILevelRewards.progressBar(progress, 1, done, task.entries().size()));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to view more!"));

        String status = progress >= 1 ? "<a>✔ " : "<c>✖ ";
        return ItemStacks.text(task.icon(), Text.of(status + "<a>{}", task.name()), lore);
    }

    private static Text entryLine(SkyBlockPlayer player, GuideEntry entry) {
        if (entry.complete(player)) return Text.of("<a>✔ <8>{}", entry.label());
        return Text.of(entry.available() ? "<c>✖ <f>{}" : "<c>✖ <8>{}", entry.label());
    }

    private static String percent(double progress) {
        return String.valueOf(Math.round(progress * 100));
    }
}
