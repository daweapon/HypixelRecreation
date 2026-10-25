package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.greatspook.MathProblem;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearChat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MathTeacherMob extends PrimalMob {
    public MathTeacherMob(UUID owner, String ownerName) {
        super(PrimalProfiles.of(Manifestation.MATH_TEACHER.mobId(), Manifestation.MATH_TEACHER.displayName()), owner, ownerName);
    }

    @Override
    protected Manifestation manifestation() {
        return Manifestation.MATH_TEACHER;
    }

    @Override
    protected boolean heldAtOneHealth() {
        return true;
    }

    @Override
    protected Map<EquipmentSlot, ItemStack> appearance() {
        return Map.of(EquipmentSlot.HELMET, ItemStack.of(Material.GLASS),
                EquipmentSlot.CHESTPLATE, leather(Material.LEATHER_CHESTPLATE, 0x6B4A2B),
                EquipmentSlot.LEGGINGS, leather(Material.LEATHER_LEGGINGS, 0x2B2B6B),
                EquipmentSlot.BOOTS, leather(Material.LEATHER_BOOTS, 0x222222),
                EquipmentSlot.MAIN_HAND, ItemStack.of(Material.WRITABLE_BOOK));
    }

    @Override
    protected void onChallengeStart(SkyBlockPlayer player) {
        MathProblem problem = MathProblem.random(ThreadLocalRandom.current());
        player.sendMessage("<c>Solve: <e>{} <c>= <e>?", problem.text());
        PrimalFearChat.listen(player, this, message -> {
            if (!problem.accepts(message)) return false;
            finishWithKill(player);
            return true;
        });
    }

    @Override
    protected void onGone() {
        PrimalFearChat.stop(owner(), this);
    }
}
