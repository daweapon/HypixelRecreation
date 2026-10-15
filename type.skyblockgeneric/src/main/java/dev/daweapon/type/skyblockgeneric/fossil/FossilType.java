package dev.daweapon.type.skyblockgeneric.fossil;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
public enum FossilType {
    HELIX("Helix Fossil", ItemType.HELIX, null, false, "It's so small, but it's a Helix Fossil!",
            "the fossil i want lived underwater", "HELIX_FOSSIL",
            "XXX.", "X..X", "X.XX", ".XX."),
    CLAW("Claw Fossil", ItemType.CLAW_FOSSIL, null, false, "That Claw Fossil looks pretty daunting...",
            "the fossil i want lived underground and dug tunnels", "CLAW_FOSSIL",
            "XX..", "XXX.", ".XXX", "..XX"),
    CLUBBED("Clubbed Fossil", ItemType.CLUBBED_FOSSIL, "ANKYLOSAURUS;4", true,
            "The Clubbed Fossil is quite majestic, I've never seen another tail like it.",
            "the fossil i want had a really fancy tail", "CLUBBED_FOSSIL",
            "XXXX.", "...XX", "...XX"),
    FOOTPRINT("Footprint Fossil", ItemType.FOOTPRINT_FOSSIL, "TYRANNOSAURUS;4", true,
            "Believe it or not, but that's a Footprint Fossil!",
            "the fossil i want was the king of his kind", "FOOTPRINT_FOSSIL",
            ".X.X.", "XXXXX", ".XXX."),
    SPINE("Spine Fossil", ItemType.SPINE_FOSSIL, "SPINOSAURUS;4", true, "A Spine Fossil! This one's pretty rare.",
            "the fossil i want was kinda spiny u know", "SPINE_FOSSIL",
            "X.X.X", "XXXXX"),
    TUSK("Tusk Fossil", ItemType.TUSK_FOSSIL, "MAMMOTH;4", true, "That Tusk Fossil looks absolutely amazing.",
            "the fossil i want lived in herds and was quite woolly", "TUSK_FOSSIL",
            "..XX", "..XX", ".XX.", "XX.."),
    UGLY("Ugly Fossil", ItemType.UGLY_FOSSIL, "GOBLIN;4", true, "That thing looks gross. It's surely an Ugly Fossil!",
            "the fossil i want is pretty rough to look at", "UGLY_FOSSIL",
            "XX.XX", "XXXXX", ".XXX."),
    WEBBED("Webbed Fossil", ItemType.WEBBED_FOSSIL, "PENGUIN;4", true, "It belonged to a particularly resilient Penguin.",
            "the fossil i want had a really pointy beak", "WEBBED_FOSSIL",
            "X.X.X", "XXXXX", ".XXX.");

    private final String displayName;
    private final ItemType item;
    private final @Nullable String petRecipe;
    private final boolean inExcavator;
    private final String donationLine;
    private final String muncherHint;
    private final String id;
    private final List<String> shape;

    FossilType(String displayName, ItemType item, @Nullable String petRecipe, boolean inExcavator, String donationLine,
               String muncherHint, String id, String... shape) {
        this.displayName = displayName;
        this.item = item;
        this.petRecipe = petRecipe;
        this.inExcavator = inExcavator;
        this.donationLine = donationLine;
        this.muncherHint = muncherHint;
        this.id = id;
        this.shape = List.of(shape);
    }

    public int tileCount() {
        return (int) shape.stream().flatMapToInt(String::chars).filter(c -> c == 'X').count();
    }

    public int width() {
        return shape.stream().mapToInt(String::length).max().orElse(0);
    }

    public int height() {
        return shape.size();
    }

    public boolean unlocksPetRecipe() {
        return petRecipe != null;
    }

    public static List<FossilType> excavatorPool() {
        return Arrays.stream(values()).filter(FossilType::isInExcavator).toList();
    }

    public static @Nullable FossilType byItem(ItemType item) {
        return Arrays.stream(values()).filter(type -> type.item == item).findFirst().orElse(null);
    }

    public static @Nullable FossilType byPetRecipe(String recipeId) {
        return Arrays.stream(values()).filter(type -> recipeId.equals(type.petRecipe)).findFirst().orElse(null);
    }
}
