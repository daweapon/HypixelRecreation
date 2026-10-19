package dev.daweapon.type.skyblockgeneric.miningevent;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobCreeperlobber;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobMurderlover;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobPitfighter;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobRaidFireslinger;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobRaidWeakling;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobSuperprotectron;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class GoblinRaidEvent extends MiningEvent {
    private static final int MAX_LIVE_MOBS = 120;
    private static final int WEAKLING_WEIGHT = 40;
    private static final int FIRESLINGER_WEIGHT = 25;
    private static final int CREEPERLOBBER_WEIGHT = 12;
    private static final int PITFIGHTER_WEIGHT = 12;

    private final Set<SkyBlockMob> mobs = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<UUID, Integer> personalKills = new HashMap<>();
    private MiningEventConfig.Site site;
    private int goal;
    private int kills;
    private int wave;
    private long nextWaveAt;
    private boolean won;

    GoblinRaidEvent(MiningEventZone zone) {
        super(MiningEventType.GOBLIN_RAID, zone);
    }

    public static @Nullable GoblinRaidEvent active() {
        return MiningEvents.current().filter(GoblinRaidEvent.class::isInstance).map(GoblinRaidEvent.class::cast).orElse(null);
    }

    @Override
    protected void onStart() {
        MiningEventConfig config = MiningEventConfig.get();
        site = config.singleSite("goblin_raid", "site");
        goal = config.integer(500, "goblin_raid", "goal");
        int threshold = config.integer(25, "goblin_raid", "personal_threshold");
        MiningEventMessages.banner(type(), "GOBLIN RAID STARTED!", "Mining Event", List.of(
                "<f>Kill all <c>" + goal + " goblins <f>to win!",
                "<f>Earn extra rewards by slaying at least <c>" + threshold + " <f>yourself!",
                " ",
                "<hover:'<e>Teleport to Old Man Garry'><click:run:'/tptogarry'><e><l>Go to The Forge (or talk to Garry)</click></hover>"));
        nextWaveAt = System.currentTimeMillis();
    }

    @Override
    public void tick(long now) {
        mobs.removeIf(mob -> mob.isRemoved() || mob.isDead());
        if (now >= nextWaveAt) {
            nextWaveAt = now + (long) (MiningEventConfig.get().number(12, "goblin_raid", "wave_seconds") * 1000);
            spawnWave();
        }
    }

    @Override
    protected void onEnd(boolean cancelled) {
        MiningEventConfig config = MiningEventConfig.get();
        int threshold = config.integer(25, "goblin_raid", "personal_threshold");
        new java.util.ArrayList<>(mobs).forEach(mob -> {
            if (!mob.isRemoved()) mob.remove();
        });
        mobs.clear();
        boolean success = kills >= goal;
        MiningEventMessages.banner(type(), "GOBLIN RAID ENDED!", null, List.of(
                success ? "<f>The goblins were driven back! <c>" + StringUtility.commaify(kills) + " <f>were slain."
                        : "<f>The goblins got away. Only <c>" + StringUtility.commaify(kills) + " <f>of <c>" + goal + " <f>were slain."));
        for (SkyBlockPlayer player : MiningEvents.players()) {
            int personal = personalKills.getOrDefault(player.getUuid(), 0);
            if (personal <= 0) {
                player.sendMessage("<7>You didn't participate in this event, so you got no rewards.");
                continue;
            }
            String key = (personal >= threshold ? "high" : "low") + "_" + (success ? "success" : "fail");
            player.sendMessage("<7>You slew <c>{} <7>goblins.", personal);
            MiningEventRewards.grant(player, config.tier("rewards", "goblin_raid", key), personal);
        }
    }

    @Override
    public @Nullable Pos warpLocation() {
        return site == null ? null : EventSites.center(HypixelConst.getInstanceContainer(), site);
    }

    public boolean isRaider(SkyBlockMob mob) {
        return mobs.contains(mob);
    }

    public void onKill(SkyBlockPlayer player, SkyBlockMob mob) {
        if (!mobs.remove(mob)) return;
        int worth = mob instanceof MobSuperprotectron ? MiningEventConfig.get().integer(10, "goblin_raid", "superprotectron_kills") : 1;
        kills += worth;
        personalKills.merge(player.getUuid(), worth, Integer::sum);
        markParticipant(player);
        if (!won && kills >= goal) {
            won = true;
            shortenTo(System.currentTimeMillis());
        }
    }

    private void spawnWave() {
        MiningEventConfig config = MiningEventConfig.get();
        Instance instance = HypixelConst.getInstanceContainer();
        wave++;
        int size = config.integer(14, "goblin_raid", "wave_size");
        int every = config.integer(5, "goblin_raid", "superprotectron_every_waves");
        for (int index = 0; index < size && mobs.size() < MAX_LIVE_MOBS; index++) spawn(instance, randomRaider());
        if (wave % every == 0) spawn(instance, MobSuperprotectron::new);
    }

    private void spawn(Instance instance, Supplier<? extends SkyBlockMob> factory) {
        SkyBlockMob mob = factory.get();
        mob.setInstance(instance, EventSites.near(instance, site));
        mobs.add(mob);
    }

    private static Supplier<? extends SkyBlockMob> randomRaider() {
        int roll = ThreadLocalRandom.current().nextInt(WEAKLING_WEIGHT + FIRESLINGER_WEIGHT + CREEPERLOBBER_WEIGHT + PITFIGHTER_WEIGHT + 8);
        if ((roll -= WEAKLING_WEIGHT) < 0) return MobRaidWeakling::new;
        if ((roll -= FIRESLINGER_WEIGHT) < 0) return MobRaidFireslinger::new;
        if ((roll -= CREEPERLOBBER_WEIGHT) < 0) return MobCreeperlobber::new;
        if ((roll -= PITFIGHTER_WEIGHT) < 0) return MobPitfighter::new;
        return MobMurderlover::new;
    }
}
