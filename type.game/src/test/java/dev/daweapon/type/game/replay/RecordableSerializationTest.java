package dev.daweapon.type.game.replay;

import dev.daweapon.commons.replay.protocol.ReplayDataReader;
import dev.daweapon.commons.replay.protocol.ReplayDataWriter;
import dev.daweapon.type.game.replay.recordable.Recordable;
import dev.daweapon.type.game.replay.recordable.RecordableBatch;
import dev.daweapon.type.game.replay.recordable.RecordableSound;
import dev.daweapon.type.game.replay.recordable.RecordableType;
import dev.daweapon.type.game.replay.recordable.RecordablePlayerSneak;
import dev.daweapon.type.game.replay.recordable.bedwars.RecordableKill;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecordableSerializationTest {
    private static ReplayDataReader roundTrip(Recordable recordable) throws IOException {
        try (ReplayDataWriter writer = new ReplayDataWriter()) {
            recordable.write(writer);
            return new ReplayDataReader(writer.toByteArray());
        }
    }

    @Test
    void typeIdsAreUniqueAndResolvable() {
        Set<Integer> ids = new HashSet<>();
        for (RecordableType type : RecordableType.values()) {
            assertTrue(ids.add(type.getId()), type.name());
            assertSame(type, RecordableType.byId(type.getId()));
        }
        assertNull(RecordableType.byId(-1));
    }

    @Test
    void everyTypeCreatesARecordableOfItsOwnType() {
        for (RecordableType type : RecordableType.values()) {
            assertEquals(type, type.create().getType());
        }
    }

    @Test
    void killRoundTripsWithAndWithoutKiller() throws IOException {
        UUID victim = UUID.randomUUID();
        UUID killer = UUID.randomUUID();
        RecordableKill withKiller = new RecordableKill(7, victim, 9, killer, (byte) 2, (byte) 4, (byte) 1);
        RecordableKill read = (RecordableKill) RecordableType.BEDWARS_KILL.createAndRead(roundTrip(withKiller));
        assertEquals(7, read.getVictimEntityId());
        assertEquals(victim, read.getVictimUuid());
        assertEquals(killer, read.getKillerUuid());
        assertEquals(4, read.getDeathCause());
        assertEquals(1, read.getFinalKill());

        RecordableKill noKiller = new RecordableKill(7, victim, -1, null, (byte) 0, (byte) 2, (byte) 0);
        RecordableKill readNone = (RecordableKill) RecordableType.BEDWARS_KILL.createAndRead(roundTrip(noKiller));
        assertNull(readNone.getKillerUuid());
        assertEquals(-1, readNone.getKillerEntityId());
    }

    @Test
    void soundRoundTripsPositionAndHalfPrecisionFloats() throws IOException {
        RecordableSound sound = new RecordableSound("minecraft:entity.generic.explode", (byte) 3, 10.5, 64.25, -3.75, 1.0f, 0.5f);
        RecordableSound read = (RecordableSound) RecordableType.SOUND.createAndRead(roundTrip(sound));
        assertEquals("minecraft:entity.generic.explode", read.getSoundId());
        assertEquals(3, read.getCategory());
        assertEquals(10.5, read.getX(), 1e-3);
        assertEquals(64.25, read.getY(), 1e-3);
        assertEquals(-3.75, read.getZ(), 1e-3);
        assertEquals(1.0f, read.getVolume(), 1e-3);
        assertEquals(0.5f, read.getPitch(), 1e-3);
    }

    @Test
    void batchPreservesChildTicksAndOrder() throws IOException {
        RecordablePlayerSneak first = new RecordablePlayerSneak(1, true);
        first.setTick(5);
        RecordablePlayerSneak second = new RecordablePlayerSneak(2, false);
        second.setTick(9);
        RecordableBatch batch = new RecordableBatch(new java.util.ArrayList<>(java.util.List.of(first, second)));

        RecordableBatch read = (RecordableBatch) RecordableType.BATCH.createAndRead(roundTrip(batch));

        assertEquals(2, read.getRecordables().size());
        assertEquals(5, read.getRecordables().get(0).getTick());
        assertEquals(9, read.getRecordables().get(1).getTick());
        assertEquals(2, read.getRecordables().get(1).getEntityId());
    }
}
