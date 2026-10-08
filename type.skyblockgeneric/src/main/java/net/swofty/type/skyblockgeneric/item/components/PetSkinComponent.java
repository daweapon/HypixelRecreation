package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SafeConfig;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetSkins;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A cosmetic Pet Skin. Applying it to a summoned pet of one of {@link #pets} replaces that pet's
 * head texture. Animated skins are catalogued but can't be applied yet.
 */
@Getter
public class PetSkinComponent extends SkyBlockItemComponent {
    private static final String ULTIMATE_COLOR = "#AA0000";

    private final String petName;
    private final List<String> pets;
    private final @Nullable Rarity minPetRarity;
    private final String texture;
    private final boolean animated;
    private final @Nullable String rarityLabel;

    private PetSkinComponent(String petName, List<String> pets, @Nullable Rarity minPetRarity, String texture,
                             boolean animated, @Nullable String rarityLabel) {
        this.petName = petName;
        this.pets = List.copyOf(pets);
        this.minPetRarity = minPetRarity;
        this.texture = texture;
        this.animated = animated;
        this.rarityLabel = rarityLabel;

        addInheritedComponent(new SkullHeadComponent(_ -> texture));
        addInheritedComponent(new InteractableComponent(PetSkins::applyFromHand, null, null));
        addInheritedComponent(new LoreUpdateComponent(new LoreConfig(
                (item, _) -> lore(item), (item, _) -> displayName(item)), true));
    }

    public static PetSkinComponent parse(SafeConfig config) {
        String minRarity = config.getString("min_pet_rarity", null);
        return new PetSkinComponent(
                config.getString("pet_name"),
                config.getList("pets", String.class),
                minRarity == null ? null : Rarity.valueOf(minRarity.toUpperCase()),
                config.getString("texture"),
                config.getBoolean("animated", false),
                config.getString("rarity_label", null));
    }

    public boolean appliesTo(ItemType petType, Rarity petRarity) {
        if (!pets.contains(petType.name())) return false;
        return minPetRarity == null || petRarity.isAtLeast(minPetRarity);
    }

    public List<ItemType> knownPets() {
        List<ItemType> known = new ArrayList<>();
        for (String id : pets) {
            ItemType type = ItemType.get(id);
            if (type != null) known.add(type);
        }
        return known;
    }

    public String colorOf(SkyBlockItem item) {
        return rarityLabel != null ? ULTIMATE_COLOR : item.getAttributeHandler().getRarity().getColor().asHexString();
    }

    public String skinName(SkyBlockItem item) {
        return item.getAttributeHandler().getPotentialType().getDisplayName();
    }

    private String displayName(SkyBlockItem item) {
        return "<color:" + colorOf(item) + ">" + skinName(item);
    }

    private List<String> lore(SkyBlockItem item) {
        List<String> lore = new ArrayList<>();
        lore.add("<8>Consumed on use");
        lore.add(" ");
        lore.add("<7>Pet skins change the look and");
        lore.add("<7>particle trail of your pet but only");
        lore.add("<7>one skin can be active at a time!");
        lore.add(" ");
        lore.add("<7>This skin can only be applied to");
        lore.add("<a>" + petName + "<7> pets.");
        lore.add(" ");
        if (animated) {
            lore.add("<a>This skin is animated!");
            lore.add(" ");
            lore.add("<8>Animated skins can't be applied yet.");
            lore.add("<8>(Not active yet)");
        } else {
            lore.add("<e>Right-click on your summoned pet to");
            lore.add("<e>apply this skin!");
        }
        lore.add(" ");
        String rarity = rarityLabel != null ? rarityLabel : item.getAttributeHandler().getRarity().name().replace('_', ' ');
        lore.add("<color:" + colorOf(item) + "><l>" + rarity + " COSMETIC");
        return lore;
    }
}
