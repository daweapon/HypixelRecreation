package net.swofty.type.jerrysworkshop.defense;

import net.swofty.type.skyblockgeneric.gifts.GiftBoosts;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Runs Defend Jerry's Workshop: five waves in which Magma Cubes erupt from Mount Jerry and march
 * on the five gift piles, hurt only by snowballs.
 *
 * Each pile still standing when a wave ends pays the wiki's gifts for that pile, plus a Volcanic
 * Rock, to everyone who dealt at least 100 damage; below that a player receives half the gifts and
 * no rock. The wiki says only that more damage means more gifts, so that split is an estimate.
 */
public final class JerryWorkshopDefenseManager {

    public enum Phase {IDLE, PREPARING, WAVE_ACTIVE}

    private static final int TOTAL_WAVES = 5;
    private static final long FIRST_WAVE_DELAY_MS = 60_000L;
    private static final long BETWEEN_WAVES_MS = 8L * 60 * 1000;
    private static final long WAVE_TIME_LIMIT_MS = 5L * 60 * 1000;
    private static final int SNOWBALLS_GRANTED_PER_WAVE = 32;
    private static final int FULL_REWARD_DAMAGE = 100;
    private static final double PILE_REACH = 3;
    private static final int PILES_LOST_BEFORE_GARY = 4;

    // Where the cubes come down from Mount Jerry onto the village path; the wiki gives no exact point.
    private static final Pos ERUPTION_POINT = new Pos(4.5, 76, -6.5);

    private static volatile Phase phase = Phase.IDLE;
    private static int currentWave = 0;
    private static long phaseEndsAtMillis = 0;
    private static boolean tickerStarted = false;
    private static boolean triggeredThisOccurrence = false;
    private static boolean garyAnnounced = false;

    private static final Map<UUID, Double> damageDealt = new ConcurrentHashMap<>();
    private static final List<WorkshopDefenseMob> activeCubes = new ArrayList<>();
    private static final Map<WorkshopDefenseMob, GiftPile> cubeTargets = new ConcurrentHashMap<>();

    private JerryWorkshopDefenseManager() {
    }

    public static synchronized void beginEvent() {
        if (phase != Phase.IDLE) {
            Logger.info("Jerry's Workshop defense already running, ignoring duplicate start");
            return;
        }
        if (HypixelConst.getInstanceContainer() == null) return;

        currentWave = 0;
        damageDealt.clear();
        broadcast("<c><l>MOUNT JERRY IS ERUPTING! <7>Magma Cubes will march on the gift piles - defend them with Snowballs!");
        startPreparing(FIRST_WAVE_DELAY_MS);
        ensureTicker();
    }

