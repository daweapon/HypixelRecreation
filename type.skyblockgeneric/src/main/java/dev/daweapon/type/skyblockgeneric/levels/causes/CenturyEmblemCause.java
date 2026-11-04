package dev.daweapon.type.skyblockgeneric.levels.causes;

import dev.daweapon.type.skyblockgeneric.levels.abstr.CauseEmblem;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;

@Getter
public class CenturyEmblemCause extends SkyBlockLevelCauseAbstr implements CauseEmblem {
    public static final String CELEBRANT = "celebrant";
    public static final String PARTYGOER = "partygoer";

    private final String key;
    private final String requirement;

    public CenturyEmblemCause(String key, String requirement) {
        this.key = key;
        this.requirement = requirement;
    }

    public static String teamKey(String team) {
        return "raffle-" + team.toLowerCase();
    }

    @Override
    public String displayName() {
        return "Raffle of the Century";
    }

    @Override
    public double xpReward() {
        return 0;
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return false;
    }

    @Override
    public String getEmblemRequiresMessage() {
        return requirement;
    }

    @Override
    public String emblemEisplayName() {
        return "Raffle of the Century";
    }
}
