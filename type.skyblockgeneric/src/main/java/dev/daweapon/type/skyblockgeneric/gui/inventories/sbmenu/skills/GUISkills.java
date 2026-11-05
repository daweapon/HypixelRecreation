package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.skills;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUISkills extends StatelessView {
    public static final int DUNGEONEERING_SLOT = 34;
    private static final java.util.Map<SkillCategories, Integer> DISPLAY_SLOTS = java.util.Map.ofEntries(
            java.util.Map.entry(SkillCategories.COMBAT, 19),
            java.util.Map.entry(SkillCategories.FARMING, 20),
            java.util.Map.entry(SkillCategories.FISHING, 21),
            java.util.Map.entry(SkillCategories.MINING, 22),
            java.util.Map.entry(SkillCategories.FORAGING, 23),
            java.util.Map.entry(SkillCategories.ENCHANTING, 24),
            java.util.Map.entry(SkillCategories.ALCHEMY, 25),
            java.util.Map.entry(SkillCategories.CARPENTRY, 28),
            java.util.Map.entry(SkillCategories.RUNECRAFTING, 29),
            java.util.Map.entry(SkillCategories.TAMING, 30),
            java.util.Map.entry(SkillCategories.SOCIAL, 32),
            java.util.Map.entry(SkillCategories.HUNTING, 33)
    );

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.skills.main.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> ItemStacks.item(Material.DIAMOND_SWORD, 1,
                Text.key("gui_sbmenu.skills.main.info"),
                Text.keyLines("gui_sbmenu.skills.main.info.lore")));

        layout.slot(DUNGEONEERING_SLOT, GUIDungeoneering.skillsMenuIcon(), (click, c) -> c.push(new GUIDungeoneering()));

        SkillCategories[] allCategories = SkillCategories.values();
        for (SkillCategories category : allCategories) {
            Integer displaySlot = DISPLAY_SLOTS.get(category);
            if (displaySlot == null) continue;
            SkillCategory skillCategory = category.asCategory();
            int slot = displaySlot;

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                List<Text> lore = new ArrayList<>();

                if (category == SkillCategories.CARPENTRY && !player.getMissionData().hasCompleted("give_wool_to_carpenter")) {
                    lore.addAll(Text.keyLines("gui_sbmenu.skills.main.carpentry_locked.lore"));
                } else {
                    List<String> textLore = new ArrayList<>(skillCategory.getDescription());
                    textLore.add(" ");

                    Integer nextLevel = player.getSkills().getNextLevel(category);

                    if (nextLevel != null) {
                        SkillCategory.SkillReward reward = skillCategory.getRewards()[nextLevel - 1];
                        player.getSkills().getDisplay(textLore, category, reward.requirement(),
                                Text.of("<7>Progress to Level {:roman}: ", nextLevel).serialize());
                        textLore.add(" ");
                        reward.getDisplay(textLore);
                    }

                    textLore.forEach(entry -> lore.add(Text.parse(entry)));

                    if (nextLevel == null) {
                        lore.add(Text.key("gui_sbmenu.skills.main.max_level"));
                    }

                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_sbmenu.skills.main.click_to_view"));
                }

                return ItemStacks.item(skillCategory.getDisplayIcon(), 1,
                        Text.of("<a>{} {:roman}", skillCategory.getName(),
                                player.getSkills().getCurrentLevel(category)),
                        lore);
            }, (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (category == SkillCategories.CARPENTRY && !player.getMissionData().hasCompleted("give_wool_to_carpenter")) return;
                c.push(new GUISkillCategory(category, 0));
            });
        }
    }
}
