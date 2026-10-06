package net.swofty.type.skyblockgeneric.levels.causes;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulExchangeLevels;

@Getter
public class FairySoulExchangeLevelCause extends SkyBlockLevelCauseAbstr {
    private final FairySoulExchangeLevels level;
    private int exchangeCount;

    public FairySoulExchangeLevelCause(int exchangeCount) {
        this.exchangeCount = exchangeCount;
        this.level = FairySoulExchangeLevels.getLevel(exchangeCount);
    }

    /**
     * Real Hypixel grants +10 SkyBlock XP per Fairy Soul exchange, each exchange being 5 souls,
     * up to 570 XP. This returned 0, so the cause was recorded but paid nothing.
     */
    public static final int XP_PER_EXCHANGE = 10;

    @Override
    public String displayName() {
        return "Fairy Soul Exchange " + exchangeCount;
    }

    @Override
    public double xpReward() {
        return XP_PER_EXCHANGE;
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return false;
    }
}
