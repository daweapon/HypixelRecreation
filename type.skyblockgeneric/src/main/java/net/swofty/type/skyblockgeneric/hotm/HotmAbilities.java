package net.swofty.type.skyblockgeneric.hotm;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HotmAbilities {
    private static final Map<UUID, Map<HotmAbility, Long>> ACTIVE_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> MANIAC_STACKS = new ConcurrentHashMap<>();
    private static final int PICKOBULUS_RANGE = 40;

    private HotmAbilities() {
    }

    public static boolean isUnlocked(SkyBlockPlayer player, HotmAbility ability) {
        return HotmService.data(player).perkLevel(ability.perkId()) > 0;
    }

    public static HotmAbility selected(SkyBlockPlayer player) {
        HotmAbility selected = HotmAbility.byId(HotmService.data(player).getSelectedAbility());
        return selected != null && isUnlocked(player, selected) ? selected : null;
    }

    public static boolean select(SkyBlockPlayer player, HotmAbility ability) {
        if (!isUnlocked(player, ability)) return false;
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        data.setSelectedAbility(ability.name());
        HotmService.save(player, data);
        return true;
    }

    public static boolean isActive(SkyBlockPlayer player, HotmAbility ability) {
        Map<HotmAbility, Long> active = ACTIVE_UNTIL.get(player.getUuid());
        if (active == null) return false;
        Long until = active.get(ability);
        if (until == null) return false;
        if (until <= System.currentTimeMillis()) {
            active.remove(ability);
            if (ability == HotmAbility.MANIAC_MINER) MANIAC_STACKS.remove(player.getUuid());
            return false;
        }
        return true;
    }

    public static long cooldownRemainingMillis(SkyBlockPlayer player, HotmAbility ability) {
        Long readyAt = HotmService.data(player).getAbilityReadyAt().get(ability.name());
        return readyAt == null ? 0 : Math.max(0, readyAt - System.currentTimeMillis());
    }

    public static double cooldownSeconds(SkyBlockPlayer player, HotmAbility ability) {
        double seconds = ability.cooldown(HotmService.abilityLevel(player));
        if (HotmService.skyMallBuff(player) == HotmSkyMallBuff.COOLDOWN) seconds *= 0.8;
        return seconds;
    }

    public static boolean activate(SkyBlockPlayer player, SkyBlockItem held) {
        HotmAbility ability = selected(player);
        if (ability == null) return false;
        long remaining = cooldownRemainingMillis(player, ability);
        if (remaining > 0) {
            player.sendMessage("<c>This ability is on cooldown for {}s.", Math.round(remaining / 1000.0));
            return true;
        }
        int level = HotmService.abilityLevel(player);
        if (ability == HotmAbility.PICKOBULUS && !pickobulus(player, held, (int) ability.value(level))) {
            player.sendMessage("<c>You must be looking at a block to throw your pickaxe!");
            return true;
        }
        if (ability.hasDuration()) {
            ACTIVE_UNTIL.computeIfAbsent(player.getUuid(), id -> new ConcurrentHashMap<>())
                    .put(ability, System.currentTimeMillis() + ability.duration(level) * 1000L);
            if (ability == HotmAbility.MANIAC_MINER) MANIAC_STACKS.put(player.getUuid(), 0);
        }
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        data.getAbilityReadyAt().put(ability.name(), System.currentTimeMillis() + Math.round(cooldownSeconds(player, ability) * 1000));
        HotmService.save(player, data);
        player.sendMessage("<a>You used <6>{}<a>!", ability.getDisplayName());
        return true;
    }

    private static boolean pickobulus(SkyBlockPlayer player, SkyBlockItem held, int radius) {
        Point target = player.getTargetBlockPosition(PICKOBULUS_RANGE);
        if (target == null) return false;
        Instance instance = player.getInstance();
        if (instance == null) return false;
        int power = held.getAttributeHandler().getBreakingPower();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;
                    Point position = target.add(x, y, z);
                    Block block = instance.getBlock(position);
                    MineableBlock mineable = MineableBlock.get(block);
                    if (mineable == null || !isOre(mineable)) continue;
                    if (!mineable.getMiningHandler().canToolBreak(held)
                            || mineable.getMiningHandler().getMiningPowerRequirement() > power) continue;
                    EventDispatcher.call(new PlayerBlockBreakEvent(player, instance, block, Block.AIR,
                            new net.minestom.server.coordinate.BlockVec(position), BlockFace.TOP));
                }
            }
        }
        return true;
    }

    private static boolean isOre(MineableBlock block) {
        ItemStatistic specific = block.getBlockType().specificBlockFortune();
        return specific == ItemStatistic.ORE_FORTUNE || specific == ItemStatistic.DWARVEN_METAL_FORTUNE
                || specific == ItemStatistic.GEMSTONE_FORTUNE;
    }

    public static boolean tunnelVisionActive(SkyBlockPlayer player) {
        return isActive(player, HotmAbility.TUNNEL_VISION);
    }

    public static double tunnelVisionBonusPercent(SkyBlockPlayer player) {
        return tunnelVisionActive(player) ? HotmAbility.TUNNEL_VISION.value(HotmService.abilityLevel(player)) : 0;
    }

    public static double gemstoneInfusionMultiplier(SkyBlockPlayer player) {
        return isActive(player, HotmAbility.GEMSTONE_INFUSION)
                ? 1 + HotmAbility.GEMSTONE_INFUSION.value(HotmService.abilityLevel(player)) / 100 : 1;
    }

    public static int breakingPowerBonus(SkyBlockPlayer player) {
        return isActive(player, HotmAbility.MANIAC_MINER) ? 1 : 0;
    }

    public static boolean onBlockMined(SkyBlockPlayer player) {
        if (!isActive(player, HotmAbility.MANIAC_MINER)) return true;
        if (player.getMana() < HotmAbility.MANIAC_MINER_MANA_PER_BLOCK) return false;
        player.setMana(player.getMana() - HotmAbility.MANIAC_MINER_MANA_PER_BLOCK);
        MANIAC_STACKS.merge(player.getUuid(), 1, Integer::sum);
        return true;
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        int level = HotmService.abilityLevel(player);
        boolean any = false;
        if (isActive(player, HotmAbility.MINING_SPEED_BOOST)) {
            builder.withAdditivePercentage(ItemStatistic.MINING_SPEED, HotmAbility.MINING_SPEED_BOOST.value(level));
            any = true;
        }
        if (isActive(player, HotmAbility.SHEER_FORCE)) {
            builder.withBase(ItemStatistic.MINING_SPREAD, HotmAbility.SHEER_FORCE.value(level));
            any = true;
        }
        if (isActive(player, HotmAbility.MANIAC_MINER)) {
            double fortune = Math.min(HotmAbility.MANIAC_MINER_FORTUNE_CAP,
                    MANIAC_STACKS.getOrDefault(player.getUuid(), 0) * HotmAbility.MANIAC_MINER.value(level));
            builder.withBase(ItemStatistic.MINING_FORTUNE, fortune);
            any = true;
        }
        return any ? builder.build() : ItemStatistics.empty();
    }

    public static void clear(UUID player) {
        ACTIVE_UNTIL.remove(player);
        MANIAC_STACKS.remove(player);
    }
}
