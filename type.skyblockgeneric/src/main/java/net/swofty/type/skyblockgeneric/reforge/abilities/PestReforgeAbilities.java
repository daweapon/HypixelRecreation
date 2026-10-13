package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.type.skyblockgeneric.garden.pest.PestRules;
import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;

public final class PestReforgeAbilities {
    private PestReforgeAbilities() {
    }

    public static final class Beady implements ReforgeAbility.PestFortune {
        @Override
        public String reforgeId() {
            return "beady";
        }

        @Override
        public double pestFortune() {
            return PestRules.BEADY_PEST_FORTUNE;
        }
    }

    public static final class Buzzing implements ReforgeAbility.PestDamage {
        @Override
        public String reforgeId() {
            return "buzzing";
        }

        @Override
        public double pestDamageMultiplier() {
            return PestRules.BUZZING_DAMAGE_MULTIPLIER;
        }
    }

    public static final class Squeaky implements ReforgeAbility.PestCooldown {
        @Override
        public String reforgeId() {
            return "squeaky";
        }

        @Override
        public double cooldownReductionPercent() {
            return PestRules.COOLDOWN_REDUCTION_SQUEAKY;
        }
    }
}
