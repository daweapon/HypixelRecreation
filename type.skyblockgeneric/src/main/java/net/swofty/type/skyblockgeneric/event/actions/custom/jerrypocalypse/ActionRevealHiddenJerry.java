package net.swofty.type.skyblockgeneric.event.actions.custom.jerrypocalypse;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.jerrypocalypse.HiddenJerry;
import net.swofty.type.skyblockgeneric.elections.jerrypocalypse.HiddenJerryType;
import net.swofty.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

// Jerrypocalypse: while Mayor Jerry is elected, gaining Skill XP from farming, mining, foraging or
// killing mobs can reveal a hidden Jerry, at the wiki's per-action odds and with a six-minute
// cooldown afterwards. The wiki also requires four hours of profile playtime, which this server
// does not track, and gives no lifetime for an uncaught Jerry, so one is removed after a minute.
public class ActionRevealHiddenJerry implements HypixelEventClass {
    private static final Map<SkillCategories, Double> CHANCES = Map.of(
            SkillCategories.FARMING, 1 / 2500.0,
            SkillCategories.MINING, 1 / 300.0,
            SkillCategories.FORAGING, 1 / 600.0,
            SkillCategories.COMBAT, 1 / 100.0);
    private static final long COOLDOWN_MS = 6 * 60 * 1000L;
    private static final int LIFETIME_SECONDS = 60;
    private static final List<String> MESSAGES = List.of(
            "<b>☺ <e>You found a {0} Jerry<e>!",
            "<b>☺ <e>Some {0} Jerry <e>was hiding, but you found it!",
            "<b>☺ <e>A wild {0} Jerry <e>spawned!");

    private static final Map<UUID, Long> lastReveal = new ConcurrentHashMap<>();

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(SkillUpdateEvent event) {
        if (event.getNewValueRaw() <= event.getOldValueRaw()) return;
        Double chance = CHANCES.get(event.getSkillCategory());
        if (chance == null) return;
        if (!ElectionManager.isPerkActive(SkyBlockMayor.Perk.JERRYPOCALYPSE)) return;

        SkyBlockPlayer player = event.getPlayer();
        long now = System.currentTimeMillis();
        Long last = lastReveal.get(player.getUuid());
        if (last != null && now - last < COOLDOWN_MS) return;
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;
        lastReveal.put(player.getUuid(), now);

        reveal(player, HiddenJerryType.roll());
    }

    private static void reveal(SkyBlockPlayer player, HiddenJerryType type) {
        if (player.getInstance() == null) return;
        double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        Pos spawn = player.getPosition().add(Math.cos(angle) * 3, 0.5, Math.sin(angle) * 3);

        HiddenJerry jerry = new HiddenJerry(type, player);
        jerry.setInstance(player.getInstance(), spawn);

        String message = MESSAGES.get(ThreadLocalRandom.current().nextInt(MESSAGES.size()))
                .replace("{0}", type.getColor() + type.getName());
        player.sendMessage(message);

        int[] elapsed = {0};
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (jerry.isRemoved() || jerry.isDead()) return TaskSchedule.stop();
            if (elapsed[0]++ >= LIFETIME_SECONDS * 2) {
                jerry.remove();
                return TaskSchedule.stop();
            }
            jerry.fleeFromFinder();
            return TaskSchedule.tick(10);
        });
    }
}
