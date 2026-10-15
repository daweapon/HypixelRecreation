package dev.daweapon.type.kuudra.fight;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.kuudra.fight.world.TentacleSlams;
import dev.daweapon.type.kuudra.mob.KuudraRushers;
import dev.daweapon.type.kuudra.mob.KuudraSpecialists;
import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryData;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraBehaviourTest {
    private static final KuudraConfig CONFIG = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra"));

    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void blightsCycleGreenYellowRed() {
        assertEquals(KuudraRushers.Blight.Phase.GREEN, KuudraRushers.Blight.phaseAt(0, 4));
        assertEquals(KuudraRushers.Blight.Phase.YELLOW, KuudraRushers.Blight.phaseAt(80, 4));
        assertEquals(KuudraRushers.Blight.Phase.RED, KuudraRushers.Blight.phaseAt(179, 4));
        assertEquals(KuudraRushers.Blight.Phase.GREEN, KuudraRushers.Blight.phaseAt(240, 4));
    }

    @Test
    void wanderingBlazeFireballsStackWithRepeatedHits() {
        int basic = CONFIG.tier(KuudraTier.BASIC).wanderingBlazeDps();
        assertEquals(400, KuudraSpecialists.WanderingBlaze.fireballDamage(basic, 1, 1), 1e-9);
        assertEquals(1_200, KuudraSpecialists.WanderingBlaze.fireballDamage(basic, 3, 1), 1e-9);
        assertEquals(12, KuudraSpecialists.WanderingBlaze.fireballDamage(basic, 3, 0.01), 1e-9);
    }

    @Test
    void dropshipsCircleThenHeadForTheMiddle() {
        Pos centre = new Pos(0, 79, 0);
        Pos start = KuudraSpecialists.Dropship.loopPosition(centre, 50, 30, 0, 0, 800, 0.75);
        assertEquals(new Pos(50, 109, 0), start);
        Pos quarter = KuudraSpecialists.Dropship.loopPosition(centre, 50, 30, 0, 200, 800, 0.75);
        assertEquals(0, quarter.x(), 1e-9);
        assertEquals(50, quarter.z(), 1e-9);
        Pos above = KuudraSpecialists.Dropship.loopPosition(centre, 50, 30, 0, 700, 800, 0.75);
        assertEquals(0, above.x(), 1e-9);
        assertEquals(0, above.z(), 1e-9);
        assertEquals(109, above.y(), 1e-9);
    }

    @Test
    void slamPathsAreStripsTowardTheTarget() {
        List<TentacleSlams.Column> path = TentacleSlams.path(new Pos(0.5, 79, 0.5), new Pos(20.5, 79, 0.5), 10, 3);
        assertTrue(path.contains(new TentacleSlams.Column(0, 0)));
        assertTrue(path.contains(new TentacleSlams.Column(10, 1)));
        assertTrue(path.contains(new TentacleSlams.Column(10, -1)));
        assertTrue(path.stream().allMatch(column -> column.x() >= 0 && column.x() <= 10 && Math.abs(column.z()) <= 1));
        TentacleSlams.Slam slam = new TentacleSlams.Slam(path, "tentacle");
        assertTrue(TentacleSlams.covers(slam, new Pos(5.2, 79, 1.7)));
        assertTrue(!TentacleSlams.covers(slam, new Pos(5.2, 79, 2.7)));
    }

    @Test
    void theKuudraBestiaryMatchesMobsYml() {
        for (BestiaryCategory.KUUDRA family : BestiaryCategory.KUUDRA.values()) {
            var spec = CONFIG.mob(family.getMobs().getFirst().getMobID());
            assertTrue(spec != null && spec.inBestiary(), family.name());
            assertEquals(spec.bestiaryBracket(), family.getMobs().getFirst().getBestiaryBracket(), family.name());
            assertEquals(spec.bestiaryCap(), new BestiaryData()
                    .getTotalKillsForMaxTier(family.getMobs().getFirst()), family.name());
        }
        long inBestiary = CONFIG.mobs().stream().filter(mob -> mob.inBestiary()).count();
        assertEquals(BestiaryCategory.KUUDRA.values().length, inBestiary);
    }

    @Test
    void fightSpecReadsEveryTier() {
        KuudraFightSpec spec = CONFIG.fight();
        for (KuudraTier tier : KuudraTier.values()) {
            assertTrue(spec.supplies().pickupSeconds().get(tier) > 0);
            assertTrue(spec.boss().shotDamage().get(tier) > 0);
            assertTrue(spec.tentacles().active(tier) > 0);
        }
        assertEquals(5.0, spec.supplies().pickupSeconds().get(KuudraTier.BURNING));
        assertEquals(100_000, spec.boss().health());
        assertEquals(4, spec.fuel().cells());
        assertEquals(6, spec.supplies().crates());
    }
}
