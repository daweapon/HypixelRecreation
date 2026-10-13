package net.swofty.type.skyblockgeneric.reforge;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;

import java.util.Collection;

public final class ReforgeEffects {
    private ReforgeEffects() {
    }

    public static ItemStatistics statistics(Collection<? extends ReforgeAbility> abilities, ReforgeContext context) {
        ItemStatistics total = ItemStatistics.empty();
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.Statistics effect) {
                total = ItemStatistics.add(total, effect.statistics(context));
            }
        }
        return total;
    }

    public static double outgoingMultiplier(Collection<? extends ReforgeAbility> abilities, ReforgeHit.Outgoing hit) {
        double multiplier = 1;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.OutgoingDamage effect) multiplier *= effect.outgoingMultiplier(hit);
        }
        return multiplier;
    }

    public static double incomingMultiplier(Collection<? extends ReforgeAbility> abilities, ReforgeHit.Incoming hit) {
        double multiplier = 1;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.IncomingDamage effect) multiplier *= effect.incomingMultiplier(hit);
        }
        return multiplier;
    }

    public static double bonusDefense(Collection<? extends ReforgeAbility> abilities, ReforgeHit.Incoming hit) {
        double defense = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.BonusDefense effect) defense += effect.defenseAgainst(hit);
        }
        return defense;
    }

    public static double defenseMultiplier(Collection<? extends ReforgeAbility> abilities, ReforgeHit.Incoming hit,
                                           double defense) {
        double bonus = bonusDefense(abilities, hit);
        double base = Math.max(0, defense);
        return (100 + base) / (100 + base + bonus);
    }

    public static double miningExperienceMultiplier(Collection<? extends ReforgeAbility> abilities) {
        double bonus = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.MiningExperience effect) bonus += effect.bonusFraction();
        }
        return 1 + bonus;
    }

    public static long titaniumMithrilPowder(Collection<? extends ReforgeAbility> abilities) {
        long powder = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.TitaniumPowder effect) powder += effect.bonusMithrilPowder();
        }
        return powder;
    }

    public static double harvestCoins(Collection<? extends ReforgeAbility> abilities) {
        double coins = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.HarvestCoins effect) coins += effect.coinsPerHarvest();
        }
        return coins;
    }

    public static ItemStatistics transmissionBoost(Collection<? extends ReforgeAbility> abilities) {
        ItemStatistics total = ItemStatistics.empty();
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.TransmissionBoost effect) {
                total = ItemStatistics.add(total, effect.boost());
            }
        }
        return total;
    }

    public static long transmissionDurationMillis(Collection<? extends ReforgeAbility> abilities) {
        long duration = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.TransmissionBoost effect) {
                duration = Math.max(duration, effect.durationMillis());
            }
        }
        return duration;
    }

    public static double hunterFortune(Collection<? extends ReforgeAbility> abilities,
                                       AttributeDefinition.AttributeCategory category) {
        double fortune = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.HunterFortune effect) fortune += effect.fortuneFor(category);
        }
        return fortune;
    }

    public static int enchantLevelBonus(Collection<? extends ReforgeAbility> abilities, EnchantmentType type) {
        int bonus = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.EnchantLevelBonus effect) bonus += effect.levelBonus(type);
        }
        return bonus;
    }

    public static int extraBurrows(Collection<? extends ReforgeAbility> abilities) {
        int burrows = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.BurrowChainLength effect) burrows += effect.extraBurrows();
        }
        return burrows;
    }

    public static double candyDropBonus(Collection<? extends ReforgeAbility> abilities) {
        double bonus = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.CandyDrop effect) bonus += effect.dropChanceBonus();
        }
        return bonus;
    }

    public static double forestWhisperFraction(Collection<? extends ReforgeAbility> abilities) {
        double fraction = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.ForestWhispers effect) fraction += effect.bonusFraction();
        }
        return fraction;
    }

    public static double pestFortune(Collection<? extends ReforgeAbility> abilities) {
        double fortune = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.PestFortune effect) fortune += effect.pestFortune();
        }
        return fortune;
    }

    public static double pestDamageMultiplier(Collection<? extends ReforgeAbility> abilities) {
        double multiplier = 1;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.PestDamage effect) multiplier *= effect.pestDamageMultiplier();
        }
        return multiplier;
    }

    public static double pestCooldownReduction(Collection<? extends ReforgeAbility> abilities) {
        double reduction = 0;
        for (ReforgeAbility ability : abilities) {
            if (ability instanceof ReforgeAbility.PestCooldown effect) reduction += effect.cooldownReductionPercent();
        }
        return reduction;
    }
}
