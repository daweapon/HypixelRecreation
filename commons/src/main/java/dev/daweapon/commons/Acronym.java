package dev.daweapon.commons;

// Words that should stay fully uppercase when normal-casing strings (see StringUtility.toNormalCase).
public enum Acronym {
    TNT,
    ;

    public static boolean isAcronym(String s) {
        for (Acronym a : values())
            if (s.equalsIgnoreCase(a.name())) return true;

        return false;
    }
}
