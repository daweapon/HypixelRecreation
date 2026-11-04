package dev.daweapon.type.theend.race;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookEvent;
import dev.daweapon.type.skyblockgeneric.race.Race;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.theend.npcs.NPCGuber;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EndRace implements Race {
    public static final String TITLE = "<d><l>THE END RACE";
    public static final Pos START = new Pos(-501.5, 121, -240.5);
    public static final Pos CHECKPOINT = new Pos(-773.5, 101, -298.5);
    private static final int TIME_LIMIT_SECONDS = 600;
    private static final long SHOUT_COOLDOWN_MILLIS = 5_000;
    private static final int BLINDNESS_CHECK_TICKS = 5;

    private final Map<UUID, Boolean> blindAllTheWay = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastShout = new ConcurrentHashMap<>();

    public void start() {
        MinecraftServer.getSchedulerManager().buildTask(this::checkBlindness)
                .repeat(TaskSchedule.tick(BLINDNESS_CHECK_TICKS)).schedule();
    }

    @Override
    public String getId() {
        return "end_race";
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Point startPosition() {
        return START;
    }

    @Override
    public Point endPosition() {
        return START;
    }

    @Override
    public List<Point> getCheckpoints() {
        return List.of(CHECKPOINT);
    }

    @Override
    public int timeLimit() {
        return TIME_LIMIT_SECONDS;
    }

    @Override
    public boolean canStart(SkyBlockPlayer player) {
        if (!EndRaceProgress.introduced(player)) {
            long now = System.currentTimeMillis();
            Long last = lastShout.get(player.getUuid());
            if (last == null || now - last >= SHOUT_COOLDOWN_MILLIS) {
                lastShout.put(player.getUuid(), now);
                NPCGuber.say(player, "Hey you! Over here! Come talk to me! I'll teach you about racing!");
            }
            return false;
        }
        blindAllTheWay.put(player.getUuid(), player.hasEffect(PotionEffect.BLINDNESS));
        return true;
    }

    @Override
    public void onCheckpoint(SkyBlockPlayer player, int checkpointIndex, long time) {
        player.sendMessage(Text.of("{} <e>Checkpoint reached in {}! Now race back to the start!",
                Text.parse(TITLE), format(time)));
    }

    @Override
    public void onFinish(SkyBlockPlayer player, long time, boolean isBest) {
        player.sendMessage(Text.of("{} <e>Race finished in {}!", Text.parse(TITLE), format(time))
                .appendIf(isBest, " <d><l>PERSONAL BEST!"));
        boolean obscure = Boolean.TRUE.equals(blindAllTheWay.remove(player.getUuid()))
                && GreatSpookEvent.isActive();

        List<String> lines = new ArrayList<>();
        int claimed = EndRaceProgress.claimedTiers(player);
        EndRaceTier[] tiers = EndRaceTier.values();
        while (claimed < tiers.length && tiers[claimed].beatenBy(time)) {
            EndRaceTier tier = tiers[claimed];
            player.addAndUpdateItem(tier.reward(), tier.amount());
            tier.rewardLines().forEach(line -> lines.add(line.replace("{player}", player.getUsername())));
            claimed++;
        }
        if (claimed != EndRaceProgress.claimedTiers(player)) EndRaceProgress.setClaimedTiers(player, claimed);
        if (time < EndRaceTier.ACHIEVEMENT_MILLIS) {
            player.getAchievementHandler().addProgressByTrigger("skyblock.the_end_race_trigger", 1);
        }
        if (obscure) {
            player.addAndUpdateItem(ItemType.OBSCURE_ENDING, 1);
            lines.add("Wow, that's impressive.");
            lines.add("Here, take this <9>Obscure Ending<f>!");
        }
        NPCGuber.sayInTurn(player, lines);
    }

    public void forget(SkyBlockPlayer player) {
        blindAllTheWay.remove(player.getUuid());
    }

    public static String format(long millis) {
        return String.format(Locale.ROOT, "%02d:%02d.%03d", millis / 60_000, (millis / 1000) % 60, millis % 1000);
    }

    private void checkBlindness() {
        for (UUID uuid : List.copyOf(blindAllTheWay.keySet())) {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(uuid);
            if (player == null) {
                blindAllTheWay.remove(uuid);
                continue;
            }
            if (!player.hasEffect(PotionEffect.BLINDNESS)) blindAllTheWay.put(uuid, false);
        }
    }
}
