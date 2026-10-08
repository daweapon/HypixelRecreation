package net.swofty.type.skyblockgeneric.crimson.questline;

import lombok.Getter;
import lombok.experimental.Accessors;

/** The arrival part of the Crimson Isle Main Questline that every player does before picking a side. */
@Getter
@Accessors(fluent = true)
public enum UniversalStep implements QuestlineStep {
    KILL_BLAZES("Help Elle fight the 3 Blazes in the Stronghold."),
    HELP_ELLE("Talk to Elle."),
    BRING_ITEMS("Bring Elle 2 Spectre Dust, 3 Blaze Ashes and 4 Magma Chunks."),
    EXPLORE("Explore the Crimson Isle."),
    TALK_TO_VILLAGERS("Talk to 4 villagers from each faction."),
    CHOOSE_FACTION("Talk to Elle and choose a faction."),
    COMPLETE("Done."),
    ;

    private final String objective;

    UniversalStep(String objective) {
        this.objective = objective;
    }
}
