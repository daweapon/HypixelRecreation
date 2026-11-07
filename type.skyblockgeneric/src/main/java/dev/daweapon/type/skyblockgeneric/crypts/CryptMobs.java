package dev.daweapon.type.skyblockgeneric.crypts;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;

import java.util.List;

import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.always;
import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.chance;
import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.pet;

/**
 * The Hub Crypts mobs from the wiki infoboxes and drop tables after the 2026 level rework (Crypt Ghoul and
 * Golden Ghoul pages, Golden Ghoul health and damage cut on 2026-09-21). The wiki gives no defense or speed:
 * defense is 0, and speed is an estimate (the Crypt Ghoul is described as faster than a normal Zombie).
 */
public final class CryptMobs {
    public static final String CRYPT_GHOUL_HEAD = "b3666d1bdf4558b0d16990b241e8185cbf58c3e03c24d904544e8cc65b1c38d1";
    public static final String GOLDEN_GHOUL_HEAD = "69be1cf0639c6b7db506686f26af222671ce52321e1c921b296933cd40ac4c62";
    public static final float AGGRO_RANGE = 8;

    public record Outfit(Material weapon, Material chestplate, Material leggings, Material boots) {
    }

    public static final Outfit CHAINMAIL = new Outfit(Material.IRON_SWORD, Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS);
    public static final Outfit GOLD = new Outfit(Material.GOLDEN_SWORD, Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS);

    public static final CrimsonMobProfile CRYPT_GHOUL = CrimsonMobProfile.builder("CRYPT_GHOUL", "Crypt Ghoul")
            .level(33).entity(EntityType.ZOMBIE).stats(2_000, 350).rewards(49, 11, 36).speed(120)
            .types(MobType.UNDEAD).bestiary(2, 20, CRYPT_GHOUL_HEAD).neverCorrupted()
            .drops(always("ROTTEN_FLESH", 1), chance("ROTTEN_FLESH", 50),
                    pet("GHOUL_PET", Rarity.EPIC, 0.003), pet("GHOUL_PET", Rarity.LEGENDARY, 0.001))
            .build();

    public static final CrimsonMobProfile GOLDEN_GHOUL = CrimsonMobProfile.builder("GOLDEN_GHOUL", "Golden Ghoul")
            .level(65).entity(EntityType.ZOMBIE).stats(20_000, 500).rewards(97, 21, 71).speed(100)
            .types(MobType.UNDEAD).bestiary(4, 15, GOLDEN_GHOUL_HEAD).neverCorrupted()
            .drops(always("ROTTEN_FLESH", 2), always("GOLD_INGOT", 1), chance("GOLD_INGOT", 9, 35),
                    chance("GOLDEN_POWDER", 0.05))
            .build();

    private CryptMobs() {
    }

    public static List<CrimsonMobProfile> all() {
        return List.of(CRYPT_GHOUL, GOLDEN_GHOUL);
    }
}
