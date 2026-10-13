package net.swofty.type.skyblockgeneric.commission;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCommissions;

import java.util.List;

public enum CommissionZone {
    DWARVEN_MINES("the King"),
    CRYSTAL_HOLLOWS("Emissary Sisko");

    private final String claimGiver;

    CommissionZone(String claimGiver) {
        this.claimGiver = claimGiver;
    }

    public String claimGiver() {
        return claimGiver;
    }

    public List<DatapointCommissions.ActiveCommission> active(DatapointCommissions.PlayerCommissionData data) {
        return this == CRYSTAL_HOLLOWS ? data.getHollowsCommissions() : data.getActiveCommissions();
    }
}
