package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import java.util.List;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerMinionMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerMinionMob.SlayerMinionProfile;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;

/**
 * The Broodfather's summons from the wiki mob pages (0.27.2 values): Primordial Bats from Bat Barrage, Egg Sacs
 * from Web of Lies, and what an unbroken sac hatches into (Primordial Hatchling at Tier III-IV, Primordial Minion
 * at Tier V).
 */
final class PrimordialBrood {
    private PrimordialBrood() {
    }

    static SlayerMinionProfile bat(int tier) {
        boolean five = tier >= 5;
        OtherLoot rewards = five ? new OtherLoot(163, 36, 119) : new OtherLoot(120, 26, 88);
        return new SlayerMinionProfile("Primordial Bat", five ? 109 : 80, EntityType.BAT, five ? 500_000 : 100_000, 250,
            160, List.of(MobType.ANIMAL, MobType.AIRBORNE), new Hooks(rewards, true));
    }

    static double batDamageFraction(int tier) {
        return tier >= 5 ? 0.002D : 0.001D;
    }

    static SlayerMinionProfile hatchling(int tier) {
        if (tier >= 5) {
            return new SlayerMinionProfile("Primordial Minion", 186, EntityType.CAVE_SPIDER, 1_500_000, 2_000, 150,
                List.of(MobType.ARTHROPOD), new Hooks(new OtherLoot(279, 62, 204), false));
        }
        return new SlayerMinionProfile("Primordial Hatchling", 87, EntityType.CAVE_SPIDER, 250_000, 150, 150,
            List.of(MobType.ARTHROPOD), new Hooks(new OtherLoot(130, 29, 95), false));
    }

    static int eggSacs(int tier) {
        return tier >= 5 ? 4 : tier == 4 ? 3 : 2;
    }

    static SlayerMinionProfile eggSac(int tier, int level) {
        int hits = tier >= 5 ? 9 : tier == 4 ? 6 : 3;
        return new SlayerMinionProfile("Tarantula Egg Sac", level, EntityType.SLIME, hits, 0, 0,
            List.of(MobType.ARTHROPOD), EggSacHooks.INSTANCE);
    }

    private record Hooks(OtherLoot rewards, boolean magicImmune) implements SlayerMinionMob.Hooks {
        @Override
        public float modifyIncomingDamage(SlayerMinionMob minion, Damage damage, float amount) {
            return magicImmune && damage.getType() == DamageType.MAGIC ? 0F : amount;
        }
    }

    private enum EggSacHooks implements SlayerMinionMob.Hooks {
        INSTANCE;

        @Override
        public boolean stationary() {
            return true;
        }

        @Override
        public boolean canAttack(SlayerMinionMob minion) {
            return false;
        }

        @Override
        public float modifyIncomingDamage(SlayerMinionMob minion, Damage damage, float amount) {
            return 1F;
        }
    }
}
