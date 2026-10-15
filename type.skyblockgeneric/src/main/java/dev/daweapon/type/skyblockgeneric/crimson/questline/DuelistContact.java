package dev.daweapon.type.skyblockgeneric.crimson.questline;

import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

/** Rollim and Kaus: their contact comes with the first training duel, and they call to ask for the next one. */
public abstract class DuelistContact extends CrimsonContact {
    private final CrimsonFaction faction;

    protected DuelistContact(String id, String speaker, String description, String headTexture, CrimsonFaction faction) {
        super(id, speaker, description, headTexture);
        this.faction = faction;
    }

    @Override
    protected String[] refusal(SkyBlockPlayer player) {
        if (CrimsonSideQuests.duelDays(player, faction) > 0) return null;
        return new String[]{"Ohh man I'm in trouble! If you were part of my <e>faction<f>, maybe you could help me."};
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        int days = CrimsonSideQuests.duelDays(player, faction);
        if (!CrimsonReputationService.isMember(player, faction)) {
            reply(player, "Why aren't you on my side anymore? I don't want to embarrass myself; come back and help me.");
        } else if (days >= CrimsonSideQuests.DUEL_DAYS) {
            reply(player, "I spent a week training my body, but I think my mind improved even more.");
        } else if (CrimsonSideQuests.isReady(player, CrimsonSideQuests.duelLastKey(faction), Duration.ofHours(24))) {
            reply(player, "I'm ready for my lesson, come and find me!");
        } else {
            reply(player, "Please come back tomorrow.");
        }
    }
}
