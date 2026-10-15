package dev.daweapon.type.skyblockgeneric.gui.inventories.enchantment;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentGuidePagesTest {
    static final class PlayerWithoutAwards extends SkyBlockPlayer {
        PlayerWithoutAwards() {
            super(null, null);
        }

        @Override
        public boolean hasCustomCollectionAward(CustomCollectionAward award) {
            return false;
        }
    }

    private static SkyBlockPlayer player;

    @BeforeAll
    static void init() throws Exception {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        var unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        player = (SkyBlockPlayer) unsafe.allocateInstance(PlayerWithoutAwards.class);
    }

    @Test
    void everyGuideEntryAndLevelPageRendersForAPlayer() {
        List<String> failures = new ArrayList<>();
        for (EnchantmentType type : EnchantmentType.values()) {
            try {
                int max = EnchantmentGuideLore.maximumLevel(type, player);
                int min = EnchantmentGuideLore.minimumLevel(type, player);
                EnchantmentGuideLore.description(type, max, player);
                EnchantmentGuideLore.sources(type, player);
                EnchantmentGuideLore.appliedTo(type);
                EnchantmentGuideLore.conflicts(type);
                EnchantmentGuideLore.levelRange(type, player);
                Text.parseLenient(EnchantmentGuideLore.title(type, max));
                for (int level = min; level <= max; level++) {
                    EnchantmentGuideLore.description(type, level, player);
                    if (level <= type.getEnch().getLevelsToApply(player).maximumLevel()) type.getApplyCost(level, player);
                }
            } catch (Throwable throwable) {
                failures.add(type.name() + ": " + throwable);
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void everyItemStackRendersThroughTheTextRenderer() {
        List<String> failures = new ArrayList<>();
        var guide = new GUIEnchantmentGuide();
        for (EnchantmentType type : EnchantmentType.values()) {
            try {
                var context = dev.daweapon.type.generic.text.RenderContext.ofServer();
                dev.daweapon.type.generic.text.HypixelTextRenderer.renderStack(guide.renderItem(type, 0, player).build(), context);
                var levels = new GUIEnchantmentLevels(type);
                dev.daweapon.type.generic.text.HypixelTextRenderer.renderStack(levels.summary(player).build(), context);
                for (int level = EnchantmentGuideLore.minimumLevel(type, player); level <= EnchantmentGuideLore.maximumLevel(type, player); level++)
                    dev.daweapon.type.generic.text.HypixelTextRenderer.renderStack(levels.levelBook(level, player).build(), context);
            } catch (Throwable throwable) {
                failures.add(type.name() + ": " + throwable);
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }
}
