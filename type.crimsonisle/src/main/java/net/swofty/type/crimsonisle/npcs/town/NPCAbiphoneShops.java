package net.swofty.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.faction.FactionVillagerNPC;
import net.swofty.type.crimsonisle.gui.town.GUIAbiphoneShop;
import net.swofty.type.crimsonisle.town.TownSkins;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonAbiphone;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * The two Abiphone sellers: Udel in the Scarleton Bank, the Dean's cousin from the Mage questline's
 * "Purchase an Abiphone" step, and Nuvian in the secret room of the Dragontail brewery (reached
 * through {@link net.swofty.type.crimsonisle.town.NuvianKeypad}).
 */
public final class NPCAbiphoneShops {
    private NPCAbiphoneShops() {
    }

    /** Udel stands on the planks at y 107; the wiki's y 108 floats a block above them. */
    public static class Udel extends CrimsonNPC {
        public Udel() {
            super("<e>Udel", TownSkins.UDEL, new Pos(-78.5, 107, -787.5));
        }

        @Override
        protected void interact(SkyBlockPlayer player) {
            boolean onAbiphoneStep = CrimsonQuestlines.mage(player) == MageStep.BUY_ABIPHONE;
            if (onAbiphoneStep && CrimsonAbiphone.hasAbiphone(player) && CrimsonSideQuests.firstMeeting(player, "udel_sold")) {
                say(player, "Great! I'll let you know when I have a more expensive version of the same Abiphone available!",
                        "Sorry I mean a more powerful version of the Abiphone for the same price.",
                        "Anyways, Abiphones are magical devices that allow you to send sounds towards another Abiphone.",
                        "It might be hard for you to wrap your head around that concept, but you'll get used to it. Trust me.",
                        "Most people around here have an Abiphone, and some of them might let you add them to your <a>contacts<f>.",
                        "If you have someone's <a>contact<f> you can call them at any time, and they can also call you whenever.",
                        "I took the liberty to add <c>Dean<f> to your contacts already. I made sure you couldn't remove it, accidents happen!",
                        "Have a good day! Make sure you come back later to check my stock!")
                        .thenRun(() -> CrimsonAbiphone.grant(player, "dean"));
                return;
            }
            if (onAbiphoneStep && CrimsonSideQuests.firstMeeting(player, "udel")) {
                say(player, "Oh, you must be <player>! My cousin told me you'd come by my place.",
                        "The friends of the Dean are my friends too!",
                        "I have a wide range of Abiphones, from the Abiphone X+ to the Abiphone XIII Pro Giga!")
                        .thenRun(() -> player.openView(new GUIAbiphoneShop(true)));
                return;
            }
            player.openView(new GUIAbiphoneShop(true));
        }
    }

    public static class Nuvian extends FactionVillagerNPC {
        public Nuvian() {
            super("<5>Nuvian", TownSkins.NUVIAN, new Pos(-693.5, 102, -735.5, -90, 0), CrimsonFaction.BARBARIAN);
        }

        @Override
        protected void talk(SkyBlockPlayer player) {
            if (CrimsonSideQuests.firstMeeting(player, "nuvian")) {
                say(player, "Who are you and who gave you the code?", "You are not supposed to be here.",
                        "To be fair, I'm not supposed to be here either.",
                        "If you are here for business then it makes no difference to me.",
                        "I sell <5>Abiphones<f> straight from the <5>Mage<f> faction.")
                        .thenRun(() -> player.openView(new GUIAbiphoneShop(false)));
                return;
            }
            player.openView(new GUIAbiphoneShop(false));
        }
    }
}
