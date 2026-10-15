package dev.daweapon.commons;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.color.TeamColor;

@UtilityClass
// Converts Adventure NamedTextColor values into Minestom TeamColor values.
public class TeamColorUtil {
    public static TeamColor fromNamedColor(NamedTextColor color) {
        return TeamColor.fromName(color.name());
    }
}
