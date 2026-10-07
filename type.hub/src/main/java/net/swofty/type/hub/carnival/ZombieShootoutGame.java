package net.swofty.type.hub.carnival;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.carnival.CarnivalGame;
import net.swofty.type.skyblockgeneric.carnival.CarnivalGameResult;
import net.swofty.type.skyblockgeneric.carnival.games.ZombieShootoutScore;
import net.swofty.type.skyblockgeneric.carnival.games.ZombieShootoutScore.ZombieTier;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Zombie Shootout west of the Carnival Cowboy: zombies pace two platforms, redstone lamps light up
 * now and then, and the Carnival Dart Tube shoots whatever the player is aiming at.
 */
public class ZombieShootoutGame extends CarnivalGameBase implements StationaryCarnivalGame {
    private static final Pos STATION = new Pos(-106.5, 71, 34.5, 90, 0);
    private static final double NEAR_X = -114.5, NEAR_Y = 71;
    private static final double FAR_X = -120.5, FAR_Y = 73;
    private static final double MIN_Z = 28.5, MAX_Z = 40.5;
    private static final List<BlockVec> LAMPS = List.of(
            new BlockVec(-124, 75, 27), new BlockVec(-124, 75, 31), new BlockVec(-124, 75, 35),
            new BlockVec(-124, 75, 39), new BlockVec(-124, 75, 43));
    private static final int SPAWN_INTERVAL_TICKS = 30;
    private static final double ADULT_SPEED = 0.1, BABY_SPEED = 0.18;
    private static final double RANGE = 40;
    private static final long SHOT_COOLDOWN_TICKS = 5;

    private final ZombieShootoutScore score = new ZombieShootoutScore();
    private final List<Target> zombies = new ArrayList<>();
    private final Map<BlockVec, Long> litUntil = new HashMap<>();
    private long nextLampAt = randomLampDelay();
    private long lastShot = -SHOT_COOLDOWN_TICKS;

    public ZombieShootoutGame(SkyBlockPlayer player) {
        super(player, CarnivalGame.ZOMBIE_SHOOTOUT, ZombieShootoutScore.SECONDS);
    }

    @Override
    public Pos station() {
        return STATION;
    }

    private final class Target {
        final ZombieTier tier;
        final boolean baby;
        final LivingEntity entity;
        final long despawnAt;
        double direction = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;

        Target(ZombieTier tier, boolean baby, Pos spawn) {
            this.tier = tier;
            this.baby = baby;
            this.despawnAt = tick + tier.getLifetimeSeconds() * 20L;
            this.entity = new LivingEntity(EntityType.ZOMBIE);
            entity.setNoGravity(true);
            entity.setAutoViewable(false);
            entity.editEntityMeta(ZombieMeta.class, meta -> meta.setBaby(baby));
            entity.setEquipment(EquipmentSlot.HELMET, ItemStack.of(tier.getHelmet()));
            entity.setEquipment(EquipmentSlot.CHESTPLATE, ItemStack.of(tier.getChestplate()));
            entity.setCustomNameVisible(true);
            entity.setInstance(player.getInstance(), spawn).thenRun(() -> entity.addViewer(player));
        }

        void walk() {
            Pos at = entity.getPosition();
            double speed = baby ? BABY_SPEED : ADULT_SPEED;
            double z = at.z() + direction * speed;
            if (z > MAX_Z || z < MIN_Z) {
                direction = -direction;
                z = Math.max(MIN_Z, Math.min(MAX_Z, z));
            }
            entity.teleport(new Pos(at.x(), at.y(), z, direction > 0 ? 0 : 180, 0));
            long left = (despawnAt - tick + 19) / 20;
            entity.setCustomName(Text.of((left <= 2 ? "<c>" : "<e>") + left + "s").asComponent());
        }
    }

    @Override
    protected void onStart() {
        player.teleport(STATION);
        for (int z = (int) Math.floor(MIN_Z) - 1; z <= (int) Math.floor(MAX_Z) + 1; z++) {
            showBlock(new BlockVec(-121, 72, z), Block.OAK_PLANKS);
            showBlock(new BlockVec(-120, 72, z), Block.OAK_PLANKS);
        }
        for (BlockVec lamp : LAMPS) showBlock(lamp, Block.REDSTONE_LAMP);
        player.sendMessage("<c>Zombie Shootout <7>has started! <e>Right-click <7>with your <a>Dart Tube <7>to shoot "
                + "<c>Zombies <7>and lit <c>Redstone Lamps<7>!");
    }

    @Override
    protected void onTick() {
        for (Target zombie : List.copyOf(zombies)) {
            if (tick >= zombie.despawnAt) remove(zombie);
            else zombie.walk();
        }
        if (tick % SPAWN_INTERVAL_TICKS == 1) {
            spawnZombie();
            if (ThreadLocalRandom.current().nextInt(3) == 0) spawnZombie();
        }
        litUntil.entrySet().removeIf(lamp -> {
            if (tick < lamp.getValue()) return false;
            showBlock(lamp.getKey(), Block.REDSTONE_LAMP);
            return true;
        });
        if (tick >= nextLampAt) {
            lightLamp();
            nextLampAt = tick + randomLampDelay();
        }
    }

