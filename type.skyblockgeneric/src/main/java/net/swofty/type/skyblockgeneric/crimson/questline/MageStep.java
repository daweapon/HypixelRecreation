package net.swofty.type.skyblockgeneric.crimson.questline;

import lombok.Getter;
import lombok.experimental.Accessors;

/** The Mage Faction Questline (+600 Mage Reputation in total). */
@Getter
@Accessors(fluent = true)
public enum MageStep implements QuestlineStep {
    NOT_STARTED("Talk to Elle."),
    SHOW_BATPHONE("Show the Maddox Batphone to the Dean in the Cathedral."),
    DELIVER_LETTER("Bring the Letter of Recommendation to Queen Nyx in the Throne Room."),
    BUY_ABIPHONE("Buy an Abiphone and call the Dean."),
    TALK_TO_ELLE("Talk to Elle in the Stronghold."),
    MEET_ELLE_AT_BASTION("Meet Elle on the cliff above The Bastion."),
    RECOVER_RESEARCH("Sneak into The Bastion and recover the stolen research."),
    CALL_DEAN("Call the Dean with your Abiphone."),
    MAGE_COUNCIL("Bring the research to the Mage Council below the Community Center."),
    RETURN_TO_ACADEMY("Talk to the Dean in the Cathedral."),
    FIND_AHONE("Look for the Dean's Apprentice north of the Burning Desert."),
    FIND_APPRENTICE("Find the Dean's Apprentice."),
    CALL_AHONE("Call Captain Ahone with your Abiphone."),
    REPORT_TO_DEAN("Tell the Dean what happened to his Apprentice."),
    BRING_MATERIALS("Bring the Dean 10 Magma Chunks and 5 Burning Eyes."),
    HELP_IGRUPAN("Help Igrupan bring his 5 chickens back to the coop."),
    HELP_ODEXAR("Help Odexar with his Volcanic Stonefish."),
    HALLOWED_SKULL("Bring the Dean a Hallowed Skull from Bladesoul."),
    LUMINO_FIBER("Bring the Dean a Lumino Fiber from Ashfang."),
    LEATHER_CLOTH("Bring the Dean a Leather Cloth from Barbarian Duke X."),
    TALK_TO_AHONE("Talk to Captain Ahone in the Mage Council."),
    MEET_AT_FORGOTTEN_SKULL("Meet Captain Ahone near the Forgotten Skull."),
    DEFEAT_KUUDRA("Defeat Kuudra with Elle."),
    TALK_TO_DEAN_FINAL("Talk to the Dean in the Cathedral."),
    COMPLETE("Done."),
    ;

    private final String objective;

    MageStep(String objective) {
        this.objective = objective;
    }
}
