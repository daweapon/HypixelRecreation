package dev.daweapon.type.skyblockgeneric.spidersden;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.LodestoneTracker;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.entity.EntityRelic;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.stream.IntStream;

/**
 * The 28 Relics of the Spider's Den (wiki: Relics, exact coordinates). Each pays 10,000 coins once per profile and
 * the Archaeologist adds 30,000 when all are found. The Archaeologist's Compass needle points at the nearest one.
 */
public final class SpiderDenRelics {
    public static final int COINS_PER_RELIC = 10_000;
    public static final int COMPLETION_COINS = 30_000;
    public static final String REWARD_FLAG = "spiders_den.relics.reward";
    public static final String COMPASS_FLAG = "spiders_den.relics.compass";
    static final String COMPASS_HANDLER = "ARCHAEOLOGIST_COMPASS";
    static final long COMPASS_COOLDOWN_MS = 2_000;

    public static final List<int[]> LOCATIONS = List.of(
            new int[]{-382, 88, -225}, new int[]{-188, 79, -346}, new int[]{-274, 99, -176}, new int[]{-296, 37, -270},
            new int[]{-348, 64, -202}, new int[]{-370, 88, -242}, new int[]{-354, 72, -285}, new int[]{-301, 70, -316},
            new int[]{-204, 62, -301}, new int[]{-181, 50, -252}, new int[]{-234, 50, -237}, new int[]{-355, 85, -211},
            new int[]{-300, 49, -218}, new int[]{-282, 48, -234}, new int[]{-298, 50, -254}, new int[]{-273, 63, -272},
            new int[]{-252, 56, -279}, new int[]{-270, 47, -291}, new int[]{-340, 88, -221}, new int[]{-311, 68, -249},
            new int[]{-311, 57, -248}, new int[]{-341, 122, -252}, new int[]{-145, 82, -335}, new int[]{-317, 68, -271},
            new int[]{-326, 49, -236}, new int[]{-215, 58, -302}, new int[]{-178, 135, -297}, new int[]{-225, 70, -314});

    private static boolean spawned;
    private static final java.util.Map<java.util.UUID, Long> LAST_COMPASS = new java.util.concurrent.ConcurrentHashMap<>();

    private SpiderDenRelics() {
    }

    public static int total() {
        return LOCATIONS.size();
    }

    static String flag(int relic) {
        return "spiders_den.relic." + relic;
    }

    public static boolean found(SkyBlockPlayer player, int relic) {
        return ChapterTriggers.done(player, flag(relic));
    }

    public static int foundCount(SkyBlockPlayer player) {
        return (int) IntStream.rangeClosed(1, total()).filter(relic -> found(player, relic)).count();
    }

    public static boolean foundAll(SkyBlockPlayer player) {
        return foundCount(player) == total();
    }

    public static Pos position(int relic) {
        int[] at = LOCATIONS.get(relic - 1);
        return new Pos(at[0] + 0.5, at[1], at[2] + 0.5);
    }

    public static void collect(SkyBlockPlayer player, int relic) {
        if (found(player, relic)) {
            player.sendMessage("<c>You have already found that Relic!");
            return;
        }
        ChapterTriggers.fire(player, flag(relic));
        player.addCoins(COINS_PER_RELIC);
        int count = foundCount(player);
        player.sendMessage("<6><l>RELIC! <f>You found a <b>Relic<f>! <8>(" + count + "/" + total() + ")");
        player.sendMessage("<6>+" + COINS_PER_RELIC + " coins");
        if (count == total()) player.sendMessage("<a>You found all " + total() + " Relics! Go and tell the Archaeologist.");
    }

    public static void maxOut(SkyBlockPlayer player) {
        for (int relic = 1; relic <= total(); relic++) if (!found(player, relic)) ChapterTriggers.fire(player, flag(relic));
    }

    public static boolean claimCompletionReward(SkyBlockPlayer player) {
        if (!foundAll(player) || ChapterTriggers.done(player, REWARD_FLAG)) return false;
        ChapterTriggers.fire(player, REWARD_FLAG);
        player.addCoins(COMPLETION_COINS);
        return true;
    }

    public static synchronized void ensureSpawned(Instance instance) {
        if (spawned) return;
        spawned = true;
        for (int relic = 1; relic <= total(); relic++) {
            Pos at = position(relic);
            if (!instance.isChunkLoaded(at)) instance.loadChunk(at).join();
            new EntityRelic(relic).spawn(instance, at);
        }
    }

    public static int nearestUnfound(SkyBlockPlayer player) {
        int best = 0;
        double distance = Double.MAX_VALUE;
        for (int relic = 1; relic <= total(); relic++) {
            if (found(player, relic)) continue;
            double d = player.getPosition().distanceSquared(position(relic));
            if (d < distance) {
                distance = d;
                best = relic;
            }
        }
        return best;
    }

    public static void registerInteractables() {
        InteractableRegistry.register(COMPASS_HANDLER, InteractableItemConfig.builder().rightClickHandler(SpiderDenRelics::useCompass).build());
    }

    private static void useCompass(SkyBlockPlayer player, SkyBlockItem item) {
        if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_SPIDERS_DEN) {
            player.sendMessage("<c>The Archaeologist's Compass only works in the Spider's Den!");
            return;
        }
        long now = System.currentTimeMillis();
        Long last = LAST_COMPASS.get(player.getUuid());
        if (last != null && now - last < COMPASS_COOLDOWN_MS) return;
        LAST_COMPASS.put(player.getUuid(), now);
        int relic = nearestUnfound(player);
        if (relic == 0) {
            player.sendMessage("<a>You have found every Relic!");
            return;
        }
        pointAt(player, position(relic));
    }

    private static void pointAt(SkyBlockPlayer player, Pos relic) {
        ItemStack held = player.getItemInMainHand();
        if (held.material() != Material.COMPASS || player.getInstance() == null) return;
        LodestoneTracker tracker = new LodestoneTracker(player.getInstance().getDimensionName(), new BlockVec(relic), false);
        player.setItemInMainHand(held.with(DataComponents.LODESTONE_TRACKER, tracker));
        player.sendMessage("<a>Your compass now points towards the nearest <b>Relic<a>!");
    }
}
