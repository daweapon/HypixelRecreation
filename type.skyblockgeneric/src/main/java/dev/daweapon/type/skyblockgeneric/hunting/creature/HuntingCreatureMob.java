package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import dev.daweapon.type.skyblockgeneric.hunting.creature.special.CreatureSpecial;
import dev.daweapon.type.skyblockgeneric.hunting.creature.special.CreatureSpecials;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.SkullHeadComponent;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;
import lombok.NonNull;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One config-driven mob class for every Galatea and Torrhus creature. Critters ignore ordinary damage and are only
 * taken out by their capture tool; the spec is handed over through a thread local because SkyBlockMob's constructor
 * reads stats before subclass fields exist.
 */
public class HuntingCreatureMob extends BestiaryMob {
    private static final ThreadLocal<CreatureSpec> IN_CONSTRUCTION = new ThreadLocal<>();
    private static final long HINT_COOLDOWN_MS = 2500;

    private final CreatureSpec spec;
    @Getter
    private boolean captured;
    private boolean toolDamage;
    private long lastHint;
    private String header = "";

    private HuntingCreatureMob(CreatureSpec spec) {
        super(spec.entity());
        this.spec = spec;
    }

    public static HuntingCreatureMob create(CreatureSpec spec) {
        IN_CONSTRUCTION.set(spec);
        try {
            return new HuntingCreatureMob(spec);
        } finally {
            IN_CONSTRUCTION.remove();
        }
    }

    public CreatureSpec spec() {
        return live();
    }

    private CreatureSpec live() {
        return spec != null ? spec : IN_CONSTRUCTION.get();
    }

    public java.util.Optional<CreatureSpecial> special() {
        return CreatureSpecials.of(live().special());
    }

    @Override
    public void onSpawn() {
        special().ifPresent(special -> special.onSpawn(this));
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (isDead() || getInstance() == null) return;
        special().ifPresent(special -> special.onTick(this, getAliveTicks()));
    }

    public void setHeader(String text) {
        this.header = text;
        refreshName();
    }

    @Override
    protected String nameHeader() {
        return header == null ? "" : header;
    }

    public boolean isCharmable() {
        return !captured && live().captures(CreatureSpec.Capture.CHARM);
    }

    @Override
    protected Damage filterIncomingDamage(Damage damage) {
        CreatureSpec spec = live();
        if (damage.getAttacker() instanceof SkyBlockPlayer hitter && !toolDamage
                && special().map(special -> special.onPlayerHit(hitter, this)).orElse(false)) return null;
        if (!(damage.getAttacker() instanceof SkyBlockPlayer player)) return toolDamage ? damage : (spec.isCritter() ? null : damage);
        if (toolDamage) return damage;
        Huntaxes.Tier huntaxe = Huntaxes.heldBy(player);
        if (huntaxe != null) return huntaxeDamage(damage, huntaxe, player);
        if (!spec.isCritter()) return damage;
        hint(player, "<c>This creature can't be hurt. Use a hunting tool on it!");
        return null;
    }

    private Damage huntaxeDamage(Damage damage, Huntaxes.Tier tier, SkyBlockPlayer player) {
        double floor = getMaxHealth() * Huntaxes.HEALTH_FLOOR;
        double room = getHealth() - floor;
        if (room <= 0.5) {
            hint(player, "<7>It is weak enough. Use a <5>Pocket Black Hole<7> to capture it!");
            return null;
        }
        double step = getMaxHealth() * tier.capPercent() / 100D;
        return new Damage(damage.getType(), damage.getSource(), damage.getAttacker(), damage.getSourcePosition(),
                (float) Math.min(step, room));
    }

    private void hint(SkyBlockPlayer player, String message) {
        long now = System.currentTimeMillis();
        if (now - lastHint < HINT_COOLDOWN_MS) return;
        lastHint = now;
        player.sendMessage(message);
    }

    public void applyToolDamage(SkyBlockPlayer player, double amount) {
        toolDamage = true;
        try {
            damage(new Damage(net.minestom.server.entity.damage.DamageType.PLAYER_ATTACK, player, player, player.getPosition(), (float) amount));
        } finally {
            toolDamage = false;
        }
    }

    public void capture(SkyBlockPlayer player) {
        if (captured || isDead() || isRemoved()) return;
        captured = true;
        CreatureCapture.award(player, this);
        fireTriggers(player, "hunt");
        if (live().isCritter() && "GALATEA".equals(live().island()) && dev.daweapon.type.skyblockgeneric.hotf.HotfService.hasPerk(player, "monster_hunter")) {
            dev.daweapon.type.skyblockgeneric.hotf.HotfService.addWhispers(player, dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency.FOREST, 25);
            player.sendMessage("<3>Monster Hunter <7>gave you <3>+25 Forest Whispers<7>.");
        }
        obliterate(player);
    }

