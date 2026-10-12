package net.swofty.type.skyblockgeneric.xp;

import net.swofty.type.skyblockgeneric.abiphone.AbiphoneContactXP;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.rabbits.HoppitysHuntXP;
import net.swofty.type.skyblockgeneric.spookyfestival.SpookyFestivalXP;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExistingSystemXPTest {
    private static long count(ChocolateRabbit.RabbitRarity rarity) {
        long n = 0;
        for (ChocolateRabbit r : ChocolateRabbit.values()) if (r.getRarity() == rarity) n++;
        return n;
    }

    @Test
    void hoppitysHuntMaxesToCap() {
        assertEquals(SkyBlockXPSource.HOPPITYS_HUNT.getMaxXP(),
                HoppitysHuntXP.total(6, (int) count(ChocolateRabbit.RabbitRarity.MYTHIC),
                        (int) count(ChocolateRabbit.RabbitRarity.DIVINE)));
    }

    @Test
    void spookyFestivalMaxesToCap() {
        assertEquals(SkyBlockXPSource.SPOOKY_FESTIVAL.getMaxXP(), SpookyFestivalXP.total(6));
        assertEquals(0, SpookyFestivalXP.bracketsReached(99));
        assertEquals(1, SpookyFestivalXP.bracketsReached(100));
        assertEquals(6, SpookyFestivalXP.bracketsReached(5000));
    }

    @Test
    void abiphoneContactsReachCapAt84() {
        assertEquals(SkyBlockXPSource.ABIPHONE_CONTACTS.getMaxXP(), AbiphoneContactXP.total(85));
    }
}
