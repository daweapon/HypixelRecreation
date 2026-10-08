package net.swofty.type.skyblockgeneric.mythological.ability;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.MobAttackService;

/**
 * Melee hits add one Bleed and thrown axes (every 1.5 seconds) add three; Bleed strikes for 75
 * times the count every two seconds and slows healing. At 27 Bleed the Minotaur runs away and its
 * axes stop landing until the Bleed wears off.
 */
final class MinotaurAbility implements MythologicalAbility {
    private static final double DAMAGE_PER_BLEED = 75;
    private static final int FLEE_AT_BLEED = 27;
    private static final int AXE_INTERVAL_TICKS = 30;
    private static final double AXE_SPEED = 0.5;
    private static final double MIN_AXE_DISTANCE = 2.5;
    private static final double MAX_AXE_DISTANCE = 16;
    private static final int FLEE_REPATH_TICKS = 10;
    private static final double FLEE_DISTANCE = 10;

    private final Bleed bleed = new Bleed("Minotaur Bleed", DAMAGE_PER_BLEED);
    private boolean fleeing;

    @Override
    public void onMeleeHit(MythologicalCreatureMob mob, SkyBlockPlayer target) {
        bleed.add(target, 1);
        mob.refreshName();
    }

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        bleed.tick(mob, ticksAlive);
        int highest = bleed.highest();
        if (!fleeing && highest >= FLEE_AT_BLEED) {
            fleeing = true;
            mob.setAiPaused(true);
        } else if (fleeing && highest == 0) {
            fleeing = false;
            mob.setAiPaused(false);
        }
        if (ticksAlive % 20 == 0) mob.refreshName();

        SkyBlockPlayer target = MythologicalCombat.target(mob).orElse(null);
        if (target == null) return;
        double distance = target.getPosition().distance(mob.getPosition());
        if (fleeing && ticksAlive % FLEE_REPATH_TICKS == 0) {
            Pos away = mob.getPosition().add(MythologicalCombat.away(target, mob, FLEE_DISTANCE, 0));
            mob.getVanillaNavigator().pathTo(away);
        }
        if (ticksAlive % AXE_INTERVAL_TICKS == 0 && distance >= MIN_AXE_DISTANCE && distance <= MAX_AXE_DISTANCE) {
            MythologicalCombat.sound(mob, "entity.witch.throw", 1f, 0.7f);
            MobProjectile.item(mob, target, ItemStack.of(Material.IRON_AXE), AXE_SPEED, player -> {
                if (fleeing) return;
                MobAttackService.strike(mob, player);
                bleed.add(player, 3);
                mob.refreshName();
            });
        }
    }

    @Override
    public String nameHeader(MythologicalCreatureMob mob) {
        int highest = bleed.highest();
        return highest > 0 ? Bleed.label(highest) : "";
    }

    @Override
    public void cleanup(MythologicalCreatureMob mob) {
        bleed.clearAll();
    }
}
