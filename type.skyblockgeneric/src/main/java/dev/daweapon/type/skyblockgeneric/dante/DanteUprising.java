package dev.daweapon.type.skyblockgeneric.dante;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class DanteUprising {
    public static final int FIRST_WAVE_GOONS = 6;
    public static final String[] INTRO = {
            "I see you have come to me with evil intentions, Technoblade!",
            "I gave this world a chance at redemption, and this is all I get?",
            "A revolt? Ants scheming behind my back?",
            "You and your Generals have no chance against me, I am no mere mortal.",
            "LET THE DEATH MATCH BEGIN.",
            "GOONS, PUNISH THEM ALL!"
    };
    public static final String[] AFTER_GOONS = {
            "These Goons are utterly useless.",
            "But we knew that already.",
            "GOONS ASSEMBLE!!!"
    };
    public static final String[] SLIME_LINES = {
            "I AM DANTE", "MY VOICE IS THUNDER", "MY SPEECH IS LAW", "MY SKIN IS TOUGHER THAN A THOUSAND ARMORS",
            "I AM DANTE AND THE WORLD QUAKES BENEATH ME", "YOU HAD A CHOICE AND YOU CHOSE DEATH",
            "BEFORE YOUR END WITNESS MY FURY", "I WILL TRANSFORM YOUR VILLAGE INTO DUST",
            "STARTING WITH THE COMMUNITY CENTER", "IT'S DOOM O'CLOCK", "TIME TO REPENT", "ORDER", "SECURITY", "FREEDOM",
            "VALUES", "JUSTICE", "NO MORE BANK", "NO MORE AUCTIONS", "WORTHLESS", "DISTRACTIONS",
            "THE SAVIOR OF THE WORLD", "YOU VOTED FOR ME MY SWEET CHILDS", "BUT TECHNOBLADE CORRUPTED YOU",
            "CAN'T YOU SEE?", "I AM BUILT DIFFERENT", "PEACE ISN'T EARNED", "PEACE IS TAKEN BY FORCE",
            "I AM STRENGTH", "THE COLOSSEUM WILL BE NO MORE", "THIS IS HOW AN ERA ENDS",
            "DID YOU REALLY THINK YOU COULD WIN?", "I'M NOT DONE", "I WILL NOW TAKE DOWN THIS USELESS WIZARD TOWER",
            "THE WORLD WILL END"
    };
    public static final String[] DEFEAT = {
            "HOW DID YOU GATHER THAT MUCH POWER?",
            "I AM DEFEATED",
            "BUT YOU ALSO LOST, RESISTANCE",
            "DEATH IS MY OLDEST FRIEND",
            "ADIOS!"
    };
    public static final String DASHLINE = "----------------------------------------------------";
    private static final double SPAWN_RADIUS = 12;
    private static final int LINE_GAP_SECONDS = 2;

    private static final List<MobDanteGoon> GOONS = new ArrayList<>();
    private static Instance instance;
    private static MobDante dante;
    private static Pos origin;
    private static int generation;
    private static boolean running;

    private DanteUprising() {
    }

    public static synchronized boolean isRunning() {
        return running;
    }

    public static synchronized boolean start(SkyBlockPlayer staff) {
        if (running) return false;
        running = true;
        generation++;
        instance = staff.getInstance();
        origin = staff.getPosition();
        int fight = generation;
        broadcast("<d>[NPC] Clerk Seraphine<f>: The Uprising begins! Take the <6>Staff of the Rising Sun<f>, it is the only weapon that can hurt Dante!");
        for (SkyBlockPlayer player : players()) UprisingGear.giveIfMissing(player);
        for (int i = 0; i < INTRO.length; i++) {
            String line = INTRO[i];
            boolean last = i == INTRO.length - 1;
            later(fight, (i + 1) * LINE_GAP_SECONDS, () -> {
                bossLine(line);
                if (last) spawnGoons();
            });
        }
        return true;
    }

    public static synchronized void stop() {
        running = false;
        generation++;
        GOONS.forEach(DanteUprising::discard);
        GOONS.clear();
        if (dante != null && !dante.isRemoved()) dante.remove();
        dante = null;
    }

    private static void discard(Entity entity) {
        if (!entity.isRemoved()) entity.remove();
    }

    private static synchronized void spawnGoons() {
        if (!running) return;
        for (int i = 0; i < FIRST_WAVE_GOONS; i++) {
            MobDanteGoon goon = new MobDanteGoon();
            goon.setInstance(instance, around(SPAWN_RADIUS));
            GOONS.add(goon);
        }
    }

    static synchronized void onGoonDown(MobDanteGoon goon) {
        if (!running || dante != null) return;
        GOONS.remove(goon);
        if (!GOONS.isEmpty()) return;
        int fight = generation;
        for (int i = 0; i < AFTER_GOONS.length; i++) {
            String line = AFTER_GOONS[i];
            later(fight, (i + 1) * LINE_GAP_SECONDS, () -> bossLine(line));
        }
        later(fight, (AFTER_GOONS.length + 1) * LINE_GAP_SECONDS, DanteUprising::transform);
    }

    private static synchronized void transform() {
        if (!running || dante != null) return;
        dante = new MobDante();
        dante.setInstance(instance, origin.add(0, 1, 0));
        bossLine("I AM DANTE");
    }

    static void speakAsSlime(Instance where) {
        if (where == null) return;
        String line = SLIME_LINES[ThreadLocalRandom.current().nextInt(SLIME_LINES.length)];
        bossLine(line);
    }

    static synchronized void onDanteDown(MobDante fallen) {
        if (!running || fallen != dante) return;
        int fight = generation;
        for (int i = 0; i < DEFEAT.length; i++) {
            String line = DEFEAT[i];
            later(fight, i + 1, () -> bossLine(line));
        }
        announceLeaderboard(fallen);
        later(fight, DEFEAT.length + 2, () -> {
            broadcast("<e><l>" + DASHLINE);
            for (String line : List.of("DANTE IS NO MORE!", "IT TOOK AN ARMY OF TENS OF THOUSANDS!", "BUT WE PREVAILED!",
                    "THE SUN RISES AGAIN!", "TIME TO START REBUILDING OUR VILLAGE!")) {
                broadcast("<b><l>" + line);
            }
            broadcast("<e><l>" + DASHLINE);
            synchronized (DanteUprising.class) {
                running = false;
                dante = null;
            }
        });
    }

    private static void announceLeaderboard(MobDante fallen) {
        List<Map.Entry<UUID, Double>> ranked = fallen.getDamageDealt().entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .toList();
        String[] places = {"<e><l>1st Damager", "<6><l>2nd Damager", "<c><l>3rd Damager"};
        for (SkyBlockPlayer viewer : players()) {
            viewer.sendMessage("<6><l>" + DASHLINE);
            viewer.sendMessage("<6><l>DANTE HAS BEEN DEFEATED!");
            for (int i = 0; i < ranked.size(); i++) {
                Map.Entry<UUID, Double> entry = ranked.get(i);
                viewer.sendMessage(places[i] + " <7>- <d>" + nameOf(fallen, entry.getKey()) + " <7>- <e>" + StringUtility.commaify(entry.getValue()));
            }
            viewer.sendMessage("<e>Your Damage: <a>" + StringUtility.commaify(fallen.damageDealtBy(viewer.getUuid())));
            viewer.sendMessage("<6><l>" + DASHLINE);
        }
    }

    private static String nameOf(MobDante fallen, UUID id) {
        String recorded = fallen.names().get(id);
        if (recorded != null) return recorded;
        SkyBlockPlayer online = SkyBlockGenericLoader.getFromUUID(id);
        return online != null ? online.getUsername() : "Unknown";
    }

    private static Pos around(double radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return origin.add((random.nextDouble() - 0.5) * 2 * radius, 1, (random.nextDouble() - 0.5) * 2 * radius);
    }

    private static void later(int fight, int seconds, Runnable action) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            synchronized (DanteUprising.class) {
                if (generation != fight) return;
            }
            action.run();
        }).delay(TaskSchedule.seconds(seconds)).schedule();
    }

    private static List<SkyBlockPlayer> players() {
        Instance where = instance;
        if (where == null) return List.of();
        return where.getPlayers().stream()
                .filter(player -> player instanceof SkyBlockPlayer)
                .map(player -> (SkyBlockPlayer) player)
                .toList();
    }

    private static void bossLine(String line) {
        broadcast("<c>[BOSS] Dante<f>: <l>" + line);
    }

    private static void broadcast(String message) {
        players().forEach(player -> player.sendMessage(message));
    }
}
