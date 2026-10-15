package dev.daweapon.type.skyblockgeneric.fishing.trophy;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.entity.FishingHook;
import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchAwardContext;
import dev.daweapon.type.skyblockgeneric.fishing.catches.TrophyTier;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Golden Fish (wiki: Trophy_Fish#Golden Fish): after 8 minutes of continuous lava fishing on the Crimson Isle
 * it may surface near the bobber, with the chance rising from 0% to 100% over the next 4 minutes (the Goldfin Shard's
 * Gold Bait attribute shortens the wait). Hook it until it is weak, then reel it in; its tier rolls like any trophy.
 * Estimates (not on the wiki): fishing stays "continuous" while you recast within 30 seconds, the fish stays for
 * 60 seconds, needs 3 hooks, and the surfacing roll happens every 10 seconds.
 */
public final class GoldenFishService {
    public static final long ELIGIBLE_AFTER_MS = 8 * 60_000L;
    public static final long RAMP_MS = 4 * 60_000L;
    static final long CONTINUITY_MS = 30_000L;
    static final long LIFETIME_MS = 60_000L;
    static final int HOOKS_TO_WEAKEN = 3;
    static final double HOOK_RADIUS = 2.5;
    private static final int CHECK_SECONDS = 10;
    private static final Map<UUID, Streak> STREAKS = new ConcurrentHashMap<>();
    private static volatile boolean running;

    private GoldenFishService() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().buildTask(GoldenFishService::tick)
                .repeat(TaskSchedule.seconds(CHECK_SECONDS)).schedule();
    }

    public static void onLavaCast(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        STREAKS.compute(player.getUuid(), (_, streak) -> {
            if (streak == null || now - streak.lastCastAt > CONTINUITY_MS) return new Streak(now, now);
            streak.lastCastAt = now;
            return streak;
        });
    }

    public static long fishingMillis(SkyBlockPlayer player) {
        Streak streak = STREAKS.get(player.getUuid());
        if (streak == null || System.currentTimeMillis() - streak.lastCastAt > CONTINUITY_MS) return 0;
        return System.currentTimeMillis() - streak.startedAt;
    }

    public static long eligibleAfter(SkyBlockPlayer player) {
        long goldBait = (long) (AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("U5")) * 1000);
        return Math.max(0, ELIGIBLE_AFTER_MS - goldBait);
    }

    public static boolean hasSurfaced(SkyBlockPlayer player) {
        Streak streak = STREAKS.get(player.getUuid());
        return streak != null && streak.fish != null;
    }

    static double surfaceChance(long fishingMillis, long eligibleAfter) {
        if (fishingMillis < eligibleAfter) return 0;
        return Math.min(100D, (fishingMillis - eligibleAfter) * 100D / RAMP_MS);
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            Streak streak = STREAKS.get(player.getUuid());
            if (streak == null) continue;
            if (streak.fish != null) {
                if (now - streak.fish.surfacedAt > LIFETIME_MS || streak.fish.entity.isRemoved()) {
                    player.sendMessage("<c>The Golden Fish swam away...");
                    streak.fish.entity.remove();
                    streak.fish = null;
                }
                continue;
            }
            FishingHook hook = FishingHook.getFishingHookForOwner(player);
            if (hook == null || hook.isRemoved() || now - streak.lastCastAt > CONTINUITY_MS) continue;
            double chance = surfaceChance(now - streak.startedAt, eligibleAfter(player));
            if (chance > 0 && LootRoll.roll(player.getUuid(), null, chance)) surface(player, streak, hook.getSpawnPosition());
        }
    }

    private static void surface(SkyBlockPlayer player, Streak streak, Pos bobber) {
        if (player.getInstance() == null) return;
        Entity fish = new Entity(EntityType.ITEM_DISPLAY);
        fish.setNoGravity(true);
        ItemType bronze = ItemType.get("GOLDEN_FISH_BRONZE");
        fish.editEntityMeta(ItemDisplayMeta.class, meta -> {
            if (bronze != null) meta.setItemStack(new SkyBlockItem(bronze).getItemStack());
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
        });
        fish.setInstance(player.getInstance(), bobber.add(3, 0.3, 0));
        streak.fish = new Surfaced(fish, System.currentTimeMillis());
        player.sendMessage("<6><l>GOLDEN FISH!</l> <e>A Golden Fish has surfaced near your bobber! Hook it!");
    }

    /** Called when the player's bobber settles in lava; returns true when the cast hooked the Golden Fish. */
    public static boolean onBobberLanded(SkyBlockPlayer player, Pos bobber) {
        Streak streak = STREAKS.get(player.getUuid());
        if (streak == null || streak.fish == null || streak.fish.weak) return false;
        if (streak.fish.entity.getPosition().distance(bobber) > HOOK_RADIUS) return false;
        streak.fish.hooks++;
        if (streak.fish.hooks >= HOOKS_TO_WEAKEN) {
            streak.fish.weak = true;
            player.sendMessage("<c>The Golden Fish is weak!");
            return false;
        }
        player.sendMessage("<c>The Golden Fish escapes your hook but looks weakened.");
        streak.fish.entity.teleport(streak.fish.entity.getPosition().add(2 - streak.fish.hooks * 2, 0, 2));
        return true;
    }

    /** Called when the player reels in; catches the weakened Golden Fish if the bobber is on it. */
    public static boolean tryReelIn(SkyBlockPlayer player, SkyBlockItem rod, Pos bobber) {
        Streak streak = STREAKS.get(player.getUuid());
        if (streak == null || streak.fish == null || !streak.fish.weak) return false;
        if (streak.fish.entity.getPosition().distance(bobber) > HOOK_RADIUS + 1) return false;
        TrophyFishDefinition golden = golden();
        streak.fish.entity.remove();
        STREAKS.remove(player.getUuid());
        if (golden == null) return true;
        var charm = rod.getAttributeHandler().getEnchantment(dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType.CHARM);
        TrophyTier tier = TrophyFishRoller.rollTier(player, golden, charm == null ? 0 : charm.level() * 2D);
        TrophyFishRoller.payload(golden, tier).apply(new CatchAwardContext(player, rod, bobber));
        return true;
    }

    public static void clear(SkyBlockPlayer player) {
        Streak streak = STREAKS.remove(player.getUuid());
        if (streak != null && streak.fish != null) streak.fish.entity.remove();
    }

    private static @Nullable TrophyFishDefinition golden() {
        return FishingRegistry.getTrophyFish().stream().filter(TrophyFishDefinition::specialGoldenFish).findFirst().orElse(null);
    }

    private static final class Streak {
        final long startedAt;
        long lastCastAt;
        @Nullable Surfaced fish;

        Streak(long startedAt, long lastCastAt) {
            this.startedAt = startedAt;
            this.lastCastAt = lastCastAt;
        }
    }

    private static final class Surfaced {
        final Entity entity;
        final long surfacedAt;
        int hooks;
        boolean weak;

        Surfaced(Entity entity, long surfacedAt) {
            this.entity = entity;
            this.surfacedAt = surfacedAt;
        }
    }
}
