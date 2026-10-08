package net.swofty.type.generic.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickDiagnosticsTest {
    private static StackTraceElement frame(String type, String method, int line) {
        return new StackTraceElement(type, method, type + ".java", line);
    }

    @Test
    void signatureListsTheProjectCallerChain() {
        StackTraceElement[] stack = {
                frame("net.minestom.server.tag.TagNbtSeparator", "separate", 35),
                frame("net.minestom.server.item.ItemStack", "getTag", 289),
                frame("net.swofty.type.skyblockgeneric.item.SkyBlockItem", "loadAsStack", 185),
                frame("net.swofty.type.skyblockgeneric.item.SkyBlockItem", "<init>", 95),
                frame("java.util.ArrayList", "forEach", 1604),
                frame("net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics", "allStatistics", 302),
        };
        String signature = SlowTickProfiler.signature(stack);
        assertTrue(signature.contains("|| project: SkyBlockItem.loadAsStack:185 <- SkyBlockItem.<init>:95 <- PlayerStatistics.allStatistics:302"), signature);
    }

    @Test
    void recognisesTheSchedulerWaitingOnTickThreads() {
        StackTraceElement[] waiting = {
                frame("jdk.internal.misc.Unsafe", "park", -2),
                frame("java.util.concurrent.locks.LockSupport", "park", 223),
                frame("java.util.concurrent.CountDownLatch", "await", 276),
                frame("net.minestom.server.thread.ThreadDispatcherImpl", "updateAndAwait", 63),
        };
        assertTrue(SlowTickProfiler.isAwaitingTickThreads(waiting));
        assertFalse(SlowTickProfiler.isAwaitingTickThreads(new StackTraceElement[]{frame("a.B", "c", 1)}));

        StackTraceElement[] idle = {
                frame("jdk.internal.misc.Unsafe", "park", -2),
                frame("net.minestom.server.thread.TickThread", "run", 63),
        };
        StackTraceElement[] busy = {
                frame("net.swofty.Mob", "tick", 10),
                frame("net.minestom.server.thread.TickThread", "tick", 82),
                frame("net.minestom.server.thread.TickThread", "run", 54),
        };
        assertFalse(SlowTickProfiler.isTicking(idle));
        assertTrue(SlowTickProfiler.isTicking(busy));
    }

    @Test
    void tpsCountsCompletedTicksRatherThanTheLastTicksLength() {
        TpsMeter.reset();
        long second = 1_000_000_000L;
        long now = 100 * second;
        for (int i = 0; i < 100; i++) TpsMeter.record(now - 5 * second + i * (5 * second / 100));
        assertEquals(20.0, TpsMeter.tps(now));

        TpsMeter.reset();
        for (int i = 0; i < 50; i++) TpsMeter.record(now - 5 * second + i * (5 * second / 50));
        assertEquals(10.2, TpsMeter.tps(now), 0.3);
    }
}
