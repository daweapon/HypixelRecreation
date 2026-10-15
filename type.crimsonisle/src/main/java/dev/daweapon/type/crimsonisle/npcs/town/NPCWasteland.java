package dev.daweapon.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.town.TalkNPC;
import dev.daweapon.type.crimsonisle.town.TownSkins;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * The Wasteland NPCs: the two Kuudra protesters (shown only after the player has defeated Kuudra),
 * Cyndarin on the Cathedral path and the Desperate Engineer with the Moody Grappleshot.
 */
public final class NPCWasteland {
    private NPCWasteland() {
    }

    private static class Protester extends TalkNPC {
        Protester(String name, String texture, Pos home, String... lines) {
            super(name, texture, home, lines);
        }

        @Override
        protected boolean visible(SkyBlockPlayer player) {
            return KuudraProgress.hasDefeatedKuudra(player);
        }
    }

    public static class Aviar extends Protester {
        public Aviar() {
            super("<a>Aviar", TownSkins.AVIAR, new Pos(-360.5, 117, -971.5),
                    "Why is there so much hate in the world?",
                    "You discover a new species and your first reaction is to kill them?",
                    "Have you even tried to understand them?", "Don't you think what you are doing is wrong?");
        }
    }

    public static class Gnyl extends Protester {
        public Gnyl() {
            super("<a>Gnyl", TownSkins.GNYL, new Pos(-376.5, 117, -971.5),
                    "Kuudra is just like us.", "Sulphur is life - if there is enough for us there is enough for them.",
                    "I would gladly share my Sulphur with them!", "Please give them a chance!");
        }
    }

    public static class Cyndarin extends TalkNPC {
        public Cyndarin() {
            super("<5>Cyndarin", TownSkins.CYNDARIN, new Pos(-204.5, 112, -956.5),
                    lines("I like to use this path to go to the <5>Cathedral<f>.",
                            "It's much more quiet around here, I can clear my head."));
        }
    }

    public static class DesperateEngineer extends CrimsonNPC {
        public DesperateEngineer() {
            super("<e>Desperate Engineer", TownSkins.DESPERATE_ENGINEER, new Pos(-289.5, 127, -982.7));
        }

        @Override
        protected String[] holograms(SkyBlockPlayer player) {
            return new String[]{"Desperate Engineer", "<e><l>CLICK"};
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            say(player, "I hate Blazes. Blazes are terrible. Blazes hurt. Blazes make me cry.")
                    .thenRun(() -> player.openView(new CatalogueShopView("desperate_engineer")));
        }
    }
}