    private static long randomLampDelay() {
        return ThreadLocalRandom.current().nextInt(160, 300);
    }

    private void spawnZombie() {
        boolean far = ThreadLocalRandom.current().nextBoolean();
        double z = MIN_Z + ThreadLocalRandom.current().nextDouble() * (MAX_Z - MIN_Z);
        Pos spawn = new Pos(far ? FAR_X : NEAR_X, far ? FAR_Y : NEAR_Y, z, 0, 0);
        boolean baby = ThreadLocalRandom.current().nextDouble() < ZombieShootoutScore.BABY_CHANCE;
        zombies.add(new Target(ZombieTier.roll(ThreadLocalRandom.current()), baby, spawn));
    }

    private void lightLamp() {
        List<BlockVec> unlit = LAMPS.stream().filter(lamp -> !litUntil.containsKey(lamp)).toList();
        if (unlit.isEmpty()) return;
        BlockVec lamp = unlit.get(ThreadLocalRandom.current().nextInt(unlit.size()));
        litUntil.put(lamp, tick + ZombieShootoutScore.LAMP_LIT_TICKS);
        showBlock(lamp, Block.REDSTONE_LAMP.withProperty("lit", "true"));
        player.playSound(Sound.sound(Key.key("block.note_block.bell"), Sound.Source.PLAYER, 1f, 1.5f));
    }

    @Override
    public void onRightClick() {
        if (tick - lastShot < SHOT_COOLDOWN_TICKS) return;
        lastShot = tick;
        score.onShot();
        player.playSound(Sound.sound(Key.key("entity.arrow.shoot"), Sound.Source.PLAYER, 0.6f, 1.6f));
        Vec eye = eye();
        Vec look = look();
        Target zombie = aimedZombie(eye, look);
        BlockVec lamp = aimedLamp(eye, look);
        double zombieDistance = zombie == null ? Double.MAX_VALUE : zombie.entity.getPosition().distance(eye.asPosition());
        double lampDistance = lamp == null ? Double.MAX_VALUE : lamp.add(0.5, 0.5, 0.5).distance(eye);
        if (zombie != null && zombieDistance <= lampDistance) {
            double points = score.onZombie(zombie.tier, zombie.baby, tick);
            player.sendMessage("<a>+" + StringUtility.commaify(Math.round(points)) + " " + zombie.tier.getColor()
                    + (zombie.baby ? "Baby " : "") + zombie.tier.getDisplayName().replace("Zombies", "Zombie"));
            player.playSound(Sound.sound(Key.key("entity.zombie.death"), Sound.Source.PLAYER, 0.7f, 1.2f));
            remove(zombie);
        } else if (lamp != null) {
            score.onLamp(tick);
            litUntil.remove(lamp);
            showBlock(lamp, Block.REDSTONE_LAMP);
            player.sendMessage("<c><l>BONUS! <a>+" + ZombieShootoutScore.LAMP_POINTS
                    + " <7>and <a>1.2x <7>points for <a>6s<7>!");
            player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 0.8f));
        }
    }

    private @Nullable Target aimedZombie(Vec eye, Vec look) {
        Target closest = null;
        double best = Double.MAX_VALUE;
        for (Target zombie : zombies) {
            Pos at = zombie.entity.getPosition();
            if (!zombie.entity.getBoundingBox().boundingBoxRayIntersectionCheck(eye, look, at)) continue;
            double distance = at.distance(eye.asPosition());
            if (distance <= RANGE && distance < best) {
                best = distance;
                closest = zombie;
            }
        }
        return closest;
    }

    private @Nullable BlockVec aimedLamp(Vec eye, Vec look) {
        for (double distance = 0; distance <= RANGE; distance += 0.1) {
            Vec point = eye.add(look.mul(distance));
            BlockVec block = new BlockVec(point.blockX(), point.blockY(), point.blockZ());
            if (litUntil.containsKey(block)) return block;
        }
        return null;
    }

    private void remove(Target zombie) {
        zombie.entity.remove();
        zombies.remove(zombie);
    }

    @Override
    protected long score() {
        return Math.round(score.getScore());
    }

    @Override
    protected void onEnd() {
        List.copyOf(zombies).forEach(this::remove);
        litUntil.clear();
    }

    @Override
    protected CarnivalGameResult result() {
        return score.result();
    }

    @Override
    protected List<String> summary(CarnivalGameResult result) {
        List<String> lines = new ArrayList<>(List.of(
                "<c><l>Zombie Shootout",
                "",
                "<7>Your Score",
                "<a>" + StringUtility.commaify(result.score()),
                "",
                "<7>Zombies Slain"));
        for (ZombieTier tier : ZombieTier.values()) {
            lines.add("<a>" + score.kills(tier) + "x " + tier.getColor() + tier.getDisplayName()
                    + " <7>- " + StringUtility.commaify(score.points(tier)) + " points");
        }
        lines.add("");
        lines.add("<c><l>BONUS! <c>" + score.getLamps() + "x Redstone Lamps <7>- "
                + StringUtility.commaify(score.getLamps() * ZombieShootoutScore.LAMP_POINTS) + " points");
        lines.add("");
        lines.add("<7>Total Kills: <e>" + score.totalKills() + " <8>(" + score.accuracyPercent() + "% accuracy)");
        return lines;
    }
}
