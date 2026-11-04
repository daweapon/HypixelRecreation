package dev.daweapon.service.api.control.admin;

import dev.daweapon.commons.economy.CoinFlowStore;
import dev.daweapon.commons.economy.CoinFlowStore.Day;
import dev.daweapon.commons.economy.CoinFlowStore.Delta;
import dev.daweapon.commons.economy.CoinFlowStore.Kind;
import dev.daweapon.commons.economy.CoinFlowStore.Supply;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InflationReportTest {
    private static final LocalDate DAY_ONE = LocalDate.of(2026, 11, 1);
    private static final LocalDate DAY_TWO = LocalDate.of(2026, 11, 2);

    @Test
    void sourcesMinusSinksIsTheNetAndTransfersAndStaffStayOut() {
        List<Day> days = List.of(
                new Day(DAY_ONE, Map.of(
                        CoinFlowStore.field(Kind.SOURCE, "Mob kill coins"), new Delta(1_000, 50),
                        CoinFlowStore.field(Kind.SINK, "Reforging"), new Delta(-300, 3),
                        CoinFlowStore.field(Kind.STAFF, "Staff tools"), new Delta(1_000_000, 1))),
                new Day(DAY_TWO, Map.of(
                        CoinFlowStore.field(Kind.SOURCE, "Mob kill coins"), new Delta(500, 20),
                        CoinFlowStore.field(Kind.SINK, "Auction listing fees"), new Delta(-150, 2),
                        CoinFlowStore.field(Kind.TRANSFER, "Bazaar" + CoinFlowStore.PAID), new Delta(-1_000, 4),
                        CoinFlowStore.field(Kind.TRANSFER, "Bazaar" + CoinFlowStore.RECEIVED), new Delta(990, 4))));
        Map<LocalDate, Supply> supply = Map.of(
                DAY_ONE, new Supply(DAY_ONE, 8_000, 2_000, 3),
                DAY_TWO, new Supply(DAY_TWO, 9_000, 2_050, 3));

        InflationReport.Report report = InflationReport.build(days, supply);

        assertEquals(1_500, report.minted());
        assertEquals(450, report.burned());
        assertEquals(1_050, report.net());
        assertEquals(1_000_000, report.staffNet());
        assertEquals(10.5, report.netAsPercentOfSupply(), 1e-9);
        assertEquals(10.5, report.supplyChangePercent(), 1e-9);
        assertEquals(0.3, report.sinkToSourceRatio(), 1e-9);
        assertEquals("Reforging", report.sinks().getFirst().category());
        assertEquals(300, report.sinks().getFirst().amount());

        InflationReport.Channel bazaar = report.transfers().getFirst();
        assertEquals("Bazaar", bazaar.channel());
        assertEquals(990, bazaar.received());
        assertEquals(1_000, bazaar.paid());
        assertEquals(-10, bazaar.net(), 1e-9);

        assertEquals(700, report.daily().getFirst().net());
        assertEquals(11_050, report.daily().getLast().supply());
    }

    @Test
    void anEmptyWindowReportsNothing() {
        InflationReport.Report report = InflationReport.build(List.of(), Map.of());
        assertEquals(0, report.net());
        assertNull(report.supplyNow());
    }
}
