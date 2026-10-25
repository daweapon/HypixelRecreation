package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.greatspook.VeganCurse;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class VeganCrawlerMob extends PrimalMob {
    private boolean cursed;

    public VeganCrawlerMob(UUID owner, String ownerName) {
        super(PrimalProfiles.of(Manifestation.VEGAN_CRAWLER.mobId(), Manifestation.VEGAN_CRAWLER.displayName()), owner, ownerName);
    }

    @Override
    protected Manifestation manifestation() {
        return Manifestation.VEGAN_CRAWLER;
    }

    @Override
    protected Map<EquipmentSlot, ItemStack> appearance() {
        return Map.of(EquipmentSlot.HELMET, ItemStack.of(Material.CARVED_PUMPKIN),
                EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, 0x3F8F2F),
                EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, 0x2F6F1F),
                EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, 0x2F6F1F));
    }

    @Override
    public void onMeleeHit(SkyBlockPlayer target) {
        if (cursed || !target.getUuid().equals(owner())) return;
        cursed = true;
        VeganCurse.apply(target, VeganCurse.MOBS.get(ThreadLocalRandom.current().nextInt(VeganCurse.MOBS.size())));
    }

    @Override
    protected void onGone() {
        ownerPlayer().ifPresent(VeganCurse::restore);
    }
}
