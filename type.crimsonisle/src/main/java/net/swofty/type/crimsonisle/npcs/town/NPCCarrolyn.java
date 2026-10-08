package net.swofty.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.StringUtility;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.gui.town.GUICarrolyn;
import net.swofty.type.crimsonisle.town.TownSkins;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.skyblockgeneric.crimson.town.CarrolynExport;
import net.swofty.type.skyblockgeneric.crimson.town.CarrolynExports;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

/** Carrolyn in Scarleton, who exports 3,000 of each crop for permanent Health and crop Fortune (wiki Carrolyn). */
public class NPCCarrolyn extends CrimsonNPC implements NPCAbiphoneTrait {
    private static final String NAME = "<5>Carrolyn";

    public NPCCarrolyn() {
        super(NAME, TownSkins.CARROLYN, new Pos(1, 103.0625, -803));
    }

    @Override
    public String getAbiphoneKey() {
        return "carrolyn";
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        player.openView(new GUICarrolyn(this::give));
    }

    private void give(SkyBlockPlayer player, CarrolynExport export) {
        if (CarrolynExports.isComplete(player, export)) {
            say(player, export.thanks());
            return;
        }
        CarrolynExports.Delivery delivery = CarrolynExports.deliver(player, export);
        if (delivery.given() == 0) {
            player.sendMessage("<c>You don't have any " + export.displayName() + "<c>!");
            return;
        }
        if (!delivery.completed()) {
            say(player, "Nice, only <6>" + StringUtility.commaify(CarrolynExports.REQUIRED - delivery.total()) + " "
                    + export.progressWord() + "<f> to go.");
            return;
        }
        scene().say(NAME, "Very good.", "Hopefully our business has been very profitable for the both of us.")
                .raw("<a><l>" + export.completeTitle().toUpperCase(Locale.ROOT) + " EXPORTATION COMPLETE!")
                .raw("<c>+5❤ Health")
                .raw("<6>+12☘ " + export.fortune().getDisplayName())
                .play(player);
    }
}
