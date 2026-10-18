package dev.daweapon.type.skyblockgeneric.user.pets;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Wisp Upgrade Stones: right-click while a Wisp of the stone's tier is summoned and at least level 80
 * to turn it into the next Wisp, back at level 1. The held item and skin carry over.
 */
public final class WispUpgradeStones {
    public static final int REQUIRED_LEVEL = 80;

    private static final Map<ItemType, Upgrade> UPGRADES = Map.of(
            ItemType.UPGRADE_STONE_FROST, new Upgrade(ItemType.DROPLET_WISP_PET, ItemType.FROST_WISP_PET),
            ItemType.UPGRADE_STONE_GLACIAL, new Upgrade(ItemType.FROST_WISP_PET, ItemType.GLACIAL_WISP_PET),
            ItemType.UPGRADE_STONE_SUBZERO, new Upgrade(ItemType.GLACIAL_WISP_PET, ItemType.SUBZERO_WISP_PET));

    private WispUpgradeStones() {
    }

    public record Upgrade(ItemType from, ItemType to) {
    }

    public static @Nullable Upgrade upgradeFor(ItemType stone) {
        return UPGRADES.get(stone);
    }

    public static @Nullable String problem(Upgrade upgrade, ItemType petType, int petLevel) {
        if (petType != upgrade.from()) return "This stone can't upgrade your summoned pet!";
        if (petLevel < REQUIRED_LEVEL) return "Your pet must be at least level " + REQUIRED_LEVEL + " to be upgraded!";
        return null;
    }

    public static void registerInteractables() {
        UPGRADES.keySet().forEach(stone -> InteractableRegistry.register("WISP_" + stone.name(),
                InteractableItemConfig.builder().rightClickHandler(WispUpgradeStones::use).build()));
    }

    public static void use(SkyBlockPlayer player, SkyBlockItem stone) {
        Upgrade upgrade = upgradeFor(stone.getAttributeHandler().getPotentialType());
        if (upgrade == null) return;
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet == null) {
            player.sendMessage("<c>You need to have a Wisp summoned to use this stone!");
            return;
        }
        String problem = problem(upgrade, pet.getAttributeHandler().getPotentialType(), PetLevels.level(pet));
        if (problem != null) {
            player.sendMessage("<c>" + problem);
            return;
        }

        SkyBlockItem upgraded = new SkyBlockItem(upgrade.to());
        upgraded.getAttributeHandler().setPetHeldItem(pet.getAttributeHandler().getPetHeldItem());
        upgraded.getAttributeHandler().setPetSkin(pet.getAttributeHandler().getPetSkin());

        player.getPetData().removePet(pet);
        player.getPetData().addPet(upgraded);
        player.getPetData().setEnabled(upgraded, true);
        player.getPetData().updatePetEntityImpl(player);

        SkyBlockItem remaining = stone.getAmount() > 1 ? stone.clone() : null;
        if (remaining != null) remaining.setAmount(stone.getAmount() - 1);
        player.setItemInHand(remaining);

        String name = "<color:" + upgraded.getAttributeHandler().getRarity().getColor().asHexString() + ">"
                + upgraded.getComponent(PetComponent.class).getPetName();
        player.sendMessage("<a>Your Wisp was upgraded to " + name + "<a>! Its level has been reset to <6>1<a>.");
        player.playSound(Sound.sound().type(Key.key("minecraft", "entity.player.levelup")).volume(1f).pitch(2f).build());
    }
}
