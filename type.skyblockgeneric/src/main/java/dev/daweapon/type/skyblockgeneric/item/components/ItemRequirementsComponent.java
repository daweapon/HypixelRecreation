package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

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
            if (type != Type.SKILL) return true;
            try {
                SkillCategories skill = SkillCategories.valueOf(category);
                return player.getSkills().getCurrentLevel(skill) >= level;
            } catch (IllegalArgumentException ignored) {
                return true;
            }
        }

        public String display() {
            return switch (type) {
                case SKILL -> category.substring(0, 1) + category.substring(1).toLowerCase() + " " + level;
                case DUNGEON_SKILL -> category + " level " + level;
                case DUNGEON_TIER -> category + " floor " + level + " completion";
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
