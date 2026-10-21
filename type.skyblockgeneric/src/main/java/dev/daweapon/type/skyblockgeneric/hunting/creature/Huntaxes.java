package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Vis Temperata: a Huntaxe strike removes its tier's share of the target's max health (15/20/25/33/50%, which gives
 * the wiki's 6/5/4/3/2 hits to reach the floor) and can never take it below 10%, so a creature can be weakened but
 * not killed. The wiki caps the strike instead; with Absorptio unbuilt the cap is applied as the damage itself.
 */
public final class Huntaxes {
    public static final double HEALTH_FLOOR = 0.10;

    public enum Tier {
        GENESIS(ItemType.VENATOR_GENESIS, 15), DOMINUS(ItemType.SILVA_DOMINUS, 20), CURSUS(ItemType.CURSUS_FERAE, 25),
        PRAEDATOR(ItemType.APEX_PRAEDATOR, 33), NEX_TITANUM(ItemType.NEX_TITANUM, 50);

        private final ItemType item;
        private final int capPercent;

        Tier(ItemType item, int capPercent) {
            this.item = item;
            this.capPercent = capPercent;
        }

        public ItemType item() {
            return item;
        }

        public int capPercent() {
            return capPercent;
        }
    }

    private Huntaxes() {
    }

    public static @Nullable Tier of(ItemType type) {
        return Arrays.stream(Tier.values()).filter(tier -> tier.item == type).findFirst().orElse(null);
    }

    public static @Nullable Tier heldBy(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        if (held == null || held.isAir()) return null;
        ItemType type = new SkyBlockItem(held).getAttributeHandler().getPotentialType();
        return type == null ? null : of(type);
    }
}
