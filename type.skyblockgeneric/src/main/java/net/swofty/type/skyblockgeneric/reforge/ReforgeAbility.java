package net.swofty.type.skyblockgeneric.reforge;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;

public interface ReforgeAbility {
    String reforgeId();

    interface Statistics extends ReforgeAbility {
        ItemStatistics statistics(ReforgeContext context);
    }

    interface OutgoingDamage extends ReforgeAbility {
        double outgoingMultiplier(ReforgeHit.Outgoing hit);
    }

    interface IncomingDamage extends ReforgeAbility {
        double incomingMultiplier(ReforgeHit.Incoming hit);
    }

    interface BonusDefense extends ReforgeAbility {
        double defenseAgainst(ReforgeHit.Incoming hit);
    }

    interface MiningExperience extends ReforgeAbility {
        double bonusFraction();
    }

    interface TitaniumPowder extends ReforgeAbility {
        long bonusMithrilPowder();
    }

    interface HarvestCoins extends ReforgeAbility {
        double coinsPerHarvest();
    }

    interface TransmissionBoost extends ReforgeAbility {
        ItemStatistics boost();

        long durationMillis();
    }

    interface HunterFortune extends ReforgeAbility {
        double fortuneFor(AttributeDefinition.AttributeCategory category);
    }

    interface EnchantLevelBonus extends ReforgeAbility {
        int levelBonus(EnchantmentType type);
    }

    interface BurrowChainLength extends ReforgeAbility {
        int extraBurrows();
    }

    interface CandyDrop extends ReforgeAbility {
        double dropChanceBonus();
    }

    interface ForestWhispers extends ReforgeAbility {
        double bonusFraction();
    }

    interface PestFortune extends ReforgeAbility {
        double pestFortune();
    }

    interface PestDamage extends ReforgeAbility {
        double pestDamageMultiplier();
    }

    interface PestCooldown extends ReforgeAbility {
        double cooldownReductionPercent();
    }
}
