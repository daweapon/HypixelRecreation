package dev.daweapon.type.skyblockgeneric.fishing.bestiary;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;

import java.util.List;

/**
 * The six sub-categories of the Bestiary's Fishing section (wiki: Bestiary/List, Fishing tab;
 * in-game "Bestiary ➜ Fishing" menu).
 */
@Getter
public enum FishingBestiarySection {
    FISHING("<3>Fishing", 20,
            new GUIMaterial("d88ba8bb50b79e441e47b7e452764d5fff6693779d2dadd9f7f52f98d7ea0"),
            "<7>View all of the <3>Sea Creatures <7>that",
            "<7>you've killed while fishing."),
    LAVA("<c>Lava", 21,
            new GUIMaterial("1642a06cd75ef307c1913ba7a224fb2082d8a2c5254fd1bf006125a087a9a868"),
            "<7>View all of the <3>Sea Creatures <7>that",
            "<7>you've killed while fishing in <c>Lava<7>."),
    SPOOKY_FESTIVAL("<6>Spooky Festival", 22,
            new GUIMaterial(Material.CARVED_PUMPKIN),
            "<7>View all of the <3>Sea Creatures <7>that",
            "<7>you've killed while fishing during the",
            "<6>Spooky Festival<7>."),
    FISHING_FESTIVAL("<b>Fishing Festival", 23,
            new GUIMaterial("ea575977e6bd0c7add94e2d8fdcc2af77e36c44d6b4c67788862a94000be6399"),
            "<7>View all of the <3>Sea Creatures <7>that",
            "<7>you've killed while fishing during the",
            "<b>Fishing Festival<7>."),
    WINTER("<f>Winter", 24,
            new GUIMaterial("4dd2d3c6d01c276226c7b0d377122e1a647b2ffb5b9b54fa98eac37bb1d09d3a"),
            "<7>View all of the <f>Winter <3>Sea Creatures",
            "<7>that you've killed while fishing."),
    BACKWATER_BAYOU("<a>Backwater Bayou", 31,
            new GUIMaterial("1c0cd33590f64d346d98cdd01606938742e715dda6737353306a44f81c8ba426"),
            "<7>View all of the <3>Sea Creatures <7>that",
            "<7>you've killed while fishing in the",
            "<a>Backwater Bayou<7>."),
    TORRHUS_CANYON("<6>Torrhus Canyon", 32,
            new GUIMaterial("837be7781a3ae4454f4670e1686dc17a07dd321d6f670ba80f91e391f0763321"),
            "<7>View all of the <3>Sea Creatures <7>that",
            "<7>you've killed while fishing in",
            "<6>Torrhus Canyon<7>."),
    MOONGLADE_MARSH("<2>Moonglade Marsh", 33,
            new GUIMaterial("a3b9003ba2d05562c75119b8a62185c67130e9282f7acbac4bc2824c21eb95d9"),
            "<7>View all of the <3>Sea Creatures <7>that",
            "<7>you've killed while fishing in",
            "<2>Moonglade Marsh<7>."),
    ;

    private final String displayName;
    private final int slot;
    private final GUIMaterial icon;
    private final List<String> description;

    FishingBestiarySection(String displayName, int slot, GUIMaterial icon, String... description) {
        this.displayName = displayName;
        this.slot = slot;
        this.icon = icon;
        this.description = List.of(description);
    }
}
