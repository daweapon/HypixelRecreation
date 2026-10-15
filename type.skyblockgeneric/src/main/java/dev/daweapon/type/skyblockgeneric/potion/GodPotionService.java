package dev.daweapon.type.skyblockgeneric.potion;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class GodPotionService {
    private static final long BASE_DURATION_MS = 12L * 60 * 60 * 1000;
    private static final long MAX_DURATION_MS = 192L * 60 * 60 * 1000;
    private static final List<PotionEffectType> EFFECTS = List.of(
            PotionEffectType.CRITICAL, PotionEffectType.REGENERATION, PotionEffectType.STRENGTH,
            PotionEffectType.AGILITY, PotionEffectType.NIGHT_VISION, PotionEffectType.ABSORPTION,
            PotionEffectType.BURNING, PotionEffectType.STUN, PotionEffectType.DODGE,
            PotionEffectType.EXPERIENCE, PotionEffectType.MANA, PotionEffectType.SPEED,
            PotionEffectType.WATER_BREATHING, PotionEffectType.ALCHEMY_XP_BOOST,
            PotionEffectType.COMBAT_XP_BOOST, PotionEffectType.ENCHANTING_XP_BOOST,
            PotionEffectType.FARMING_XP_BOOST, PotionEffectType.FISHING_XP_BOOST,
            PotionEffectType.FORAGING_XP_BOOST, PotionEffectType.MINING_XP_BOOST,
            PotionEffectType.RABBIT, PotionEffectType.RESISTANCE, PotionEffectType.ARCHERY,
            PotionEffectType.JUMP_BOOST, PotionEffectType.MAGIC_FIND, PotionEffectType.PET_LUCK,
            PotionEffectType.SPIRIT, PotionEffectType.SPELUNKER, PotionEffectType.ADRENALINE,
            PotionEffectType.FIRE_RESISTANCE, PotionEffectType.HASTE, PotionEffectType.TRUE_RESISTANCE
    );

    private static final Set<String> EFFECT_NAMES = EFFECTS.stream()
            .map(effect -> effect.getLevelDisplay(effect.getMaxLevel()))
            .collect(Collectors.toSet());

    private GodPotionService() {
    }

    public static boolean isGodPotionEffect(TemporaryStatistic statistic) {
        return EFFECT_NAMES.contains(statistic.getDisplayName());
    }

    public static void consume(SkyBlockPlayer player, SkyBlockItem item) {
        long duration = durationFor(player);
        long remaining = remaining(player);
        if (remaining > MAX_DURATION_MS - duration) {
            player.sendMessage("<c>You can't handle that much Godly power at once!");
            return;
        }
        setRemaining(player, remaining + duration);
        applyEffects(player, remaining + duration);
        player.setItemInMainHand(player.getItemInMainHand().withAmount(player.getItemInMainHand().amount() - 1));
        player.sendMessage("<a><bold>SIP! <e>The <c>God Potion <e>grants you powers for <b>{}h {}m<e>!",
                (remaining + duration) / 3_600_000, ((remaining + duration) / 60_000) % 60);
    }

    public static void restore(SkyBlockPlayer player) {
        long remaining = remaining(player);
        if (remaining > 0) applyEffects(player, remaining);
    }

    public static boolean isActive(SkyBlockPlayer player) {
        return remaining(player) > 0;
    }

    public static void pauseClock(SkyBlockPlayer player) {
        remaining(player);
    }

    public static void resumeClock(SkyBlockPlayer player) {
        setLastUpdated(player, System.currentTimeMillis());
    }

    private static long durationFor(SkyBlockPlayer player) {
        int alchemyLevel = player.getSkills().getCurrentLevel(SkillCategories.ALCHEMY);
        long duration = BASE_DURATION_MS + Math.round(alchemyLevel * 0.24 * 60 * 60 * 1000);
        return Math.round(duration * MiscPetHooks.godPotionDurationMultiplier(player));
    }

    private static long remaining(SkyBlockPlayer player) {
        long remaining = getRemaining(player);
        long lastUpdated = getLastUpdated(player);
        long now = System.currentTimeMillis();
        if (lastUpdated > 0 && !player.isOnIsland()) {
            remaining = Math.max(0, remaining - (now - lastUpdated));
            setRemaining(player, remaining);
        }
        setLastUpdated(player, now);
        return remaining;
    }

    private static void applyEffects(SkyBlockPlayer player, long duration) {
        player.getStatistics().removeTemporaryStatistics(temporary ->
                EFFECTS.stream().anyMatch(effect -> effect.getLevelDisplay(effect.getMaxLevel()).equals(temporary.getDisplayName())));
        for (PotionEffectType effect : EFFECTS) {
            effect.getHandler().applyToPlayer(player, effect.getMaxLevel(), duration);
        }
        dev.daweapon.type.skyblockgeneric.consumable.JerryCandy.apply(player, duration);
    }

    private static long getRemaining(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GOD_POTION_REMAINING_MS, DatapointLong.class).getValue();
    }

    private static void setRemaining(SkyBlockPlayer player, long value) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GOD_POTION_REMAINING_MS, DatapointLong.class).setValue(value);
    }

    private static long getLastUpdated(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GOD_POTION_LAST_UPDATED, DatapointLong.class).getValue();
    }

    private static void setLastUpdated(SkyBlockPlayer player, long value) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GOD_POTION_LAST_UPDATED, DatapointLong.class).setValue(value);
    }
}
