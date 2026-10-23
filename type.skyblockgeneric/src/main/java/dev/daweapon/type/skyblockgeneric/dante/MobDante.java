package dev.daweapon.type.skyblockgeneric.dante;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.cube.SlimeMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.DamageIndicator;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class MobDante extends CrimsonIsleMob {
    private static final int SIZE = 12;
    private static final double AGGRO_RANGE = 80;
    private static final int HOP_INTERVAL = 40;
    private static final double HOP_SPEED = 10;
    private static final double HOP_LIFT = 8;
    private static final int STRIKE_INTERVAL = 50;
    private static final double STRIKE_DAMAGE = 1_500;
    private static final double STRIKE_REACH = 3;
    private static final int VOLLEY_INTERVAL = 120;
    private static final int VOLLEY_TARGETS = 3;
    private static final double VOLLEY_DAMAGE = 900;
    private static final int SUMMON_INTERVAL = 400;
    private static final int SUMMON_COUNT = 3;
    private static final int MAX_SLIME_GOONS = 8;
    private static final int SPEECH_INTERVAL = 300;
    private static final long WARN_COOLDOWN_MILLIS = 3000;

    private final Map<UUID, String> names = new ConcurrentHashMap<>();
    private final List<MobDanteSlimeGoon> slimeGoons = new ArrayList<>();
    private boolean staffStrike;
    private boolean defeated;
    private long lastWarning;

    public MobDante() {
        super(DanteProfiles.DANTE);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(SlimeMeta.class, meta -> meta.setSize(SIZE));
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    public long damageCooldown() {
        return 50;
    }

    @Override
    public void takeKnockback(float strength, double x, double z) {
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        if (staffStrike) return true;
        long now = System.currentTimeMillis();
        if (now - lastWarning > WARN_COOLDOWN_MILLIS) {
            lastWarning = now;
            player.sendMessage("<c>Only the <6>Staff of the Rising Sun <c>can hurt Dante!");
        }
        return false;
    }

    public Map<UUID, String> names() {
        return names;
    }

    public void rayOfHope(SkyBlockPlayer player, float amount) {
        if (isDead() || isRemoved() || getInstance() == null) return;
        names.put(player.getUuid(), player.getUsername());
        staffStrike = true;
        try {
            damage(new Damage(DamageType.MAGIC, player, player, player.getPosition(), amount));
        } finally {
            staffStrike = false;
        }
        new DamageIndicator().damage(amount).pos(getPosition().add(0, getBoundingBox().height() / 2, 0))
                .critical(false).display(getInstance());
    }

    @Override
    protected void onAiTick(int ticks) {
        List<SkyBlockPlayer> fighters = playersWithin(AGGRO_RANGE);
        if (fighters.isEmpty()) return;
        if (ticks % HOP_INTERVAL == 0 && isOnGround()) {
            SkyBlockPlayer target = fighters.stream()
                    .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(getPosition())))
                    .orElseThrow();
            SlimeMovement.hop(this, target, HOP_SPEED, HOP_LIFT);
        }
        if (ticks % STRIKE_INTERVAL == 0) damageStrike(fighters);
        if (ticks % VOLLEY_INTERVAL == 0) slimeVolley(fighters);
        if (ticks % SUMMON_INTERVAL == 0) summonSlimeGoons();
        if (ticks % SPEECH_INTERVAL == 0) DanteUprising.speakAsSlime(getInstance());
    }

    private void damageStrike(List<SkyBlockPlayer> fighters) {
        double reach = getBoundingBox().width() / 2 + STRIKE_REACH;
        for (SkyBlockPlayer player : fighters) {
            if (player.getPosition().distance(getPosition()) > reach) continue;
            float dealt = MobAttackService.strike(this, player, STRIKE_DAMAGE * ResistanceArmorSet.damageTakenMultiplier(player));
            player.sendMessage("<c><l>Dante</l><c> struck you for " + StringUtility.commaify(dealt) + " damage!");
        }
    }

    private void slimeVolley(List<SkyBlockPlayer> fighters) {
        List<SkyBlockPlayer> shuffled = new ArrayList<>(fighters);
        Collections.shuffle(shuffled);
        for (SkyBlockPlayer target : shuffled.subList(0, Math.min(VOLLEY_TARGETS, shuffled.size()))) {
            MobProjectile.item(this, target, ItemStack.of(Material.SLIME_BALL), 0.9, hit -> {
                float dealt = MobAttackService.strike(this, hit, VOLLEY_DAMAGE * ResistanceArmorSet.damageTakenMultiplier(hit));
                hit.sendMessage("<c><l>Dante</l><c> hit you with <a><l>Slime Volley</l><c> for " + StringUtility.commaify(dealt) + " damage!");
            });
        }
    }

    private void summonSlimeGoons() {
        slimeGoons.removeIf(goon -> goon.isDead() || goon.isRemoved());
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < SUMMON_COUNT && slimeGoons.size() < MAX_SLIME_GOONS; i++) {
            MobDanteSlimeGoon goon = new MobDanteSlimeGoon();
            Pos at = getPosition().add((random.nextDouble() - 0.5) * 14, 1, (random.nextDouble() - 0.5) * 14);
            goon.setInstance(getInstance(), at);
            slimeGoons.add(goon);
        }
    }

    public void despawnSlimeGoons() {
        slimeGoons.forEach(goon -> {
            if (!goon.isRemoved()) goon.remove();
        });
        slimeGoons.clear();
    }

    @Override
    public void kill() {
        super.kill();
        if (defeated) return;
        defeated = true;
        despawnSlimeGoons();
        DanteUprising.onDanteDown(this);
    }

    @Override
    public void remove() {
        despawnSlimeGoons();
        super.remove();
    }
}
