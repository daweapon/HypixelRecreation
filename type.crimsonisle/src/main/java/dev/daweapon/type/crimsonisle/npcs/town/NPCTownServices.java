package dev.daweapon.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.town.TownServices;
import dev.daweapon.type.crimsonisle.town.TownSkins;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

/**
 * The Bank, Auction House and Bazaar agents of Scarleton (Belanor, Elmar, Selenar) and Dragontail
 * (Bruto, Bromm and the two Auction Agents, Barter). The bankers and auction masters greet the
 * player once, then open the menu straight away.
 */
public final class NPCTownServices {
    private static final String[] AUCTION_MASTER = {"Hey there, I'm the Auction Master.",
            "At the auction house you can put your valuable items up for auction!",
            "You may also want to check back here to see what items other players are selling to see if you can get a good deal.",
            "Talk to me if you would like to start your first auction or if you want to see the items currently being sold."};

    private NPCTownServices() {
    }

    private abstract static class ServiceNPC extends CrimsonNPC {
        private final String meetingKey;
        private final String[] greeting;
        private final Consumer<SkyBlockPlayer> open;

        ServiceNPC(String name, String texture, Pos home, String meetingKey, String[] greeting, Consumer<SkyBlockPlayer> open) {
            super(name, texture, home);
            this.meetingKey = meetingKey;
            this.greeting = greeting;
            this.open = open;
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            if (greeting.length > 0 && CrimsonSideQuests.firstMeeting(player, meetingKey)) {
                say(player, greeting).thenRun(() -> open.accept(player));
                return;
            }
            open.accept(player);
        }
    }

    public static class Belanor extends ServiceNPC {
        public Belanor() {
            super("<e>Belanor", TownSkins.BELANOR, new Pos(-83.5, 108, -775.5), "belanor",
                    new String[]{"Deposit your <6>Coins <f>before the <c>Mushroom Bulls <f>get to you!"}, TownServices::openBank);
        }
    }

    public static class Bruto extends ServiceNPC {
        public Bruto() {
            super("<e>Bruto", TownSkins.BRUTO, new Pos(-612.5, 100, -786.5), "bruto",
                    new String[]{"Deposit your <6>Coins <f>before the <c>Flaming Spiders <f>get to you!"}, TownServices::openBank);
        }
    }

    public static class Elmar extends ServiceNPC {
        public Elmar() {
            super("<5>Elmar", TownSkins.ELMAR, new Pos(-60.5, 108, -746.5), "elmar", AUCTION_MASTER,
                    TownServices::openAuctionHouse);
        }
    }

    public static class Bromm extends ServiceNPC {
        public Bromm() {
            super("<e>Bromm", TownSkins.BROMM, new Pos(-636.5, 103, -784.5), "bromm", AUCTION_MASTER,
                    TownServices::openAuctionHouse);
        }
    }

    /** The wiki lists this agent at Bromm's exact spot, so it stands a block and a half beside him. */
    public static class AuctionAgentEast extends ServiceNPC {
        public AuctionAgentEast() {
            super("<6>Auction Agent", TownSkins.AUCTION_AGENT, new Pos(-637.5, 103, -782.5), "auction_agent",
                    new String[0], TownServices::openAuctionHouse);
        }
    }

    public static class AuctionAgentWest extends ServiceNPC {
        public AuctionAgentWest() {
            super("<6>Auction Agent", TownSkins.AUCTION_AGENT, new Pos(-639.5, 103, -783.5), "auction_agent",
                    new String[0], TownServices::openAuctionHouse);
        }
    }

    public static class Selenar extends ServiceNPC {
        public Selenar() {
            super("<5>Selenar", TownSkins.SELENAR, new Pos(-70.5, 107, -756.5), "selenar", new String[0],
                    player -> TownServices.openBazaar(player, "<5>Selenar"));
        }
    }

    public static class Barter extends ServiceNPC {
        public Barter() {
            super("<d>Barter", TownSkins.BARTER, new Pos(-624.5, 101, -794.5), "barter", new String[0],
                    player -> TownServices.openBazaar(player, "<d>Barter"));
        }
    }
}
