package dev.daweapon.type.skyblockgeneric.romero;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class RomeroJulietteNPC extends HypixelNPC {
    protected RomeroJulietteNPC() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e>Juliette", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return RomeroSkins.JULIETTE_SIGNATURE;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return RomeroSkins.JULIETTE_TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-415.5, 130, -121.5, 14, 0);
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
        if (RomeroJulietteQuest.isCompleted(player)) {
            setDialogue(player, "idle");
            return;
        }
        if (!RomeroJulietteQuest.isStarted(player)) {
            if (!RomeroJulietteQuest.meetsCombatRequirement(player)) {
                player.sendMessage("<c>You need Combat {} to start the Romero and Juliette quest!", RomeroJulietteQuest.COMBAT_REQUIREMENT);
                return;
            }
            setDialogue(player, "j1-start").thenRun(() -> RomeroJulietteQuest.start(player));
            return;
        }
        RomeroStep step = RomeroJulietteQuest.currentStep(player);
        if (!RomeroJulietteQuest.isDelivered(player)) {
            setDialogue(player, step == RomeroStep.WEDDING ? "j10-before-suit" : "j" + step.getNumber() + "-start");
            return;
        }
        if (!RomeroJulietteQuest.holdsQuestItem(player)) {
            setDialogue(player, "not-holding");
            return;
        }
        String key = step == RomeroStep.WEDDING ? "j10-after-suit" : "j" + step.getNumber() + "-complete";
        setDialogue(player, key).thenRun(() -> {
            if (RomeroJulietteQuest.isDelivered(player) && RomeroJulietteQuest.holdsQuestItem(player)) RomeroJulietteQuest.completeStep(player);
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        String name = player == null ? "Player" : ((SkyBlockPlayer) player).getUsername();
        List<DialogueSet> sets = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : RomeroDialogue.get().juliette().entrySet()) {
            String[] lines = entry.getValue().stream().map(line -> line.replace("{player}", name)).toArray(String[]::new);
            sets.add(DialogueSet.builder().key(entry.getKey()).lines(lines).build());
        }
        return sets.toArray(DialogueSet[]::new);
    }
}
