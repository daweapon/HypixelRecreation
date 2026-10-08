package net.swofty.type.skyblockgeneric.crimson.kuudra.follower;

import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The Teeth Plaque shows its owner's current stage. Only the final form's lore is known (NEU); the
 * decaying stages reuse its flavour text with a line saying it can still break down (an estimate).
 */
public final class KuudraTeethPlaqueLore {
    public static final String HANDLER_ID = "KUUDRA_TEETH_PLAQUE";

    private KuudraTeethPlaqueLore() {
    }

    public static void register() {
        LoreRegistry.register(HANDLER_ID, new LoreConfig(
                (_, player) -> lore(player),
                (_, player) -> stage(player).coloured()));
    }

    private static TeethPlaqueStage stage(@Nullable SkyBlockPlayer player) {
        TeethPlaqueStage stage = player == null ? null : KuudraFollowerProgress.plaqueStage(player);
        return stage == null ? TeethPlaqueStage.EXQUISITE : stage;
    }

    private static List<String> lore(@Nullable SkyBlockPlayer player) {
        TeethPlaqueStage stage = stage(player);
        List<String> lines = new ArrayList<>(List.of(
                "<7><o>Once used to poison a great beast.",
                "<7><o>Its remains show the true mark of a",
                "<7><o>great adventurer.",
                ""));
        if (stage == TeethPlaqueStage.FINAL) {
            lines.add("<8><o>This item is in its final form!");
            if (player != null) {
                lines.add("");
                lines.add("<7>Discoverer: <f>" + player.getUsername());
            }
        } else {
            lines.add("<8><o>Something could still break this down.");
        }
        lines.add("");
        lines.add("<8><l>* <8>Soulbound <8><l>*");
        lines.add(stage.footer());
        return lines;
    }
}
