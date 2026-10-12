package net.swofty.type.skyblockgeneric.reforge;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.skyblock.item.reforge.Reforge;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointInventory;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import net.swofty.type.skyblockgeneric.glacite.ColdService;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockInventory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class ReforgeLoadout {
    private ReforgeLoadout() {
    }

    private record PlayerWorld(SkyBlockPlayer player) implements ReforgeWorld {
        @Override
        public ServerType server() {
            return HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType();
        }

        @Override
        public double cold() {
            return ColdService.cold(player);
        }

        @Override
        public int armorThornsTiers() {
            return player.getStatistics().getAllOfEnchants(EnchantmentType.THORNS, false).entrySet().stream()
                    .mapToInt(entry -> entry.getKey() * entry.getValue()).sum();
        }
    }

    public static ItemStatistics statistics(SkyBlockItem item, SkyBlockPlayer player) {
        Reforge reforge = reforgeOf(item);
        if (reforge == null || player == null) return ItemStatistics.empty();
        return ReforgeAbilities.active(reforge)
                .map(ability -> ReforgeEffects.statistics(List.of(ability), new ReforgeContext(new PlayerWorld(player),
                        reforge.getAfterCalculation(ItemStatistics.empty(),
                                item.getAttributeHandler().getRarity().ordinal() + 1))))
                .orElse(ItemStatistics.empty());
    }

    public static double outgoingMultiplier(SkyBlockPlayer player, SkyBlockMob mob, boolean critical) {
        List<ReforgeAbility> abilities = abilities(player);
        if (abilities.isEmpty()) return 1;
        return ReforgeEffects.outgoingMultiplier(abilities, new ReforgeHit.Outgoing(player.getUuid(),
                mobTypes(mob), critical, ThreadLocalRandom.current()));
    }

    public static double incomingMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
        List<ReforgeAbility> abilities = abilities(player);
        return abilities.isEmpty() ? 1 : ReforgeEffects.incomingMultiplier(abilities, incoming(mob));
    }

    public static double defenseMultiplier(SkyBlockPlayer player, SkyBlockMob mob, double defense) {
        List<ReforgeAbility> abilities = abilities(player);
        return abilities.isEmpty() ? 1 : ReforgeEffects.defenseMultiplier(abilities, incoming(mob), defense);
    }

    public static double miningExperienceMultiplier(SkyBlockPlayer player) {
        return ReforgeEffects.miningExperienceMultiplier(abilities(player));
    }

    public static long titaniumMithrilPowder(SkyBlockPlayer player) {
        return ReforgeEffects.titaniumMithrilPowder(abilities(player));
    }

    public static double harvestCoins(SkyBlockPlayer player) {
        return ReforgeEffects.harvestCoins(abilities(player));
    }

    public static void onTransmission(SkyBlockPlayer player) {
        List<ReforgeAbility> abilities = abilities(player);
        ItemStatistics boost = ReforgeEffects.transmissionBoost(abilities);
        if (boost.toString().isEmpty()) return;
        long expiration = System.currentTimeMillis() + ReforgeEffects.transmissionDurationMillis(abilities);
        player.getStatistics().boostStatistic(new TemporaryStatistic(boost, expiration));
    }

    public static double hunterFortune(SkyBlockPlayer player, AttributeDefinition hunted) {
        return ReforgeEffects.hunterFortune(abilities(player), hunted.category());
    }

    public static int effectiveEnchantLevel(SkyBlockPlayer player, EnchantmentType type, int level) {
        int bonus = ReforgeEffects.enchantLevelBonus(abilities(player), type);
        if (bonus == 0) return level;
        int maximum = type.getEnch().getLevelsToApply(player).maximumLevel();
        return Math.max(level, Math.min(level + bonus, maximum));
    }

    public static int extraBurrows(SkyBlockItem spade) {
        return ReforgeAbilities.active(reforgeOf(spade)).map(ability -> ReforgeEffects.extraBurrows(List.of(ability))).orElse(0);
    }

    public static double candyDropBonus(SkyBlockPlayer player) {
        return ReforgeEffects.candyDropBonus(abilities(player));
    }

    public static long forestWhispers(SkyBlockPlayer player, long awarded) {
        double extra = awarded * ReforgeEffects.forestWhisperFraction(abilities(player));
        long whole = (long) extra;
        return whole + (ThreadLocalRandom.current().nextDouble() < extra - whole ? 1 : 0);
    }

    public static List<SkyBlockItem> items(SkyBlockPlayer player) {
        List<SkyBlockItem> items = new ArrayList<>();
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());
        items.add(cache.get(PlayerItemOrigin.MAIN_HAND));
        items.add(cache.get(PlayerItemOrigin.HELMET));
        items.add(cache.get(PlayerItemOrigin.CHESTPLATE));
        items.add(cache.get(PlayerItemOrigin.LEGGINGS));
        items.add(cache.get(PlayerItemOrigin.BOOTS));
        SkyBlockInventory inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        for (UnderstandableSkyBlockItem equipment : List.of(inventory.getNecklace(), inventory.getCloak(),
                inventory.getBelt(), inventory.getGloves())) {
            items.add(new SkyBlockItem(equipment));
        }
        return items;
    }

    private static List<ReforgeAbility> abilities(SkyBlockPlayer player) {
        List<ReforgeAbility> abilities = new ArrayList<>();
        for (SkyBlockItem item : items(player)) {
            ReforgeAbilities.active(reforgeOf(item)).ifPresent(abilities::add);
        }
        return abilities;
    }

    private static Reforge reforgeOf(SkyBlockItem item) {
        return item == null || item.isAir() ? null : item.getAttributeHandler().getReforge();
    }

    private static Set<MobType> mobTypes(SkyBlockMob mob) {
        EnumSet<MobType> types = EnumSet.noneOf(MobType.class);
        types.addAll(mob.getMobTypes());
        return types;
    }

    private static ReforgeHit.Incoming incoming(SkyBlockMob mob) {
        return new ReforgeHit.Incoming(mobTypes(mob), mob instanceof SeaCreatureMob);
    }
}