    public boolean atHuntFloor() {
        return getHealth() <= getMaxHealth() * Huntaxes.HEALTH_FLOOR + 0.5;
    }

    @Override
    public String getDisplayName() {
        return live().name();
    }

    @Override
    public Integer getLevel() {
        return live().level();
    }

    @Override
    public String getMobID() {
        return live().id();
    }

    @Override
    public int getMaxBestiaryTier() {
        return live().bestiary().maxTier();
    }

    @Override
    public int getBestiaryBracket() {
        return live().bestiary().bracketNumber();
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return iconOf(live());
    }

    public static GUIMaterial iconOf(CreatureSpec spec) {
        if (spec.shard() != null) {
            ItemType type = ItemType.get("SHARD_" + spec.shard().toUpperCase().replaceAll("[^A-Z0-9]+", "_"));
            if (type != null) {
                SkyBlockItem item = new SkyBlockItem(type);
                if (item.hasComponent(SkullHeadComponent.class)) {
                    return new GUIMaterial(item.getComponent(SkullHeadComponent.class).getSkullTexture(item));
                }
            }
        }
        return new GUIMaterial(Material.PRISMARINE_SHARD);
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        if (live().behavior() == CreatureSpec.Behavior.FLY) return List.of(new FlyWanderGoal(this));
        return behaviour(live()).goals(this);
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return behaviour(live()).targets(this);
    }

    private static SeaCreatureBehaviour behaviour(CreatureSpec spec) {
        return switch (spec.behavior()) {
            case AGGRESSIVE -> SeaCreatureBehaviour.aggressive(1.5, 20, 12);
            case STATIONARY -> SeaCreatureBehaviour.of(List.of(), List.of());
            default -> SeaCreatureBehaviour.passive(8);
        };
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        CreatureSpec spec = live();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, spec.health())
                .withBase(ItemStatistic.DAMAGE, spec.damage())
                .withBase(ItemStatistic.SPEED, spec.speed())
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        List<SkyBlockLootTable.LootRecord> records = live().drops().stream()
                .filter(drop -> ItemType.get(drop.item()) != null)
                .map(drop -> new SkyBlockLootTable.LootRecord(ItemType.get(drop.item()), drop.min(), drop.chance()))
                .toList();
        if (records.isEmpty()) return null;
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return records;
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    @Override
    public SkyBlockItem createDrop(ItemType type, int amount) {
        for (CreatureSpec.Drop drop : live().drops()) {
            if (ItemType.get(drop.item()) == type && drop.max() > drop.min()) {
                amount += ThreadLocalRandom.current().nextInt(drop.max() - drop.min() + 1);
                break;
            }
        }
        SkyBlockItem drop = super.createDrop(type, amount);
        if (type == ItemType.ENCHANTED_BOOK) {
            for (CreatureSpec.Drop row : live().drops()) {
                if (row.enchant() == null || ItemType.get(row.item()) != type) continue;
                drop.getAttributeHandler().addEnchantment(new dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment(
                        dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType.valueOf(row.enchant()), row.level()));
                break;
            }
        }
        return drop;
    }

    @Override
    public SkillCategories getSkillCategory() {
        CreatureSpec.Xp xp = live().xp();
        if (xp.hunting() > 0) return SkillCategories.HUNTING;
        if (xp.farming() > 0) return SkillCategories.FARMING;
        if (xp.fishing() > 0) return SkillCategories.FISHING;
        return SkillCategories.COMBAT;
    }

    @Override
    public long damageCooldown() {
        return 400;
    }

    @Override
    public OtherLoot getOtherLoot() {
        CreatureSpec spec = live();
        CreatureSpec.Xp xp = spec.xp();
        double amount = Math.max(Math.max(xp.hunting(), xp.combat()), Math.max(xp.farming(), xp.fishing()));
        return new OtherLoot((long) amount, spec.coins(), spec.orbs());
    }

    @Override
    public List<MobType> getMobTypes() {
        return live().mobTypes().isEmpty() ? List.of(MobType.CRITTER) : live().mobTypes();
    }

    private void fireTriggers(SkyBlockPlayer player, String verb) {
        CreatureSpec spec = live();
        java.util.Set<String> keys = new java.util.LinkedHashSet<>();
        keys.add(spec.id());
        if (spec.bestiary().family() != null) keys.add(spec.bestiary().family());
        if (spec.shard() != null) keys.add(spec.shard());
        for (String key : keys) {
            dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers.fire(player, verb + "." + key.toLowerCase().replaceAll("[^a-z0-9]+", "_"));
        }
    }

    @Override
    public void kill() {
        super.kill();
        if (!captured && getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer killer) {
            fireTriggers(killer, "kill");
        }
        for (CreatureSpec.Message message : live().messages()) {
            if (!"kill".equals(message.when())) continue;
            if (getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player) {
                player.sendMessage(Text.parse(message.text()));
            }
        }
    }
}
