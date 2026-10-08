package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.faction.CrimsonChoices;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.crimsonisle.faction.MageScenes;
import net.swofty.type.crimsonisle.gui.faction.GUIPresentItem;
import net.swofty.type.crimsonisle.gui.faction.GUIQuestClaim;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.skyblockgeneric.abiphone.impl.AbiphoneDean;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// The Dean of the Academy in the Cathedral, the heart of the Mage Faction Questline (wiki: Dean).
public class NPCDean extends CrimsonNPC implements NPCAbiphoneTrait {
    private static final Pos CATHEDRAL = new Pos(-16, 123, -882.5, 180, 0);
    private static final Pos MAGE_COUNCIL = new Pos(-121.5, 82, -752.5);
    private static final String ID = "dean";

    public NPCDean() {
        super(CrimsonScene.DEAN, CrimsonSkins.DEAN, CATHEDRAL);
    }

    @Override
    protected boolean moves() {
        return true;
    }

    @Override
    protected Pos position(SkyBlockPlayer player) {
        return CrimsonQuestlines.mage(player) == MageStep.MAGE_COUNCIL ? MAGE_COUNCIL : CATHEDRAL;
    }

    @Override
    public String getAbiphoneKey() {
        return ID;
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        MageStep step = CrimsonQuestlines.mage(player);
        boolean joining = step == MageStep.SHOW_BATPHONE || step == MageStep.DELIVER_LETTER;
        if (!joining && !CrimsonReputationService.isMember(player, CrimsonFaction.MAGE)) {
            sayRandom(player, "I am busy right now.", "Sorry, I only work with Mages...");
            return;
        }
        switch (step) {
            case SHOW_BATPHONE -> batphone(player);
            case DELIVER_LETTER -> say(player, "You should bring that letter to the <c>Queen <f>ASAP. Did you lose the letter?")
                    .thenRun(() -> CrimsonChoices.yesNo(player, ID, () -> openLetterClaim(player),
                            () -> say(player, "Alright, if you are struggling to find the <c>Queen<f>, she is usually in the <2>throne room<f>, or in her quarters.")));
            case BUY_ABIPHONE -> say(player, "Once that's done, go visit my cousin <c>Udel <f>in his shop in the plaza.",
                    "I'll let him know you are coming and he'll make you a price for your own <2>Abiphone<f>.");
            case MAGE_COUNCIL -> MageScenes.council(player);
            case RETURN_TO_ACADEMY -> say(player, "I can't thank you enough, what you did was heroic!",
                    "You are probably aware but we used to be Pig farmers but Sulphur turned our pigs into Piglins.",
                    "For decades we had to survive with almost nothing to eat, until we learned magic.",
                    "Piglins can eat nether warts, but we certainly can't!",
                    "Anyways, if you want to learn more about that, there are books in the Academy regarding the Dark Ages.",
                    "I am trying to finish my work on this machine but my <c>Apprentice <f>is missing.",
                    "He is out there in the wild looking for precious ingredients and he should have been back a long time ago.",
                    "Their Abiphone doesn't seem to be working, I am starting to get worried.",
                    "Would you be so kind to look for them, they are probably <2>North of that Red Sand Desert<f>.")
                    .thenRun(() -> CrimsonQuestlines.advance(player, MageStep.RETURN_TO_ACADEMY, MageStep.FIND_AHONE, 0));
            case FIND_AHONE, FIND_APPRENTICE, CALL_AHONE -> say(player, "Please find my apprentice, he should be North of the Burning Desert.");
            case REPORT_TO_DEAN -> say(player, AbiphoneDean.deanApprenticeNews())
                    .thenRun(() -> CrimsonQuestlines.advance(player, MageStep.REPORT_TO_DEAN, MageStep.BRING_MATERIALS, 0));
            case BRING_MATERIALS -> materials(player);
            case HELP_IGRUPAN, HELP_ODEXAR -> say(player, "Thank you very much <player>, I will call you when I'm done");
            case HALLOWED_SKULL -> hallowedSkull(player);
            case LUMINO_FIBER -> deliver(player, ItemType.LUMINO_FIBER, MageStep.LUMINO_FIBER, MageStep.LEATHER_CLOTH,
                    new String[]{"Thanks! I almost have all the components I need now.",
                            "The last one is a <2>Leather Cloth<f>, and not just any kind.",
                            "There is a rogue Barbarian going by the name of <c>Duke X<f>.",
                            "He owns all the <2>Leather Cloth <f>on the island, find him and you'll find the items."},
                    "The reason why I need a <2>Lumino Fiber<f>? Well...it's hard to explain.",
                    "I have reasons to believe there is a lot of Sulphur in the <c>North<f>, it would explain the appearance of those Blaze species.");
            case LEATHER_CLOTH -> deliver(player, ItemType.LEATHER_CLOTH, MageStep.LEATHER_CLOTH, MageStep.TALK_TO_AHONE,
                    new String[]{"I knew I could count on you.",
                            "This strong Cloth will be able to contain the skull and the lumino fibers together.",
                            "In theory this should help refining Sulphur, if I can get it to work.",
                            "Would you mind talking to <c>Captain Ahone<f>?",
                            "Please tell him I need a full backpack worth of Sulphur."},
                    "Piglins can grow incredibly strong if they consume large amounts of Sulphur, but they lose their sanity in the process.",
                    "Last I heard, they are located near the Barbarian Faction.");
            case TALK_TO_AHONE, MEET_AT_FORGOTTEN_SKULL, DEFEAT_KUUDRA -> sayRandom(player,
                    "Captain Ahone should be below the Community Center.", "There is still so much to learn about Sulphur.");
            case TALK_TO_DEAN_FINAL -> say(player, AbiphoneDean.deanAfterKuudra())
                    .thenRun(() -> CrimsonQuestlines.advance(player, MageStep.TALK_TO_DEAN_FINAL, MageStep.COMPLETE, 0));
            default -> sayRandom(player, "This monster probably feeds itself with Sulphur.",
                    "If left unchecked, it is possible that the powers of this creature will grow.");
        }
    }

