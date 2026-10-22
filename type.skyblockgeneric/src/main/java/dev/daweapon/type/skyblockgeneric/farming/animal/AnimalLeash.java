package dev.daweapon.type.skyblockgeneric.farming.animal;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AnimalLeash {
    public static final int MAX_PER_PLAYER = 4;
    private static final double WALK_FROM = 3.5;
    private static final double TELEPORT_FROM = 7;
    private static final double MAX_VERTICAL_WALK = 2;

    private static final Map<UUID, Leashed> LEASHED = new ConcurrentHashMap<>();
    private static final AtomicBoolean STARTED = new AtomicBoolean();

    private record Leashed(SkyBlockPlayer owner, SkyBlockMob mob, FarmAnimal animal) {
    }

    private AnimalLeash() {
    }

    public static boolean isLeashed(SkyBlockMob mob) {
        return LEASHED.containsKey(mob.getUuid());
    }

    public static boolean isLeashedBy(SkyBlockPlayer player, SkyBlockMob mob) {
        Leashed leashed = LEASHED.get(mob.getUuid());
        return leashed != null && leashed.owner().getUuid().equals(player.getUuid());
    }

    public static void toggle(SkyBlockPlayer player, SkyBlockMob mob, FarmAnimal animal) {
        Leashed existing = LEASHED.get(mob.getUuid());
        if (existing != null) {
            if (!existing.owner().getUuid().equals(player.getUuid())) {
                player.sendMessage("<c>Someone else is already leading this animal!");
                return;
            }
            LEASHED.remove(mob.getUuid());
            player.sendMessage("<e>You let go of the <a>{}<e>.", animal.displayName());
            return;
        }
        if (ownedBy(player).size() >= MAX_PER_PLAYER) {
            player.sendMessage("<c>You can only lead {} animals at once!", MAX_PER_PLAYER);
            return;
        }
        LEASHED.put(mob.getUuid(), new Leashed(player, mob, animal));
        player.sendMessage("<a>You are now leading the <e>{}<a>! Bring it to <e>Jake<a>.", animal.displayName());
        start();
    }

    public static List<SkyBlockMob> ownedBy(SkyBlockPlayer player) {
        List<SkyBlockMob> owned = new ArrayList<>();
        for (Leashed leashed : LEASHED.values()) {
            if (leashed.owner().getUuid().equals(player.getUuid())) owned.add(leashed.mob());
        }
        return owned;
    }

    public static void release(SkyBlockMob mob) {
        LEASHED.remove(mob.getUuid());
    }

    private static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().buildTask(AnimalLeash::tick)
                .delay(TaskSchedule.tick(10))
                .repeat(TaskSchedule.tick(10))
                .schedule();
    }

    private static void tick() {
        for (Leashed leashed : List.copyOf(LEASHED.values())) {
            SkyBlockPlayer owner = leashed.owner();
            SkyBlockMob mob = leashed.mob();
            if (mob.isDead() || mob.isRemoved() || !owner.isOnline() || owner.getInstance() == null) {
                LEASHED.remove(mob.getUuid());
                continue;
            }
            if (mob.getInstance() != owner.getInstance()) {
                LEASHED.remove(mob.getUuid());
                owner.sendMessage("<e>The <a>{}<e> got lost along the way.", leashed.animal().displayName());
                continue;
            }
            follow(owner, mob);
        }
    }

    private static void follow(SkyBlockPlayer owner, SkyBlockMob mob) {
        Pos ownerPos = owner.getPosition();
        double distance = mob.getPosition().distance(ownerPos);
        if (distance <= WALK_FROM) return;
        double vertical = Math.abs(mob.getPosition().y() - ownerPos.y());
        if (distance > TELEPORT_FROM || vertical > MAX_VERTICAL_WALK) {
            double yawRadians = Math.toRadians(ownerPos.yaw());
            Pos behind = ownerPos.add(Math.sin(yawRadians) * 1.5, 0, -Math.cos(yawRadians) * 1.5);
            mob.teleport(behind);
            return;
        }
        mob.getNavigator().setPathTo(ownerPos);
    }
}
