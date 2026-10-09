package net.swofty.type.generic.entity.ai.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaNavigatorBudgetTest {
    private static final Pos PEN = new Pos(30.5, 64, 30.5);
    private static InstanceContainer instance;

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setGenerator(unit -> {
            unit.modifier().fillHeight(unit.absoluteStart().blockY(), 64, Block.NETHERRACK);
            Pos start = unit.absoluteStart().asPos();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int bx = start.blockX() + x;
                    int bz = start.blockZ() + z;
                    if (Math.floorMod(bx * 7 + bz * 13, 5) == 0 && unit.absoluteStart().blockY() <= 64
                            && unit.absoluteEnd().blockY() > 65) {
                        unit.modifier().setBlock(bx, 64, bz, Block.NETHER_BRICK_FENCE);
                    }
                }
            }
        });
        for (int cx = -3; cx <= 3; cx++) for (int cz = -3; cz <= 3; cz++) instance.loadChunk(cx, cz).join();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                Block wall = Math.abs(x) == 3 || Math.abs(z) == 3 ? Block.NETHER_BRICK_FENCE : Block.AIR;
                instance.setBlock(PEN.blockX() + x, 64, PEN.blockZ() + z, wall);
                instance.setBlock(PEN.blockX() + x, 65, PEN.blockZ() + z, wall);
                instance.setBlock(x, 64, z, Block.AIR);
            }
        }
    }

    private static VanillaNavigator mob() {
        EntityCreature mob = new EntityCreature(EntityType.ZOMBIE);
        mob.setInstance(instance, new Pos(0.5, 64, 0.5)).join();
        return new VanillaNavigator(mob);
    }

    @Test
    void anUnreachableStrollStaysWithinItsNodeBudget() {
        VanillaNavigator navigator = mob();
        Pos outside = PEN;
        for (int i = 0; i < 20; i++) {
            navigator.pathTo(outside);
            navigator.pathTo(outside, 150);
        }

        int runs = 50;
        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) navigator.pathTo(outside);
        long full = (System.nanoTime() - start) / runs;
        start = System.nanoTime();
        for (int i = 0; i < runs; i++) navigator.pathTo(outside, 150);
        long budgeted = (System.nanoTime() - start) / runs;
        System.out.printf("path into a sealed pen 42 blocks away: default cap %.3f ms, 150-node stroll cap %.3f ms%n",
                full / 1e6, budgeted / 1e6);
    }

    @Test
    void aShortStrollStillFindsItsWay() {
        VanillaNavigator navigator = mob();
        assertTrue(navigator.pathTo(new Pos(2.5, 64, 1.5), 150));
        assertFalse(navigator.getNodes().isEmpty());
    }
}
