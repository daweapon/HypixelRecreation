package dev.daweapon.type.skyblockgeneric.trapper;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class TrapperService {
    private static final int SPAWN_ATTEMPTS = 60;
    private static final int ENDERMAN_SLAYER_LEVEL = 9;
    private static final int ENDERMAN_ELUSIVE_WEIGHT = 7;
    private static final String OPTION_ID = "trapper_task";

    private record Hunt(TrapperMob mob, long expiresAt, String zone) {
    }

    private static final Map<UUID, Hunt> HUNTS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> COOLDOWN_UNTIL = new ConcurrentHashMap<>();
    private static boolean running;

    private TrapperService() {
    }

    public static void offer(SkyBlockPlayer player, Consumer<String> say) {
        Hunt hunt = HUNTS.get(player.getUuid());
        if (hunt != null && !hunt.mob().isDead() && !hunt.mob().isRemoved()) {
            say.accept("You already have a mob spawned! Hunt that mob first!");
            return;
        }
        long wait = COOLDOWN_UNTIL.getOrDefault(player.getUuid(), 0L) - System.currentTimeMillis();
        if (wait > 0) {
            say.accept("Sorry I don't have any animals for you to hunt.");
            say.accept("Try coming back in <a>" + (int) Math.ceil(wait / 1000D) + "s<f>!");
            return;
        }
        long minutes = TrapperConfig.get().timeLimitMillis() / 60_000;
        say.accept("You will have " + minutes + " minutes to find the mob from when you accept the task.");
        say.accept("Any longer than that and the animal will run away!");
        player.sendMessage("<b><l>Accept the trapper's task to hunt the animal?");
        NPCOption.sendOption(player, OPTION_ID, false, List.of(
                new NPCOption.Option("yes", NamedTextColor.GREEN, true, "YES", _ -> start(player, say)),
                new NPCOption.Option("no", NamedTextColor.RED, true, "NO", _ -> say.accept("If you change your mind, you know where to find me!"))));
    }

    public static boolean hasHunt(SkyBlockPlayer player) {
        Hunt hunt = HUNTS.get(player.getUuid());
        return hunt != null && !hunt.mob().isDead() && !hunt.mob().isRemoved();
    }

    public static TrapperMob mobOf(SkyBlockPlayer player) {
        Hunt hunt = HUNTS.get(player.getUuid());
        return hunt == null || hunt.mob().isDead() || hunt.mob().isRemoved() ? null : hunt.mob();
    }

    static void start(SkyBlockPlayer player, Consumer<String> say) {
        if (hasHunt(player) || player.getInstance() == null) return;
        TrapperRarity rarity = rollRarity(player);
        TrapperAnimal animal = TrapperAnimal.values()[ThreadLocalRandom.current().nextInt(TrapperAnimal.values().length)];
        Instance instance = player.getInstance();
        List<TrapperConfig.Zone> zones = new ArrayList<>(TrapperConfig.get().zones());
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            TrapperConfig.Zone zone = zones.get(ThreadLocalRandom.current().nextInt(zones.size()));
            Pos spot = groundIn(instance, zone);
            if (spot == null) continue;
            TrapperMob mob = TrapperMob.create(rarity, animal, player);
            mob.setInstance(instance, spot);
            HUNTS.put(player.getUuid(), new Hunt(mob, System.currentTimeMillis() + TrapperConfig.get().timeLimitMillis(), zone.name()));
            ensureRunning();
            say.accept("You can find your " + rarity.coloured() + " <f>animal near the <b>" + zone.name() + "<f>.");
            return;
        }
        say.accept("I couldn't locate any animals. Come back in a little bit!");
    }

    private static Pos groundIn(Instance instance, TrapperConfig.Zone zone) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = random.nextInt(Math.min(zone.x1(), zone.x2()), Math.max(zone.x1(), zone.x2()) + 1);
        int z = random.nextInt(Math.min(zone.z1(), zone.z2()), Math.max(zone.z1(), zone.z2()) + 1);
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) instance.loadChunk(x >> 4, z >> 4).join();
        for (int y = zone.maxY(); y >= zone.minY(); y--) {
            Block block = instance.getBlock(x, y, z);
            if (block.isAir() || block.isLiquid() || !block.isSolid()) continue;
            if (!instance.getBlock(x, y + 1, z).isAir() || !instance.getBlock(x, y + 2, z).isAir()) return null;
            return new Pos(x + 0.5, y + 1, z + 0.5);
        }
        return null;
    }

    private static TrapperRarity rollRarity(SkyBlockPlayer player) {
        boolean topCrest = TrapperRewards.crestAtTopTier(player);
        boolean enderman = SlayerRequirement.level(player, SlayerType.VOIDGLOOM_SERAPH) >= ENDERMAN_SLAYER_LEVEL;
        double total = 0;
        double[] weights = new double[TrapperRarity.values().length];
        for (TrapperRarity rarity : TrapperRarity.values()) {
            double weight = rarity.getWeight();
            if (rarity == TrapperRarity.ELUSIVE && enderman) weight += ENDERMAN_ELUSIVE_WEIGHT;
            if (topCrest && (rarity == TrapperRarity.ENDANGERED || rarity == TrapperRarity.ELUSIVE)) weight *= TrapperRewards.CREST_ODDS_BONUS;
            weights[rarity.ordinal()] = weight;
            total += weight;
        }
        double roll = ThreadLocalRandom.current().nextDouble(total);
        for (TrapperRarity rarity : TrapperRarity.values()) {
            roll -= weights[rarity.ordinal()];
            if (roll < 0) return rarity;
        }
        return TrapperRarity.TRACKABLE;
    }

    private static synchronized void ensureRunning() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            long now = System.currentTimeMillis();
            HUNTS.forEach((id, hunt) -> {
                SkyBlockPlayer owner = hunt.mob().getOwner();
                boolean gone = owner.isRemoved() || hunt.mob().isDead() || hunt.mob().isRemoved();
                if (gone) {
                    HUNTS.remove(id, hunt);
                    if (!hunt.mob().isRemoved()) hunt.mob().remove();
                } else if (now >= hunt.expiresAt()) {
                    expire(hunt, owner, "<c>The animal ran away! <7>You took too long to hunt it.");
                }
            });
            return TaskSchedule.seconds(1);
        });
    }

    private static void expire(Hunt hunt, SkyBlockPlayer owner, String message) {
        HUNTS.remove(owner.getUuid(), hunt);
        hunt.mob().remove();
        COOLDOWN_UNTIL.put(owner.getUuid(), System.currentTimeMillis() + TrapperConfig.get().cooldownMillis());
        owner.sendMessage(message);
    }

    static void vanished(TrapperMob mob) {
        Hunt hunt = HUNTS.get(mob.getOwner().getUuid());
        if (hunt == null || hunt.mob() != mob) return;
        expire(hunt, mob.getOwner(), "<c>The " + mob.rarity().coloured() + " <c>animal saw you and vanished!");
    }

    static void killed(TrapperMob mob) {
        SkyBlockPlayer owner = mob.getOwner();
        Hunt hunt = HUNTS.get(owner.getUuid());
        if (hunt == null || hunt.mob() != mob || mob.vanished()) return;
        HUNTS.remove(owner.getUuid(), hunt);
        COOLDOWN_UNTIL.put(owner.getUuid(), System.currentTimeMillis() + TrapperConfig.get().cooldownMillis());
        reward(owner, mob.rarity(), mob.animal());
    }

    public static void reward(SkyBlockPlayer player, TrapperRarity rarity, TrapperAnimal animal) {
        int pelts = TrapperRewards.peltsFor(player, rarity);
        Pelts.add(player, pelts);
        TrapperRewards.creditCrest(player, pelts);
        player.getSkills().increase(player, SkillCategories.HUNTING, (double) rarity.getHuntingXp());
        player.addCoins(rarity.getCoins());
        player.sendMessage("<5><l>TRAPPER! <7>You hunted down the " + rarity.coloured() + " <7>" + animal.getDisplayName()
                + "<7> and earned <5>" + pelts + " Pelt" + (pelts == 1 ? "" : "s") + "<7>, <6>" + StringUtility.commaify(rarity.getCoins())
                + " Coin" + (rarity.getCoins() == 1 ? "" : "s") + " <7>and <b>" + StringUtility.commaify(rarity.getHuntingXp()) + " Hunting XP<7>!");
        if (LootRoll.roll(player.getUuid(), ItemType.DYE_PELT, rarity.dyeChancePercent())) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.DYE_PELT));
            player.sendMessage("<6><l>RARE DROP! <6>Pelt Dye");
        }
    }

    public static long cooldownRemainingSeconds(SkyBlockPlayer player) {
        return Math.max(0, (COOLDOWN_UNTIL.getOrDefault(player.getUuid(), 0L) - System.currentTimeMillis() + 999) / 1000);
    }

    public static void clearCooldown(SkyBlockPlayer player) {
        COOLDOWN_UNTIL.remove(player.getUuid());
    }
}
