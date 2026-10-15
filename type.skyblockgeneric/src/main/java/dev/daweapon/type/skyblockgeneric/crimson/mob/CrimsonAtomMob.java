package dev.daweapon.type.skyblockgeneric.crimson.mob;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Exe, Wai and Zee (wiki: Exe): atoms floating in place in the Mystic Marsh that take no damage. Following a
 * Charminizer, merging into Wai and Zee, and the Atominizer's X, Y and Z need those items' abilities, so they
 * are not modelled yet.
 */
public abstract class CrimsonAtomMob extends CrimsonIsleMob {
    protected CrimsonAtomMob(CrimsonMobProfile profile) {
        super(profile);
    }

    @Override
    protected Temper temper() {
        return Temper.PASSIVE;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
        editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setInvisible(true);
            meta.setSmall(true);
            meta.setHasNoGravity(true);
        });
        setHelmet(ItemStack.of(switch (getMobID()) {
            case "WAI" -> Material.ENDER_EYE;
            case "ZEE" -> Material.HEART_OF_THE_SEA;
            default -> Material.ENDER_PEARL;
        }));
        teleport(getPosition().add(0, 1, 0));
    }
}
