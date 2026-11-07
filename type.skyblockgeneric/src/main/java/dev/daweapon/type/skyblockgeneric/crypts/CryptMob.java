package dev.daweapon.type.skyblockgeneric.crypts;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import net.minestom.server.item.ItemStack;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Hub Crypts zombie: the shared mob engine (LootRoll drops, damage credit) with the armour and weapon it
 * wears on Hypixel, hostile within 8 blocks, and counted in the Hub Bestiary.
 */
public class CryptMob extends CrimsonIsleMob {
    private final CryptMobs.Outfit outfit;

    public CryptMob(CrimsonMobProfile profile, CryptMobs.Outfit outfit) {
        super(profile);
        this.outfit = outfit;
        setItemInMainHand(ItemStack.of(outfit.weapon()));
        setChestplate(ItemStack.of(outfit.chestplate()));
        setLeggings(ItemStack.of(outfit.leggings()));
        setBoots(ItemStack.of(outfit.boots()));
    }

    public CryptMobs.Outfit getOutfit() {
        return outfit;
    }

    @Override
    protected float aggroRange() {
        return CryptMobs.AGGRO_RANGE;
    }

    @Override
    public boolean countsForBestiary() {
        return HubBestiaryIds.IDS.contains(getMobID());
    }

    private static final class HubBestiaryIds {
        private static final Set<String> IDS = Arrays.stream(BestiaryCategory.HUB.values())
                .flatMap(family -> family.getMobs().stream())
                .map(BestiaryMob::getMobID)
                .collect(Collectors.toUnmodifiableSet());
    }
}
