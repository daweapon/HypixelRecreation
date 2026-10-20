package dev.daweapon.type.skyblockgeneric.chapters;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record ChapterReward(long hotf, Map<SkillCategories, Long> skills, long forestEssence, int skyBlockXp,
                            Map<String, Integer> shards, Map<ItemType, Integer> items, long whispers) {

    public List<String> lines() {
        List<String> lines = new ArrayList<>();
        if (hotf > 0) lines.add("<8>+<a>" + format(hotf) + " HOTF EXP");
        skills.forEach((skill, amount) -> lines.add("<8>+<3>" + format(amount) + " <7>" + skillName(skill) + " Experience"));
        if (forestEssence > 0) lines.add("<8>+<a>" + format(forestEssence) + " Forest Essence");
        if (skyBlockXp > 0) lines.add("<8>+<b>" + skyBlockXp + " SkyBlock XP");
        shards.forEach((name, amount) -> lines.add("<8>+<a>" + amount + " <f>" + name + " Shard"));
        items.forEach((item, amount) -> lines.add("<8>+<a>" + amount + "x <color:" + item.rarity.getColor() + ">" + item.getDisplayName()));
        if (whispers > 0) lines.add("<8>+<3>" + format(whispers) + " <7>Forest Whispers");
        return lines;
    }

    private static String skillName(SkillCategories skill) {
        String name = skill.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private static String format(long value) {
        return String.format("%,d", value);
    }
}
