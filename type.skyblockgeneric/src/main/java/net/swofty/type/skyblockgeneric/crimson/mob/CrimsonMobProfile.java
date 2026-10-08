package net.swofty.type.skyblockgeneric.crimson.mob;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Everything the wiki publishes about one Crimson Isle mob: the 2026 level, health, damage, Combat XP,
 * coins and orbs, its Bestiary bracket and tier cap (0 when it isn't in the Bestiary), its drops, and the
 * faction whose reputation a kill costs.
 */
public record CrimsonMobProfile(
        String id,
        String displayName,
        int level,
        EntityType entityType,
        double health,
        double damage,
        double defense,
        double speed,
        long combatXp,
        int coins,
        int orbs,
        List<MobType> mobTypes,
        int bracket,
        int maxTier,
        GUIMaterial guiMaterial,
        List<MobDrop> drops,
        @Nullable CrimsonFaction faction,
        int reputationLoss,
        boolean corruptible
) {

    public ItemStatistics statistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, health)
                .withBase(ItemStatistic.DAMAGE, damage)
                .withBase(ItemStatistic.DEFENSE, defense)
                .withBase(ItemStatistic.SPEED, speed)
                .build();
    }

    public OtherLoot otherLoot() {
        return new OtherLoot(combatXp, coins, orbs);
    }

    public boolean inBestiary() {
        return maxTier > 0;
    }

    public static Builder builder(String id, String displayName) {
        return new Builder(id, displayName);
    }

    public static final class Builder {
        private final String id;
        private final String displayName;
        private int level = 1;
        private EntityType entityType = EntityType.ZOMBIE;
        private double health = 100;
        private double damage;
        private double defense;
        private double speed = 100;
        private long combatXp;
        private int coins;
        private int orbs;
        private List<MobType> mobTypes = List.of(MobType.INFERNAL);
        private int bracket = 1;
        private int maxTier;
        private GUIMaterial guiMaterial = new GUIMaterial(net.minestom.server.item.Material.NETHERRACK);
        private List<MobDrop> drops = List.of();
        private CrimsonFaction faction;
        private int reputationLoss;
        private boolean corruptible = true;

        private Builder(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public Builder level(int level) { this.level = level; return this; }
        public Builder entity(EntityType entityType) { this.entityType = entityType; return this; }
        public Builder stats(double health, double damage) { this.health = health; this.damage = damage; return this; }
        public Builder defense(double defense) { this.defense = defense; return this; }
        public Builder speed(double speed) { this.speed = speed; return this; }
        public Builder rewards(long combatXp, int coins, int orbs) {
            this.combatXp = combatXp;
            this.coins = coins;
            this.orbs = orbs;
            return this;
        }
        public Builder types(MobType... mobTypes) { this.mobTypes = List.of(mobTypes); return this; }
        public Builder bestiary(int bracket, int maxTier, String headTexture) {
            this.bracket = bracket;
            this.maxTier = maxTier;
            this.guiMaterial = new GUIMaterial(headTexture);
            return this;
        }
        public Builder drops(MobDrop... drops) { this.drops = List.of(drops); return this; }
        public Builder faction(CrimsonFaction faction, int reputationLoss) {
            this.faction = faction;
            this.reputationLoss = reputationLoss;
            return this;
        }
        public Builder neverCorrupted() { this.corruptible = false; return this; }

        public CrimsonMobProfile build() {
            return new CrimsonMobProfile(id, displayName, level, entityType, health, damage, defense, speed, combatXp,
                    coins, orbs, mobTypes, bracket, maxTier, guiMaterial, drops, faction, reputationLoss, corruptible);
        }
    }
}
