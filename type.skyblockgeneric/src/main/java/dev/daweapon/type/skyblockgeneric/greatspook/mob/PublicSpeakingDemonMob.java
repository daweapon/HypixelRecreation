package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;

public class PublicSpeakingDemonMob extends PrimalMob {
    private final PublicSpeakingGroup group;

    public PublicSpeakingDemonMob(UUID owner, String ownerName, PublicSpeakingGroup group) {
        super(PrimalProfiles.of(Manifestation.PUBLIC_SPEAKING_DEMON.mobId(), Manifestation.PUBLIC_SPEAKING_DEMON.displayName()), owner, ownerName);
        this.group = group;
    }

    @Override
    protected Manifestation manifestation() {
        return Manifestation.PUBLIC_SPEAKING_DEMON;
    }

    @Override
    protected boolean heldAtOneHealth() {
        return true;
    }

    @Override
    protected Map<EquipmentSlot, ItemStack> appearance() {
        return Map.of(EquipmentSlot.HELMET, leather(Material.LEATHER_HELMET, 0xD4AF37),
                EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, 0xD4AF37),
                EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, 0xD4AF37),
                EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, 0xD4AF37));
    }

    @Override
    protected void onOwnerHit(SkyBlockPlayer player) {
        group.sync(this);
    }

    @Override
    protected void onChallengeStart(SkyBlockPlayer player) {
        group.beginChallenge(this, player);
    }

    @Override
    protected void afterRewards(SkyBlockPlayer killer, net.minestom.server.instance.Instance instance,
                                net.minestom.server.coordinate.Pos position) {
        group.finished(this, killer);
    }

    @Override
    protected void onGone() {
        group.gone(this, owner());
    }
}
