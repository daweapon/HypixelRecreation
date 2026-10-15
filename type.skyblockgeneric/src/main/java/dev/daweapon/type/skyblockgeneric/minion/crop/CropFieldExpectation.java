package dev.daweapon.type.skyblockgeneric.minion.crop;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;

import java.util.List;

/**
 * Every tile around the minion needs the crop's soil underneath and room (or the crop itself) on
 * top, otherwise the minion reports that its location isn't perfect.
 */
public record CropFieldExpectation(MinionCrop crop) implements SkyBlockMinion.MinionExpectation {
    @Override
    public boolean meetsExpectation(Instance container, IslandMinionData.IslandMinion minion) {
        for (Pos tile : cropTiles(minion)) {
            if (!crop.acceptsSoil(container.getBlock(tile.sub(0, 1, 0)))) return false;
            if (!crop.acceptsCropTile(container.getBlock(tile))) return false;
        }
        return true;
    }

    public static List<Pos> cropTiles(IslandMinionData.IslandMinion minion) {
        return MathUtility.getRangeExcludingSelf(minion.getPosition(), 2 + minion.getBonusRange());
    }
}
