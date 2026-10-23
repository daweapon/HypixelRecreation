package dev.daweapon.type.skyblockgeneric.dante;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;

public enum OfficialBuilding {
    BANK("Bank", RegionType.BANK, null),
    AUCTION_HOUSE("Auction House", RegionType.AUCTION_HOUSE, null),
    BAZAAR_ALLEY("Bazaar Alley", RegionType.BAZAAR_ALLEY, null),
    FARMHOUSE("Farmhouse", null, new int[]{54, 66, -156, 70, 85, -140});

    private final String displayName;
    private final RegionType regionType;
    private final int[] box;

    OfficialBuilding(String displayName, RegionType regionType, int[] box) {
        this.displayName = displayName;
        this.regionType = regionType;
        this.box = box;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean contains(Pos position) {
        if (box != null) {
            return position.x() >= box[0] && position.x() <= box[3]
                    && position.y() >= box[1] && position.y() <= box[4]
                    && position.z() >= box[2] && position.z() <= box[5];
        }
        for (SkyBlockRegion region : SkyBlockRegion.getRegions()) {
            if (region.getType() == regionType && region.insideRegion(position)) return true;
        }
        return false;
    }

    public static OfficialBuilding at(Pos position) {
        for (OfficialBuilding building : values()) {
            if (building.contains(position)) return building;
        }
        return null;
    }
}
