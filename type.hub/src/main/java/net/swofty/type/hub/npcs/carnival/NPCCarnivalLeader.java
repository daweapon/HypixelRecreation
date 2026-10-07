package net.swofty.type.hub.npcs.carnival;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.carnival.CarnivalSkin;
import net.swofty.type.hub.gui.carnival.GUICarnivalTickets;
import net.swofty.type.skyblockgeneric.carnival.Carnival;
import net.swofty.type.skyblockgeneric.carnival.CarnivalNpc;
import net.swofty.type.skyblockgeneric.carnival.CarnivalProfile;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Hands out 25 Carnival Tickets each real-life day and answers questions about the Carnival.
public class NPCCarnivalLeader extends HypixelNPC {
    private enum Question {
        DONE("done", NamedTextColor.GREEN, "I should be good, thanks."),
        GAMES("games", NamedTextColor.AQUA, "What sorts of games can I play?"),
        TICKETS("tickets", NamedTextColor.GOLD, "How can I get more Carnival Tickets?"),
        TOKENS("tokens", NamedTextColor.LIGHT_PURPLE, "How do I earn Carnival Tokens?"),
        SHOP("shop", NamedTextColor.DARK_PURPLE, "Where can I turn my Carnival Tokens in?");

        private final String dialogue;
        private final NamedTextColor color;
        private final String text;

        Question(String dialogue, NamedTextColor color, String text) {
            this.dialogue = dialogue;
            this.color = color;
            this.text = text;
        }
    }

    public NPCCarnivalLeader() {
        super(new CarnivalNPCConfiguration("Carnival Leader", CarnivalSkin.LEADER, new Pos(-89.5, 72, 11.5, 180, 0)));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player) || !Carnival.isOpen()) return;
        CarnivalProfile profile = Carnivals.profile(player);
        if (!profile.hasMet(CarnivalNpc.CARNIVAL_LEADER)) {
            setDialogue(player, "introduction").thenRun(() -> offerTickets(player, "first-claim"));
        } else if (Carnivals.canClaimDailyTickets(profile)) {
            setDialogue(player, "new-day").thenRun(() -> offerTickets(player, "claimed"));
        } else {
            setDialogue(player, "already-claimed").thenRun(() -> ask(player, null));
        }
    }

    private void offerTickets(SkyBlockPlayer player, String afterClaim) {
        player.openView(new GUICarnivalTickets(claimer -> {
            CarnivalProfile profile = Carnivals.profile(claimer);
            profile.meet(CarnivalNpc.CARNIVAL_LEADER);
            Carnivals.save(claimer, profile);
            setDialogue(claimer, afterClaim);
        }));
    }

    private void ask(SkyBlockPlayer player, Question answered) {
        List<NPCOption.Option> options = java.util.Arrays.stream(Question.values())
                .filter(question -> question != answered)
                .map(question -> NPCOption.Option.builder()
                        .key(question.dialogue)
                        .color(question.color)
                        .bold(false)
                        .name(question.text)
                        .action(_ -> setDialogue(player, question.dialogue).thenRun(() -> {
                            if (question != Question.DONE) ask(player, question);
                        }))
                        .build())
                .toList();
        NPCOption.sendOption(player, "carnival_leader", true, options);
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        String name = "<7>" + player.getUsername() + "<f>";
        return new DialogueSet[]{
                DialogueSet.builder().key("introduction").lines(
                        "Welcome to <e>The Carnival<f>, " + name + "!",
                        "Here, you'll be able to partake in <a>fun activities<f>, earn <e>Carnival Tokens<f>, and purchase <d>cool prizes<f>!",
                        "Each <b>day<f>, I'll give you <8>25x <a>Carnival Tickets<f>, which will let you play the <a>games<f>.",
                        "You'll also gain <a>Carnival Tickets <f>for participating in different <a>Events<f>!",
                        "Here, I'll give you some to start things off.").build(),
                DialogueSet.builder().key("first-claim").lines(
                        "Alright, you should be good to go!",
                        "If you have any questions, come find me.",
                        "Good luck, and have <e>A Carnivalot of Fun™<f>!").build(),
                DialogueSet.builder().key("new-day").lines(
                        "Nice to see you here again, " + name + "!",
                        "It's a new <b>day<f>, which means you get more <a>Carnival Tickets<f>!").build(),
                DialogueSet.builder().key("claimed").lines(
                        "Remember, if you have any questions, I'll be right here.",
                        "Good luck, and have <e>A Carnivalot of Fun™<f>!").build(),
                DialogueSet.builder().key("already-claimed").lines(
                        "Welcome to <e>The Carnival<f>, " + name + "!",
                        "You've already claimed your <a>Carnival Tickets <f>for <b>today<f>, but I'm happy to answer any questions you might have.").build(),
                DialogueSet.builder().key("done").lines(
                        "Alrighty.",
                        "Good luck, and have <e>A Carnivalot of Fun™<f>!").build(),
                DialogueSet.builder().key("games").lines(
                        "Currently, <e>The Carnival <f>offers <a>3 <f>different games!",
                        "Behind me is <6>Fruit Digging<f>.",
                        "Try your luck in this sand-flinging adventure and win big!",
                        "Over there is <c>Zombie Shootout<f>.",
                        "Aim for a high <a>score <f>by killing the pesky <c>Zombies <f>walking around the arena!",
                        "Lastly, there's <3>Catch a Fish<f>, located by the <b>mountain pond<f>.",
                        "Catch some <3>fish <f>and have a relaxing time!",
                        "It's <e>A Carnivalot of Fun™<f>!",
                        "Any other questions?").build(),
                DialogueSet.builder().key("tickets").lines(
                        "Oh, that's easy!",
                        "Each <b>day<f>, I'll give you <8>25x <a>Carnival Tickets<f>.",
                        "You'll also gain <a>Carnival Tickets <f>for participating in different <a>Events<f>!",
                        "Any other questions?").build(),
                DialogueSet.builder().key("tokens").lines(
                        "By competing in <e>Carnival <f>games!",
                        "You'll be awarded <e>1 Token <f>for every <a>100 points <f>you earn.",
                        "Any other questions?").build(),
                DialogueSet.builder().key("shop").lines(
                        "Why, at the <6>orange stand <f>right over there!",
                        "You can exchange your <e>Tokens <f>for <d>permanent buffs <f>in different <a>Events <f>throughout <b>SkyBlock<f>, as well as some <e>fun masks <f>to play around with.",
                        "Any other questions?").build()
        };
    }
}
