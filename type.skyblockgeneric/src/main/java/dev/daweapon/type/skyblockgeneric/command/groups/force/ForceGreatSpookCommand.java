package dev.daweapon.type.skyblockgeneric.command.groups.force;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookEvent;

@CommandParameters(labels = "greatspook", description = "Turns the Great Spook on or off on every server, or back to its dates",
        usage = "/force greatspook [on|off|auto]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceGreatSpookCommand extends ForceToggleCommand {
    @Override
    protected String eventName() {
        return "Great Spook";
    }

    @Override
    protected boolean set(boolean active) {
        return GreatSpookEvent.setOverride(active);
    }

    @Override
    protected boolean clear() {
        return GreatSpookEvent.clearOverride();
    }

    @Override
    protected boolean active() {
        return GreatSpookEvent.isActive();
    }
}
