package net.swofty.type.skyblockgeneric.item.rune;

import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeRuneInfusedWith;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.swofty.type.skyblockgeneric.item.rune.RuneEffect.Trigger.ARROW;
import static net.swofty.type.skyblockgeneric.item.rune.RuneEffect.Trigger.KILL;
import static net.swofty.type.skyblockgeneric.item.rune.RuneEffect.Trigger.MOVE;

public final class RuneEffects {
    public record Active(RuneEffect effect, int level) {
    }

    private static final Map<String, RuneEffect> EFFECTS = Map.ofEntries(
            Map.entry("BLOOD_RUNE", new RuneEffect(Particle.DRIPPING_LAVA, KILL, 3, 0)),
            Map.entry("SNOW_RUNE", new RuneEffect(Particle.SNOWFLAKE, KILL, 4, 0.3f)),
            Map.entry("HEARTS_RUNE", new RuneEffect(Particle.HEART, KILL, 3, 0.4f)),
            Map.entry("MUSIC_RUNE", new RuneEffect(Particle.NOTE, KILL, 3, 0.4f)),
            Map.entry("JERRY_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, KILL, 3, 0.4f)),
            Map.entry("LIGHTNING_RUNE", new RuneEffect(Particle.ELECTRIC_SPARK, KILL, 6, 0.5f)),
            Map.entry("SPIRIT_RUNE", new RuneEffect(Particle.ENCHANT, KILL, 8, 0.5f)),
            Map.entry("ZOMBIE_SLAYER_RUNE", new RuneEffect(Particle.PORTAL, KILL, 8, 0.5f)),
            Map.entry("SOULTWIST_RUNE", new RuneEffect(Particle.WITCH, KILL, 6, 0.5f)),
            Map.entry("HEARTSPLOSION_RUNE", new RuneEffect(Particle.HEART, KILL, 6, 0.6f)),
            Map.entry("MEOW_MUSIC_RUNE", new RuneEffect(Particle.NOTE, KILL, 3, 0.4f)),
            Map.entry("BARK_TUNES_RUNE", new RuneEffect(Particle.NOTE, KILL, 3, 0.4f)),
            Map.entry("EXPLOSIFALL_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, KILL, 5, 0.5f)),
            Map.entry("AXE_FADING_WHITE_RUNE", new RuneEffect(Particle.CLOUD, ARROW, 1, 0.05f)),
            Map.entry("AXE_FADING_GREEN_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, ARROW, 1, 0.05f)),
            Map.entry("AXE_FADING_BLUE_RUNE", new RuneEffect(Particle.DUST.withColor(new Color(60, 90, 255)), ARROW, 1, 0.05f)),
            Map.entry("AXE_RAINBOW_RUNE", new RuneEffect(Particle.DUST.withColor(new Color(255, 105, 180)), ARROW, 1, 0.05f)),
            Map.entry("AXE_SHATTER_RUNE", new RuneEffect(Particle.ASH, KILL, 8, 0.6f)),
            Map.entry("AXE_SOUL_SLICE_RUNE", new RuneEffect(Particle.SOUL, ARROW, 1, 0.05f)),
            Map.entry("ICE_RUNE", new RuneEffect(Particle.CLOUD, ARROW, 1, 0.05f)),
            Map.entry("LAVA_RUNE", new RuneEffect(Particle.LAVA, ARROW, 1, 0.05f)),
            Map.entry("GEM_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, ARROW, 1, 0.05f)),
            Map.entry("SLIMY_RUNE", new RuneEffect(Particle.ITEM_SLIME, ARROW, 1, 0.05f)),
            Map.entry("WHITE_SPIRAL_RUNE", new RuneEffect(Particle.FIREWORK, ARROW, 1, 0.05f)),
            Map.entry("SMOKEY_RUNE", new RuneEffect(Particle.SMOKE, ARROW, 1, 0.05f)),
            Map.entry("MAGIC_RUNE", new RuneEffect(Particle.WITCH, ARROW, 1, 0.05f)),
            Map.entry("FIRE_SPIRAL_RUNE", new RuneEffect(Particle.FLAME, ARROW, 1, 0.05f)),
            Map.entry("GOLDEN_RUNE", new RuneEffect(Particle.DUST.withColor(new Color(255, 215, 0)), ARROW, 1, 0.05f)),
            Map.entry("WAKE_RUNE", new RuneEffect(Particle.SPLASH, ARROW, 1, 0.05f)),
            Map.entry("BITE_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, ARROW, 1, 0.05f)),
            Map.entry("DRAGON_RUNE", new RuneEffect(Particle.LARGE_SMOKE, ARROW, 1, 0.05f)),
            Map.entry("LAVATEARS_RUNE", new RuneEffect(Particle.DRIPPING_LAVA, ARROW, 1, 0.05f)),
            Map.entry("ZAP_RUNE", new RuneEffect(Particle.ELECTRIC_SPARK, MOVE, 2, 0.3f)),
            Map.entry("HOT_RUNE", new RuneEffect(Particle.LAVA, MOVE, 1, 0.3f)),
            Map.entry("REDSTONE_RUNE", new RuneEffect(Particle.DUST.withColor(new Color(220, 20, 20)), MOVE, 2, 0.3f)),
            Map.entry("SPARKLING_RUNE", new RuneEffect(Particle.FIREWORK, MOVE, 1, 0.3f)),
            Map.entry("CLOUDS_RUNE", new RuneEffect(Particle.CLOUD, MOVE, 1, 0.3f)),
            Map.entry("RAINBOW_RUNE", new RuneEffect(Particle.DUST.withColor(new Color(255, 105, 180)), MOVE, 3, 0.4f)),
            Map.entry("SNAKE_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, MOVE, 2, 0.3f)),
            Map.entry("DARKNESS_WITHIN_RUNE", new RuneEffect(Particle.LARGE_SMOKE, MOVE, 1, 0.3f)),
            Map.entry("COUTURE_RUNE", new RuneEffect(Particle.FLAME, MOVE, 2, 0.3f)),
            Map.entry("ENDERSNAKE_RUNE", new RuneEffect(Particle.WITCH, MOVE, 2, 0.3f)),
            Map.entry("TIDAL_RUNE", new RuneEffect(Particle.SPLASH, MOVE, 3, 0.3f)),
            Map.entry("ENCHANT_RUNE", new RuneEffect(Particle.ENCHANT, MOVE, 3, 0.5f)),
            Map.entry("FIERY_BURST_RUNE", new RuneEffect(Particle.FLAME, MOVE, 2, 0.3f)),
            Map.entry("ICE_SKATES_RUNE", new RuneEffect(Particle.SNOWFLAKE, MOVE, 2, 0.3f)),
            Map.entry("GOLDEN_CARPET_RUNE", new RuneEffect(Particle.FLAME, MOVE, 2, 0.3f)),
            Map.entry("RAINBOW_CARPET_RUNE", new RuneEffect(Particle.FIREWORK, MOVE, 2, 0.3f)),
            Map.entry("FLOWER_CARPET_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, MOVE, 2, 0.3f)),
            Map.entry("FLOWING_FALL_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, MOVE, 2, 0.3f)),
            Map.entry("BLOOMING_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, MOVE, 2, 0.6f)),
            Map.entry("GRAND_GARDENING_RUNE", new RuneEffect(Particle.HAPPY_VILLAGER, MOVE, 2, 0.6f)),
            Map.entry("GRAND_LEAFING_RUNE", new RuneEffect(Particle.CRIT, MOVE, 2, 0.4f)),
            Map.entry("GRAND_SEARING_RUNE", new RuneEffect(Particle.FLAME, MOVE, 3, 0.5f)),
            Map.entry("GRAND_FREEZING_RUNE", new RuneEffect(Particle.SNOWFLAKE, MOVE, 3, 0.5f)),
            Map.entry("SUNNY_SPIRAL_RUNE", new RuneEffect(Particle.FLAME, MOVE, 2, 0.6f)),
            Map.entry("PRIMAL_FEAR_RUNE", new RuneEffect(Particle.WITCH, MOVE, 3, 0.5f)),
            Map.entry("SPELLBOUND_RUNE", new RuneEffect(Particle.ENCHANT, MOVE, 3, 0.4f)),
            Map.entry("SMITTEN_RUNE", new RuneEffect(Particle.HEART, MOVE, 1, 0.3f)),
            Map.entry("RAINY_DAY_RUNE", new RuneEffect(Particle.SPLASH, MOVE, 3, 0.4f)),
            Map.entry("SUPER_PUMPKIN_RUNE", new RuneEffect(Particle.FLAME, MOVE, 2, 0.6f)),
            Map.entry("ORNAMENTAL_RUNE", new RuneEffect(Particle.HEART, MOVE, 2, 0.6f)),
            Map.entry("TURKEY_RUNE", new RuneEffect(Particle.DUST.withColor(new Color(150, 90, 40)), MOVE, 3, 0.5f)),
            Map.entry("PEAFOWL_RUNE", new RuneEffect(Particle.DUST.withColor(new Color(30, 120, 255)), MOVE, 3, 0.5f)),
            Map.entry("HARMONY_RUNE", new RuneEffect(Particle.NOTE, MOVE, 2, 0.5f)),
            Map.entry("BLAZING_SUN_RUNE", new RuneEffect(Particle.FLAME, MOVE, 2, 0.3f)),
            Map.entry("CROWNED_RUNE", new RuneEffect(Particle.DUST.withColor(new Color(255, 215, 0)), MOVE, 2, 0.3f)),
            Map.entry("ANTLERS_RUNE", new RuneEffect(Particle.DUST.withColor(new Color(120, 80, 40)), MOVE, 1, 0.2f))
    );

    private RuneEffects() {
    }

    public static @Nullable RuneEffect find(String runeId) {
        return EFFECTS.get(runeId);
    }

    public static List<String> registeredRunes() {
        return List.copyOf(EFFECTS.keySet());
    }

    public static @Nullable Active active(@Nullable SkyBlockItem item, RuneEffect.Trigger trigger) {
        if (item == null) return null;
        ItemAttributeRuneInfusedWith.RuneData data = item.getAttributeHandler().getRuneData();
        if (data == null || !data.hasRune()) return null;
        RuneEffect effect = find(data.getRuneType().name());
        if (effect == null || effect.trigger() != trigger) return null;
        return new Active(effect, data.getLevel() == null ? 1 : data.getLevel());
    }

    public static List<Active> active(List<SkyBlockItem> items, RuneEffect.Trigger trigger) {
        List<Active> result = new ArrayList<>();
        for (SkyBlockItem item : items) {
            Active active = active(item, trigger);
            if (active != null) result.add(active);
        }
        return result;
    }

    public static void emit(SkyBlockPlayer player, Point at, Active active) {
        RuneEffect effect = active.effect();
        player.sendPacket(new ParticlePacket(effect.particle(), true, false,
                at.x(), at.y(), at.z(),
                effect.spread(), effect.spread(), effect.spread(), 0, effect.count(active.level())));
    }
}
