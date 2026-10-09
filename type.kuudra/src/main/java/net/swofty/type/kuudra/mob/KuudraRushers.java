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

    static void dress(KuudraMob mob, Color color, Material helmet, Material hand) {
        mob.setEquipment(EquipmentSlot.HELMET, ItemStack.of(helmet));
        mob.setEquipment(EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, color));
        mob.setEquipment(EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, color));
        mob.setEquipment(EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, color));
        if (hand != null) mob.setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(hand));
    }

    /** Rushes players with rapid attacks; Kuudra Follower Artifact/Relic owners take less from it (handled by gear). */
    public static final class Follower extends KuudraMob {
        public Follower(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.ZOMBIE, MobType.UNDEAD, MobType.INFERNAL);
            dress(this, new Color(90, 10, 10), Material.WITHER_SKELETON_SKULL, Material.STONE_SWORD);
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
            super(fight, spec, EntityType.HUSK, MobType.INFERNAL);
            setEquipment(EquipmentSlot.HELMET, ItemStack.of(Material.TNT));
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
            setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.GOLDEN_AXE));
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
            setEquipment(EquipmentSlot.HELMET, ItemStack.of(Material.TNT));
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

        private static final Color[] COLORS = {new Color(60, 200, 60), new Color(230, 210, 40), new Color(200, 30, 30)};
        private Phase phase = Phase.GREEN;

        public Blight(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.ZOMBIE, MobType.UNDEAD);
            dress(this, COLORS[0], Material.ZOMBIE_HEAD, null);
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
            setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.IRON_SHOVEL));
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
