package dev.daweapon.type.hub.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.gui.GUIShopLibrarian;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class VillagerLibrarian extends HypixelNPC {
    public VillagerLibrarian() {
        super(new VillagerConfiguration(){
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<f>Librarian", "<e><l>CLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-68.5, 70, -79.5, -45, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.LIBRARIAN;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        MissionData data = player.getMissionData();

        if (data.isCurrentlyActive("speak_to_librarian")) {
            setDialogue(player, "quest-hello").thenRun(() -> {
                data.endMission("speak_to_librarian");
            });
            return;
        }

        player.openView(new GUIShopLibrarian());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("quest-hello").lines(
                                "Greetings! Welcome to the <b>Library<f>!",
                                "The <b>Library <f>is your one-stop shop for all things enchanting. Enchant items, purchase <a>Enchanted Books<f>, and more!",
                                "You can enchant items by clicking any <a>Enchanting Table<f>. Enchanting costs <3>experience levels <f>- the more levels you spend, the better enchantments you will receive.",
                                "Use the <a>Enchanting Table <f>to enchant an item!"
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
