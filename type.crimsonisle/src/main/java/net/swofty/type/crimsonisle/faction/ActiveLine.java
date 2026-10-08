package net.swofty.type.crimsonisle.faction;

import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/** Which faction questline drives the shared quest NPCs (Elle, Captain Ahone, Ugo) for a player. */
public final class ActiveLine {
    private ActiveLine() {
    }

    public static @Nullable CrimsonFaction of(SkyBlockPlayer player) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        if (faction != null) return faction;
        if (inProgress(CrimsonQuestlines.mage(player) != MageStep.NOT_STARTED, CrimsonQuestlines.mage(player) == MageStep.COMPLETE)) {
            return CrimsonFaction.MAGE;
        }
        if (inProgress(CrimsonQuestlines.barbarian(player) != BarbarianStep.NOT_STARTED, CrimsonQuestlines.barbarian(player) == BarbarianStep.COMPLETE)) {
            return CrimsonFaction.BARBARIAN;
        }
        return null;
    }

    public static boolean isMage(SkyBlockPlayer player, MageStep step) {
        return of(player) == CrimsonFaction.MAGE && CrimsonQuestlines.mage(player) == step;
    }

    public static boolean isBarbarian(SkyBlockPlayer player, BarbarianStep step) {
        return of(player) == CrimsonFaction.BARBARIAN && CrimsonQuestlines.barbarian(player) == step;
    }

    public static boolean atForgottenSkull(SkyBlockPlayer player) {
        return isMage(player, MageStep.MEET_AT_FORGOTTEN_SKULL) || isMage(player, MageStep.DEFEAT_KUUDRA)
                || isBarbarian(player, BarbarianStep.MEET_AT_FORGOTTEN_SKULL) || isBarbarian(player, BarbarianStep.DEFEAT_KUUDRA);
    }

    private static boolean inProgress(boolean started, boolean complete) {
        return started && !complete;
    }
}
