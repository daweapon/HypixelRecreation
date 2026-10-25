package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearDialogue;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DarknessShadeMob extends PrimalMob {
    public static final int MAX_LINES = 3;
    private static final int BLINDNESS_TICKS = 80;
    private static final String COW_HAT = "COW_HEAD";
    private static final double MIN_TELEPORT = 2.5;
    private static final double MAX_TELEPORT = 5;

    private int linesSaid;

    public DarknessShadeMob(UUID owner, String ownerName) {
        super(PrimalProfiles.of(Manifestation.DARKNESS_SHADE.mobId(), Manifestation.DARKNESS_SHADE.displayName()), owner, ownerName);
    }

    @Override
    protected Manifestation manifestation() {
        return Manifestation.DARKNESS_SHADE;
    }

    @Override
    protected Map<EquipmentSlot, ItemStack> appearance() {
        return Map.of(EquipmentSlot.HELMET, leather(Material.LEATHER_HELMET, 0x050505),
                EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, 0x050505),
                EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, 0x050505),
                EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, 0x050505));
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % 20 != 0) return;
        ownerPlayer().ifPresent(DarknessShadeMob::blind);
    }

    private static void blind(SkyBlockPlayer player) {
        if (COW_HAT.equals(WornArmor.id(player.getHelmet()))) return;
        player.addEffect(new Potion(PotionEffect.BLINDNESS, 0, BLINDNESS_TICKS));
    }

    @Override
    protected void onOwnerHit(SkyBlockPlayer player) {
        if (getInstance() == null) return;
        double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        double radius = MIN_TELEPORT + ThreadLocalRandom.current().nextDouble(MAX_TELEPORT - MIN_TELEPORT);
        Pos around = player.getPosition();
        teleport(new Pos(around.x() + Math.cos(angle) * radius, around.y(), around.z() + Math.sin(angle) * radius,
                (float) Math.toDegrees(angle) + 90, 0));
        if (linesSaid >= MAX_LINES) return;
        linesSaid++;
        say(player, PrimalFearDialogue.DARKNESS_SHADE.get(ThreadLocalRandom.current().nextInt(PrimalFearDialogue.DARKNESS_SHADE.size())));
    }

    @Override
    protected void onGone() {
        ownerPlayer().ifPresent(player -> player.removeEffect(PotionEffect.BLINDNESS));
    }
}