    private void batphone(SkyBlockPlayer player) {
        boolean met = CrimsonQuestlines.counter(player, "dean_met") > 0;
        CrimsonQuestlines.setCounter(player, "dean_met", 1);
        if (CrimsonQuestlines.counter(player, "dean_shown") > 0) {
            say(player, "Have you changed your mind?").thenRun(() -> offerLetter(player, false));
            return;
        }
        String[] opening = met ? new String[]{"You again, what do you want this time?"} : new String[]{
                "You... are not one of my students!",
                "I hope you are not here to rob me, I could still take on a dozen of young guns like you!",
                "Oh, that's not why you are here? Then why are you here? Speak up I don't have all day!"};
        say(player, opening).thenRun(() -> CrimsonChoices.show(player, ID, "I have something to show you:",
                () -> player.openView(new GUIPresentItem(ItemType.AATROX_BATPHONE, () -> {
                    CrimsonQuestlines.setCounter(player, "dean_shown", 1);
                    say(player, "Oh, where did you find this? This looks like an older, simpler version of our <2>Ability Phones<f>, in a way this is remarkable.",
                            "But why do you have this, are you interested in that kind of magical technology?")
                            .thenRun(() -> offerLetter(player, true));
                }))));
    }

    private void offerLetter(SkyBlockPlayer player, boolean firstAsk) {
        CrimsonChoices.yesNo(player, ID, () -> {
            if (player.getAmountInInventory(ItemType.AATROX_BATPHONE) > 0) player.removeItemFromPlayer(ItemType.AATROX_BATPHONE, 1);
            say(player, "Perfect, well then you are at the right place, let me write you a <2>letter of recommendation<f>!",
                    "Hold on...", "Here take this!").thenRun(() -> openLetterClaim(player));
        }, () -> {
            if (firstAsk) {
                say(player, "Oh, you mean...you are more interested in advanced magical technology right?")
                        .thenRun(() -> offerLetter(player, false));
            } else {
                say(player, "In that case thanks for the gift, and have a great life.");
            }
        });
    }

