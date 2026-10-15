package dev.daweapon.type.crimsonisle.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.GUICrimsonEssenceShop;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vulcan runs the Crimson Essence Shop in the Forgotten Skull. On Hypixel he only opens it after a
 * Kuudra win; that fight isn't built, so the shop is open to everyone.
 */
public class NPCVulcan extends HypixelNPC implements NPCAbiphoneTrait {
    private static final Set<UUID> GREETED = ConcurrentHashMap.newKeySet();

    public NPCVulcan() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<c>Vulcan", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U3ZjliOGI4MDk5ODViYTk4ZGQ2MTI0ZmUzODVmNmE0NDY2YWE2ZTE4MjQ5YTFmZTYzN2NiNjVjNDBkZmM3OTciLCJtZXRhZGF0YSI6e319fSwicHJvZmlsZUlkIjoiOGVhMzQyN2Y2ZDFlMmNhN2I3NjM3Y2U2YmYzYjU2MzMiLCJwcm9maWxlTmFtZSI6Im5lYTg5IiwiaXNQdWJsaWMiOnRydWUsInRpbWVzdGFtcCI6MTc3NTExNDcwODYzOH0=";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<c>Vulcan");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-362.5, 114, -1030.5);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        if (isInDialogue(player)) return;
        if (!GREETED.add(player.getUuid())) {
            player.openView(new GUICrimsonEssenceShop());
            return;
        }
        setDialogue(player, "hello").thenRun(() -> player.openView(new GUICrimsonEssenceShop()));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Do you really have what it takes to fight <c>Kuudra<f>?",
                        "Because if you don't... I have exactly what you need!",
                        "I'm the <d>Crimson Essence<f> guy, but you can call me <c>Vulcan<f>."
                ).build()
        };
    }

    @Override
    public String getAbiphoneKey() {
        return "vulcan";
    }
}
