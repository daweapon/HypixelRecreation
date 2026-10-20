package dev.daweapon.type.skyblockgeneric.starlyn;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StarlynSidebarTest {
    private final StarlynConfig config = StarlynConfig.load(StarlynSister.MIRIA, Path.of("../configuration/skyblock/starlyn"));

    @Test
    void titleCountsDownInMinutesAndSecondsWithoutSpaces() {
        assertEquals("<e>Miria's Contest <a>2m40s", StarlynSidebar.of(StarlynSister.MIRIA, config, 160, 160_000).title());
    }

    @Test
    void belowTheTopBracketTheNextOneNamesWhatIsMissing() {
        StarlynSidebar.Lines lines = StarlynSidebar.of(StarlynSister.MIRIA, config, 160, 60_000);
        assertEquals("<f>COMMON <f>with <e>160", lines.bracket());
        assertEquals("<a>Uncommon <7>requires <e>+90", lines.requirement());
    }

    @Test
    void pastLegendaryShowsTheSurplusOverIt() {
        StarlynSidebar.Lines lines = StarlynSidebar.of(StarlynSister.MIRIA, config, 6500, 60_000);
        assertEquals("<6>LEGENDARY <f>with <e>6.5k", lines.bracket());
        assertEquals("<7>+1.5k over <6>Legendary", lines.requirement());
    }

    @Test
    void scoreUnderCommonShowsOnlyTheScore() {
        StarlynSidebar.Lines lines = StarlynSidebar.of(StarlynSister.MIRIA, config, 10, 60_000);
        assertEquals("<f>Score <e>10", lines.bracket());
        assertEquals("<f>Common <7>requires <e>+40", lines.requirement());
    }
}
