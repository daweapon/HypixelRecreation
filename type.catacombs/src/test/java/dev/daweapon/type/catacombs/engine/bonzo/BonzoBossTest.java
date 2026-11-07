package dev.daweapon.type.catacombs.engine.bonzo;

import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.type.catacombs.engine.CatacombsMob;
import dev.daweapon.type.catacombs.engine.look.MobLook;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bonzo's fake death as both captured runs synced it: DYING at 0 health, then SLEEPING at 0.1, then up again. */
class BonzoBossTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private static BonzoBoss bonzo() {
        MobLook look = CatacombsMob.lookOf("BONZO");
        BonzoBoss.Fight fight = new BonzoBoss.Fight() {
            @Override
            public void bonzoHurt(BonzoBoss bonzo) {
            }

            @Override
            public void bonzoLifeLost(BonzoBoss bonzo) {
            }

            @Override
            public boolean targetable(SkyBlockPlayer player) {
                return false;
            }

            @Override
            public void mobDied(CatacombsMob mob) {
            }
        };
        return new BonzoBoss(DungeonMobRegistry.defaults().mob("BONZO"), look, fight, Pos.ZERO);
    }

    @Test
    void heFallsOverThenLiesFlatWithoutReallyDying() {
        BonzoBoss bonzo = bonzo();
        bonzo.setItemInMainHand(ItemStack.of(Material.BLAZE_ROD));
        float standingName = bonzo.getNameDisplayHeightOffset();
        bonzo.vulnerable(true);
        bonzo.kill();
        assertTrue(bonzo.down());

        bonzo.fallOver();
        assertEquals(EntityPose.DYING, bonzo.getPose());
        assertEquals(0f, bonzo.getLivingEntityMeta().getHealth(), "the client sees 0 health so it plays the fall");
        assertTrue(bonzo.getItemInMainHand().isAir(), "the staff leaves his hand");
        assertFalse(bonzo.isDead(), "he never really dies");
        assertTrue(bonzo.getHealth() > 0);
        assertEquals(standingName + 1.6f, bonzo.getNameDisplayHeightOffset(), 1e-4, "the name stays at standing height");

        bonzo.lieDown();
        assertEquals(EntityPose.SLEEPING, bonzo.getPose());
        assertEquals(0.1f, bonzo.getLivingEntityMeta().getHealth(), 1e-6);
        assertTrue(bonzo.lying());
        assertFalse(bonzo.damage(new net.minestom.server.entity.damage.Damage(
                net.minestom.server.entity.damage.DamageType.GENERIC, null, null, null, 1_000)), "he cannot be hurt while down");

        bonzo.revive(CatacombsMob.lookOf("BONZO"));
        assertEquals(EntityPose.STANDING, bonzo.getPose());
        assertFalse(bonzo.lying());
        assertFalse(bonzo.down());
        assertEquals(bonzo.getMaxHealth(), bonzo.getHealth());
        assertTrue(bonzo.getLivingEntityMeta().getHealth() > 1, "the client sees him alive again");
        assertEquals(standingName, bonzo.getNameDisplayHeightOffset(), 1e-4);
    }
}
