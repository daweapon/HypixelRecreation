package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.mob.ShaftMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobLittlefoot extends ShaftMob {
    private static final String TEXTURE = "6cce41cd32db68f699c281f83f20766c83c97fd49664eb8c7584b9f343d960d1";
    private static final double FLEE_TRIGGER = 14;
    private static final double FLEE_DISTANCE = 12;
    private static final long ENRAGE_MILLIS = 3_000;
    private static final long ATTACK_COOLDOWN_MILLIS = 1_000;
    private static final double ATTACK_RANGE = 2.2;

    private volatile long enrageUntil;
    private volatile boolean enraged;

    public MobLittlefoot() {
        super(EntityType.ZOMBIE);
    }

    @Override
    public void onInit() {
        setHelmet(head(TEXTURE));
    }

    @Override
    public Integer getLevel() {
        return 508;
    }

    @Override
    public String getMobID() {
        return "LITTLEFOOT";
    }

    @Override
    public String getDisplayName() {
        return "Littlefoot";
    }

    @Override
    protected double baseHealth() {
        return 25_000_000;
    }

    @Override
    protected double attackDamage() {
        return 10_000;
    }

    @Override
    protected double baseSpeed() {
        return 320;
    }

    @Override
    protected double coldPerHit() {
        return 3;
    }

    @Override
    protected long powder() {
        return 10_000;
    }

    @Override
    public int getBestiaryBracket() {
        return 2;
    }

    @Override
    public int getMaxBestiaryTier() {
        return 5;
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return new GUIMaterial(TEXTURE);
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.GLACIAL, MobType.HUMANOID, MobType.ELUSIVE);
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        boolean applied = super.damage(damage);
        if (applied && !enraged && damage.getAttacker() instanceof SkyBlockPlayer attacker) {
            enraged = true;
            enrageUntil = System.currentTimeMillis() + ENRAGE_MILLIS;
            attacker.sendMessage("<7><i>Looks like you've angered Littlefoot!");
        }
        return applied;
    }

    @Override
    public SkyBlockItem createDrop(ItemType type, int amount) {
        int count = type == ItemType.ENCHANTED_GLACITE ? ThreadLocalRandom.current().nextInt(16, 33) : amount;
        return super.createDrop(type, count);
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new LittlefootGoal(this));
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of();
    }

    @Override
    public SkyBlockLootTable getLootTable() {
        return new SkyBlockLootTable() {
            @Override
            public @NotNull List<LootRecord> getLootTable() {
                return List.of(
                        new LootRecord(ItemType.ENCHANTED_GLACITE, 16, 100),
                        new LootRecord(ItemType.GLACITE_AMALGAMATION, 1, 2),
                        new LootRecord(ItemType.LITTLEFOOT_FLUFF, 1, 0.2));
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(3_810, 1_270, 1_524);
    }

    private static final class LittlefootGoal extends GoalSelector {
        private final MobLittlefoot mob;
        private long lastAttack;
        private long lastPath;

        LittlefootGoal(MobLittlefoot mob) {
            super(mob);
            this.mob = mob;
        }

        private SkyBlockPlayer nearest(double range) {
            if (mob.getInstance() == null) return null;
            return mob.getInstance().getNearbyEntities(mob.getPosition(), range).stream()
                    .filter(entity -> entity instanceof SkyBlockPlayer player && !player.isDead())
                    .map(entity -> (SkyBlockPlayer) entity)
                    .min(Comparator.comparingDouble(player -> player.getDistanceSquared(mob)))
                    .orElse(null);
        }

        @Override
        public boolean shouldStart() {
            return nearest(mob.enraged ? 64 : FLEE_TRIGGER) != null;
        }

        @Override
        public void start() {
        }

        @Override
        public void tick(long time) {
            SkyBlockPlayer player = nearest(mob.enraged ? 64 : FLEE_TRIGGER);
            if (player == null || time - lastPath < 400) {
                if (player != null && mob.enraged) attack(player, time);
                return;
            }
            lastPath = time;
            if (!mob.enraged) {
                Pos away = mob.getPosition().add(mob.getPosition().sub(player.getPosition()).asVec().normalize().mul(FLEE_DISTANCE));
                mob.getVanillaNavigator().pathTo(away);
            } else if (System.currentTimeMillis() >= mob.enrageUntil) {
                mob.getVanillaNavigator().pathTo(player.getPosition());
                attack(player, time);
            }
        }

        private void attack(SkyBlockPlayer player, long time) {
            if (System.currentTimeMillis() < mob.enrageUntil || time - lastAttack < ATTACK_COOLDOWN_MILLIS) return;
            if (mob.getDistanceSquared(player) > ATTACK_RANGE * ATTACK_RANGE) return;
            lastAttack = time;
            mob.lookAt(player);
            MobAttackService.strike(mob, player);
        }

        @Override
        public boolean shouldEnd() {
            return nearest(mob.enraged ? 64 : FLEE_TRIGGER) == null;
        }

        @Override
        public void end() {
            mob.getVanillaNavigator().stop();
        }
    }
}
