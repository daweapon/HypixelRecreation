package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles.Toggles.ToggleType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.impl.AbiphoneHoppity;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.gui.inventories.rabbits.GUIHoppityShop;
import dev.daweapon.type.skyblockgeneric.hoppity.HuntEggSchedule;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCHoppity extends HypixelNPC implements NPCAbiphoneTrait {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc5ZTdmMzM0MWI2NzJkOWRlNjU2NGNiYWNhMDUyYTZhNzIzZWE0NjZhMmU2NmFmMzViYTFiYTg1NWYwZDY5MiJ9fX0=";

    public NPCHoppity() {
        super(new HumanConfiguration() {
            @Override
            public boolean visible(HypixelPlayer player) {
                return huntActive();
            }

            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<a>Hoppity", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(56.5, 70, -2.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    private static boolean huntActive() {
        return HuntEggSchedule.isHuntActive(SkyBlockCalendar.getElapsed());
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (!huntActive()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!player.getToggles().get(ToggleType.HAS_SPOKEN_TO_HOPPITY)) {
            setDialogue(player, "hello").thenRun(() -> player.getToggles().set(ToggleType.HAS_SPOKEN_TO_HOPPITY, true));
            return;
        }
        player.openView(new GUIHoppityShop());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Hello!!!",
                        "I'm the proud owner of the Chocolate Factory!",
                        "My Chocolate is the finest chocolate in the entire world.",
                        "I have a brand new marketing strategy, and every Spring season I will spread various Rabbit Eggs across the islands.",
                        "Collect all the Rabbits I left in those eggs and I will let you use my factory!",
                        "Happy egg hunting!"
                ).build()
        };
    }

    @Override
    public String getAbiphoneKey() {
        return AbiphoneHoppity.ID;
    }
}
