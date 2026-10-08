package net.swofty.type.skyblockgeneric.crimson.matriarch;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Matriarch (wiki: The_Matriarch). While she has pearls for a player she reels them in from across her lair and
 * swallows them into the Belly of the Beast. Using a Matriarch's Perfume on her refills her to 3 pearls (4 times a
 * day). After escaping she lets the player go for 30 seconds (estimate) so they can walk away; her pull range is
 * an estimate too.
 */
public final class MatriarchService {
    static final Pos MATRIARCH = new Pos(-533.5, 41, -888.5);
    static final Pos LAIR_EXIT = new Pos(-539.5, 40, -899.5);
    static final double PULL_RANGE = 12;
    static final double SWALLOW_RANGE = 3;
    static final long ESCAPE_GRACE_MS = 30_000;
    private static final Map<UUID, BellyRun> RUNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> GRACE = new ConcurrentHashMap<>();
    private static @Nullable LivingEntity matriarch;
    private static boolean running;

    private MatriarchService() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return TaskSchedule.tick(1);
        });
        MinecraftServer.getGlobalEventHandler().addListener(EntityAttackEvent.class, event -> {
            if (!(event.getEntity() instanceof SkyBlockPlayer player)) return;
            BellyRun run = RUNS.get(player.getUuid());
            if (run != null) run.hit(event.getTarget());
        });
        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent.class, event -> {
            if (event.getTarget() != matriarch || !(event.getPlayer() instanceof SkyBlockPlayer player)) return;
            usePerfume(player);
        });
    }

    public static boolean inBelly(SkyBlockPlayer player) {
        return RUNS.containsKey(player.getUuid());
    }

    private static void spawnMatriarch(Instance instance) {
        LivingEntity entity = new LivingEntity(EntityType.GHAST);
        entity.setNoGravity(true);
        entity.getAttribute(Attribute.SCALE).setBaseValue(1.6);
        entity.setInvulnerable(true);
        entity.setCustomName(Component.text("§6The Matriarch"));
        entity.setCustomNameVisible(true);
        if (!instance.isChunkLoaded(MATRIARCH)) instance.loadChunk(MATRIARCH).join();
        entity.setInstance(instance, MATRIARCH);
        matriarch = entity;
    }

    private static void tick() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null || SkyBlockGenericLoader.getLoadedPlayers().isEmpty()) return;
        if (matriarch == null || matriarch.isRemoved()) spawnMatriarch(instance);
        RUNS.entrySet().removeIf(entry -> {
            if (entry.getValue().tick()) return false;
            entry.getValue().end();
            return true;
        });
        long now = System.currentTimeMillis();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getInstance() != instance || player.isDead() || inBelly(player)) continue;
            double distance = player.getPosition().distance(MATRIARCH);
            if (distance > PULL_RANGE || GRACE.getOrDefault(player.getUuid(), 0L) > now) continue;
            if (HeavyPearls.onCooldown(player) || HeavyPearls.available(player) <= 0) continue;
            if (distance <= SWALLOW_RANGE) {
                swallow(player, instance);
            } else {
                Vec pull = MATRIARCH.sub(player.getPosition()).asVec().normalize().mul(8);
                player.setVelocity(pull);
            }
        }
    }

    private static void swallow(SkyBlockPlayer player, Instance instance) {
        BellyRun run = new BellyRun(player);
        RUNS.put(player.getUuid(), run);
        run.begin(instance);
    }

    static void escape(SkyBlockPlayer player) {
        BellyRun run = RUNS.remove(player.getUuid());
        if (run != null) run.end();
        GRACE.put(player.getUuid(), System.currentTimeMillis() + ESCAPE_GRACE_MS);
        player.teleport(LAIR_EXIT);
        player.sendMessage("<a>You escaped the Belly of the Beast!");
    }

    static void usePerfume(SkyBlockPlayer player) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (held.getAttributeHandler().getPotentialType() != ItemType.MATRIARCHS_PERFUME) {
            int left = HeavyPearls.available(player);
            player.sendMessage("<6>The Matriarch <7>has <6>" + left + " Heavy Pearl" + (left == 1 ? "" : "s") + " <7>for you today.");
            return;
        }
        if (!HeavyPearls.usePerfume(player)) {
            player.sendMessage("<c>You've already used " + HeavyPearls.DAILY_PERFUMES + " Matriarch's Perfumes today!");
            return;
        }
        player.setItemInMainHand(held.getAmount() > 1
                ? player.getItemInMainHand().withAmount(held.getAmount() - 1)
                : net.minestom.server.item.ItemStack.AIR);
        player.sendMessage("<a>The Matriarch loves the smell! She now has <6>3 Heavy Pearls <a>for you. <7>("
                + HeavyPearls.perfumesUsed(player) + "/" + HeavyPearls.DAILY_PERFUMES + " today)");
    }
}
