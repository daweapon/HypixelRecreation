package dev.daweapon.type.theend.dragon;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermanHigh;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermiteLow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The door between the island and the Dragon's Nest. It seals when a dragon spawns, so players who die
 * and respawn outside must break it (1,000,000 damage, melee or ranged) to get back in; Endermites and
 * Endermen climb out as it takes damage.
 */
public final class DragonGate {
    public static final double HEALTH = 1_000_000;
    public static final int DOOR_X = -572;
    public static final int DOOR_MIN_Z = -293;
    public static final int DOOR_MAX_Z = -268;
    public static final int DOOR_MIN_Y = 17;
    public static final int DOOR_MAX_Y = 32;
    public static final int GLASS_FROM_Y = 28;
    public static final double DEFENDER_DAMAGE_STEP = 100_000;
    private static final Pos MOB_POSITION = new Pos(DOOR_X + 0.5, DOOR_MIN_Y, -280.5);
    private static final double HITBOX_WIDTH = 3;
    private static final double HITBOX_HEIGHT = DOOR_MAX_Y - DOOR_MIN_Y + 1;
    private static final double HITBOX_DEPTH = DOOR_MAX_Z - DOOR_MIN_Z + 1;
    private static final Pos OUTSIDE = new Pos(-568.5, 17, -270.5, 90, 0);
    private static final List<Pos> DEFENDER_SPOTS = List.of(
            new Pos(-568.5, 17, -270.5), new Pos(-568.5, 17, -266.5), new Pos(-568.5, 19, -262.5));

    private static final double[] THRESHOLDS = {300_000, 550_000, 800_000, 1_000_000};
    private static final String[] MESSAGES = {
            "The Dragon's Gate is weakening!",
            "The Dragon's Gate is starting to budge!",
            "The Dragon's Gate is nearly open!",
            "The Dragon's Gate has opened!"};

    private static final Map<Pos, Block> replaced = new LinkedHashMap<>();
    private static final List<InteractionEntity> hitboxes = new ArrayList<>();
    private static DragonGateMob gate;
    private static int announced;
    private static int defenderWaves;

    private DragonGate() {
    }

    public static String[] messagesFor(int alreadyAnnounced, double totalDamage) {
        List<String> due = new ArrayList<>();
        for (int stage = alreadyAnnounced; stage < THRESHOLDS.length; stage++) {
            if (totalDamage < THRESHOLDS[stage]) break;
            due.add(MESSAGES[stage]);
        }
        return due.toArray(String[]::new);
    }

    public static boolean sealed() {
        return gate != null;
    }

    public static void close(Instance instance) {
        if (sealed()) return;
        announced = 0;
        defenderWaves = 0;
        for (int z = DOOR_MIN_Z; z <= DOOR_MAX_Z; z++) {
            for (int y = DOOR_MIN_Y; y <= DOOR_MAX_Y; y++) {
                Pos cell = new Pos(DOOR_X, y, z);
                if (!instance.isChunkLoaded(cell)) instance.loadChunk(cell).join();
                Block current = instance.getBlock(cell);
                if (!current.isAir()) continue;
                replaced.put(cell, current);
                instance.setBlock(cell, y >= GLASS_FROM_Y ? Block.PURPLE_STAINED_GLASS : Block.OBSIDIAN);
            }
        }
        for (Player player : instance.getPlayers()) {
            if (replaced.containsKey(new Pos(player.getPosition().blockX(), player.getPosition().blockY(), player.getPosition().blockZ()))) {
                player.teleport(OUTSIDE);
            }
        }
        DragonGateMob mob = new DragonGateMob(damage -> damaged(instance, damage));
        mob.setBoundingBox(HITBOX_WIDTH, HITBOX_HEIGHT, HITBOX_DEPTH);
        mob.setInstance(instance, MOB_POSITION);
        gate = mob;
        for (int z = DOOR_MIN_Z + 1; z <= DOOR_MAX_Z; z += (int) HITBOX_WIDTH) {
            InteractionEntity hitbox = new InteractionEntity((float) HITBOX_WIDTH, (float) HITBOX_HEIGHT, (player, event) -> { });
            hitbox.onAttack(player -> {
                DragonGateMob current = gate;
                if (current != null && !current.isRemoved()) EventDispatcher.call(new EntityAttackEvent(player, current));
            });
            hitbox.setInstance(instance, new Pos(DOOR_X + 0.5, DOOR_MIN_Y, z + 0.5));
            hitboxes.add(hitbox);
        }
    }

    public static void open(Instance instance) {
        replaced.forEach(instance::setBlock);
        replaced.clear();
        hitboxes.forEach(hitbox -> {
            if (!hitbox.isRemoved()) hitbox.remove();
        });
        hitboxes.clear();
        if (gate != null && !gate.isRemoved()) gate.remove();
        gate = null;
    }

    private static void damaged(Instance instance, double damageTaken) {
        String[] due = messagesFor(announced, damageTaken);
        for (String message : due) {
            Text line = Text.of("<5>☬ <d><l>{}", message);
            instance.getPlayers().forEach(player -> player.sendMessage(line));
        }
        announced += due.length;
        int waves = (int) Math.min(HEALTH / DEFENDER_DAMAGE_STEP, damageTaken / DEFENDER_DAMAGE_STEP);
        for (; defenderWaves < waves; defenderWaves++) spawnDefenders(instance);
        if (damageTaken >= HEALTH) open(instance);
    }

    private static void spawnDefenders(Instance instance) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        spawn(instance, new MobEndermiteLow(), DEFENDER_SPOTS.get(random.nextInt(DEFENDER_SPOTS.size())));
        spawn(instance, new MobEndermiteLow(), DEFENDER_SPOTS.get(random.nextInt(DEFENDER_SPOTS.size())));
        spawn(instance, new MobEndermanHigh(), DEFENDER_SPOTS.get(random.nextInt(DEFENDER_SPOTS.size())));
    }

    private static void spawn(Instance instance, SkyBlockMob mob, Pos at) {
        mob.setInstance(instance, at);
    }
}
