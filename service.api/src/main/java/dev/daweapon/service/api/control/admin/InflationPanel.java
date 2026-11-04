package dev.daweapon.service.api.control.admin;

import dev.daweapon.commons.economy.CoinFlowStore;
import dev.daweapon.commons.economy.CoinFlowStore.Supply;

public final class InflationPanel {
    public static final int MAX_DAYS = 365;

    private InflationPanel() {}

    public static InflationReport.Report report(int days, boolean refresh) {
        OrphanScan.Result scan = OrphanScan.scan(refresh);
        CoinFlowStore.recordSupply(new Supply(CoinFlowStore.today(), scan.totalCoins(), scan.totalBank(), scan.profiles()));
        int window = Math.clamp(days, 1, MAX_DAYS);
        return InflationReport.build(CoinFlowStore.readDays(window), CoinFlowStore.readSupply());
    }
}
