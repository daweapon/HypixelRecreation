package dev.daweapon.type.skyblockgeneric.fishing;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryEconomyHooks;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetDrops;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.generic.HypixelConst;
import net.minestom.server.coordinate.Pos;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.skyblockgeneric.entity.FishingHook;
import dev.daweapon.type.skyblockgeneric.fishing.bait.FishingBaitService;
import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchAwardContext;
import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchPayload;
import dev.daweapon.type.skyblockgeneric.fishing.hotspot.FishingHotspotService;
import dev.daweapon.type.skyblockgeneric.fishing.item.FishingItemSupport;
import dev.daweapon.type.skyblockgeneric.fishing.resolver.FishingCatchResolver;
import dev.daweapon.type.skyblockgeneric.fishing.rod.FishingRodPartService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.FishingBaitComponent;
import dev.daweapon.type.skyblockgeneric.item.components.FishingRodPartComponent;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetEvents;
import org.jetbrains.annotations.Nullable;

// Orchestrates a fishing cast end-to-end: tracks active sessions, computes bite wait time, and
// resolves/awards the catch (bait consumption, sinker rolls, skill XP) when a hook is reeled in.
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FishingService {
    private static final Map<UUID, FishingSession> SESSIONS = new ConcurrentHashMap<>();

    public static FishingSession beginCast(SkyBlockPlayer player, SkyBlockItem rod, FishingMedium medium) {
        FishingBaitComponent bait = FishingBaitService.getFirstAvailableBait(player, medium);
        FishingSession session = new FishingSession(
            player.getUuid(),
            rod.getAttributeHandler().getPotentialType().name(),
            medium,
            bait == null ? null : bait.getItemId(),
            System.currentTimeMillis(),
            0L,
            0L,
            false,
            false
        );
        SESSIONS.put(player.getUuid(), session);
        if (rod.getAttributeHandler().getPotentialType() == dev.daweapon.commons.skyblock.item.ItemType.LEGEND_ROD) {
            player.getAchievementHandler().completeAchievement("skyblock.legendary_rod");
        }
        return session;
    }

    public static @Nullable FishingSession getSession(UUID playerUuid) {
        return SESSIONS.get(playerUuid);
    }

    public static void updateSession(FishingSession session) {
        SESSIONS.put(session.ownerUuid(), session);
    }

    public static void clearSession(UUID playerUuid) {
        SESSIONS.remove(playerUuid);
    }

    public static long computeWaitTicks(SkyBlockPlayer player, SkyBlockItem rod, @Nullable FishingBaitComponent bait, Pos bobber) {
        ItemStatistic speed = ItemStatistic.FISHING_SPEED;
        double fishingSpeed = player.getStatistics().allStatistics().getOverall(speed)
            + FishingRodPartService.getStatistics(rod).getOverall(speed)
            + FishingHotspotService.getActiveHotspotBuffs(player, rodMedium(rod), bobber).getOverall(speed);
        ItemType held = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType();
        if (held != rod.getAttributeHandler().getPotentialType()) {
            fishingSpeed += rod.getAttributeHandler().getStatistics().getOverall(speed);
        }
        if (bait != null) {
            SkyBlockItem baitItem = FishingItemSupport.getItem(bait.getItemId());
            if (baitItem != null) {
                fishingSpeed += baitItem.getAttributeHandler().getStatistics().getOverall(speed);
            }
            if ("CORRUPTED_BAIT".equals(bait.getItemId())) {
                fishingSpeed /= 2.0D;
            }
        }
        ServerType server = HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType();
        return FishingSpeed.waitTicks(fishingSpeed, FishingSpeed.cap(server), ThreadLocalRandom.current());
    }

    private static FishingMedium rodMedium(SkyBlockItem rod) {
        ItemType type = rod.getAttributeHandler().getPotentialType();
        var metadata = FishingItemSupport.getRodMetadata(type == null ? null : type.name());
        return metadata == null ? FishingMedium.WATER : metadata.getMedium();
    }

    public static @Nullable CatchPayload resolveCatch(SkyBlockPlayer player, SkyBlockItem rod, FishingHook hook) {
        FishingSession session = getSession(player.getUuid());
        if (session == null || session.resolved()) {
            return null;
        }

        FishingBaitComponent bait = FishingItemSupport.getBait(session.baitItemId());
        SkyBlockRegion region = player.getRegion();
        var hotspotBuffs = FishingHotspotService.getActiveHotspotBuffs(player, session.medium(), hook.getSpawnPosition());
        boolean hotspotActive = hasAnyBuffValue(hotspotBuffs);
        FishingContext context = new FishingContext(
            player,
            rod,
            session.medium(),
            bait,
            FishingRodPartService.getHook(rod),
            FishingRodPartService.getLine(rod),
            FishingRodPartService.getSinker(rod),
            region == null ? null : region.getType().name(),
            hook.getSpawnPosition(),
            hotspotActive,
            hotspotBuffs,
            System.currentTimeMillis() - session.castAt()
        );

        CatchPayload payload = FishingCatchResolver.resolve(context);
        if (payload == null) {
            return null;
        }

        consumeBait(player, rod, session);
        awardCatch(player, rod, hook, payload, context);
        updateSession(session.withResolved(true));
        return payload;
    }

    private static void consumeBait(SkyBlockPlayer player, SkyBlockItem rod, FishingSession session) {
        if (session.baitItemId() == null) {
            return;
        }
        if (rollBaitPreservation(rod) || AccessoryEconomyHooks.preservesBait(player)) {
            return;
        }
        FishingBaitService.consumeOneBait(player, session.baitItemId());
    }

    private static boolean rollBaitPreservation(SkyBlockItem rod) {
        var caster = rod.getAttributeHandler().getEnchantment(dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType.CASTER);
        if (caster != null && Math.random() * 100 < caster.level()) {
            return true;
        }
        FishingRodPartComponent sinker = FishingRodPartService.getSinker(rod);
        return sinker != null
                && sinker.getBaitPreservationChance() > 0
                && Math.random() * 100 < sinker.getBaitPreservationChance();
    }

    private static void awardCatch(SkyBlockPlayer player, SkyBlockItem rod, FishingHook hook, CatchPayload payload,
                                   FishingContext context) {
        markFirstFishToggle(player);
        rollSinkerMaterialize(player, rod);

        SkyBlockRegion region = player.getRegion();
        payload = PetEvents.fishCaught(player, payload, region == null ? null : region.getType().name(), context);
        payload.apply(new CatchAwardContext(player, rod, hook.getSpawnPosition()));
        dev.daweapon.type.skyblockgeneric.crystalhollows.FairyGrottoFishing.onCatch(player, region == null ? null : region.getType(), rodMedium(rod));
        GatheringPetDrops.fishCaught(player, rodMedium(rod), payload);

        if (!(payload instanceof CatchPayload.SeaCreature) && payload.skillXp() > 0) {
            player.getSkills().increase(player, dev.daweapon.type.skyblockgeneric.skill.SkillCategories.FISHING, payload.skillXp());
        }

        player.setItemInHand(rod);
    }

    private static void markFirstFishToggle(SkyBlockPlayer player) {
        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_CAUGHT_FIRST_FISH)) {
            player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_CAUGHT_FIRST_FISH, true);
        }
    }

    private static void rollSinkerMaterialize(SkyBlockPlayer player, SkyBlockItem rod) {
        FishingRodPartComponent sinker = FishingRodPartService.getSinker(rod);
        if (sinker == null || sinker.getMaterializedItemId() == null) {
            return;
        }
        dev.daweapon.commons.skyblock.item.ItemType materialized =
                dev.daweapon.commons.skyblock.item.ItemType.valueOf(sinker.getMaterializedItemId());
        if (LootRoll.roll(player.getUuid(), materialized, sinker.getMaterializedChance() * 100)) {
            player.addAndUpdateItem(materialized);
        }
    }

    private static boolean hasAnyBuffValue(dev.daweapon.commons.skyblock.statistics.ItemStatistics statistics) {
        for (dev.daweapon.commons.skyblock.statistics.ItemStatistic statistic : dev.daweapon.commons.skyblock.statistics.ItemStatistic.values()) {
            if (statistics.getOverall(statistic) != 0.0D) {
                return true;
            }
        }
        return false;
    }
}
