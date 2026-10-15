package dev.daweapon.type.skyblockgeneric.elections.perks;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor.Perk;

import java.util.ArrayList;
import java.util.List;

public final class PerkLore {
    private PerkLore() {}

    public static List<Text> description(Perk perk, int wrapWidth) {
        List<Text> lines = new ArrayList<>(Text.parseLenient(wrap(wrapWidth, perk.getDescription())).lines());
        PerkAvailability.gap(perk).ifPresent(gap -> {
            String label = gap.partial() ? "<8>Partly in this server: " : "<8>Not in this server yet: ";
            lines.addAll(Text.parseLenient(wrap(wrapWidth, label + gap.reason())).lines());
        });
        return lines;
    }

    private static String wrap(int width, String content) {
        return "<wrap:" + width + ">" + content + "</wrap>";
    }
}
