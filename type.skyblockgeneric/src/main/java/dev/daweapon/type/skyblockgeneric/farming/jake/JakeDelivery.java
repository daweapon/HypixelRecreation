package dev.daweapon.type.skyblockgeneric.farming.jake;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.farming.animal.AnimalLeash;
import dev.daweapon.type.skyblockgeneric.farming.animal.FarmAnimal;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class JakeDelivery {
    public static final double LEASHED_RANGE = 12;
    public static final double LOOSE_RANGE = 6;

    public record Delivery(SkyBlockMob mob, FarmAnimal animal) {
        public void consume() {
            if (!animal.consumedOnDelivery()) return;
            AnimalLeash.release(mob);
            mob.remove();
        }
    }

    private JakeDelivery() {
    }

    public static @Nullable Delivery find(SkyBlockPlayer player, Pos jake) {
        Instance instance = player.getInstance();
        if (instance == null) return null;
        List<Delivery> candidates = new ArrayList<>();
        for (SkyBlockMob mob : AnimalLeash.ownedBy(player)) {
            FarmAnimal animal = FarmAnimal.of(mob);
            if (animal != null && !mob.isDead() && mob.getInstance() == instance && mob.getPosition().distance(jake) <= LEASHED_RANGE) {
                candidates.add(new Delivery(mob, animal));
            }
        }
        instance.getNearbyEntities(jake, LOOSE_RANGE).forEach(entity -> {
            if (!(entity instanceof SkyBlockMob mob) || mob.isDead()) return;
            FarmAnimal animal = FarmAnimal.of(mob);
            if (animal == null || candidates.stream().anyMatch(delivery -> delivery.mob() == mob)) return;
            candidates.add(new Delivery(mob, animal));
        });
        return candidates.stream()
                .filter(delivery -> !JakeShop.isOpen(delivery.animal().unlocks()))
                .findFirst()
                .orElse(candidates.isEmpty() ? null : candidates.getFirst());
    }
}
