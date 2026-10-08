package net.swofty.type.skyblockgeneric.crimson.quest;

import net.swofty.type.skyblockgeneric.crimson.dojo.DojoGrade;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTest;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import org.jetbrains.annotations.Nullable;

/**
 * One entry of the Faction Quest pool. {@code target} is the trophy fish id, item id, miniboss, Dojo test or
 * Kuudra tier name depending on the type; {@code faction} limits Rescue Missions to the side they belong to.
 */
public record FactionQuest(String id, FactionQuestType type, FactionQuestTier tier, String name, String target,
                           int amount, @Nullable CrimsonFaction faction) {

    public String tabName() {
        return switch (type) {
            case FISHING, FETCH -> name + " x" + amount;
            default -> name;
        };
    }

    public String objective() {
        return switch (type) {
            case FISHING -> "<7>Catch " + (amount == 1 ? "a " : "<a>" + amount + " ") + "<f>" + name
                    + " <7>Trophy Fish of any quality!";
            case FETCH -> "<7>Bring <a>" + amount + "x <f>" + name + " <7>to the Town Board!";
            case MINIBOSS -> "<7>Kill the <c>" + name + " <7>miniboss <a>" + amount + " <7>time" + (amount == 1 ? "" : "s") + "!";
            case RESCUE -> "<7>Rescue the hostage held in " + (faction == CrimsonFaction.MAGE
                    ? "<2>The Bastion" : "<5>the Cathedral") + "<7>!";
            case DOJO -> "<7>Get rank " + dojoGrade().coloured() + " <7>or better in the <6>" + dojoTest().fullName() + "<7>!";
            case KUUDRA -> "<7>Defeat <c>" + kuudraTier().getDisplayName() + " Kuudra <7>or a higher tier!";
        };
    }

    public @Nullable CrimsonMiniboss miniboss() {
        return type == FactionQuestType.MINIBOSS ? CrimsonMiniboss.valueOf(target) : null;
    }

    public DojoTest dojoTest() {
        return DojoTest.valueOf(target);
    }

    public DojoGrade dojoGrade() {
        return DojoGrade.valueOf(tier.name());
    }

    public KuudraTier kuudraTier() {
        return KuudraTier.valueOf(target);
    }
}
