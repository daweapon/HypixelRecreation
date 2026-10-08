package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.UniversalStep;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobIntroBlaze;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The 3 Blazes Elle is fighting at the Stronghold entrance (wiki: Blaze (Elle's Quest)). They are
 * spawned for a player on the first step who comes near Elle, never respawn once killed, and are
 * removed when the player moves on or leaves.
 */
public final class IntroBlazes {
    private static final double TRIGGER_RANGE = 24;
    private static final List<Pos> SPOTS = List.of(
            new Pos(-362.5, 80, -473.5), new Pos(-367.5, 80, -474.5), new Pos(-364.5, 80, -471.5));
    private static final Map<UUID, List<SkyBlockMob>> SPAWNED = new ConcurrentHashMap<>();

    private IntroBlazes() {
    }

    public static void check(SkyBlockPlayer player) {
        List<SkyBlockMob> mine = SPAWNED.get(player.getUuid());
        if (CrimsonQuestlines.universal(player) != UniversalStep.KILL_BLAZES) {
            if (mine != null) despawn(player.getUuid());
            return;
        }
        if (mine != null) {
            mine.removeIf(mob -> mob.isRemoved() || mob.isDead());
            if (mine.isEmpty()) SPAWNED.remove(player.getUuid());
            return;
        }
        if (player.getPosition().distance(ElleQuest.STRONGHOLD) > TRIGGER_RANGE) return;
        int remaining = (int) Math.max(0, CrimsonQuestlines.INTRO_BLAZES - CrimsonQuestlines.counter(player, "intro_blazes"));
        List<SkyBlockMob> spawned = new ArrayList<>();
        for (int index = 0; index < remaining; index++) {
            MobIntroBlaze blaze = new MobIntroBlaze();
            blaze.setInstance(HypixelConst.getInstanceContainer(), SPOTS.get(index));
            spawned.add(blaze);
        }
        SPAWNED.put(player.getUuid(), spawned);
    }

    public static void cleanUp(Set<UUID> online) {
        SPAWNED.keySet().removeIf(uuid -> {
            if (online.contains(uuid)) return false;
            SPAWNED.get(uuid).forEach(SkyBlockMob::remove);
            return true;
        });
    }

    private static void despawn(UUID player) {
        List<SkyBlockMob> mobs = SPAWNED.remove(player);
        if (mobs != null) mobs.stream().filter(mob -> !mob.isRemoved()).forEach(SkyBlockMob::remove);
    }
}
