package dev.daweapon.type.skyblockgeneric.user.statistics;

import dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazeFlares;
import net.minestom.server.entity.Player;
import java.util.*;
import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.deployable.DeployableBuffs;
import dev.daweapon.type.skyblockgeneric.deployable.DeployableMath;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.RegenerationValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.glacite.ColdService;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.Salvation;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks;
import dev.daweapon.type.skyblockgeneric.item.set.sets.terror.HydraStrike;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockProgressMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;

final class PlayerStatisticLoops {
    private static final Map<Player, BossBar> barCache = new HashMap<>();

    static void run() {
        barLoop();
        healthLoop();
        manaLoop();
        missionLoop();
        statisticsLoop();
        experiencedStatisticsLoop();
        speedLoop();
    }

    public static void experiencedStatisticsLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
                Thread.startVirtualThread(() -> {
                    List<String> experiencedStatistics = player.getSkyblockDataHandler().get(
                            SkyBlockDataHandler.Data.EXPERIENCED_STATISTICS, DatapointStringList.class
                    ).getValue();

                    ItemStatistics statistics = player.getStatistics().allStatistics();
                    for (ItemStatistic statistic : ItemStatistic.values()) {
                        if (experiencedStatistics.contains(statistic.name())) continue;

                        List<String> description = ItemStatistics.getDescription(statistic);

                        double experiencedValue = statistics.getOverall(statistic);
                        if (experiencedValue <= 0) continue;

                        experiencedStatistics.add(statistic.name());
                        player.getSkyblockDataHandler().get(
                                SkyBlockDataHandler.Data.EXPERIENCED_STATISTICS, DatapointStringList.class
                        ).setValue(experiencedStatistics);

                        player.sendMessage("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                        player.sendMessage("<6><l>NEW STAT DISCOVERED! <r>{}", statistic.getCompleteDisplayName());
                        player.sendMessage(" ");
                        player.sendMessage(Text.parse(String.join(" ", description).replace("<7>", "")));
                        player.sendMessage(" ");
                        player.sendMessage("<hover:'<e>Click to view the {} Wiki page!'><click:url:'https://wiki.hypixel.net/{}'><e><l>CLICK HERE <r><e>to learn more on the Official SkyBlock Wiki!</click></hover>",
                            statistic.getDisplayName(),
                            StringUtility.toNormalCase(statistic.name()).replace(" ", "_"));
                        player.sendMessage("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                    }
                });
            });
            return TaskSchedule.seconds(10);
        });
    }

    public static void barLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
                SkyBlockActionBar actionBar = SkyBlockActionBar.getFor(player);

                float absorption = player.getAdditionalHearts();
                int hearts = Math.round(player.getHealth() + absorption);
                int maxHearts = Math.round(player.getMaxHealth());
                actionBar.setDefaultDisplay(SkyBlockActionBar.BarSection.HEALTH, absorption > 0.0f
                        ? Text.of("<6>{}/{}❤", hearts, maxHearts)
                        : Text.of("<c>{}/{}❤", hearts, maxHearts));
                actionBar.setDefaultDisplay(SkyBlockActionBar.BarSection.DEFENSE,
                        player.getDefense() == 0 ? Text.empty()
                                : Text.of("<a>{}❈ Defense", Math.round(player.getDefense())));
                actionBar.setDefaultDisplay(SkyBlockActionBar.BarSection.MANA,
                        Text.of("<b>{}/{}✎ Mana", Math.round(player.getMana()), Math.round(player.getMaxMana())));
                actionBar.setDefaultDisplay(SkyBlockActionBar.BarSection.SET_BONUS, HydraStrike.display(player));
                actionBar.setDefaultDisplay(SkyBlockActionBar.BarSection.ABILITY_CHARGE, Salvation.display(player));

                player.sendActionBar(actionBar.render());
            });
            return TaskSchedule.tick(4);
        }, ExecutionType.TICK_END);
    }

    public static void statisticsLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
                Thread.startVirtualThread(() -> {
                    player.getStatistics().updateAccessoryStatistics();
                });
            });
            return TaskSchedule.seconds(2);
        });
    }

    public static void speedLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
                Thread.startVirtualThread(() -> {
                    double speed = Math.min(player.getStatistics().allStatistics().getOverall(ItemStatistic.SPEED), ColdService.speedCap(player));
                    speed = Math.min(speed, dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities.RanchersBootsSpeedCap.cap(player));
                    speed = dev.daweapon.type.skyblockgeneric.region.rules.TempleLockdown.limitSpeed(
                            dev.daweapon.type.skyblockgeneric.region.rules.TempleLockdown.isLocked(player), speed);
                    if (player.isSpeedManaged()) return; // if the Speed is currently managed by another source, don't override it
                    player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue((float) (speed / 1000));
                });
            });
            return TaskSchedule.tick(4);
        });
    }

    public static void healthLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
                if (player.getHealth() > player.getMaxHealth())
                    player.setHealth(player.getMaxHealth());

                if (player.getHealth() < player.getMaxHealth()) {
                    float healthToIncreaseBy = DeployableMath.regenPerPulse(player.getMaxHealth(),
                            player.getStatistics().allStatistics().getOverall(ItemStatistic.HEALTH_REGENERATION),
                            player.getStatistics().getHealthRegenerationPercentBonus());

                    RegenerationValueUpdateEvent event = new RegenerationValueUpdateEvent(player, healthToIncreaseBy);
                    SkyBlockValueEvent.callValueUpdateEvent(event);
                    healthToIncreaseBy = (float) event.getValue();

                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healthToIncreaseBy));
                }
            });
            return TaskSchedule.tick(30);
        }, ExecutionType.TICK_END);
    }

    public static void missionLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
                if (player.getRegion() == null) return;
                RegionType region = player.getRegion().getType();
                List<MissionData.ActiveMission> activeMissions = player.getMissionData().getActiveMissions(region);

                if (activeMissions.isEmpty()) {
                    if (barCache.containsKey(player)) {
                        BossBar oldBar = barCache.get(player);
                        player.hideBossBar(oldBar);
                        barCache.remove(player);
                    }
                    return;
                }
                MissionData.ActiveMission activeMission = activeMissions.getFirst();
                BossBar bar;

                if (activeMission.isProgress()) {
                    int maxProgress = ((SkyBlockProgressMission) MissionData.getMissionClass(activeMission)).getMaxProgress();
                    float progress = (float) activeMission.getMissionProgress() / maxProgress;

                    bar = BossBar.bossBar(
                            Text.of("Objective: <e>{}  <7>(<e>{}<7>/<a>{}<7>)",
                                    MissionData.getMissionClass(activeMission).getName(),
                                    activeMission.getMissionProgress(), maxProgress),
                            progress,
                            BossBar.Color.YELLOW,
                            BossBar.Overlay.PROGRESS);
                } else {
                    bar = BossBar.bossBar(
                            Text.of("Objective: <e>{}", MissionData.getMissionClass(activeMission).getName()),
                            1f,
                            BossBar.Color.YELLOW,
                            BossBar.Overlay.NOTCHED_6);
                }

                if (barCache.containsKey(player)) {
                    BossBar oldBar = barCache.get(player);
                    if (oldBar.equals(bar)) return;
                    player.hideBossBar(oldBar);
                }
                player.showBossBar(bar);
                barCache.put(player, bar);
            });
            return TaskSchedule.tick(30);
        }, ExecutionType.TICK_END);
    }

    public static void manaLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
                if (player.getMana() > player.getMaxMana())
                    player.setMana(player.getMaxMana());
                if (player.getMana() <= player.getMaxMana()) {
                    float manaPool = player.getMaxMana();
                    player.setMana(Math.min(manaPool, Math.min(manaPool, player.getMana() + (manaPool / 50) +
                            (int) ((manaPool / 50) * (player.getStatistics().getManaRegenerationPercentBonus()
                            + MiscPetHooks.manaRegenPercent(player) / 100D
                            + DeployableBuffs.manaRegenFraction(player)
                            + BlazeFlares.manaRegenFraction(player))))));
                }
            });
            return TaskSchedule.seconds(1);
        }, ExecutionType.TICK_END);
    }
}
