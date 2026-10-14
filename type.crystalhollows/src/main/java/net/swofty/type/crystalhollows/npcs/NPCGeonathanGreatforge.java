package net.swofty.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.drill.DrillFuel;
import net.swofty.type.skyblockgeneric.gui.inventories.drill.GUIDrillAnvil;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

public class NPCGeonathanGreatforge extends AbstractHollowsNPC {
    private static final String MET = "met_geonathan";

    public NPCGeonathanGreatforge() {
        super("Geonathan Greatforge", "<2>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIxM2NmMGNhNzlhMzYxMWI4ZTA1ZmU5ZTI2NGZiMmJmOGQyN2U0NjRkYzEyZGM2ZTk1ZGQwYWUwYzMzNWE1NjEifX19", new Pos(530.5, 106, 556.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        if (!hasMet(player, MET)) {
            setDialogue(player, "hello").thenRun(() -> markMet(player, MET));
            return;
        }
        if (!DrillFuel.hasDrill(player)) {
            setDialogue(player, "no-drill-" + (1 + ThreadLocalRandom.current().nextInt(3)));
            return;
        }
        player.openView(new GUIDrillAnvil());
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Sup, <b>" + player.getUsername() + "<f>? I am <2>Geonathan Greatforge<f>, your one stop shop for refueling and upgrading <a>Drills<f>.",
                        "Drills are like pickaxes, but better! They are powered with <2>Drill Fuel<f>, which comes in many different forms.",
                        "<9>Biofuel<f> is one of such fuels, which applies <2>3,000<f> fuel to your Drill!",
                        "You can also apply <6>Drill Upgrades<f> to Drills, offering various buffs!"
                ).build(),
                DialogueSet.builder().key("no-drill-1").lines(
                        "Drills are sweet! Too bad you don't have one. I hear you can create them in the <6>Forge<f>!"
                ).build(),
                DialogueSet.builder().key("no-drill-2").lines(
                        "Come back to me when you have a <a>Drill<f> in your inventory!"
                ).build(),
                DialogueSet.builder().key("no-drill-3").lines(
                        "What are you doing talking to me without a <a>Drill<f>? Come back when you have one!"
                ).build()
        };
    }
}
