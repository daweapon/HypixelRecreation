package dev.daweapon.type.crystalhollows.temple;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crystalhollows.config.Box;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkourStateTest {
    private static final Pos START = new Pos(0.5, 0, 0.5);
    private static final Pos MID = new Pos(10.5, 0, 0.5);
    private static final Pos END = new Pos(20.5, 0, 0.5);

    private static Box at(Pos pos) {
        return Box.of(pos.sub(0.4, 0, 0.4), pos.add(0.4, 0, 0.4));
    }

    private static Parkour parkour(String id) {
        return new Parkour(id, at(START), START, List.of(new Parkour.Checkpoint(at(MID), MID)), at(END));
    }

    @Test
    void finishOnlyCountsAfterStartAndEveryCheckpoint() {
        ParkourState state = new ParkourState();
        List<Parkour> parkours = List.of(parkour("1"));
        assertTrue(state.update(parkours, END).isEmpty());
        assertEquals(List.of(new ParkourState.Started("1")), state.update(parkours, START));
        assertTrue(state.update(parkours, END).isEmpty());
        assertEquals(List.of(new ParkourState.CheckpointReached("1", 0)), state.update(parkours, MID));
        assertEquals(MID, state.respawn());
        assertEquals(List.of(new ParkourState.Completed("1")), state.update(parkours, END));
        assertTrue(state.isComplete("1"));
        assertNull(state.respawn());
    }

    @Test
    void allCompleteNeedsEveryParkour() {
        ParkourState state = new ParkourState();
        List<Parkour> parkours = List.of(parkour("1"), parkour("2"));
        state.update(parkours, START);
        state.update(parkours, MID);
        state.update(parkours, END);
        assertEquals(2, state.completedCount());
        assertTrue(state.allComplete(parkours));
        assertFalse(new ParkourState().allComplete(parkours));
    }

    @Test
    void shiftMovesEveryBoxAndRespawn() {
        Parkour shifted = parkour("1").shift(new Pos(100, 50, 100));
        assertTrue(shifted.finish().contains(new Pos(120.5, 50, 100.5)));
        assertEquals(new Pos(110.5, 50, 100.5), shifted.checkpoints().getFirst().respawn());
    }

    @Test
    void shippedParkourConfigIsEmpty() {
        assertTrue(JungleTemple.read(Path.of("../configuration/skyblock/crystal_hollows/parkours.yml")).isEmpty());
    }
}
