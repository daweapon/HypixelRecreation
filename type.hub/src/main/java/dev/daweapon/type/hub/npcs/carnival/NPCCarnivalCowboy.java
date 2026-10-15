package dev.daweapon.type.hub.npcs.carnival;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.carnival.CarnivalGameBase;
import dev.daweapon.type.hub.carnival.CarnivalSkin;
import dev.daweapon.type.hub.carnival.ZombieShootoutGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalNpc;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Runs Zombie Shootout.
public class NPCCarnivalCowboy extends CarnivalGameNPC {

    public NPCCarnivalCowboy() {
        super("Carnival Cowboy", CarnivalSkin.DOUG, new Pos(-103.5, 70, 38.5, 90, 0), CarnivalNpc.CARNIVAL_COWBOY,
                new Replies("Sure does, partner! I'd like to give it a try.", "I think I'll save this one for later.",
                        "Sure thing, partner!", "Could ya tell me the rules again?", "I'd like to do somthin' else fer now.",
                        "More ready than a rooster at dawn!", "Reckon I'll wrangle up a fresh thought instead."));
    }

    @Override
    protected CarnivalGameBase newGame(SkyBlockPlayer player) {
        return new ZombieShootoutGame(player);
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("introduction").lines(
                        "Howdy there, partner!",
                        "Interested in playin' <c>Zombie Shootout<f>, are ya?",
                        "Them rules are quite easy to get the hang of.",
                        "I give ya a fancy lil' <a>dart stick<f>, you shoot sum <c>Zombies<f>!",
                        "Ye'll get more score based on what armor the Zombie.",
                        "There'll also be sum Redstone Lamps aroun' the arena that can be shot fer points when they light up!",
                        "That all make sense to ya?").build(),
                DialogueSet.builder().key("welcome").lines(
                        "Howdy there, partner!",
                        "Wouldja like to play <c>Zombie Shootout<f>?").build(),
                DialogueSet.builder().key("rules").lines(
                        "Them rules are quite easy to get the hang of.",
                        "I give ya a fancy lil' <a>dart stick<f>, you shoot sum <c>Zombies<f>!",
                        "Ye'll get more <a>score <f>based on what <7>armor <f>the <c>Zombie<f>'s wearin'.",
                        "There'll also be sum <c>Redstone Lamps <f>aroun' the arena that can be shot fer <a>points <f>when they <6>light up<f>!",
                        "Are ya ready to play now?").build(),
                DialogueSet.builder().key("start").lines(
                        "I won't keep ya, then.",
                        "Good luck, pal!").build(),
                DialogueSet.builder().key("no-tickets").lines(
                        "Sorry pal, but ya need <8>5x <a>Carnival Tickets <f>to play!",
                        "Guess we'll have to save it fer later.").build(),
                DialogueSet.builder().key("decline").lines(
                        "Well, gosh darn it!",
                        "Stop wasting my time then, will ya?").build(),
                DialogueSet.builder().key("closed").lines(
                        "Aw shucks, the Carnival is about to close! Hope to see yer next time.").build()
        };
    }
}
