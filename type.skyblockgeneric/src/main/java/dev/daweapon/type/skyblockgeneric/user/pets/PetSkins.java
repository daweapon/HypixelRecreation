package dev.daweapon.type.skyblockgeneric.user.pets;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.components.PetSkinComponent;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Applying and removing Pet Skins. A pet wears at most one skin; applying a new one or removing it
 * gives the old skin back as an item.
 */
public final class PetSkins {
    public static final String SKINNED_MARKER = "✦";

    private PetSkins() {
    }

    public enum Outcome {
        APPLIED(""),
        NOT_A_SKIN("<c>That isn't a pet skin!"),
        ANIMATED("<c>Animated pet skins can't be applied yet!"),
        UNKNOWN_PET("<c>This skin is for a pet that isn't in SkyBlock yet!"),
        WRONG_PET("<c>This skin can't be applied to this pet!"),
        ALREADY_APPLIED("<c>Your pet is already wearing this skin!");

        private final String message;

        Outcome(String message) {
            this.message = message;
        }
    }

    public record Result(Outcome outcome, @Nullable ItemType previous) {
    }

    public static @Nullable PetSkinComponent skinOf(SkyBlockItem pet) {
        SkyBlockItem item = skinItemOf(pet);
        return item == null ? null : item.getComponent(PetSkinComponent.class);
    }

    public static @Nullable String textureOf(SkyBlockItem pet) {
        PetSkinComponent skin = skinOf(pet);
        return skin == null ? null : skin.getTexture();
    }

    public static String nameSuffix(SkyBlockItem pet) {
        SkyBlockItem skin = skinItemOf(pet);
        return skin == null ? "" : " <color:" + skin.getComponent(PetSkinComponent.class).colorOf(skin) + ">" + SKINNED_MARKER;
    }

    public static String loreSuffix(SkyBlockItem pet) {
        SkyBlockItem skin = skinItemOf(pet);
        if (skin == null) return "";
        PetSkinComponent component = skin.getComponent(PetSkinComponent.class);
        return ", <color:" + component.colorOf(skin) + ">" + component.skinName(skin);
    }

    private static @Nullable SkyBlockItem skinItemOf(SkyBlockItem pet) {
        ItemType skin = pet.getAttributeHandler().getPetSkin();
        if (skin == null) return null;
        SkyBlockItem item = new SkyBlockItem(skin);
        return item.hasComponent(PetSkinComponent.class) ? item : null;
    }

    public static Result apply(SkyBlockItem pet, SkyBlockItem skinItem) {
        if (!skinItem.hasComponent(PetSkinComponent.class)) return new Result(Outcome.NOT_A_SKIN, null);
        PetSkinComponent skin = skinItem.getComponent(PetSkinComponent.class);
        if (skin.isAnimated()) return new Result(Outcome.ANIMATED, null);
        if (skin.knownPets().isEmpty()) return new Result(Outcome.UNKNOWN_PET, null);
        ItemType petType = pet.getAttributeHandler().getPotentialType();
        if (!skin.appliesTo(petType, pet.getAttributeHandler().getRarity())) return new Result(Outcome.WRONG_PET, null);

        ItemType skinType = skinItem.getAttributeHandler().getPotentialType();
        ItemType previous = pet.getAttributeHandler().getPetSkin();
        if (previous == skinType) return new Result(Outcome.ALREADY_APPLIED, null);
        pet.getAttributeHandler().setPetSkin(skinType);
        return new Result(Outcome.APPLIED, previous);
    }

    public static @Nullable ItemType remove(SkyBlockItem pet) {
        ItemType previous = pet.getAttributeHandler().getPetSkin();
        if (previous != null) pet.getAttributeHandler().setPetSkin(null);
        return previous;
    }

    public static void applyFromHand(SkyBlockPlayer player, SkyBlockItem skinItem) {
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet == null) {
            player.sendMessage("<c>You need to have a pet summoned to apply a pet skin!");
            return;
        }
        Result result = apply(pet, skinItem);
        if (result.outcome() != Outcome.APPLIED) {
            player.sendMessage(result.outcome().message);
            return;
        }

        SkyBlockItem remaining = skinItem.getAmount() > 1 ? skinItem.clone() : null;
        if (remaining != null) remaining.setAmount(skinItem.getAmount() - 1);
        player.setItemInHand(remaining);
        if (result.previous() != null) player.addAndUpdateItem(new SkyBlockItem(result.previous()));
        player.getPetData().updatePetEntityImpl(player);

        player.sendMessage("<a>You applied the " + skinItem.getDisplayName() + " <a>to your " + petName(pet) + "<a>!");
        player.playSound(Sound.sound()
                .type(Key.key("minecraft", "entity.player.levelup"))
                .volume(1f)
                .pitch(2f)
                .build());
    }

    public static void removeToInventory(SkyBlockPlayer player, SkyBlockItem pet) {
        ItemType skin = pet.getAttributeHandler().getPetSkin();
        if (skin == null) {
            player.sendMessage("<c>This pet isn't wearing a skin!");
            return;
        }
        SkyBlockItem skinItem = new SkyBlockItem(skin);
        if (!player.canFitItem(PlayerItemUpdater.playerUpdate(player, skinItem.getItemStack(), true).build())) {
            player.sendMessage("<c>Your inventory is full!");
            return;
        }
        remove(pet);
        player.addAndUpdateItem(skinItem);
        if (player.getPetData().getEnabledPet() == pet) player.getPetData().updatePetEntityImpl(player);
        player.sendMessage("<a>You removed the " + skinItem.getDisplayName() + " <a>from your " + petName(pet) + "<a>!");
    }

    private static String petName(SkyBlockItem pet) {
        return "<color:" + pet.getAttributeHandler().getRarity().getColor().asHexString() + ">"
                + pet.getComponent(PetComponent.class).getPetName();
    }
}
