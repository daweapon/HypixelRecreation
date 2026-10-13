package net.swofty.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crystalhollows.gui.GUIShopGeonathan;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.DrillComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCGeonathanGreatforge extends AbstractHollowsNPC {
    private static final String MET = "met_geonathan";

    public NPCGeonathanGreatforge() {
        super("Geonathan Greatforge", "<6>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIxM2NmMGNhNzlhMzYxMWI4ZTA1ZmU5ZTI2NGZiMmJmOGQyN2U0NjRkYzEyZGM2ZTk1ZGQwYWUwYzMzNWE1NjEifX19", new Pos(530.5, 106, 556.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        if (!hasMet(player, MET)) {
            setDialogue(player, "hello").thenRun(() -> markMet(player, MET));
            return;
        }
        if (!hasDrill(player)) {
            setDialogue(player, "no-drill");
            return;
        }
        player.openView(new GUIShopGeonathan());
    }

    private static boolean hasDrill(SkyBlockPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            SkyBlockItem item = new SkyBlockItem(player.getInventory().getItemStack(slot));
            if (!item.isAir() && item.hasComponent(DrillComponent.class)) return true;
        }
        return false;
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Geonathan Greatforge, your one stop shop for refueling and upgrading Drills!"
                ).build(),
                DialogueSet.builder().key("no-drill").lines(
                        "You don't have a Drill on you! You can get one from the Forge."
                ).build()
        };
    }
}
