package dev.daweapon.type.crimsonisle.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.crimsonisle.faction.FactionVillagers;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.GUICrimsonBlacksmith;

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
        FactionVillagers.register("Igor", "<c>Igor", new Pos(-549.5, 98, -707.5), CrimsonFaction.BARBARIAN);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (isInDialogue(event.player())) return;
        if (FactionVillagers.intercept("Igor", (SkyBlockPlayer) event.player())) return;
        event.player().openView(new GUICrimsonBlacksmith("Igor"));
    }
}
