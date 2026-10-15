package dev.daweapon.type.skyblockgeneric.xp;

import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactXP;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.HoppitysHuntXP;
import dev.daweapon.type.skyblockgeneric.spookyfestival.SpookyFestivalXP;
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