    // Polled by TypeJerrysWorkshopLoader. The Season of Jerry event opens on Late Winter 24, the
    // day Mount Jerry erupts, so the defense starts once per occurrence.
    public static synchronized void checkAndAutoStart() {
        boolean active = SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.SEASON_OF_JERRY);
        if (!active) {
            triggeredThisOccurrence = false;
            return;
        }
        if (triggeredThisOccurrence || phase != Phase.IDLE) return;
        triggeredThisOccurrence = true;
        beginEvent();
    }

    public static Phase getPhase() {
        return phase;
    }

    public static int getCurrentRound() {
        return currentWave;
    }

    public static long getPhaseRemainingMillis() {
        return Math.max(0, phaseEndsAtMillis - System.currentTimeMillis());
    }

    public static void recordDamage(UUID playerUuid, double amount) {
        if (phase != Phase.WAVE_ACTIVE) return;
        damageDealt.merge(playerUuid, amount, Double::sum);
    }

    private static void ensureTicker() {
        if (tickerStarted) return;
        tickerStarted = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return phase == Phase.IDLE ? TaskSchedule.stop() : TaskSchedule.seconds(1);
        });
    }

    private static synchronized void tick() {
        if (phase == Phase.PREPARING && System.currentTimeMillis() >= phaseEndsAtMillis) {
            startWave();
        } else if (phase == Phase.WAVE_ACTIVE) {
            advanceCubes();
            showPileStatus();
            boolean cleared = activeCubes.stream().allMatch(WorkshopDefenseMob::isRemoved);
            boolean allLost = Arrays.stream(GiftPile.values()).allMatch(GiftPile::isDestroyed);
            if (cleared || allLost || System.currentTimeMillis() >= phaseEndsAtMillis) {
                endWave();
            }
        }
    }

    private static void startPreparing(long delay) {
        phase = Phase.PREPARING;
        phaseEndsAtMillis = System.currentTimeMillis() + delay;
        broadcast("<e>Wave " + (currentWave + 1) + " erupts in " + (delay / 60_000 > 0
                ? (delay / 60_000) + " minutes" : (delay / 1000) + " seconds") + " - prepare yourselves!");
    }

    private static void startWave() {
        currentWave++;
        phase = Phase.WAVE_ACTIVE;
        phaseEndsAtMillis = System.currentTimeMillis() + WAVE_TIME_LIMIT_MS;
        damageDealt.clear();
        garyAnnounced = false;
        GiftPile.restoreAll();
        spawnWave();
        grantSnowballs();
        broadcast("<c><l>WAVE " + currentWave + " HAS BEGUN! <7>Pelt the Magma Cubes with Snowballs!");
    }

    private static void spawnWave() {
        activeCubes.clear();
        cubeTargets.clear();
        Instance instance = HypixelConst.getInstanceContainer();
        for (DefendCube cube : DefendCube.forWave(currentWave)) {
            WorkshopDefenseMob mob = new WorkshopDefenseMob(cube, currentWave);
            mob.setInstance(instance, groundAt(instance, ERUPTION_POINT.add(Math.random() * 4 - 2, 0, Math.random() * 4 - 2)));
            activeCubes.add(mob);
        }
    }

    private static Pos groundAt(Instance instance, Pos around) {
        instance.loadChunk(around).join();
        for (int y = 140; y > around.blockY() - 20; y--) {
            if (!instance.getBlock(around.blockX(), y - 1, around.blockZ()).isAir()
                    && instance.getBlock(around.blockX(), y, around.blockZ()).isAir()) {
                return around.withY(y);
            }
        }
        return around;
    }

    private static void advanceCubes() {
        Arrays.stream(GiftPile.values()).forEach(pile -> pile.setTargeted(false));
        for (WorkshopDefenseMob cube : activeCubes) {
            if (cube.isRemoved()) continue;
            GiftPile target = nearestStandingPile(cube.getPosition());
            if (target == null) continue;
            target.setTargeted(true);

            if (cube.getPosition().distance(target.getPosition()) <= PILE_REACH) {
                target.damage(cube.getCube().pileDamagePerSecond());
                if (target.isDestroyed()) {
                    broadcast("<c>Gift Pile " + target.getSymbol() + " has been destroyed!");
                }
                continue;
            }

            if (cubeTargets.get(cube) != target || cube.getVanillaNavigator().isComplete()) {
                cubeTargets.put(cube, target);
                if (!cube.getVanillaNavigator().pathTo(target.getPosition())) {
                    Vec direction = target.getPosition().sub(cube.getPosition()).asVec().normalize();
                    cube.setVelocity(direction.mul(6).withY(4));
                }
            }
        }

        long lost = Arrays.stream(GiftPile.values()).filter(GiftPile::isDestroyed).count();
        if (!garyAnnounced && lost >= PILES_LOST_BEFORE_GARY) {
            garyAnnounced = true;
            broadcast("<e>[NPC] Gary<f>: I finally got my snow machine to work! This snow should put a damper on those Magma Cubes! You can thank me later!");
        }
    }

    private static GiftPile nearestStandingPile(Pos from) {
        return Arrays.stream(GiftPile.values())
                .filter(pile -> !pile.isDestroyed())
                .min(Comparator.comparingDouble(pile -> pile.getPosition().distanceSquared(from)))
                .orElse(null);
    }

    private static void showPileStatus() {
        String status = Arrays.stream(GiftPile.values()).map(GiftPile::status).collect(Collectors.joining(" "));
        Text text = Text.parseLenient("<7>Gift Piles: " + status);
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.sendActionBar(text.asComponent());
        }
    }

    private static void grantSnowballs() {
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.getInventory().addItemStack(ItemStack.of(Material.SNOWBALL, SNOWBALLS_GRANTED_PER_WAVE));
        }
    }

    private static void endWave() {
        for (WorkshopDefenseMob cube : activeCubes) {
            if (!cube.isRemoved()) cube.remove();
        }
        activeCubes.clear();
        cubeTargets.clear();

        distributeRewards();

        if (currentWave >= TOTAL_WAVES) {
            phase = Phase.IDLE;
            tickerStarted = false;
            broadcast("<a><l>JERRY'S WORKSHOP HAS BEEN DEFENDED! <7>Mount Jerry has calmed down - for now.");
        } else {
            startPreparing(BETWEEN_WAVES_MS);
        }
    }

    private static void distributeRewards() {
        List<GiftPile> standing = Arrays.stream(GiftPile.values()).filter(pile -> !pile.isDestroyed()).toList();
        int white = standing.stream().mapToInt(GiftPile::getWhiteGifts).sum();
        int green = standing.stream().mapToInt(GiftPile::getGreenGifts).sum();
        int red = standing.stream().mapToInt(GiftPile::getRedGifts).sum();

        damageDealt.forEach((uuid, damage) -> {
            SkyBlockPlayer player = findLoadedPlayer(uuid);
            if (player == null || damage <= 0) return;

            boolean full = damage >= FULL_REWARD_DAMAGE;
            int playerWhite = ElectionPerks.boostedCount(SkyBlockMayor.Perk.SWEET_BENEVOLENCE, full ? white : white / 2);
            int playerGreen = ElectionPerks.boostedCount(SkyBlockMayor.Perk.SWEET_BENEVOLENCE, full ? green : green / 2);
            int playerRed = ElectionPerks.boostedCount(SkyBlockMayor.Perk.SWEET_BENEVOLENCE, full ? red : red / 2);
            playerWhite = Carnivals.boostedCount(player, CarnivalPerk.PRESENT_CONNOISSEUR, playerWhite);
            playerGreen = Carnivals.boostedCount(player, CarnivalPerk.PRESENT_CONNOISSEUR, playerGreen);
            playerRed = Carnivals.boostedCount(player, CarnivalPerk.PRESENT_CONNOISSEUR, playerRed);
            playerWhite = GiftBoosts.withGiftAttackBonus(player, ItemType.WHITE_GIFT, playerWhite);
            playerGreen = GiftBoosts.withGiftAttackBonus(player, ItemType.GREEN_GIFT, playerGreen);
            playerRed = GiftBoosts.withGiftAttackBonus(player, ItemType.RED_GIFT, playerRed);
            int whiteToRed = redFad(player, playerWhite);
            int greenToRed = redFad(player, playerGreen);
            playerWhite -= whiteToRed;
            playerGreen -= greenToRed;
            playerRed += whiteToRed + greenToRed;
            int rocks = full ? standing.size() : 0;

            give(player, ItemType.WHITE_GIFT, playerWhite);
            give(player, ItemType.GREEN_GIFT, playerGreen);
            give(player, ItemType.RED_GIFT, playerRed);
            give(player, ItemType.VOLCANIC_ROCK, rocks);

            player.sendMessage("<a>Wave " + currentWave + " complete! <7>You dealt <c>" + Math.round(damage)
                    + " <7>damage and received <f>" + playerWhite + " White<7>, <a>" + playerGreen + " Green<7>, <c>"
                    + playerRed + " Red <7>Gifts and <5>" + rocks + " Volcanic Rock<7>.");
        });
    }

    private static int redFad(SkyBlockPlayer player, int gifts) {
        double chance = Carnivals.perkValue(player, CarnivalPerk.RED_FAD) / 100D;
        int converted = 0;
        for (int i = 0; i < gifts; i++) {
            if (LootRoll.roll(player.getUuid(), ItemType.RED_GIFT, chance * 100)) converted++;
        }
        return converted;
    }

    private static void give(SkyBlockPlayer player, ItemType type, int amount) {
        for (int i = 0; i < amount; i++) {
            player.addAndUpdateItem(new SkyBlockItem(type));
        }
    }

    private static SkyBlockPlayer findLoadedPlayer(UUID uuid) {
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getUuid().equals(uuid)) return player;
        }
        return null;
    }

    private static void broadcast(String markup) {
        Text text = Text.parseLenient(markup);
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.sendMessage(text.asComponent());
        }
    }
}
