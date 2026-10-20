package dev.daweapon.type.theend.dragon;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DragonDownMessage {
    public static final int RULE_WIDTH = 52;
    private static final String RULE = "<a><l>" + "▬".repeat(RULE_WIDTH);
    private static final String[] ORDINALS = {"1st", "2nd", "3rd"};
    private static final double CHAT_CHAR_PIXELS = 6;
    private static final double SPACE_PIXELS = 4;

    public record Name(String markup, String plain) {
    }

    public record Damager(Name name, double damage) {
    }

    private DragonDownMessage() {
    }

    public static String ordinal(int place) {
        return ORDINALS[place - 1];
    }

    public static String damage(double damage) {
        return String.format(Locale.US, "%,d", Math.round(damage));
    }

    public static String centre(String markup, int plainLength) {
        int spaces = (int) Math.max(0, (RULE_WIDTH - plainLength) * CHAT_CHAR_PIXELS / 2 / SPACE_PIXELS);
        return " ".repeat(spaces) + markup;
    }

    public static List<String> lines(String variantName, @Nullable Name finalBlow, List<Damager> top,
                                     int ownPosition, double ownDamage, int essence) {
        List<String> lines = new ArrayList<>();
        lines.add(RULE);
        String title = variantName.toUpperCase(Locale.ROOT) + " DOWN!";
        lines.add(centre("<6><l>" + title, title.length()));
        if (finalBlow != null) {
            String text = finalBlow.plain() + " dealt the final blow.";
            lines.add(centre("<f>" + finalBlow.markup() + " <e>dealt the final blow.", text.length()));
        }
        for (int i = 0; i < top.size() && i < ORDINALS.length; i++) {
            Damager damager = top.get(i);
            String amount = damage(damager.damage());
            String plain = ordinal(i + 1) + " Damager - " + damager.name().plain() + " - " + amount;
            lines.add(centre("<e><l>" + ordinal(i + 1) + " Damager <7>- " + damager.name().markup()
                    + " <7>- <e>" + amount, plain.length()));
        }
        if (ownPosition > 0) {
            String own = "Your Damage: " + damage(ownDamage) + " (Position #" + ownPosition + ")";
            lines.add(centre("<e>Your Damage: <a>" + damage(ownDamage) + " <7>(Position #" + ownPosition + ")", own.length()));
            String reward = "+" + essence + " Dragon Essence";
            lines.add(centre("<b>" + reward, reward.length()));
        }
        lines.add(RULE);
        return lines;
    }
}
