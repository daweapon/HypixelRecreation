package dev.daweapon.type.skyblockgeneric.pity;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@FunctionalInterface
public interface PityMeter {
    PityMeter UNAVAILABLE = _ -> List.of(PityProgress.UNAVAILABLE);

    List<String> lines(SkyBlockPlayer player);
}