    private void openLetterClaim(SkyBlockPlayer player) {
        player.openView(new GUIQuestClaim("Letter of Recommendation", new SkyBlockItem(ItemType.DEAN_LETTER_OF_RECOMMENDATION), () -> {
            if (player.getAmountInInventory(ItemType.DEAN_LETTER_OF_RECOMMENDATION) > 0) {
                player.sendMessage("<c>You already have one in your inventory!");
                return;
            }
            player.addAndUpdateItem(ItemType.DEAN_LETTER_OF_RECOMMENDATION);
            if (CrimsonQuestlines.advance(player, MageStep.SHOW_BATPHONE, MageStep.DELIVER_LETTER, 0)) {
                say(player, "First you should go to the <c>Queen <f>with it, we have a very strict code around here and you won't go far without the <c>Queen<f>'s approval.",
                        "Hopefully the <c>Queen <f>will grant you access to some parts of the city.",
                        "Once that's done, go visit my cousin <c>Udel <f>in his shop in the plaza.",
                        "I'll let him know you are coming and he'll make you a price for your own <2>Abiphone<f>.");
            }
        }));
    }

    private void materials(SkyBlockPlayer player) {
        if (player.getAmountInInventory(ItemType.MAGMA_CHUNK) < 10 || player.getAmountInInventory(ItemType.BURNING_EYE) < 5) {
            say(player, "Let me know when you obtained the 10 Magma Chunks and 5 Burning Eyes.");
            return;
        }
        if (!CrimsonQuestlines.advance(player, MageStep.BRING_MATERIALS, MageStep.HELP_IGRUPAN, 150)) return;
        player.removeItemFromPlayer(ItemType.MAGMA_CHUNK, 10);
        player.removeItemFromPlayer(ItemType.BURNING_EYE, 5);
        say(player, "Magma Chunks and Burning Eyes, perfect!", "That should be enough to finish my work.", "I will call you once I'm done.");
    }

    private void hallowedSkull(SkyBlockPlayer player) {
        if (CrimsonQuestlines.counter(player, "dean_components") == 0) {
            CrimsonQuestlines.setCounter(player, "dean_components", 1);
            say(player, "Oh, I was just about to call you!",
                    "I have almost completed my research, however I need a few extra components.",
                    "Those ones are going to be tougher to obtain though.", "You will have to defeat mighty opponents.",
                    "The first ingredient I need is a <2>Hallowed Skull<f>, which is obtained from a <c>Bladesoul<f>.",
                    "This creature can be found inside the <c>Stronghold<f>, south of the island.");
            return;
        }
        deliver(player, ItemType.HALLOWED_SKULL, MageStep.HALLOWED_SKULL, MageStep.LUMINO_FIBER,
                new String[]{"A Hallowed Skull! Splendid!",
                        "I need a couple more components, the next one is a <2>Lumino Fiber<f>.",
                        "Only the strongest of Blazes can create those, and there is a terrifying Blaze in the <c>North West<f>.",
                        "If you can defeat them, I'm sure you'll obtain a <2>Lumino Fiber<f>."},
                "Hallowed Skulls can be obtained from <c>Bladesoul <f>inside the Stronghold.",
                "The Stronghold was built by Blazes and Wither Skeletons, they are still expanding it slowly, quite fascinating.");
    }

    private void deliver(SkyBlockPlayer player, ItemType item, MageStep from, MageStep to, String[] thanks, String... hints) {
        if (player.getAmountInInventory(item) < 1) {
            sayRandom(player, hints);
            return;
        }
        if (!CrimsonQuestlines.advance(player, from, to, 0)) return;
        player.removeItemFromPlayer(item, 1);
        say(player, thanks);
    }
}
