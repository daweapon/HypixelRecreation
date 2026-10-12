package net.swofty.type.skyblockgeneric.levels;

import net.swofty.type.skyblockgeneric.bank.BankAccountTier;
import net.swofty.type.skyblockgeneric.bank.BankUpgradeAwards;
import net.swofty.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.rabbits.HoppitysHuntXP;
import net.swofty.type.skyblockgeneric.spookyfestival.SpookyFestivalXP;
import net.swofty.type.skyblockgeneric.warps.TravelScrollAwards;
import net.swofty.type.skyblockgeneric.warps.TravelScrollType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XpSourceCapsTest {

    @Test
    void bankUpgradesAddUpToTheTask() {
        assertEquals(SkyBlockXPSource.BANK_UPGRADES.getMaxXP(), BankUpgradeAwards.skyBlockXp(BankAccountTier.PALATIAL));
    }

    @Test
    void spookyFestivalBracketsAddUpToTheTask() {
        assertEquals(SkyBlockXPSource.SPOOKY_FESTIVAL.getMaxXP(), SpookyFestivalXP.total(SpookyFestivalXP.POINT_BRACKETS));
    }

    @Test
    void everyObtainableTravelScrollReachesTheTask() {
        var all = Arrays.stream(TravelScrollType.values()).map(TravelScrollType::getInternalName).toList();
        assertTrue(TravelScrollAwards.skyBlockXp(all) >= SkyBlockXPSource.FAST_TRAVELS_UNLOCKED.getMaxXP());
    }

    @Test
    void brainFoodAddsUpToTheTask() {
        assertEquals(SkyBlockXPSource.BRAIN_FOOD.getMaxXP(), MythologicalItemHandlers.brainFoodSkyBlockXp(MythologicalItemHandlers.BRAIN_FOOD_LIMIT));
    }

    @Test
    void hoppitysHuntReachesItsTaskWithTheRabbitsThatExist() {
        long mythic = Arrays.stream(ChocolateRabbit.values()).filter(r -> r.getRarity() == ChocolateRabbit.RabbitRarity.MYTHIC).count();
        long divine = Arrays.stream(ChocolateRabbit.values()).filter(r -> r.getRarity() == ChocolateRabbit.RabbitRarity.DIVINE).count();
        int total = HoppitysHuntXP.total(HoppitysHuntXP.MAX_PRESTIGES, (int) mythic, (int) divine);
        System.out.println("Hoppity: " + mythic + " mythic + " + divine + " divine -> " + total + " of " + SkyBlockXPSource.HOPPITYS_HUNT.getMaxXP());
        assertTrue(total >= SkyBlockXPSource.HOPPITYS_HUNT.getMaxXP(), "unreachable: " + total);
    }
}
