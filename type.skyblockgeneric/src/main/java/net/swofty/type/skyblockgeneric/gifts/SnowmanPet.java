package net.swofty.type.skyblockgeneric.gifts;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Snowman pet's abilities: Blizzard slows nearby enemies by 25% and weakens their attacks,
 * Frostbite cuts the damage you take, Snow Cannon fires a snowball for a share of each melee hit,
 * and a Mythic Snowman gives every snowball a 50% chance to deal double damage.
 */
public final class SnowmanPet {
    public static final double BLIZZARD_SLOW = 0.25;
    public static final double SNOW_CANNON_CAP = 200_000;
    public static final double SNOW_CANNON_RANGE = 8;
    public static final double OUCH_CHANCE = 0.5;
    private static final long SNOW_CANNON_COOLDOWN_MS = 1_000;
    private static final double SNOW_CANNON_SPEED = 1.6;
    private static final Key BLIZZARD_MODIFIER = Key.key("skyblock", "snowman_blizzard");
    private static final Set<SkyBlockMob> slowed = new HashSet<>();

    private SnowmanPet() {
    }

    public static double reduced(double damage, double percent) {
        return damage * (1 - Math.max(0, Math.min(100, percent)) / 100D);
    }

    public static double snowCannonDamage(double meleeDamage, double percent) {
        return Math.min(SNOW_CANNON_CAP, Math.max(0, meleeDamage) * percent / 100D);
    }

    public static void blizzard(PetEvent event, double[] values, AbilityRuntime runtime) {
        if (!(event instanceof PetEvent.DamagedByMob hit)) return;
        if (hit.mob().getPosition().distance(hit.player().getPosition()) > values[0]) return;
        hit.damage(reduced(hit.damage(), values[1]));
    }

    public static void snowCannon(PetEvent event, double[] values, AbilityRuntime runtime) {
        if (!(event instanceof PetEvent.MeleeHit hit)) return;
        SkyBlockPlayer player = hit.player();
        SkyBlockMob target = hit.mob();
        if (target.isDead() || target.isRemoved()) return;
        if (target.getPosition().distance(player.getPosition()) > SNOW_CANNON_RANGE) return;
        long now = System.currentTimeMillis();
        if (now - runtime.getLastProc() < SNOW_CANNON_COOLDOWN_MS) return;
        runtime.setLastProc(now);

        double damage = snowCannonDamage(hit.damage(), values[0]);
        if (damage <= 0) return;
        Pos from = player.getPosition().add(0, player.getEyeHeight(), 0);
        Pos aim = target.getPosition().add(0, target.getBoundingBox().height() / 2, 0);
        SnowballProjectile.shoot(player, from, aim.sub(from).asVec(), SNOW_CANNON_SPEED, mob ->
                AttackService.applyHit(player, mob, (float) (damage * snowballMultiplier(player)), false));
    }

    public static double snowballMultiplier(SkyBlockPlayer player) {
        return isMythicSnowman(player) && ThreadLocalRandom.current().nextDouble() < OUCH_CHANCE ? 2 : 1;
    }

    public static void startBlizzard() {
        MinecraftServer.getSchedulerManager().buildTask(SnowmanPet::tickBlizzard)
                .repeat(TaskSchedule.tick(10))
                .schedule();
    }

    private static void tickBlizzard() {
        Set<SkyBlockMob> inRange = new HashSet<>();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            Instance instance = player.getInstance();
            double range = PetAbilities.value(player, ItemType.SNOWMAN_PET, "Blizzard", 0);
            if (range <= 0 || instance == null) continue;
            for (Entity entity : instance.getNearbyEntities(player.getPosition(), range)) {
                if (entity instanceof SkyBlockMob mob && !mob.isDead()) inRange.add(mob);
            }
        }
        for (SkyBlockMob mob : inRange) {
            if (slowed.add(mob)) {
                mob.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(
                        new AttributeModifier(BLIZZARD_MODIFIER, -BLIZZARD_SLOW, AttributeOperation.ADD_MULTIPLIED_TOTAL));
            }
        }
        slowed.removeIf(mob -> {
            if (inRange.contains(mob)) return false;
            if (!mob.isRemoved()) mob.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(BLIZZARD_MODIFIER);
            return true;
        });
    }

    private static boolean isMythicSnowman(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler() != null && PetAbilities.has(player, ItemType.SNOWMAN_PET, "Ouch!");
    }
}
