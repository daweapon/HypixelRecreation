package dev.daweapon.type.skyblockgeneric.healing;

import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentHabaneroTactics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbilityComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class HealingWands {
    public static final int VITALITY_COST = 25;
    public static final int COOLDOWN_TICKS = 20;

    public record Wand(String itemId, String abilityId, String abilityName, int perSecond, int seconds) {
    }

    public static final List<Wand> WANDS = List.of(
            new Wand("WAND_OF_HEALING", "SMALL_HEAL", "Small Heal", 60, 5),
            new Wand("WAND_OF_MENDING", "MEDIUM_HEAL", "Medium Heal", 80, 5),
            new Wand("WAND_OF_RESTORATION", "BIG_HEAL", "Big Heal", 100, 6),
            new Wand("WAND_OF_ATONEMENT", "HUGE_HEAL", "Huge Heal", 120, 6));

    private static final Map<UUID, Integer> GENERATION = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> SET_WORN_SINCE = new ConcurrentHashMap<>();
    private static final List<String> REVENANT_PIECES = List.of("REVENANT_CHESTPLATE", "REVENANT_LEGGINGS", "REVENANT_BOOTS");
    private static final List<String> REAPER_PIECES = List.of("REAPER_CHESTPLATE", "REAPER_LEGGINGS", "REAPER_BOOTS");

    private HealingWands() {
    }

    public static void register() {
        for (Wand wand : WANDS) {
            AbilityRegistry.register(new RegisteredAbility(
                    wand.abilityId(),
                    wand.abilityName(),
                    (_, _) -> "<7>Heal <c>" + wand.perSecond() + "❤<7>/s for <a>" + wand.seconds() + "s<7>.\n<8>Wand heals don't stack.",
                    RegisteredAbility.AbilityActivation.RIGHT_CLICK,
                    COOLDOWN_TICKS,
                    new RegisteredAbility.AbilityVitalityCost(VITALITY_COST),
                    (player, _, _, _) -> {
                        cast(player, wand);
                        return true;
                    }));
        }
    }

    public static void cast(SkyBlockPlayer player, Wand wand) {
        double perTick = HealingMath.tickHeal(wand.perSecond(), multiplier(player));
        UUID id = player.getUuid();
        int generation = GENERATION.merge(id, 1, Integer::sum);
        AtomicInteger ticks = new AtomicInteger(wand.seconds());
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (GENERATION.getOrDefault(id, 0) != generation || !player.isOnline()) return TaskSchedule.stop();
            heal(player, perTick);
            return ticks.decrementAndGet() > 0 ? TaskSchedule.seconds(1) : TaskSchedule.stop();
        });
    }

    public static double multiplier(SkyBlockPlayer player) {
        return HealingMath.multiplier(setBonusActive(player), EnchantmentHabaneroTactics.wandHealBonusPercent(player));
    }

    public static boolean setBonusActive(SkyBlockPlayer player) {
        return HealingMath.setBonusActive(wearsZombieSlayerSet(player),
                SET_WORN_SINCE.getOrDefault(player.getUuid(), 0L), System.currentTimeMillis());
    }

    public static void trackArmor(SkyBlockPlayer player) {
        if (wearsZombieSlayerSet(player)) SET_WORN_SINCE.putIfAbsent(player.getUuid(), System.currentTimeMillis());
        else SET_WORN_SINCE.remove(player.getUuid());
    }

    public static boolean holdsVitalityItem(SkyBlockPlayer player) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (held.isAir() || !held.hasComponent(AbilityComponent.class)) return false;
        return held.getComponent(AbilityComponent.class).getAbilities().stream()
                .anyMatch(ability -> ability.getCost() instanceof RegisteredAbility.AbilityVitalityCost);
    }

    private static void heal(SkyBlockPlayer player, double amount) {
        player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + amount));
    }

    private static boolean wearsZombieSlayerSet(SkyBlockPlayer player) {
        List<String> worn = List.of(idOf(player.getChestplate()), idOf(player.getLeggings()), idOf(player.getBoots()));
        return worn.equals(REVENANT_PIECES) || worn.equals(REAPER_PIECES);
    }

    private static String idOf(ItemStack stack) {
        SkyBlockItem item = new SkyBlockItem(stack);
        return item.isAir() ? "" : item.getAttributeHandler().getTypeAsString();
    }
}
