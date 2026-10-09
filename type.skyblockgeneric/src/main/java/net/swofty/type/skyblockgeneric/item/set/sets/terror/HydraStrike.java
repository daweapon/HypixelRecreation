package net.swofty.type.skyblockgeneric.item.set.sets.terror;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Terror Armor's Hydra Strike tiered bonus. Two or more Terror pieces of any tier count; the lowest tier worn decides
 * the numbers (wiki). Stacks raise arrow damage and arrow speed, and at 10 stacks every shot adds two weaker arrows.
 */
public final class HydraStrike {
    public static final int EXTRA_ARROWS = 2;
    private static final Map<UUID, HydraStrikeStacks> STACKS = new ConcurrentHashMap<>();

    private HydraStrike() {
    }

    public record Wear(TerrorTier tier, int pieces) {
        public double damageMultiplier(int stacks) {
            return 1 + stacks * tier.damagePerStack(pieces) / 100;
        }

        public double arrowSpeedMultiplier(int stacks) {
            return 1 + stacks * tier.arrowSpeedPerStack() / 100;
        }

        public double extraArrowFactor(int stacks) {
            return stacks >= HydraStrikeStacks.MAX_STACKS ? tier.extraArrowDamage() / 100 : 0;
        }

        public long decayMillis() {
            return TerrorTier.decayMillis(pieces);
        }
    }

    public static @Nullable Wear wear(Collection<@Nullable ItemType> armor) {
        TerrorTier lowest = null;
        int pieces = 0;
        for (ItemType type : armor) {
            TerrorTier tier = TerrorTier.of(type);
            if (tier == null) continue;
            pieces++;
            if (lowest == null || tier.ordinal() < lowest.ordinal()) lowest = tier;
        }
        return lowest == null || pieces < TerrorTier.MINIMUM_PIECES ? null : new Wear(lowest, pieces);
    }

    public static @Nullable Wear wear(SkyBlockPlayer player) {
        List<ItemType> armor = new ArrayList<>(4);
        for (ItemStack stack : List.of(player.getHelmet(), player.getChestplate(), player.getLeggings(), player.getBoots())) {
            if (stack == null || stack.isAir()) continue;
            armor.add(new SkyBlockItem(stack).getAttributeHandler().getPotentialType());
        }
        return wear(armor);
    }

    public static void onArrowHit(SkyBlockPlayer player) {
        Wear wear = wear(player);
        if (wear == null) return;
        STACKS.computeIfAbsent(player.getUuid(), _ -> new HydraStrikeStacks()).gain(System.currentTimeMillis(), wear.decayMillis());
    }

    public static int stacks(SkyBlockPlayer player, @Nullable Wear wear) {
        HydraStrikeStacks stacks = STACKS.get(player.getUuid());
        if (stacks == null) return 0;
        if (wear == null) {
            STACKS.remove(player.getUuid());
            return 0;
        }
        return stacks.stacks(System.currentTimeMillis(), wear.decayMillis());
    }

    public static double damageMultiplier(SkyBlockPlayer player) {
        Wear wear = wear(player);
        return wear == null ? 1 : wear.damageMultiplier(stacks(player, wear));
    }

    public static Text display(SkyBlockPlayer player) {
        if (!STACKS.containsKey(player.getUuid())) return Text.empty();
        int stacks = stacks(player, wear(player));
        return stacks <= 0 ? Text.empty() : Text.of("<6><l>{}⁑", stacks);
    }
}
