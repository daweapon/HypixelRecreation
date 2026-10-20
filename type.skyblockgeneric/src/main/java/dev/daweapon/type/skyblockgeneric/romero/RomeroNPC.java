package dev.daweapon.type.skyblockgeneric.romero;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class RomeroNPC extends HypixelNPC {
    private static final String CLICK = "<e><l>[CLICK]";
    private static final String RESTORE_CLICK = "<hover:'<e>Click to restore your gift!'><click:run:'/romerorestore'><e><l>[CLICK]</click></hover>";
    private final RomeroStep step;

    protected RomeroNPC(RomeroStep step) {
        super(configuration(step));
        this.step = step;
    }

    private static HumanConfiguration configuration(RomeroStep step) {
        return new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e>Romero", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return RomeroSkins.ROMERO_SIGNATURE;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return RomeroSkins.ROMERO_TEXTURE;
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<e>Romero");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return step.position();
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                if (step == RomeroStep.WEDDING) return RomeroJulietteQuest.step(skyBlockPlayer) >= RomeroStep.WEDDING.getNumber();
                return RomeroJulietteQuest.isActive(skyBlockPlayer, step);
            }
        };
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (RomeroJulietteQuest.isCompleted(player)) {
            setDialogue(player, "idle");
            return;
        }
        if (!RomeroJulietteQuest.isActive(player, step)) return;
        if (RomeroJulietteQuest.isDelivered(player)) {
            boolean holding = player.countItem(step.questItem()) > 0;
            setDialogue(player, holding ? "bring-item" : "bring-item-missing");
            return;
        }
        RomeroInteraction.Response response = RomeroInteraction.respond(player, step);
        setDialogue(player, response.key()).thenRun(() -> {
            if (response.completes() && RomeroInteraction.respond(player, step).completes()) {
                RomeroInteraction.consume(player, step);
                RomeroJulietteQuest.deliver(player, step);
            }
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        String name = player == null ? "Player" : ((SkyBlockPlayer) player).getUsername();
        List<DialogueSet> sets = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : RomeroDialogue.get().romero().entrySet()) {
            String[] lines = entry.getValue().stream()
                    .map(line -> line.replace("{player}", name).replace(CLICK, RESTORE_CLICK))
                    .toArray(String[]::new);
            sets.add(DialogueSet.builder().key(entry.getKey()).lines(lines).build());
        }
        return sets.toArray(DialogueSet[]::new);
    }
}
