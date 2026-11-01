package dev.daweapon.type.skyblockgeneric.levels.guide;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Predicate;

public record GuideRule(boolean available, Predicate<SkyBlockPlayer> check) {
    public static final GuideRule UNAVAILABLE = new GuideRule(false, player -> false);

    public boolean complete(SkyBlockPlayer player) {
        return available && check.test(player);
    }
}
