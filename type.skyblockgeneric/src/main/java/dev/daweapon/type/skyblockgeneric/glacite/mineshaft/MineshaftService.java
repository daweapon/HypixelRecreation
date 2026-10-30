package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMineshaftPity;
import dev.daweapon.type.skyblockgeneric.entity.TextDisplayEntity;
import dev.daweapon.type.skyblockgeneric.glacite.ColdService;
import dev.daweapon.type.skyblockgeneric.glacite.GlaciteZones;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import dev.daweapon.type.skyblockgeneric.region.mining.MiningSpread;
import dev.daweapon.type.skyblockgeneric.user.AreaSidebar;
import dev.daweapon.type.skyblockgeneric.user.PlayerDeathOverrides;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class MineshaftService {
    public static final int MAX_CONCURRENT = 8;
    public static final long PORTAL_MILLIS = 30_000;
    public static final long REENTRY_LOCK_MILLIS = 30_000;
    public static final long EMPTY_GRACE_MILLIS = 15_000;
    public static final long MAX_LIFETIME_MILLIS = 2L * 60 * 60 * 1000;
    public static final String SURVEYOR = "surveyor";
    public static final String CRYSTALLINE = "crystalline";
    public static final String VANGUARD_SEEKER = "vanguard_seeker";
    public static final String NO_STONE_UNTURNED = "no_stone_unturned";
    public static final String MINESHAFT_MAYHEM = "mineshaft_mayhem";

    private static final Map<UUID, MineshaftInstance> BY_INSTANCE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_ENTRY = new ConcurrentHashMap<>();
    private static boolean started;

    private MineshaftService() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(MineshaftService::tick).repeat(TaskSchedule.seconds(1)).schedule();
        AreaSidebar.registerDetail(player -> {
            MineshaftInstance shaft = current(player);
            return shaft == null ? null : List.of(Text.of("<8>{}", shaft.layout().code()));
        });
        PlayerDeathOverrides.register(MineshaftService::onDeath);
    }

    public static @Nullable MineshaftInstance current(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        return instance == null ? null : BY_INSTANCE.get(instance.getUuid());
    }

    public static @Nullable MineshaftInstance of(Instance instance) {
        return instance == null ? null : BY_INSTANCE.get(instance.getUuid());
    }

    public static boolean isInside(SkyBlockPlayer player) {
        return current(player) != null;
    }

    public static int activeShafts() {
        return BY_INSTANCE.size();
    }

    public static DatapointMineshaftPity.PityData pityData(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MINESHAFT_PITY, DatapointMineshaftPity.class).getValue();
    }

    public static void savePity(SkyBlockPlayer player, DatapointMineshaftPity.PityData data) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.MINESHAFT_PITY, DatapointMineshaftPity.class).setValue(data);
    }

    public static int pityCounter(SkyBlockPlayer player) {
        return pityData(player).getCounter();
    }

    public static void setDisplayedPity(SkyBlockPlayer player, int displayed) {
        DatapointMineshaftPity.PityData data = pityData(player);
        data.setCounter(MineshaftPity.START - Math.max(0, Math.min(MineshaftPity.START, displayed)));
        savePity(player, data);
    }

    public static double chanceBonusPercent(SkyBlockPlayer player) {
        double bonus = HotmService.perkValue(player, SURVEYOR);
        if (HotmService.data(player).getCoreLevel() >= MineshaftPity.CORE_BONUS_LEVEL) bonus += MineshaftPity.CORE_BONUS_PERCENT;
        return bonus;
    }

    public static void onBlockMined(SkyBlockPlayer player, MineableBlock block, @Nullable RegionType region) {
        if (MineshaftBlocks.quality(block) <= 0) return;
        MineshaftInstance shaft = current(player);
        if (shaft != null) {
            rollScrap(player, shaft);
            return;
        }
        if (GlaciteZones.isColdZone(region) && !GlaciteZones.isMineshaft(region)) onTunnelsBlockMined(player, block);
    }

    private static void onTunnelsBlockMined(SkyBlockPlayer player, MineableBlock block) {
        if (MineshaftPortal.isActive(player.getUuid())) return;
        DatapointMineshaftPity.PityData data = pityData(player);
        long now = System.currentTimeMillis();
        boolean frozen = MineshaftPity.frozen(data.getExitedAt(), data.getExitedServer(), HypixelConst.getServerName(), now);
        MineshaftPity.Outcome outcome = MineshaftPity.step(player.getUuid(), data.getCounter(),
                MineshaftBlocks.quality(block, MiningSpread.isSpreading()), chanceBonusPercent(player), ThreadLocalRandom.current());
        if (!outcome.spawned() && frozen) return;
        data.setCounter(outcome.counter());
        savePity(player, data);
        if (outcome.spawned()) MineshaftPortal.spawn(player, rollLayout(player, ShaftType.matching(block)));
    }

    public static ShaftLayout rollLayout(SkyBlockPlayer player, @Nullable ShaftType matched) {
        return MineshaftTypeRoller.roll(player.getUuid(), matched, HotmService.perkValue(player, VANGUARD_SEEKER),
                Math.min(100, MineshaftTypeRoller.BASE_CRYSTAL_PERCENT + HotmService.perkValue(player, CRYSTALLINE)),
                ThreadLocalRandom.current());
    }

    private static void rollScrap(SkyBlockPlayer player, MineshaftInstance shaft) {
        MayhemBuff buff = shaft.buff(player.getUuid());
        double mayhem = buff == MayhemBuff.SCRAP_CHANCE ? MayhemBuff.SCRAP_BONUS_PERCENT : 0;
        double noStone = HotmService.perkValue(player, NO_STONE_UNTURNED);
        if (!SuspiciousScrap.roll(player.getUuid(), shaft.scrapsFound(player.getUuid()), ThreadLocalRandom.current(), noStone, mayhem)) return;
        shaft.addScrap(player.getUuid());
        SuspiciousScrap.award(player);
    }

    public static void recordCorpseLooted(SkyBlockPlayer player) {
        MineshaftInstance shaft = current(player);
        if (shaft != null) shaft.addCorpse(player.getUuid());
    }

    public static double coldVulnerability(SkyBlockPlayer player) {
        MineshaftInstance shaft = current(player);
        return shaft == null ? 0 : MineshaftCold.vulnerability(shaft.elapsedMillis(System.currentTimeMillis()));
    }

    public static @Nullable String entryRefusal(SkyBlockPlayer player) {
        long since = System.currentTimeMillis() - LAST_ENTRY.getOrDefault(player.getUuid(), 0L);
        if (since < REENTRY_LOCK_MILLIS) return "You can't enter a Mineshaft this soon after entering another one!";
        if (BY_INSTANCE.size() >= MAX_CONCURRENT) return "All the Mineshafts on this server are busy, try again in a moment!";
        return null;
    }

    public static void enterFromPortal(SkyBlockPlayer player, ShaftLayout layout) {
        String refusal = entryRefusal(player);
        if (refusal != null) {
            player.sendMessage("<c>" + refusal);
            MineshaftPortal.remove(player.getUuid());
            return;
        }
        MineshaftPortal.remove(player.getUuid());
        enter(player, layout);
    }

    public static boolean enter(SkyBlockPlayer player, ShaftLayout layout) {
        if (isInside(player)) {
            player.sendMessage("<c>You're already inside a Mineshaft!");
            return false;
        }
        long now = System.currentTimeMillis();
        LAST_ENTRY.put(player.getUuid(), now);
        MineshaftWorld world = MineshaftWorld.create(layout);
        MineshaftInstance shaft = new MineshaftInstance(player.getUuid(), layout, world, player.getInstance());
        BY_INSTANCE.put(world.instance().getUuid(), shaft);
        decorate(shaft);
        populateCorpses(shaft, player);
        arrive(player, shaft, now);
        DatapointMineshaftPity.PityData pity = pityData(player);
        pity.setShaftsEntered(pity.getShaftsEntered() + 1);
        savePity(player, pity);
        player.setInstance(world.instance(), MineshaftPlaceholderLayout.ENTRANCE).thenRun(() -> {
            player.sendMessage("<7>You entered a <b>{}<7> Glacite Mineshaft <8>({})", layout.type().displayName(), layout.code());
            MayhemBuff buff = shaft.buff(player.getUuid());
            if (buff != null) player.sendMessage("<d><l>MINESHAFT MAYHEM! <r><7>You gained {}<7>!", buff.description());
        });
        return true;
    }

    public static boolean admit(SkyBlockPlayer player, MineshaftInstance shaft) {
        if (shaft.isDead(player.getUuid())) {
            player.sendMessage("<c>You died in that Mineshaft and can't go back in!");
            return false;
        }
        long now = System.currentTimeMillis();
        LAST_ENTRY.put(player.getUuid(), now);
        arrive(player, shaft, now);
        player.setInstance(shaft.instance(), MineshaftPlaceholderLayout.ENTRANCE);
        return true;
    }

    private static void arrive(SkyBlockPlayer player, MineshaftInstance shaft, long now) {
        UUID id = player.getUuid();
        shaft.markEntered(id, now);
        shaft.setAnnouncedSteps(id, MineshaftCold.steps(shaft.elapsedMillis(now)));
        if (HotmService.hasPerk(player, MINESHAFT_MAYHEM) && shaft.buff(id) == null) {
            shaft.setBuff(id, MayhemBuff.roll(id, ThreadLocalRandom.current()));
        }
        DatapointHOTM.PlayerHOTMData hotm = HotmService.data(player);
        hotm.getAbilityReadyAt().clear();
        HotmService.save(player, hotm);
    }

    private static void populateCorpses(MineshaftInstance shaft, SkyBlockPlayer owner) {
        try {
            CorpseService.populate(shaft.instance(), shaft.layout().code(), owner);
        } catch (RuntimeException exception) {
            Logger.error(exception, "Couldn't populate the {} mineshaft with corpses", shaft.layout().code());
        }
    }

    private static void decorate(MineshaftInstance shaft) {
        InteractionEntity exit = new InteractionEntity(1.4f, 2f, (clicker, _) -> exit((SkyBlockPlayer) clicker, true));
        TextDisplayEntity title = new TextDisplayEntity("<c><l>LEAVE MINESHAFT", meta -> {});
        TextDisplayEntity rewards = new TextDisplayEntity("<7>Leaving grants <b>Glacite Powder<7>: <b>1,000 <7>+ <b>500<7>/scrap, <b>250<7>/corpse, <b>10<7>/Cold", meta -> {});
        exit.setInstance(shaft.instance(), MineshaftPlaceholderLayout.EXIT_MARKER);
        title.setInstance(shaft.instance(), MineshaftPlaceholderLayout.EXIT_MARKER.add(0, 2.6, 0));
        rewards.setInstance(shaft.instance(), MineshaftPlaceholderLayout.EXIT_MARKER.add(0, 2.2, 0));
        for (Entity entity : new Entity[]{exit, title, rewards}) shaft.addDecoration(entity);
    }

    public static void exit(SkyBlockPlayer player, boolean withRewards) {
        MineshaftInstance shaft = current(player);
        if (shaft == null) return;
        if (withRewards) payLeaveRewards(player, shaft);
        leave(player, shaft);
    }

    private static void payLeaveRewards(SkyBlockPlayer player, MineshaftInstance shaft) {
        UUID id = player.getUuid();
        long base = MineshaftRewards.leavePowder(shaft.scrapsFound(id), shaft.corpsesLooted(id), -ColdService.cold(player));
        long gained = HotmService.gainPowder(player, HotmPowder.GLACITE, base);
        player.sendMessage("<b><l>MINESHAFT! <r><7>You left the Mineshaft and earned <b>{} Glacite Powder<7>.", String.format("%,d", gained));
    }

    private static void leave(SkyBlockPlayer player, MineshaftInstance shaft) {
        shaft.markDeparted(player.getUuid());
        DatapointMineshaftPity.PityData pity = pityData(player);
        pity.setExitedAt(System.currentTimeMillis());
        pity.setExitedServer(HypixelConst.getServerName());
        savePity(player, pity);
        Instance target = shaft.origin() != null && shaft.origin() != shaft.instance() ? shaft.origin() : HypixelConst.getInstanceContainer();
        player.setInstance(target, ColdService.BASE_CAMP_SPAWN);
    }

    public static void freeze(SkyBlockPlayer player) {
        MineshaftInstance shaft = current(player);
        if (shaft == null) return;
        shaft.markDead(player.getUuid());
        ColdService.reset(player);
        player.sendMessage("<7>You froze to death.");
        player.sendMessage("<7><o>Maybe next time you'll find a way to escape the cold...");
        leave(player, shaft);
    }

    private static boolean onDeath(SkyBlockPlayer player) {
        MineshaftInstance shaft = current(player);
        if (shaft == null) return false;
        shaft.markDead(player.getUuid());
        ColdService.reset(player);
        player.setHealth(player.getMaxHealth());
        player.sendMessage("<c>☠ <7>You died in the Mineshaft and can't go back in.");
        leave(player, shaft);
        return true;
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        for (MineshaftInstance shaft : List.copyOf(BY_INSTANCE.values())) {
            try {
                tickShaft(shaft, now);
            } catch (RuntimeException exception) {
                Logger.error(exception, "Couldn't tick the {} mineshaft", shaft.layout().code());
            }
        }
    }

    private static void tickShaft(MineshaftInstance shaft, long now) {
        var players = shaft.instance().getPlayers();
        if (players.isEmpty()) {
            if (now - shaft.lastEnteredAt() > EMPTY_GRACE_MILLIS) destroy(shaft);
            return;
        }
        boolean expired = shaft.elapsedMillis(now) > MAX_LIFETIME_MILLIS;
        int steps = MineshaftCold.steps(shaft.elapsedMillis(now));
        for (Player online : List.copyOf(players)) {
            SkyBlockPlayer player = (SkyBlockPlayer) online;
            if (expired) {
                player.sendMessage("<c>This Mineshaft collapsed!");
                leave(player, shaft);
                continue;
            }
            if (steps > shaft.announcedSteps(player.getUuid())) {
                shaft.setAnnouncedSteps(player.getUuid(), steps);
                player.sendMessage(MineshaftCold.STEP_MESSAGE);
                player.playSound(Sound.sound(Key.key("entity.player.hurt_freeze"), Sound.Source.PLAYER, 1f, 1f));
            }
        }
    }

    private static void destroy(MineshaftInstance shaft) {
        BY_INSTANCE.remove(shaft.instance().getUuid());
        shaft.removeDecorations();
        shaft.world().destroy();
    }
}
