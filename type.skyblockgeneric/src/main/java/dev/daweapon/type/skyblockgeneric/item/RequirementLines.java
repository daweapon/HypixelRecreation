package dev.daweapon.type.skyblockgeneric.item;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.garden.GardenProgress;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
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
    private static final Pattern GARDEN = Pattern.compile("Requires Garden Level (\\d+)");
    private static final Pattern SLAYER = Pattern.compile("Requires (\\w+) Slayer (\\d+)");
    private static final Pattern CATACOMBS_FLOOR = Pattern.compile("Requires The Catacombs (Entrance|Floor [IVX]+) Completion");

    public record Requirement(Kind kind, String name, int level) {
    }

    public enum Kind {
        SKILL,
        HOTM,
        SLAYER,
        GARDEN,
        CATACOMBS,
        CATACOMBS_FLOOR
    }

    private RequirementLines() {
    }

    public static @Nullable Requirement parse(String markupLine) {
        String text = markupLine.replaceAll("§.", "").replaceAll("<[^>]*>", "");
        Matcher hotm = HOTM.matcher(text);
        if (hotm.find()) return new Requirement(Kind.HOTM, "", Integer.parseInt(hotm.group(1)));
        Matcher garden = GARDEN.matcher(text);
        if (garden.find()) return new Requirement(Kind.GARDEN, "", Integer.parseInt(garden.group(1)));
        Matcher floor = CATACOMBS_FLOOR.matcher(text);
        if (floor.find()) return new Requirement(Kind.CATACOMBS_FLOOR, floor.group(1), floorNumber(floor.group(1)));
        Matcher skill = SKILL.matcher(text);
        if (skill.find()) {
            int level = Integer.parseInt(skill.group(2));
            return skill.group(1).equalsIgnoreCase("Catacombs")
                    ? new Requirement(Kind.CATACOMBS, "", level)
                    : new Requirement(Kind.SKILL, skill.group(1), level);
        }
        Matcher slayer = SLAYER.matcher(text);
        if (slayer.find()) return new Requirement(Kind.SLAYER, slayer.group(1), Integer.parseInt(slayer.group(2)));
        return null;
    }

    public static boolean isMet(Requirement requirement, SkyBlockPlayer player) {
        return switch (requirement.kind()) {
            case HOTM -> HotmService.data(player).getTier() >= requirement.level();
            case GARDEN -> GardenProgress.level(Gardens.profile(player)) >= requirement.level();
            case SLAYER -> SlayerRequirement.isMet(player, requirement.name(), requirement.level());
            case CATACOMBS -> CatacombsProgress.level(player) >= requirement.level();
            case CATACOMBS_FLOOR -> CatacombsProgress.hasCompleted(player, CatacombsFloor.byNumber(requirement.level()));
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

    private static int floorNumber(String floorName) {
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            if (floor.displayName().equals(floorName)) return floor.ordinal();
        }
        return 0;
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
