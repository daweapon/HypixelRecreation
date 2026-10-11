package net.swofty.type.skyblockgeneric.user.pets;

import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * One-line entry points for game systems to tell the player's active pets something happened.
 * Each returns the (possibly pet-modified) amount or payload.
 */
public final class PetEvents {
    private PetEvents() {
    }

    public static double xpGained(SkyBlockPlayer player, PetEvent.XpType type, @Nullable SkyBlockMob mob,
                                  double amount) {
        if (!hasPetData(player)) return amount;
        PetEvent.XpGain event = new PetEvent.XpGain(player, player.getPetData().getEnabledPet(), type, mob, amount);
        return player.getPetData().dispatch(event).amount();
    }

    public static CatchPayload fishCaught(SkyBlockPlayer player, CatchPayload payload, @Nullable String regionId) {
        return fishCaught(player, payload, regionId, null);
    }

    public static CatchPayload fishCaught(SkyBlockPlayer player, CatchPayload payload, @Nullable String regionId,
                                          @Nullable FishingContext context) {
        if (!hasPetData(player)) return payload;
        PetEvent.FishCaught event = new PetEvent.FishCaught(player, player.getPetData().getEnabledPet(), payload,
                regionId, context);
        return player.getPetData().dispatch(event).payload();
    }

    public static int cropHarvested(SkyBlockPlayer player, Material crop, int amount) {
        if (!hasPetData(player)) return amount;
        PetEvent.CropHarvested event = new PetEvent.CropHarvested(player, player.getPetData().getEnabledPet(), crop,
                amount);
        return player.getPetData().dispatch(event).crops();
    }

    public static void jumped(SkyBlockPlayer player) {
        if (!hasPetData(player)) return;
        player.getPetData().dispatch(new PetEvent.Jump(player, player.getPetData().getEnabledPet()));
    }

    public static void interacted(SkyBlockPlayer player) {
        if (!hasPetData(player)) return;
        player.getPetData().dispatch(new PetEvent.PetInteract(player, player.getPetData().getEnabledPet()));
    }

    private static boolean hasPetData(SkyBlockPlayer player) {
        return player != null && player.getSkyblockDataHandler() != null;
    }
}
