package dev.daweapon.service.control;

import dev.daweapon.service.control.triage.ExceptionTriage;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionTriageTest {

    private static String trace(String time, String message, String line) {
        return time + " 2026-11-03 10:00:00 [Thread-" + line + "] dev.daweapon.Loader.run() ERROR: " + message + "\n"
                + time + " java.lang.IllegalStateException: profile missing\n"
                + time + " \tat dev.daweapon.type.skyblockgeneric.data.Profile.load(Profile.java:" + line + ")\n"
                + time + " \tat java.base/java.lang.Thread.run(Thread.java:1583)\n"
                + time + " 2026-11-03 10:00:01 [main] dev.daweapon.Loader.run() INFO: carrying on\n";
    }

    @Test
    void groupsTheSameBugAcrossContainersAndBuilds() {
        ExceptionTriage triage = new ExceptionTriage(ExceptionTriage.DEFAULT_PROJECT_PACKAGES, false);
        triage.scan("hypixelcore_hub", trace("2026-11-03T10:00:00.000000001Z", "Failed to load", "42"));
        triage.scan("hypixelcore_hub", trace("2026-11-03T11:00:00.000000001Z", "Failed to load", "42"));
        triage.scan("hypixelcore_island", trace("2026-11-03T09:00:00.000000001Z", "Failed to load", "57"));

        ExceptionTriage.Report report = triage.report();
        assertEquals(1, report.groups().size(), "line numbers must not split one bug into several groups");
        ExceptionTriage.Group group = report.groups().get(0);
        assertEquals("java.lang.IllegalStateException @ dev.daweapon.type.skyblockgeneric.data.Profile.load",
                group.signature());
        assertEquals(3, group.count());
        assertEquals(2, group.perContainer().get("hypixelcore_hub"));
        assertEquals(1, group.perContainer().get("hypixelcore_island"));
        assertEquals(Instant.parse("2026-11-03T09:00:00.000000001Z"), group.firstSeen());
        assertEquals(Instant.parse("2026-11-03T11:00:00.000000001Z"), group.lastSeen());
        assertEquals("hypixelcore_hub", group.lastContainer());
        assertTrue(group.sample().stream().anyMatch(l -> l.contains("Profile.load")));
        assertEquals(3, report.totalEvents());
    }

    @Test
    void prefersTheTopProjectFrameOverLibraryFrames() {
        String log = "java.lang.NullPointerException: x\n"
                + "\tat net.minestom.server.Thing.tick(Thing.java:10)\n"
                + "\tat dev.daweapon.type.hub.Npc.tick(Npc.java:20)\n";
        ExceptionTriage triage = new ExceptionTriage(List.of("dev.daweapon."), false);
        triage.scan("hypixelcore_hub", log);
        assertEquals("java.lang.NullPointerException @ dev.daweapon.type.hub.Npc.tick",
                triage.report().groups().get(0).signature());
    }

    @Test
    void errorLinesWithoutTracesGroupByNormalisedMessage() {
        String log = "2026-11-03 10:00:00 [pool-1] dev.daweapon.X.y() ERROR: Failed to refresh election 1234 for 3f2a9c1e-1111-2222-3333-444455556666\n"
                + "2026-11-03 10:00:05 [pool-7] dev.daweapon.X.y() ERROR: Failed to refresh election 99 for 0a2a9c1e-1111-2222-3333-444455556666\n";
        ExceptionTriage triage = new ExceptionTriage(List.of("dev.daweapon."), false);
        triage.scan("service_election", log);
        List<ExceptionTriage.Group> groups = triage.report().groups();
        assertEquals(1, groups.size());
        assertEquals(ExceptionTriage.Kind.ERROR, groups.get(0).kind());
        assertEquals(2, groups.get(0).count());
    }

    @Test
    void ignoresWarningsThatMerelyMentionAnException() {
        ExceptionTriage triage = new ExceptionTriage(List.of("dev.daweapon."), false);
        triage.scan("service_api", "WARN: retrying after java.net.ConnectException: refused\nINFO: ok\n");
        assertEquals(0, triage.report().totalEvents());
    }

    @Test
    void bootRaceNoiseIsExcludedUnlessAskedFor() {
        String log = "ERROR: redis.clients.jedis.exceptions.JedisConnectionException: Unexpected end of stream\n";
        ExceptionTriage filtered = new ExceptionTriage(List.of("dev.daweapon."), false);
        filtered.scan("service_party", log);
        assertEquals(0, filtered.report().totalEvents());

        ExceptionTriage raw = new ExceptionTriage(List.of("dev.daweapon."), true);
        raw.scan("service_party", log);
        assertEquals(1, raw.report().totalEvents());
    }

    @Test
    void normalisationStripsVolatileParts() {
        assertEquals("dev.X.y() ERROR: lost <uuid> after #ms at <hex>",
                ExceptionTriage.normalise("2026-11-03 10:00:00 [Thread-12] dev.X.y() ERROR: lost "
                        + "3f2a9c1e-1111-2222-3333-444455556666 after 250ms at deadbeefcafe"));
    }
}
