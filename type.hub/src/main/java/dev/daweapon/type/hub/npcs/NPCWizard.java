package dev.daweapon.type.hub.npcs;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.gui.GUIDimensionalInfusion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.Consumer;

public class NPCWizard extends HypixelNPC {

    public NPCWizard() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<d>Wizard", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Ubs3ogFzpCcDZZQiNcIrTQTykcnJsac0Fm+mXYMFVh7CNuTdmGlcJcqdb8fXmBJFOfO9/8zAxKVyn3YVehkzzFdHJyMiPqK++arqf0eBNElbON9LUlPvOGca7JCBYBJdZWbpTS3lbzL9VZRoba9GmM0XZfvunZyLTF671JrKT0adDREe4B7HJaqmLAPOXZlzQokxeyN0oqXAPThkQPcR2UzKNQpwAAPiQhrPc7dTvikw9sHOVPX9GsTsqUAjh1vQOZl4DeALMZwG5Clzc+SDWAa5hgNn15SVCWlbPcsumGziXo1EPhYrx5XbGkDS/9OE+y7/7qMQE1YAUoVw4umREJ5YoqWfv5Elko1dOkF6HfVLuyglFIoJenPJ6/jy7C1tNpHbCG/xOKBeMAGR1+YNCIm3JWzgZbFDMpKjFhfgQEOaLPsQPLPxAYA+RjNRpQpXSke8Zfi64j2j85G4gCcOy8zxlYjHdKgxiZNSIs2cyLSU87BdJFaVBKkQvnB5z9AsJu6u1OoC0A4NakRDQyhjzn93RO8i6LoF2MQshEbBn/2q1ja/KfIw4ZjsK11G7xTP+qnVdkXzgC0eGwnfAqkltbfbmwnGVShe0+cVF3FNs+C/j2BtjptUm7Fhw7nu/I40J+u+jTA/NdZl4JWGCIDIzsawACfO14Xt5DjrmtcY24U=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTkxNDEyMjMzNjIsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJmNjlhYjJmNGU3MzQxYWMyNjI1OTcwODNjYTM4YWQwMWJmYmJmODczMGZiZmJkZDEwY2E5MGFlZTMzN2EyZGQifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(48.5, 119, 99.5, -231, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<d>Wizard";
            }
        });
    }

    private static final int REQUIRED_SKYBLOCK_LEVEL = 12;
    private static final String OPTION_ID = "wizard";

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        if (player.getSkyBlockExperience().getLevel().asInt() < REQUIRED_SKYBLOCK_LEVEL) {
            setDialogue(player, "low-level").thenRun(() ->
                player.sendMessage("<7>(Requires SkyBlock Level {})", REQUIRED_SKYBLOCK_LEVEL));
            return;
        }
        if (player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_WIZARD)) {
            player.openView(new GUIDimensionalInfusion());
            return;
        }

        setDialogue(player, "greeting").thenRun(() -> NPCOption.sendOption(player, OPTION_ID, true, List.of(
            option("yes", NamedTextColor.GREEN, "Yes", p -> setDialogue(p, "breach").thenRun(() ->
                askThen(p, "succeed", "And did you succeed?", "succeeded", () ->
                    askThen(p, "why", "Why aren't you there yourself?", "why-not-you", () ->
                        askThen(p, "need", "Sounds bad. Why do you need me?", "need-you", () -> offerHelp(p)))))),
            option("no", NamedTextColor.RED, "No", p -> setDialogue(p, "declined"))
        )));
    }

    private void askThen(HypixelPlayer player, String key, String label, String dialogueKey, Runnable next) {
        NPCOption.sendOption(player, OPTION_ID, true, List.of(
            option(key, NamedTextColor.GREEN, label, p -> setDialogue(p, dialogueKey).thenRun(next))
        ));
    }

    private void offerHelp(HypixelPlayer player) {
        NPCOption.sendOption(player, OPTION_ID, true, List.of(
            option("absolutely", NamedTextColor.GREEN, "Absolutely.", this::acceptQuest),
            option("gear", NamedTextColor.GOLD, "I'm doing it for gear.", this::acceptQuest)
        ));
    }

    private void acceptQuest(HypixelPlayer player) {
        setDialogue(player, "infusion").thenRun(() -> {
            player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_WIZARD, true);
            player.openView(new GUIDimensionalInfusion());
        });
    }

    private static NPCOption.Option option(String key, NamedTextColor colour, String label, Consumer<HypixelPlayer> action) {
        return new NPCOption.Option(key, colour, false, label, action);
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.ofTranslation("low-level", "npcs_hub.wizard.dialogue.low_level"),
            DialogueSet.ofTranslation("greeting", "npcs_hub.wizard.dialogue.greeting"),
            DialogueSet.ofTranslation("declined", "npcs_hub.wizard.dialogue.declined"),
            DialogueSet.ofTranslation("breach", "npcs_hub.wizard.dialogue.breach"),
            DialogueSet.ofTranslation("succeeded", "npcs_hub.wizard.dialogue.succeeded"),
            DialogueSet.ofTranslation("why-not-you", "npcs_hub.wizard.dialogue.why_not_you"),
            DialogueSet.ofTranslation("need-you", "npcs_hub.wizard.dialogue.need_you"),
            DialogueSet.ofTranslation("infusion", "npcs_hub.wizard.dialogue.infusion")
        };
    }
}
