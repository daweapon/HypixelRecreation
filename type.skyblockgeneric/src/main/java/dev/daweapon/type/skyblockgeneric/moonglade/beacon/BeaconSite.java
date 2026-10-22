package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.generic.HypixelConst;

import java.util.List;
import java.util.Optional;

public enum BeaconSite {
    MOONGLADE(ServerType.SKYBLOCK_GALATEA, "Moonglade Beacon", "beacon.signal_level",
            List.of(ItemStatistic.FIG_FORTUNE, ItemStatistic.MANGROVE_FORTUNE)),
    TORRHUS(ServerType.SKYBLOCK_TORRHUS_CANYON, "Torrhus Beacon", "beacon.torrhus_signal_level",
            List.of(ItemStatistic.HELIX_FORTUNE));

    private final ServerType server;
    private final String displayName;
    private final String levelTrigger;
    private final List<ItemStatistic> statistics;

    BeaconSite(ServerType server, String displayName, String levelTrigger, List<ItemStatistic> statistics) {
        this.server = server;
        this.displayName = displayName;
        this.levelTrigger = levelTrigger;
        this.statistics = statistics;
    }

    public static Optional<BeaconSite> current() {
        if (HypixelConst.getTypeLoader() == null) return Optional.empty();
        ServerType type = HypixelConst.getTypeLoader().getType();
        for (BeaconSite site : values()) if (site.server == type) return Optional.of(site);
        return Optional.empty();
    }

    public String displayName() {
        return displayName;
    }

    public String levelTrigger() {
        return levelTrigger;
    }

    public List<ItemStatistic> statistics() {
        return statistics;
    }
}
