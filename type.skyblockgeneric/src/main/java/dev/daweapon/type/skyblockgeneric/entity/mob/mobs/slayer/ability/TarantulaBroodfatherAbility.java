package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import java.util.ArrayList;
import java.util.List;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerMinionMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class TarantulaBroodfatherAbility extends SlayerAbilitySupport {
    private boolean web66;
    private boolean web33;
    private boolean barrage;
    private boolean conjoined;
    private final List<SlayerMinionMob> bats = new ArrayList<>();
    private final List<SlayerMinionMob> brood = new ArrayList<>();

    private static final int EGG_HATCH_TICKS = 200;
    private static final int BAT_WAVE_TICKS = 600;
    private static final double BAT_REACH = 6D;

    @Override
    public void onSpawn(SlayerBossMob boss) {
        repeating(boss, 80, 120, () -> {
            SkyBlockPlayer owner = owner(boss);
            if (owner == null) {
                return;
            }
            Pos behind = owner.getPosition().sub(owner.getPosition().direction().mul(2)).withY(owner.getPosition().y());
            boss.teleport(behind);
            owner.sendMessage("<5>Backstab!");
        });

        repeating(boss, 20, 20, () -> {
            int tier = boss.getProfile().tier().tier().number();
            if (tier >= 3 && !web66 && healthRatio(boss) <= 0.66D) {
                web66 = true;
                webOfLies(boss);
            }
            if (tier >= 3 && !web33 && healthRatio(boss) <= 0.33D) {
                web33 = true;
                webOfLies(boss);
            }
            if (tier >= 4 && !barrage && healthRatio(boss) <= (tier == 4 ? 0.50D : 0.66D)) {
                barrage = true;
                batBarrage(boss);
            }
        });
    }

    @Override
    public float modifyIncomingDamage(SlayerBossMob boss, Damage damage, float amount) {
        if (boss.getProfile().tier().tier().number() >= 5 && !conjoined && amount >= boss.getHealth()) {
            conjoined = true;
            boss.setMaxHealth((float) (boss.getProfile().tier().bossHealth() * 2D));
            boss.setHealth((float) boss.getProfile().tier().bossHealth());
            nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<5>Til Death Do Us Part! <d>The Conjoined Brood awakens!"));
            return 0F;
        }
        return amount;
    }

    @Override
    public void onMeleeHit(SlayerBossMob boss, SkyBlockPlayer target) {
        int tier = boss.getProfile().tier().tier().number();
        if (tier >= 2) {
            trueDamage(boss, target, tier * 25D, "Noxious Paralysis");
        }
    }

    private void webOfLies(SlayerBossMob boss) {
        nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<5>Web of Lies! <d>Destroy the egg sacs!"));
        heal(boss, boss.getProfile().tier().bossHealth() * 0.05D);
        int tier = boss.getProfile().tier().tier().number();
        int sacs = PrimordialBrood.eggSacs(tier);
        for (int i = 0; i < sacs; i++) {
            double angle = Math.PI * 2 * i / sacs;
            Pos pos = boss.getPosition().add(Math.cos(angle) * 1.5, 3, Math.sin(angle) * 1.5);
            SlayerMinionMob sac = spawnMinion(boss, PrimordialBrood.eggSac(tier, boss.getLevel()), pos, 0);
            if (sac == null) continue;
            brood.add(sac);
            sac.setNoGravity(true);
            delayed(boss, EGG_HATCH_TICKS, () -> hatch(boss, sac, tier));
        }
        particles(boss, Particle.ITEM_COBWEB, 1.5f, 40);
    }

    private void hatch(SlayerBossMob boss, SlayerMinionMob sac, int tier) {
        if (sac.isRemoved() || sac.isDead()) return;
        Pos at = sac.getPosition();
        sac.remove();
        SlayerMinionMob hatchling = spawnMinion(boss, PrimordialBrood.hatchling(tier), at, 0);
        if (hatchling != null) brood.add(hatchling);
    }

    @Override
    public void onDeath(SlayerBossMob boss) {
        bats.forEach(bat -> {
            if (!bat.isRemoved()) bat.remove();
        });
        brood.forEach(minion -> {
            if (!minion.isRemoved()) minion.remove();
        });
        bats.clear();
        brood.clear();
    }

    private void batBarrage(SlayerBossMob boss) {
        int tier = boss.getProfile().tier().tier().number();
        repeating(boss, 1, BAT_WAVE_TICKS, () -> spawnBats(boss, tier));
        repeating(boss, 20, 20, () -> {
            double fraction = PrimordialBrood.batDamageFraction(tier);
            for (SlayerMinionMob bat : List.copyOf(bats)) {
                if (bat.isRemoved() || bat.isDead()) {
                    bats.remove(bat);
                    continue;
                }
                nearbyPlayers(boss, 32).stream()
                    .filter(player -> player.getPosition().distance(bat.getPosition()) <= BAT_REACH)
                    .forEach(player -> trueDamage(boss, player, player.getMaxHealth() * fraction, "Bat Barrage", false));
            }
        });
    }

    private void spawnBats(SlayerBossMob boss, int tier) {
        nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<5>Bat Barrage!"));
        int count = tier >= 5 ? 6 : 4;
        for (int i = 0; i < count; i++) {
            SlayerMinionMob bat = spawnMinion(boss, PrimordialBrood.bat(tier),
                boss.getPosition().add(i - 2, 1, i % 2 == 0 ? 2 : -2), BAT_WAVE_TICKS);
            if (bat != null) bats.add(bat);
        }
    }
}
