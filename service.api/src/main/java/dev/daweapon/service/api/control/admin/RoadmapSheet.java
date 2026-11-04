package dev.daweapon.service.api.control.admin;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

// Read-only view of the public tracker sheet. The tabs name their columns differently (Task vs Item,
// Area vs Type) but keep the same order, so cells are read by position.
public final class RoadmapSheet {
    public enum Tab {
        ROADMAP("Roadmap"),
        BUGS("Bugs & Loose Ends"),
        DONE("Done");

        private final String sheetName;

        Tab(String sheetName) {
            this.sheetName = sheetName;
        }

        public String sheetName() {
            return sheetName;
        }

        public static Tab parse(String value) {
            if (value == null) return ROADMAP;
            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return ROADMAP;
            }
        }
    }

    public record Row(String number, String task, String scope, String area, String priority, String effort,
                      String status, String notes) {}

    public record Snapshot(Tab tab, List<Row> rows, Instant fetchedAt) {}

    private static final String SHEET_ID = "11n4a2VfNAKf4SYMc9xEo-EvJzuOsWGzfWguNS-0PiY0";
    private static final Duration CACHE = Duration.ofMinutes(5);
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Map<Tab, Snapshot> CACHED = new ConcurrentHashMap<>();

    private RoadmapSheet() {}

    public static Snapshot fetch(Tab tab, boolean refresh) throws Exception {
        Snapshot cached = CACHED.get(tab);
        if (!refresh && cached != null && cached.fetchedAt().plus(CACHE).isAfter(Instant.now())) return cached;

        String url = "https://docs.google.com/spreadsheets/d/" + SHEET_ID + "/gviz/tq?tqx=out:csv&sheet="
                + URLEncoder.encode(tab.sheetName(), StandardCharsets.UTF_8).replace("+", "%20")
                + "&cb=" + ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IllegalStateException("tracker sheet returned HTTP " + response.statusCode());
        }

        Snapshot snapshot = new Snapshot(tab, toRows(CsvTable.parse(response.body())), Instant.now());
        CACHED.put(tab, snapshot);
        return snapshot;
    }

    public static List<Row> toRows(List<List<String>> table) {
        List<Row> rows = new ArrayList<>();
        for (int i = 1; i < table.size(); i++) {
            List<String> cells = table.get(i);
            String number = cell(cells, 0);
            String task = cell(cells, 1);
            if (number.isEmpty() && task.isEmpty()) continue;
            rows.add(new Row(number, task, cell(cells, 2), cell(cells, 3), cell(cells, 4), cell(cells, 5),
                    cell(cells, 6), cell(cells, 7)));
        }
        return rows;
    }

    private static String cell(List<String> cells, int index) {
        return index < cells.size() ? cells.get(index).trim() : "";
    }
}
