package net.swofty.type.crimsonisle.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.essence.GUICrimsonBlacksmith;

// Igor, the Dragontail blacksmith: essence upgrades, salvaging and the Kuudra Armor Type Transfer.
public class NPCIgor extends HypixelNPC {
    public NPCIgor() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<d>Igor", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmZmOTY2NDYxYjRiZDY4ZGYzZmY3YzZhZGFlMGNkYTVmMjdhYzg1M2ExNmUyYTJjNzg0OWIzYzAyNjEwMTM5MCIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJhM2I0YTBiN2NlZGMyMDBiOTdjMGE0MzZiZTA0ZWY0NyIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzUyMTU4NDk4NTE0fQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<d>Igor");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-549.5, 98, -707.5);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (isInDialogue(event.player())) return;
        event.player().openView(new GUICrimsonBlacksmith("Igor"));
    }
}
