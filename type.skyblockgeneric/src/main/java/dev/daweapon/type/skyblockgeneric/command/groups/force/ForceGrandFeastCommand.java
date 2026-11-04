package dev.daweapon.type.skyblockgeneric.command.groups.force;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastCalendar;

@CommandParameters(labels = "grandfeast", description = "Turns the Grand Feast on or off on every server, or back to Finnegan's perk",
        usage = "/force grandfeast [on|off|auto]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceGrandFeastCommand extends ForceToggleCommand {
    @Override
    protected String eventName() {
        return "Grand Feast";
    }

    @Override
    protected boolean set(boolean active) {
        return FeastCalendar.setGrandOverride(active);
    }

    @Override
    protected boolean clear() {
        return FeastCalendar.clearGrandOverride();
    }

    @Override
    protected boolean active() {
        return FeastCalendar.grand();
    }
}
