package net.swofty.type.skyblockgeneric.slayer.inferno;

import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointInventory;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Damage effects of Blaze Slayer gear: dagger multipliers against Infernal, Wither and Skeletal mobs,
 * the Demonslayer Gauntlet against Infernal mobs, and the Burststopper accessories against big hits.
 */
public final class BlazeSlayerGear {
    private static final double GAUNTLET_MULTIPLIER = 1.15;
    private static final double BURST_THRESHOLD = 0.5;

    private BlazeSlayerGear() {
    }

    public static double outgoingMultiplier(SkyBlockPlayer player, @Nullable SkyBlockMob target) {
        if (target == null) return 1;
        double multiplier = 1;
        BlazeDagger dagger = BlazeDagger.of(new SkyBlockItem(player.getItemInMainHand()));
        if (dagger != null) multiplier *= dagger.multiplierAgainst(target.getMobTypes());
        if (target.getMobTypes().contains(MobType.INFERNAL) && wearsGloves(player, ItemType.DEMONLORD_GAUNTLET)) {
            multiplier *= GAUNTLET_MULTIPLIER;
        }
        return multiplier;
    }

    public static float burststop(SkyBlockPlayer player, float damage) {
        if (damage < player.getMaxHealth() * BURST_THRESHOLD) return damage;
        boolean artifact = false;
        boolean talisman = false;
        for (SkyBlockItem accessory : AccessoryOwnership.activeItems(player)) {
            ItemType type = accessory.getAttributeHandler().getPotentialType();
            if (type == ItemType.BURSTSTOPPER_ARTIFACT) artifact = true;
            if (type == ItemType.BURSTSTOPPER_TALISMAN) talisman = true;
        }
        if (artifact) return damage * 0.9F;
        if (talisman) return damage * 0.95F;
        return damage;
    }

    private static boolean wearsGloves(SkyBlockPlayer player, ItemType type) {
        if (player.getSkyblockDataHandler() == null) return false;
        UnderstandableSkyBlockItem gloves = player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue().getGloves();
        return gloves != null && new SkyBlockItem(gloves).getAttributeHandler().getPotentialType() == type;
    }
}
