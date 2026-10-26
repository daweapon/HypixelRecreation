package dev.daweapon.type.skyblockgeneric.hunting;

import java.util.List;

import static dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition.AttributeCategory;
import static dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition.AttributeSkill;

public record SpecialFusionRecipe(AttributeId output, Clause first, Clause second) {
    public boolean matches(AttributeDefinition a, AttributeDefinition b) {
        return first.matches(a) && second.matches(b) || first.matches(b) && second.matches(a);
    }

    public record Clause(List<Alternative> alternatives) {
        public Clause {
            alternatives = List.copyOf(alternatives);
        }

        public boolean matches(AttributeDefinition definition) {
            return alternatives.stream().anyMatch(alternative -> alternative.matches(definition));
        }
    }

    public record Alternative(boolean any, AttributeRarity rarity, AttributeRarity minRarity, AttributeCategory category,
                              String family, AttributeSkill skill, AttributeId shard) {
        public boolean matches(AttributeDefinition definition) {
            if (any) return true;
            if (shard != null && !shard.equals(definition.id())) return false;
            if (rarity != null && rarity != definition.rarity()) return false;
            if (minRarity != null && definition.rarity().ordinal() < minRarity.ordinal()) return false;
            if (category != null && category != definition.category()) return false;
            if (skill != null && skill != definition.skill()) return false;
            return family == null || family.equals(FusionData.family(definition.id()));
        }
    }
}
