package dev.daweapon.type.skyblockgeneric.event.actions.player.fall;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.FallDamageValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Tracks fall height and applies fall damage, adjusted by value-update events and the active pet
public class ActionPlayerFall implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        Pos newPosition = event.getNewPosition();
        Pos currentPosition = player.getPosition();

        if (player.isFlying() || player.getGameMode().equals(GameMode.CREATIVE) || player.isInLaunchpad()) {
            player.setFallHeight(currentPosition.blockY());
            return;
        }

        Integer currentHeight = player.getFallHeight();
        if (currentHeight == null) {
            currentHeight = currentPosition.blockY();
        }

        if (newPosition.y() > currentPosition.y() && currentHeight < newPosition.blockY()) {
            player.setFallHeight(newPosition.blockY());
            return;
        }

        if (player.isOnGround()) {
            int fallDistance = currentHeight - newPosition.blockY();
            if (fallDistance > 4 && !dev.daweapon.type.skyblockgeneric.fishing.lava.CrimsonFishingGear.bounce(player, fallDistance)) {
                float baseDamage = (float) ((fallDistance * 2) - 4);
                FallDamageValueUpdateEvent valueEvent = new FallDamageValueUpdateEvent(player, baseDamage);
                SkyBlockValueEvent.callValueUpdateEvent(valueEvent);
                float finalDamage = (float) valueEvent.getValue();

                SkyBlockItem pet = player.getPetData().getEnabledPet();
                PetEvent.FallDamage fallDamageEvent = player.getPetData()
                        .dispatch(new PetEvent.FallDamage(player, pet, finalDamage, fallDistance));
                finalDamage = (float) fallDamageEvent.damage();

                if (finalDamage > 0)
                    player.damage(DamageType.FALL, finalDamage);
            }

            player.setFallHeight(newPosition.blockY());
        }
    }
}
