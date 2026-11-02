package dev.daweapon.type.skyblockgeneric.levels.guide;

import java.util.function.Predicate;

public record GuideRule(boolean available, Predicate<GuideScan> check) {
    public static final GuideRule UNAVAILABLE = new GuideRule(false, scan -> false);

    public boolean complete(GuideScan scan) {
        return available && check.test(scan);
    }
}
