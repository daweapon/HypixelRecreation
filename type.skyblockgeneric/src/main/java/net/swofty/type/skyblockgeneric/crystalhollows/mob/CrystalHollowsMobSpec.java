package net.swofty.type.skyblockgeneric.crystalhollows.mob;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.ArrayList;
import java.util.List;

public final class CrystalHollowsMobSpec {
    public record Drop(ItemType item, int min, int max, double chance) {
        public static Drop always(ItemType item, int min, int max) {
            return new Drop(item, min, max, 100);
        }

        public static Drop rolled(ItemType item, int min, int max, double chance) {
            return new Drop(item, min, max, chance);
        }

        public static Drop single(ItemType item, double chance) {
            return new Drop(item, 1, 1, chance);
        }
    }

    private final String id;
    private final String name;
    private final int level;
    private final double health;
    private final double damage;
    private double speed = 100D;
    private int bracket = 1;
    private int maxTier = 10;
    private GUIMaterial icon = new GUIMaterial(Material.STONE);
    private long combatXp;
    private int coins;
    private int orbs;
    private boolean aggressive = true;
    private int aggroRange = 6;
    private int strollRadius = 12;
    private long damageCooldown = 500;
    private List<MobType> types = List.of(MobType.HUMANOID);
    private final List<Drop> drops = new ArrayList<>();

    private CrystalHollowsMobSpec(String id, String name, int level, double health, double damage) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.health = health;
        this.damage = damage;
    }

    public static CrystalHollowsMobSpec of(String id, String name, int level, double health, double damage) {
        return new CrystalHollowsMobSpec(id, name, level, health, damage);
    }

    public CrystalHollowsMobSpec speed(double speed) {
        this.speed = speed;
        return this;
    }

    public CrystalHollowsMobSpec bestiary(int bracket, int maxTier, Material icon) {
        this.bracket = bracket;
        this.maxTier = maxTier;
        this.icon = new GUIMaterial(icon);
        return this;
    }

    public CrystalHollowsMobSpec rewards(long combatXp, int coins, int orbs) {
        this.combatXp = combatXp;
        this.coins = coins;
        this.orbs = orbs;
        return this;
    }

    public CrystalHollowsMobSpec types(MobType... types) {
        this.types = List.of(types);
        return this;
    }

    public CrystalHollowsMobSpec passive() {
        this.aggressive = false;
        return this;
    }

    public CrystalHollowsMobSpec provokedOnly() {
        this.aggroRange = 0;
        return this;
    }

    public CrystalHollowsMobSpec aggroRange(int aggroRange) {
        this.aggroRange = aggroRange;
        return this;
    }

    public CrystalHollowsMobSpec damageCooldown(long damageCooldown) {
        this.damageCooldown = damageCooldown;
        return this;
    }

    public CrystalHollowsMobSpec drops(Drop... drops) {
        this.drops.addAll(List.of(drops));
        return this;
    }

    public String id() { return id; }
    public String name() { return name; }
    public int level() { return level; }
    public double health() { return health; }
    public double damage() { return damage; }
    public double speed() { return speed; }
    public int bracket() { return bracket; }
    public int maxTier() { return maxTier; }
    public GUIMaterial icon() { return icon; }
    public long combatXp() { return combatXp; }
    public int coins() { return coins; }
    public int orbs() { return orbs; }
    public boolean aggressive() { return aggressive; }
    public int aggroRange() { return aggroRange; }
    public int strollRadius() { return strollRadius; }
    public long damageCooldown() { return damageCooldown; }
    public List<MobType> types() { return types; }
    public List<Drop> drops() { return List.copyOf(drops); }
}
