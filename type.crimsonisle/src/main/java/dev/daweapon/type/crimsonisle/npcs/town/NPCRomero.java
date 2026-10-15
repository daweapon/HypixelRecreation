package dev.daweapon.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.town.TownSkins;
import dev.daweapon.type.skyblockgeneric.crimson.town.RomeroCrimsonStep;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Romero's Crimson Isle stop (step 4 of Romero and Juliette, wiki Romero): he wants an Enchanted Red
 * Mushroom Block for Juliette's stew. Only shown while that step is active (see {@link RomeroCrimsonStep}).
 */
public class NPCRomero extends CrimsonNPC {
    private static final String NAME = "<e>Romero";

    public NPCRomero() {
        super(NAME, TownSkins.ROMERO, new Pos(-388.5, 95, -478.5));
    }

    @Override
    protected boolean visible(SkyBlockPlayer player) {
        return RomeroCrimsonStep.isActive(player);
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        ItemType held = SkyBlockItem.isSkyBlockItem(player.getItemInMainHand())
                ? new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType() : null;
        if (held == null) {
            say(player, "Hey, you again!", "I wanted mushrooms to concoct a stew,", "but these <c>big red <fones won't cut it.",
                    "I need some decent mushrooms.");
            return;
        }
        if (held == ItemType.ENCHANTED_RED_MUSHROOM_BLOCK) {
            player.removeItemFromPlayer(ItemType.ENCHANTED_RED_MUSHROOM_BLOCK, 1);
            say(player, "<a>Fantastic!", "There's some stew for <e>Juliette<f>!", "<a>Mmh! <fTastes great!")
                    .thenRun(() -> RomeroCrimsonStep.complete(player));
        } else if (held == ItemType.RED_MUSHROOM) {
            say(player, "I can't cook a gourmet stew with tiny mushrooms!", "I need bigger ones!");
        } else if (held == ItemType.BROWN_MUSHROOM) {
            say(player, "That's not the color of mushroom I'm looking for.");
        } else if (held == ItemType.RED_MUSHROOM_BLOCK) {
            say(player, "<a>Sweet! <fJust what I need!", "<c>EXCEPT<f>... Could you enchant it?",
                    "I need the perfect stew for my beloved <e>Juliette<f>.");
        } else if (held == ItemType.BROWN_MUSHROOM_BLOCK) {
            say(player, "That's a big mushroom that I don't need.",
                    "It's well known that good stews aren't made from brown mushrooms.");
        } else if (held == ItemType.ENCHANTED_BROWN_MUSHROOM_BLOCK) {
            say(player, "I need <c>red mushrooms<f>.");
        } else {
            say(player, "Hey, you again!", "I wanted mushrooms to concoct a stew,",
                    "but these <c>big red <fones won't cut it.", "I need some decent mushrooms.");
        }
    }
}
