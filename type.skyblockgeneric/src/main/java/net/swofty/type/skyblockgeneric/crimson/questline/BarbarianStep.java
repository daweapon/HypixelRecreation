package net.swofty.type.skyblockgeneric.crimson.questline;

import lombok.Getter;
import lombok.experimental.Accessors;

/** The Barbarian Faction Questline (+900 Barbarian Reputation in total). */
@Getter
@Accessors(fluent = true)
public enum BarbarianStep implements QuestlineStep {
    NOT_STARTED("Talk to Elle."),
    FOLLOW_ELLE("Meet Elle in the Burning Desert."),
    KILL_CORRUPTED_MONSTER("Kill the Corrupted Flaming Spider in the Burning Desert."),
    FIND_THE_NOISE("Talk to Elle next to the cave."),
    FEED_THE_PIG("Give the lost Piglin 1 Sulphur."),
    ESCORT_UGO("Escort Ugo to the Chief's Hut in Dragontail."),
    TALK_TO_UGO("Talk to Ugo outside the Chief's Hut."),
    MEET_ELLE_AT_CATHEDRAL("Meet Elle outside the Cathedral."),
    STEAL_RESEARCH("Sneak into the Dean's office and steal the research."),
    RETURN_TO_UGO("Bring the research back to Ugo in Dragontail."),
    PRESENT_RESEARCH("Present the research to Chief Scorn."),
    UGO_TOWN_BOARD("Talk to Ugo at the Town Board."),
    DOJO_TRAINING("Pass the Tests of Force, Stamina and Mastery at the Dojo."),
    REPORT_DOJO("Talk to Ugo."),
    KILL_MAGE_OUTLAW("Kill the Mage Outlaw in the Courtyard."),
    REPORT_MAGE_OUTLAW("Talk to Ugo."),
    KILL_MAGMA_BOSS("Kill the Magma Boss below the Volcano."),
    REPORT_MAGMA_BOSS("Talk to Ugo."),
    HELP_LASEA("Bring Lasea 1 Flyfish and 1 Skeleton Fish."),
    HELP_GRIS("Bring Gris a Heavy Pearl."),
    TALK_TO_CHIEF("Talk to Chief Scorn."),
    MEET_AT_FORGOTTEN_SKULL("Meet Ugo near the Forgotten Skull."),
    DEFEAT_KUUDRA("Defeat Kuudra with Elle."),
    TALK_TO_CHIEF_FINAL("Talk to Chief Scorn."),
    COMPLETE("Done."),
    ;

    private final String objective;

    BarbarianStep(String objective) {
        this.objective = objective;
    }
}
