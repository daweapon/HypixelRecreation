package dev.daweapon.type.skyblockgeneric.event.actions.custom.achievement;

import net.minestom.server.entity.EntityType;
import net.minestom.server.event.player.PlayerTickEvent;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.achievement.PlayerAchievementHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobVanquisher;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import dev.daweapon.type.skyblockgeneric.event.custom.CrimsonMinibossKilledEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.fishing.lava.LavaSeaCreatureMob;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.registry.SeaCreatureDefinition;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.set.ArmorSetRegistry;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;

import java.util.List;

public class ActionCombatAchievements implements HypixelEventClass {
    private static final int CHECK_INTERVAL_TICKS = 40;
    private static final int BIG_GAME_FISHING_LEVEL = 20;
    private static final double THE_FLASH_SPEED = 500;
    private static final int PEAK_OF_THE_MOUNTAIN_TIER = 5;
    private static final String MINIBOSS_KILLED_KEY = "miniboss:killed:";
    private static final List<CrimsonMiniboss> MINIBOSSES = List.of(CrimsonMiniboss.BLADESOUL,
            CrimsonMiniboss.MAGE_OUTLAW, CrimsonMiniboss.BARBARIAN_DUKE_X, CrimsonMiniboss.ASHFANG);

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onTick(PlayerTickEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (player.getAliveTicks() % CHECK_INTERVAL_TICKS != 0) return;
        if (SkyBlockDataHandler.getUser(player) == null) return;
        PlayerAchievementHandler achievements = player.getAchievementHandler();

        if (!achievements.hasAchievement("skyblock.glass_cannon")
                && player.getArmorSet() == ArmorSetRegistry.ELEGANT_TUXEDO) {
            achievements.completeAchievement("skyblock.glass_cannon");
        }
        if (!achievements.hasAchievement("skyblock.the_flash")
                && player.getStatistics().allStatistics().getOverall(ItemStatistic.SPEED) >= THE_FLASH_SPEED) {
            achievements.completeAchievement("skyblock.the_flash");
        }
        if (!achievements.hasAchievement("skyblock.peak_of_the_mountain")) {
            DatapointHOTM.PlayerHOTMData hotm = player.getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).getValue();
            if (hotm.getTier() >= PEAK_OF_THE_MOUNTAIN_TIER) {
                achievements.completeAchievement("skyblock.peak_of_the_mountain");
            }
        }
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        SkyBlockMob mob = event.getKilledMob();
        PlayerAchievementHandler achievements = player.getAchievementHandler();
        ItemType held = PlayerItemOrigin.getFromCache(player.getUuid()).get(PlayerItemOrigin.MAIN_HAND)
                .getAttributeHandler().getPotentialType();

        if (mob.getEntityType() == EntityType.SQUID && held == ItemType.PRISMARINE_BLADE) {
            achievements.completeAchievement("skyblock.water_blade");
        }
        if (mob.getEntityType() == EntityType.ZOMBIE && held == ItemType.ZOMBIE_SWORD) {
            achievements.completeAchievement("skyblock.it_never_ends");
        }
        if (mob.getEntityType() == EntityType.PIG && held == ItemType.PIGMAN_SWORD) {
            achievements.completeAchievement("skyblock.this_is_fair");
        }
        if (mob instanceof MobVanquisher) {
            achievements.completeAchievement("skyblock.vanquished");
        }
        if (requiredFishingLevel(mob) >= BIG_GAME_FISHING_LEVEL) {
            achievements.completeAchievement("skyblock.big_game_fisher");
        }
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onMiniboss(CrimsonMinibossKilledEvent event) {
        SkyBlockPlayer player = event.player();
        PlayerAchievementHandler achievements = player.getAchievementHandler();
        CrimsonMiniboss miniboss = event.miniboss();

        if (miniboss == CrimsonMiniboss.MAGMA_BOSS) {
            achievements.completeAchievement("skyblock.cute_little_cube");
            return;
        }

        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(MINIBOSS_KILLED_KEY + miniboss.name(), 1L));
        DatapointCrimsonIsle.CrimsonIsleData data = DatapointCrimsonIsle.of(player);
        if (MINIBOSSES.stream().allMatch(each -> data.getNumber(MINIBOSS_KILLED_KEY + each.name()) > 0)) {
            achievements.completeAchievement("skyblock.i_own_this_place");
        }
    }

    private static int requiredFishingLevel(SkyBlockMob mob) {
        String id = switch (mob) {
            case SeaCreatureMob seaCreature -> seaCreature.getSeaCreatureId();
            case LavaSeaCreatureMob lava -> lava.getCreature().name();
            default -> null;
        };
        if (id == null) return 0;
        SeaCreatureDefinition definition = FishingRegistry.getSeaCreature(id);
        return definition == null ? 0 : definition.requiredFishingLevel();
    }
}
