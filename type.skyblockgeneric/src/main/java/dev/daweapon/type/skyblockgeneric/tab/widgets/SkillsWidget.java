package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkills;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetToggle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SkillsWidget implements TabWidget {
    private static final List<SkillCategories> LISTED = Arrays.stream(SkillCategories.values())
            .filter(category -> !category.isCosmetic())
            .toList();
    private static final List<SkillCategories> AVERAGED = LISTED.stream()
            .filter(category -> category != SkillCategories.RUNECRAFTING && category != SkillCategories.CARPENTRY)
            .toList();

    @Override
    public String id() {
        return "skills";
    }

    @Override
    public String displayName() {
        return "Skills";
    }

    @Override
    public Material icon() {
        return Material.DIAMOND_SWORD;
    }

    @Override
    public int defaultPriority() {
        return 30;
    }

    @Override
    public List<TabWidgetToggle> toggles() {
        return LISTED.stream()
                .map(category -> new TabWidgetToggle(toggleId(category), category.toString(), true))
                .toList();
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        DatapointSkills.PlayerSkills skills = player.getSkills();
        double average = AVERAGED.stream().mapToInt(skills::getCurrentLevel).average().orElse(0);
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<a><l>Skills: <e>{}", StringUtility.decimalify(average, 1)));
        for (SkillCategories category : LISTED) {
            if (!context.shows(this, toggleId(category))) continue;
            if (category == SkillCategories.CARPENTRY && !player.getMissionData().hasCompleted("give_wool_to_carpenter")) continue;
            lines.add(Text.of(" <7>{} {}: ", category.toString(), skills.getCurrentLevel(category))
                    .append(Text.of(progress(skills, category))));
        }
        return lines;
    }

    private String progress(DatapointSkills.PlayerSkills skills, SkillCategories category) {
        return skills.getNextLevel(category) == null ? "<c><l>MAX" : "<a>" + skills.getPercentage(category) + "%";
    }

    private String toggleId(SkillCategories category) {
        return category.name().toLowerCase();
    }
}
