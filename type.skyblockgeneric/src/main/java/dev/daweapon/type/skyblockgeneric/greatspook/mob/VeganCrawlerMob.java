package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.greatspook.VeganCurse;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class VeganCrawlerMob extends PrimalMob {
    private static final double FOOD_RADIUS = 3;
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
        VeganCurse.Animal animal = VeganCurse.Animal.values()[ThreadLocalRandom.current().nextInt(VeganCurse.Animal.values().length)];
        VeganCurse.apply(target, animal, foodSpots());
    }

    private List<Pos> foodSpots() {
        List<Pos> spots = new ArrayList<>();
        int count = VeganCurse.FOOD.size();
        double offset = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        for (int i = 0; i < count; i++) {
            double angle = offset + Math.PI * 2 * i / count;
            spots.add(getPosition().add(Math.cos(angle) * FOOD_RADIUS, 0, Math.sin(angle) * FOOD_RADIUS));
        }
        return spots;
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % 4 != 0 || !cursed) return;
        ownerPlayer().ifPresent(player -> {
            VeganCurse.Collected collected = VeganCurse.collected(player);
            if (collected != null && collected.liked()) finishWithKill(player);
        });
    }

    @Override
    protected void onGone() {
        ownerPlayer().ifPresent(VeganCurse::restore);
    }
}
