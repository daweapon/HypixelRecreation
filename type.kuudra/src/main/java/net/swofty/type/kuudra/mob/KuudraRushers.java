package net.swofty.type.kuudra.mob;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.kuudra.fight.KuudraFight;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** The melee minions that run at players (research K1 5.3). */
public final class KuudraRushers {
    private KuudraRushers() {
    }

    static ItemStack leather(Material piece, Color color) {
        return ItemStack.builder(piece).set(DataComponents.DYED_COLOR, color).build();
    }

    static ItemStack head(String texture) {
        return ItemStacks.head(texture, "").build();
    }

    static void dress(KuudraMob mob, Color color, String headTexture, Material hand) {
        mob.setEquipment(EquipmentSlot.HELMET, head(headTexture));
        mob.setEquipment(EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, color));
        mob.setEquipment(EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, color));
        mob.setEquipment(EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, color));
        if (hand != null) mob.setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(hand));
    }

    /** Rushes players with rapid attacks; Kuudra Follower Artifact/Relic owners take less from it (handled by gear). */
    public static final class Follower extends KuudraMob {
        public Follower(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.ZOMBIE, MobType.UNDEAD, MobType.INFERNAL);
            dress(this, new Color(3494666), "e57de815a24c961b28d539c92e0044ca4f37dad2672822f42d85e3ccf4d193b7", null);
        }

        @Override
        public long damageCooldown() {
            return 250;
        }

        @Override
        public boolean attacksBallista() {
            return true;
        }
    }

    /** Rushes players; leaves a lit TNT behind when it dies. */
    public static final class Landmine extends KuudraMob {
        public Landmine(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.ZOMBIE, MobType.INFERNAL);
            dress(this, new Color(3494666), "a76a2811d1e176a07b6d0a657b910f134896ce30850f6e80c7c83732d85381ea", null);
        }

        @Override
        public boolean attacksBallista() {
            return true;
        }

        @Override
        protected void onDeath() {
            fight.primeTnt(getPosition(), fight.spec().mobs().landmineFuseTicks(), damageStat(), this);
        }
    }

    /** Jumps at players like a Magma Cube but never splits. */
    public static final class InfernoMagmaCube extends KuudraMob {
        public InfernoMagmaCube(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.MAGMA_CUBE, MobType.CUBIC, MobType.MAGMATIC);
            editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(2));
        }
    }

    /** Rushes players; an Explosive Imp jumps out when it dies. */
    public static final class Berserker extends KuudraMob {
        public Berserker(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.ZOMBIE, MobType.INFERNAL);
            dress(this, new Color(5968906), "32f94e36f698649bc3d58dfde1b2b6552d068200bfd8b1372e33d4566e6a78d8", Material.IRON_SWORD);
        }

        @Override
        public boolean attacksBallista() {
            return true;
        }

        @Override
        protected void onDeath() {
            fight.spawnMob("EXPLOSIVE_IMP", getPosition());
        }
    }

    /** Runs at players and explodes when it dies. */
    public static final class ExplosiveImp extends KuudraMob {
        public ExplosiveImp(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.ZOMBIE, MobType.INFERNAL);
            editEntityMeta(ZombieMeta.class, meta -> meta.setBaby(true));
            dress(this, new Color(2818563), "a67e255a5b1956eb95cd97ac3be4caf710c26e3dcbd5bb00284e268c17a7bde1", null);
        }

        @Override
        public boolean attacksBallista() {
            return true;
        }

        @Override
        protected void onDeath() {
            fight.explode(getPosition(), fight.spec().mobs().explosionRadius(), damageStat(), this);
        }
    }

    /** Cycles green, yellow and red; can't be hurt in red unless the attacker has Blight Slayer. */
    public static final class Blight extends KuudraMob {
        public enum Phase {GREEN, YELLOW, RED}

        private static final Color[] COLORS = {new Color(3494666), new Color(16776960), new Color(13369344)};
        private static final String[] HEADS = {
                "29c36e290cbf8f0f74f7effe587a56849855d0418fe0a0e3af6b75108631a0de",
                "4eb77f82b4f24af2a8e022e2c009947bf508878214f1d844d200234bccb72e",
                "ae2ff1efd2ef35a9b685caa1788580237eaf0a1e54a12568ad606c9453ee6c68"};
        private Phase phase = Phase.GREEN;

        public Blight(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.ZOMBIE, MobType.UNDEAD);
            dress(this, COLORS[0], HEADS[0], null);
        }

        public static Phase phaseAt(int ticks, int phaseSeconds) {
            int length = Math.max(1, phaseSeconds * 20);
            return Phase.values()[(ticks / length) % Phase.values().length];
        }

        @Override
        protected void behave(int ticks) {
            Phase now = phaseAt(ticks, fight.spec().mobs().blightPhaseSeconds());
            if (now == phase) return;
            phase = now;
            Color color = COLORS[now.ordinal()];
            setEquipment(EquipmentSlot.HELMET, head(HEADS[now.ordinal()]));
            setEquipment(EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, color));
            setEquipment(EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, color));
            setEquipment(EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, color));
        }

        public boolean isRed() {
            return phase == Phase.RED;
        }

        @Override
        protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
            return !isRed() || fight.perks().value(player.getUuid(), "BLIGHT_SLAYER") > 0;
        }

        @Override
        public boolean attacksBallista() {
            return true;
        }
    }

    /** Hits so hard players fly, and knocks them off cannons. */
    public static final class Knocker extends KuudraMob {
        public Knocker(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.ZOMBIFIED_PIGLIN, MobType.INFERNAL);
        }

        @Override
        public boolean attacksBallista() {
            return true;
        }

        @Override
        public void onMeleeHit(SkyBlockPlayer target) {
            fight.knockOffCannon(target);
            Vec away = target.getPosition().sub(getPosition()).asVec().withY(0);
            if (away.lengthSquared() < 1e-4) away = new Vec(1, 0, 0);
            double strength = fight.spec().mobs().knockerKnockback();
            target.setVelocity(away.normalize().mul(strength).withY(strength / 3));
        }
    }

    /** From wave 35: very fast, and its kills are final until a Support revives the player. */
    public static final class Chaosmite extends KuudraMob {
        public Chaosmite(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.ENDERMITE, MobType.ARCANE, MobType.ARTHROPOD, MobType.ENDER);
        }
    }
}
