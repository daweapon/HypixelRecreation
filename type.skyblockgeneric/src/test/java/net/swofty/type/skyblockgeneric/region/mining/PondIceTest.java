package net.swofty.type.skyblockgeneric.region.mining;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegenConfiguration.MiningTask;
import net.swofty.type.skyblockgeneric.region.mining.configurations.JerryPondIceConfiguration;
import net.swofty.type.skyblockgeneric.region.mining.configurations.SunkenJerryPondIceConfiguration;
import net.swofty.type.skyblockgeneric.region.mining.handler.SkyBlockMiningHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PondIceTest {
    private static final Vec CELL = new Vec(-60, 75, 30);

    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void onlyIceSittingDirectlyOnWaterIsBreakable() {
        assertTrue(PondIce.isBreakable(Block.ICE, Block.WATER));
        assertTrue(PondIce.isBreakable(Block.ICE, Block.WATER.withProperty("level", "3")));
        assertFalse(PondIce.isBreakable(Block.ICE, Block.SNOW_BLOCK));
        assertFalse(PondIce.isBreakable(Block.ICE, Block.ICE));
        assertFalse(PondIce.isBreakable(Block.PACKED_ICE, Block.WATER));
        assertFalse(PondIce.isBreakable(Block.SNOW_BLOCK, Block.WATER));
        assertFalse(PondIce.isBreakable(Block.WATER, Block.WATER));
    }

    @Test
    void regrowDelayStaysWithinTheWindow() {
        Random random = new Random(7);
        for (int i = 0; i < 10_000; i++) {
            long delay = PondIce.regrowDelayMillis(120_000, 300_000, random);
            assertTrue(delay >= 120_000 && delay <= 300_000, "delay " + delay);
        }
        assertEquals(5_000, PondIce.regrowDelayMillis(5_000, 5_000, random));
    }

    @Test
    void stageOneOpensWaterAndSchedulesIceWithinTheWindow() {
        long before = System.currentTimeMillis();
        MiningTask task = new JerryPondIceConfiguration().handleStageOne(MiningTask.builder().build(), new Pos(CELL));
        assertEquals(Block.WATER, task.getIntermediaryBlock());
        assertEquals(Block.ICE, task.getReviveBlock());
        assertTrue(task.getRegenerationTime() >= before + 120_000);
        assertTrue(task.getRegenerationTime() <= System.currentTimeMillis() + 300_000);
    }

    @Test
    void sunkenPondRefreezesFaster() {
        long before = System.currentTimeMillis();
        MiningTask task = new SunkenJerryPondIceConfiguration().handleStageOne(MiningTask.builder().build(), new Pos(CELL));
        assertTrue(task.getRegenerationTime() >= before + 60_000);
        assertTrue(task.getRegenerationTime() <= System.currentTimeMillis() + 150_000);
    }

    @Test
    void floatingBobberBlocksRegrowth() {
        Vec bobber = new Vec(-59.5, 75.98, 30.5);
        assertTrue(PondIce.occupies(bobber, bobber, CELL, 0.25));
        Vec bobbingHigh = new Vec(-59.5, 76.07, 30.5);
        assertTrue(PondIce.occupies(bobbingHigh, bobbingHigh, CELL, 0.25));
    }

    @Test
    void swimmingPlayerBlocksRegrowth() {
        assertTrue(PondIce.occupies(new Vec(-59.8, 75.2, 30.2), new Vec(-59.2, 77.0, 30.8), CELL, 0.25));
    }

    @Test
    void playerOnNeighbouringIceDoesNotBlockRegrowth() {
        assertFalse(PondIce.occupies(new Vec(-58.7, 76.0, 30.2), new Vec(-58.1, 77.8, 30.8), CELL, 0.25));
        assertFalse(PondIce.occupies(new Vec(-59.8, 76.3, 30.2), new Vec(-59.2, 78.1, 30.8), CELL, 0.25));
    }

    @Test
    void entityTouchingOnlyTheCellFaceDoesNotBlockRegrowth() {
        assertFalse(PondIce.occupies(new Vec(-61.6, 75.0, 30.2), new Vec(-61.0, 76.8, 30.8), CELL, 0.25));
    }

    @Test
    void iceBreaksWithAnyItemOnVanillaTimings() {
        SkyBlockMiningHandler handler = MineableBlock.ICE.getMiningHandler();
        assertTrue(handler.canToolBreak(null));
        assertEquals(0, handler.getMiningPowerRequirement());
        assertTrue(handler.usesVanillaBreakTime());
        assertEquals(15, handler.getBreakTimeForTool(null));
        assertFalse(handler.breaksInstantly());
    }
}
