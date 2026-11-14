package dev.daweapon.pvp.feature.effect;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.pvp.utils.PotionFlags;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PotionColorUtilsTest {
    @Test
    void rgbaPacksChannels() {
        assertEquals(0xFF112233, PotionColorUtils.rgba(255, 0x11, 0x22, 0x33));
        assertEquals(0, PotionColorUtils.rgba(0, 0, 0, 0));
    }

    @Test
    void noEffectsYieldsMinusOne() {
        assertEquals(-1, PotionColorUtils.getPotionColor(List.of()));
    }

    @Test
    void singleEffectReturnsItsOwnColourFullyOpaque() {
        int color = PotionEffect.SPEED.color();
        int expected = PotionColorUtils.rgba(255, color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF);
        assertEquals(expected, PotionColorUtils.getPotionColor(List.of(new Potion(PotionEffect.SPEED, (byte) 0, 100, PotionFlags.defaultFlags()))));
    }

    @Test
    void amplifierWeightsTheBlend() {
        int a = PotionEffect.SPEED.color();
        int b = PotionEffect.STRENGTH.color();
        int redA = a >> 16 & 0xFF;
        int redB = b >> 16 & 0xFF;
        int result = PotionColorUtils.getPotionColor(List.of(
                new Potion(PotionEffect.SPEED, (byte) 0, 100, PotionFlags.defaultFlags()),
                new Potion(PotionEffect.STRENGTH, (byte) 2, 100, PotionFlags.defaultFlags())));
        assertEquals((redA * 1 + redB * 3) / 4, result >> 16 & 0xFF);
        assertEquals(255, result >>> 24);
    }
}
