package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;
import java.util.Map;

public record StructureSpec(String id, String name, RegionType region, boolean optional, int clearance,
                            List<Pos> candidates, Map<String, Pos> anchors) {
}
