package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsAvailability;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.Predicate;

@Getter
public class ItemRequirementsComponent extends SkyBlockItemComponent {
    private final List<Requirement> requirements;

    public ItemRequirementsComponent(List<Requirement> requirements) {
        this.requirements = List.copyOf(requirements);
    }

    public boolean canUse(SkyBlockPlayer player) {
        return requirements.stream().allMatch(requirement -> requirement.isMet(player));
    }

    public boolean ensureCanUse(SkyBlockPlayer player) {
        Requirement unmet = requirements.stream()
                .filter(requirement -> !requirement.isMet(player))
                .findFirst()
                .orElse(null);
        if (unmet == null) return true;
        player.sendMessage("<c>You do not meet this item's {} requirement!", unmet.display());
        return false;
    }

    public record Requirement(Type type, String category, int level, String description) {
        public boolean isMet(SkyBlockPlayer player) {
            if (type == Type.SLAYER) return SlayerRequirement.isMet(player, category, level);
            if (type == Type.DUNGEON_SKILL) {
                return !CatacombsAvailability.isPublic() || isDungeonSkillMet(CatacombsProgress.level(player));
            }
            if (type == Type.DUNGEON_TIER) {
                return isDungeonTierMet(floor -> !isEnforceable(floor) || CatacombsProgress.hasCompleted(player, floor));
            }
            if (type != Type.SKILL) return true;
            try {
                SkillCategories skill = SkillCategories.valueOf(category);
                return player.getSkills().getCurrentLevel(skill) >= level;
            } catch (IllegalArgumentException ignored) {
                return true;
            }
        }

        public boolean isDungeonSkillMet(int catacombsLevel) {
            return catacombsLevel >= level;
        }

        private static boolean isEnforceable(CatacombsFloor floor) {
            return CatacombsAvailability.isPublic() && CatacombsAvailability.isPlayable(floor);
        }

        public boolean isDungeonTierMet(Predicate<CatacombsFloor> completed) {
            return level < 0 || level >= CatacombsFloor.values().length || completed.test(CatacombsFloor.byNumber(level));
        }

        public String display() {
            return switch (type) {
                case SKILL -> category.substring(0, 1) + category.substring(1).toLowerCase() + " " + level;
                case DUNGEON_SKILL -> "Catacombs Skill " + level;
                case DUNGEON_TIER -> level >= 0 && level < CatacombsFloor.values().length
                        ? "The Catacombs " + CatacombsFloor.byNumber(level).displayName() + " Completion"
                        : "The Catacombs Floor " + level + " Completion";
                case SLAYER -> SlayerRequirement.display(category, level);
                case PENDING -> description;
            };
        }
    }

    public enum Type {
        SKILL,
        DUNGEON_SKILL,
        DUNGEON_TIER,
        SLAYER,
        PENDING
    }
}
