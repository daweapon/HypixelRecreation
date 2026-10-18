package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@FunctionalInterface
public interface TabPlayerBlock {

    List<TabLine> lines(SkyBlockPlayer viewer);
}
