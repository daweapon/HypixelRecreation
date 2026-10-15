package dev.daweapon.type.skyblockgeneric.gems;

import dev.daweapon.type.skyblockgeneric.commission.Objective;

import java.util.EnumMap;
import java.util.Map;

public final class GemstoneColours {
    private static final Map<Gemstone, Character> CODES = new EnumMap<>(Gemstone.class);

    static {
        CODES.put(Gemstone.RUBY, 'c');
        CODES.put(Gemstone.AMBER, '6');
        CODES.put(Gemstone.SAPPHIRE, 'b');
        CODES.put(Gemstone.JADE, 'a');
        CODES.put(Gemstone.AMETHYST, '5');
        CODES.put(Gemstone.TOPAZ, 'e');
        CODES.put(Gemstone.JASPER, 'd');
        CODES.put(Gemstone.OPAL, 'f');
        CODES.put(Gemstone.AQUAMARINE, '3');
        CODES.put(Gemstone.CITRINE, '4');
        CODES.put(Gemstone.ONYX, '8');
        CODES.put(Gemstone.PERIDOT, '2');
    }

    private GemstoneColours() {
    }

    public static char code(Gemstone gemstone) {
        return CODES.get(gemstone);
    }

    public static String tag(Gemstone gemstone) {
        return "<" + code(gemstone) + ">";
    }

    public static Gemstone of(Objective.BlockTarget target) {
        return switch (target) {
            case GEM_AMBER -> Gemstone.AMBER;
            case GEM_AMETHYST -> Gemstone.AMETHYST;
            case GEM_JADE -> Gemstone.JADE;
            case GEM_RUBY -> Gemstone.RUBY;
            case GEM_SAPPHIRE -> Gemstone.SAPPHIRE;
            case GEM_TOPAZ -> Gemstone.TOPAZ;
            case AQUAMARINE -> Gemstone.AQUAMARINE;
            case CITRINE -> Gemstone.CITRINE;
            case ONYX -> Gemstone.ONYX;
            case PERIDOT -> Gemstone.PERIDOT;
            default -> null;
        };
    }
}
