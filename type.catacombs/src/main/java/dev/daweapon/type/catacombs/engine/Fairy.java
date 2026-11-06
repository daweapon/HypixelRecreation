package dev.daweapon.type.catacombs.engine;

import dev.daweapon.type.catacombs.engine.look.Skin;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;

import java.util.function.BiConsumer;

/** One Fairy Room fairy: a fairy head on a small invisible armor stand, named in pink with 4 health. */
final class Fairy extends CrimsonIsleMob {
    private static final String ID = "DUNGEON_FAIRY";

    private final BiConsumer<Fairy, SkyBlockPlayer> onKilled;

    Fairy(String name, int health, Skin head, BiConsumer<Fairy, SkyBlockPlayer> onKilled) {
        super(CrimsonMobProfile.builder(ID, name)
                .entity(EntityType.ARMOR_STAND)
                .stats(health, 0)
                .speed(0)
                .types(MobType.ELUSIVE)
                .neverCorrupted()
                .build());
        this.onKilled = onKilled;
        editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setSmall(true);
            meta.setInvisible(true);
        });
        setNoGravity(true);
        setHelmet(ItemStack.builder(Material.PLAYER_HEAD).set(DataComponents.PROFILE, new ResolvableProfile(head.playerSkin())).build());
        refreshName();
    }

    @Override
    protected String nameLine() {
        return "<d>" + getProfile().displayName() + " <a>" + (long) Math.ceil(getHealth()) + "<c>❤";
    }

    @Override
    protected Temper temper() {
        return Temper.PASSIVE;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        onKilled.accept(this, killer);
    }
}
