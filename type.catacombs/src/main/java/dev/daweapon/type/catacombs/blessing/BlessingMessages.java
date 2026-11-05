package dev.daweapon.type.catacombs.blessing;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.dungeons.catacombs.blessing.BlessingEffect;
import dev.daweapon.dungeons.catacombs.blessing.BlessingStat;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/** Hypixel's blessing chat: the gold "DUNGEON BUFF!" line, then indented grey lines saying what it granted. */
public final class BlessingMessages {
    private static final String BUFF = "<6><l>DUNGEON BUFF!</l> <f>";
    private static final String INDENT = "     <7>";
    private static final DecimalFormat NUMBER = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private BlessingMessages() {
    }

    public static String name(BlessingType type, int level) {
        return type.displayName() + " " + StringUtility.getAsRomanNumeral(level);
    }

    /** The finder's own line; {@code {}} in the others' line is where the finder's name goes. */
    public static String foundByYou(BlessingType type, int level) {
        return BUFF + "You found a <d>" + name(type, level) + "<f>!";
    }

    public static String foundBy(BlessingType type, int level) {
        return BUFF + "{} <f>found a <d>" + name(type, level) + "<f>!";
    }

    public static String foundInRoom(BlessingType type, int level) {
        return BUFF + "A <d>" + name(type, level) + " <f>was found!";
    }

    public static String obtained(BlessingType type) {
        return "{} <e>has obtained <d>" + type.displayName() + "<e>!";
    }

    public static List<String> granted(BlessingType type, int level, double effectiveness) {
        return switch (type) {
            case POWER -> List.of(
                    INDENT + "Granted you " + both(type, BlessingStat.STRENGTH, level, effectiveness) + " <c>❁ Strength<7>.",
                    INDENT + "Also granted you " + both(type, BlessingStat.CRIT_DAMAGE, level, effectiveness) + " <9>☠ Crit Damage<7>.");
            case STONE -> List.of(INDENT + "Granted you " + both(type, BlessingStat.DEFENSE, level, effectiveness)
                    + " <a>❈ Defense <7>and <a>+" + flat(type, BlessingStat.DAMAGE, level, effectiveness) + " <c>Damage<7>.");
            case LIFE -> List.of(INDENT + "Granted you <a>+" + multiplier(type, BlessingStat.HEALTH, level, effectiveness)
                    + "x <c>HP <7>and <a>+" + multiplier(type, BlessingStat.HEALTH_REGEN, level, effectiveness) + "x <c>❣ Health Regen<7>.");
            case WISDOM -> List.of(INDENT + "Granted you " + both(type, BlessingStat.INTELLIGENCE, level, effectiveness)
                    + " <b>✎ Intelligence <7>and <a>+" + flat(type, BlessingStat.SPEED, level, effectiveness) + " <f>✦ Speed<7>.");
            case TIME -> List.of(INDENT + "Granted you " + both(type, BlessingStat.HEALTH, level, effectiveness) + " <c>HP<7>, "
                    + both(type, BlessingStat.DEFENSE, level, effectiveness) + " <a>❈ Defense<7>, "
                    + both(type, BlessingStat.INTELLIGENCE, level, effectiveness) + " <b>✎ Intelligence<7>, and "
                    + both(type, BlessingStat.STRENGTH, level, effectiveness) + " <c>❁ Strength<7>.");
        };
    }

    private static String both(BlessingType type, BlessingStat stat, int level, double effectiveness) {
        return "<a>+" + flat(type, stat, level, effectiveness) + " <7>& <a>+" + multiplier(type, stat, level, effectiveness) + "x";
    }

    static String flat(BlessingType type, BlessingStat stat, int level, double effectiveness) {
        return NUMBER.format(effect(type, stat).flat(level) * effectiveness);
    }

    static String multiplier(BlessingType type, BlessingStat stat, int level, double effectiveness) {
        return NUMBER.format(1 + effect(type, stat).percent(level) * effectiveness / 100);
    }

    private static BlessingEffect effect(BlessingType type, BlessingStat stat) {
        return type.effects().stream().filter(effect -> effect.stat() == stat).findFirst()
                .orElse(new BlessingEffect(stat, 0, 0));
    }
}
