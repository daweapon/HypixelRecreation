package dev.daweapon.type.skyblockgeneric.slayer.inferno;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
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

    public static double bonusCombatXp(SkyBlockPlayer player, SkyBlockMob killed, double baseXp) {
        BlazeDagger dagger = BlazeDagger.of(new SkyBlockItem(player.getItemInMainHand()));
        return dagger == null ? 0 : daggerCombatXp(dagger, killed.getMobTypes().contains(MobType.INFERNAL), baseXp);
    }

    public static double daggerCombatXp(BlazeDagger dagger, boolean infernal, double baseXp) {
        return infernal ? baseXp * dagger.combatWisdom() / 100D : 0;
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
