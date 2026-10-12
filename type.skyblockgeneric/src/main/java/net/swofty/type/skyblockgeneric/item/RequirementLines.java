package net.swofty.type.skyblockgeneric.item;

import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.slayer.SlayerRequirement;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lore lines such as "Requires Mining Skill 30" are only a warning: once the viewing player meets the
 * requirement the line is dropped. Phrases that cannot be checked are always kept.
 */
public final class RequirementLines {
    private static final Pattern SKILL = Pattern.compile("Requires (\\w+) Skill (\\d+)");
    private static final Pattern HOTM = Pattern.compile("Requires Heart of the Mountain Tier (\\d+)");
    private static final Pattern SLAYER = Pattern.compile("Requires (\\w+) Slayer (\\d+)");

    public record Requirement(Kind kind, String name, int level) {
    }

    public enum Kind {
        SKILL,
        HOTM,
        SLAYER
    }

    private RequirementLines() {
    }

    public static @Nullable Requirement parse(String markupLine) {
        String text = markupLine.replaceAll("§.", "").replaceAll("<[^>]*>", "");
        Matcher hotm = HOTM.matcher(text);
        if (hotm.find()) return new Requirement(Kind.HOTM, "", Integer.parseInt(hotm.group(1)));
        Matcher skill = SKILL.matcher(text);
        if (skill.find()) return new Requirement(Kind.SKILL, skill.group(1), Integer.parseInt(skill.group(2)));
        Matcher slayer = SLAYER.matcher(text);
        if (slayer.find()) return new Requirement(Kind.SLAYER, slayer.group(1), Integer.parseInt(slayer.group(2)));
        return null;
    }

    public static boolean isMet(Requirement requirement, SkyBlockPlayer player) {
        return switch (requirement.kind()) {
            case HOTM -> HotmService.data(player).getTier() >= requirement.level();
            case SLAYER -> SlayerRequirement.isMet(player, requirement.name(), requirement.level());
            case SKILL -> {
                try {
                    yield player.getSkills().getCurrentLevel(SkillCategories.valueOf(requirement.name().toUpperCase()))
                            >= requirement.level();
                } catch (IllegalArgumentException unknownSkill) {
                    yield false;
                }
            }
        };
    }

    public static List<String> withoutMetRequirements(List<String> lines, @Nullable SkyBlockPlayer viewer) {
        if (viewer == null) return lines;
        List<String> kept = new ArrayList<>();
        for (String line : lines) {
            Requirement requirement = parse(line);
            if (requirement != null && isMet(requirement, viewer)) {
                if (!kept.isEmpty() && kept.getLast().isBlank()) kept.removeLast();
                continue;
            }
            kept.add(line);
        }
        return kept;
    }
}
