package dev.daweapon.type.hub.npcs.carnival;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.carnival.CarnivalGameBase;
import dev.daweapon.type.hub.carnival.CarnivalSkin;
import dev.daweapon.type.hub.carnival.CatchAFishGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalNpc;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Runs Catch a Fish at the mountain pond.
public class NPCCarnivalFisherman extends CarnivalGameNPC {

    public NPCCarnivalFisherman() {
        super("Carnival Fisherman", CarnivalSkin.FISHERMAN, new Pos(-81.5, 73, 29.5, 180, 0), CarnivalNpc.CARNIVAL_FISHERMAN,
                new Replies("I sure am!", "Maybe later.", "You guessed it!", "Could you tell me the rules again?",
                        "Nope.", "Let's do it!", "I think I'll reconsider."));
    }

    @Override
    protected CarnivalGameBase newGame(SkyBlockPlayer player) {
        return new CatchAFishGame(player);
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("introduction").lines(
                        "Welcome to <3>Catch a Fish<f>, friend!",
                        "Mayor Foxy graciously allowed me to use this <b>pond <f>for my attraction.",
                        "Anyways, the rules of the game are as follows:",
                        "You will be given <a>100s <f>to catch some <b>fish <f>with a <7>fishing rod <f>I will provide you.",
                        "Each type of <b>fish <f>has a different <c>color <f>and <a>point value<f>.",
                        "Try to catch as many as you can before the time runs out.",
                        "So, interested in playing?").build(),
                DialogueSet.builder().key("welcome").lines(
                        "Welcome to <3>Catch a Fish<f>, friend!",
                        "Are you here to play?").build(),
                DialogueSet.builder().key("rules").lines(
                        "Of course!",
                        "You will be given <a>100s <f>to catch some <3>fish <f>with a <7>fishing rod <f>I will provide you.",
                        "Each type of <3>fish <f>has a different <c>color <f>and <a>point value<f>.",
                        "Try to catch as many as you can before the <c>time <f>runs out.",
                        "Would you like to play?").build(),
                DialogueSet.builder().key("start").lines(
                        "Well, here goes...",
                        "Good luck, friend!").build(),
                DialogueSet.builder().key("no-tickets").lines(
                        "Sorry friend, but it looks like you don't have the 5x <a>Carnival Tickets <f>required to play...",
                        "Maybe next time.").build(),
                DialogueSet.builder().key("decline").lines(
                        "Alas, not everyone can appreciate the art of <a>fishing<f>...",
                        "I'll be waiting here if you change your mind.").build(),
                DialogueSet.builder().key("closed").lines(
                        "Aww, the Carnival is closing for the year now. There's always a next time!").build()
        };
    }
}
