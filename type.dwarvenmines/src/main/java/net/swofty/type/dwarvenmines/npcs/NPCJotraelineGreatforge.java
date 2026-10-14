package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.drill.DrillFuel;
import net.swofty.type.skyblockgeneric.gui.inventories.drill.GUIDrillAnvil;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

public class NPCJotraelineGreatforge extends HypixelNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODUzNjJiNjFmMjk4ZGVjMmRmNzY1NDU3ZDZiNDE3ZTkxN2QyMzA5ZWJjOGY2OTFmMDg1NTQ4OTY4MmRkZWM1OSJ9fX0=";

    public NPCJotraelineGreatforge() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<2>Jotraeline Greatforge", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<2>Jotraeline Greatforge";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-6.5, 145, -18.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        var toggles = player.getToggles();
        if (!toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_JOTRAELINE)) {
            setDialogue(player, "hello").thenRun(() -> toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_JOTRAELINE, true));
            return;
        }
        if (!DrillFuel.hasDrill(player)) {
            setDialogue(player, "no-drill-" + (1 + ThreadLocalRandom.current().nextInt(3)));
            return;
        }
        player.openView(new GUIDrillAnvil());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Sup, <b>" + player.getUsername() + "<f>? I am <2>Jotraeline Greatforge<f>, your one stop shop for refueling and upgrading <a>Drills<f>.",
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
