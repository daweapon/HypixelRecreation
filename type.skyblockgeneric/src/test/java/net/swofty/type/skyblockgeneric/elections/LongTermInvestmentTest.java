package net.swofty.type.skyblockgeneric.elections;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LongTermInvestmentTest {

    @Test
    void sittingMinisterSitsOutTheNextElection() {
        for (int attempt = 0; attempt < 20; attempt++) {
            ElectionData data = sitting("AATROX", List.of("SLAYER_XP_BUFF"), "COLE", "PROSPECTION");
            data.startNewElection(3);

            assertNull(candidate(data, SkyBlockMayor.COLE));
            assertNull(candidate(data, SkyBlockMayor.AATROX));
        }
    }

    @Test
    void diazAsMayorBringsTheMinisterBackWithEveryPerk() {
        ElectionData data = sitting("DIAZ", List.of("LONG_TERM_INVESTMENT"), "COLE", "PROSPECTION");
        data.startNewElection(3);

        ElectionData.CandidateData cole = candidate(data, SkyBlockMayor.COLE);
        assertNotNull(cole);
        assertEquals(Arrays.stream(SkyBlockMayor.COLE.getAllPerks()).map(Enum::name).toList(), cole.getActivePerks());
    }

    @Test
    void diazAsMinisterWithLongTermInvestmentReturnsHerself() {
        ElectionData data = sitting("MARINA", List.of("FISHING_XP_BUFF"), "DIAZ", "LONG_TERM_INVESTMENT");
        data.startNewElection(3);

        ElectionData.CandidateData diaz = candidate(data, SkyBlockMayor.DIAZ);
        assertNotNull(diaz);
        assertEquals(SkyBlockMayor.DIAZ.getAllPerks().length, diaz.getActivePerks().size());
    }

    private static ElectionData sitting(String mayor, List<String> mayorPerks, String minister, String ministerPerk) {
        ElectionData data = new ElectionData();
        data.setCurrentMayor(mayor);
        data.setCurrentMayorPerks(mayorPerks);
        data.setCurrentMinister(minister);
        data.setMinisterPerk(ministerPerk);
        return data;
    }

    private static ElectionData.CandidateData candidate(ElectionData data, SkyBlockMayor mayor) {
        return data.getCandidates().stream()
                .filter(candidate -> candidate.getMayorName().equals(mayor.name()))
                .findFirst()
                .orElse(null);
    }
}
