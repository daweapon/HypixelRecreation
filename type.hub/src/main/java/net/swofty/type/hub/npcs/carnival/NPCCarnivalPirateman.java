package net.swofty.type.hub.npcs.carnival;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.carnival.CarnivalGameBase;
import net.swofty.type.hub.carnival.CarnivalSkin;
import net.swofty.type.hub.carnival.FruitDiggingGame;
import net.swofty.type.skyblockgeneric.carnival.CarnivalNpc;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Runs Fruit Digging.
public class NPCCarnivalPirateman extends CarnivalGameNPC {

    public NPCCarnivalPirateman() {
        super("Carnival Pirateman", CarnivalSkin.PIRATEMAN, new Pos(-107, 71, 28, 180, 0), CarnivalNpc.CARNIVAL_PIRATEMAN,
                new Replies("Aye aye, Captain!", "I'll find me a different game.", "Aye sure do!",
                        "Could ye tell me the rules again, Captain?", "I'll set me sights elsewhere.",
                        "I be ready as the tide!", "Avast, I've changed me mind..."));
    }

    @Override
    protected CarnivalGameBase newGame(SkyBlockPlayer player) {
        return new FruitDiggingGame(player);
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        String rules = "The rules of the game are as follows - I give ye a <a>shovel<f>, ye dig up the sand on the lookout fer <6>fruit<f>!";
        return new DialogueSet[]{
                DialogueSet.builder().key("introduction").lines(
                        "Welcome to <6>Fruit Digging<f>, matey!",
                        rules,
                        "There's a bunch o' different <6>fruits<f>, each with differin' <a>point values <f>and <6>abilities<f>.",
                        "Ye can <e><l>RIGHT CLICK <f>with yer <a>shovel <f>to swap between <6>dowsing modes<f>, which give ye a little <b>info <f>every time ye <e>dig<f>!",
                        "Yer supposed to use that <b>info <f>to avoid them <c>bombs <f>and pick the best blocks to mine.",
                        "It might all sound a little <4>confusin'<f>, but I bet ye'll get the hang of it if ye give it a try.",
                        "What do ye say?").build(),
                DialogueSet.builder().key("welcome").lines(
                        "Ahoy there, matey!",
                        "Would ye like to do some <6>Fruit Digging<f>?").build(),
                DialogueSet.builder().key("rules").lines(
                        "The rules are as follows - I give ye a <a>shovel<f>, ye dig up the sand on the lookout fer <6>fruit<f>!",
                        "There's a bunch o' different <6>fruits<f>, each with differin' <a>point values <f>and <6>abilities<f>.",
                        "Ye can <e><l>RIGHT CLICK <f>with yer <a>shovel <f>to swap between <6>dowsing modes<f>, which give ye a little <b>info <f>every time ye <e>dig<f>!",
                        "Yer supposed to use that <b>info <f>to avoid them <c>bombs <f>and pick the best blocks to mine.",
                        "With that said, are ye ready to play?").build(),
                DialogueSet.builder().key("start").lines(
                        "Here's yer <a>shovel<f>, then.",
                        "Good luck, matey!").build(),
                DialogueSet.builder().key("no-tickets").lines(
                        "Sorry lad, but ye don't have <8>5x <a>Carnival Tickets<f>. Ye gotta pay to play!",
                        "Ye'll have to get yer nutrients elsewhere.").build(),
                DialogueSet.builder().key("decline").lines(
                        "A swashbucklin' shame, if ye ask me...",
                        "Whenever ye be ready, I'll be waitin'").build(),
                DialogueSet.builder().key("closed").lines(
                        "Blimey! The Carnival is about to close. Ye'll gotta come back next time.").build()
        };
    }
}
