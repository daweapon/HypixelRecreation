package dev.daweapon.type.thefarmingislands.lab;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.farming.lab.LabQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Point;

import java.util.List;

import static dev.daweapon.type.thefarmingislands.lab.LabScene.Line.narrate;
import static dev.daweapon.type.thefarmingislands.lab.LabScene.Line.say;

public final class LabScenes {
    public static final Pos FIRST_ROOM = new Pos(174.5, 26, -477);
    public static final Pos SECOND_VISIT_START = new Pos(173.875, 28, -498.5);
    public static final Pos SECOND_ROOM_WAIT = new Pos(190.5, 22, -499.5);
    public static final Pos HIDDEN_SECTION_BETH = new Pos(200.576657, 14, -497.9686263);
    public static final Pos HIDDEN_SECTION_JAKE = new Pos(204, 14, -497);
    public static final Pos JAKE_FIRST = new Pos(171.5, 27, -491.5);
    public static final Pos LEVER = new Pos(177.5, 29, -497.5);
    public static final Pos CROP_ANALYZER = new Pos(204.5, 15, -498.5);
    public static final double TRIGGER_RANGE = 5;

    private static final double MIN_X = 160;
    private static final double MAX_X = 215;
    private static final double MIN_Y = 8;
    private static final double MAX_Y = 36;
    private static final double MIN_Z = -510;
    private static final double MAX_Z = -465;

    private LabScenes() {
    }

    public static boolean insideLab(Point position) {
        return position.x() >= MIN_X && position.x() <= MAX_X && position.y() >= MIN_Y && position.y() <= MAX_Y
                && position.z() >= MIN_Z && position.z() <= MAX_Z;
    }

    public static void firstVisit(SkyBlockPlayer player) {
        if (LabQuest.stage(player) != LabQuest.Stage.LAB1) return;
        LabScene.play(player, List.of(
                say("Beth", "This is so strange..."),
                say("Beth", "I can't believe this has been down here for all this time... I am speechless."),
                say("Beth", "WHAT? There are <b>animals <f>inside of here?!?! <c>Jake <f>is doing <c>horrible experiments <f>with these <b>animals <f>that he catches..."),
                say("Beth", "He has gone completely mad! He's a psychopath!"),
                say("Beth", "And look at all of these tools he has laying around here... a SAW?... a HAMMER?"),
                say("Beth", "What is this psycho doing with these <b>animals<f>???"),
                say("Beth", "<c>Jake <f>has a lot of explaining to do!"),
                say("Jake", "... BETH?? AND {player}, WHAT ARE YOU GUYS DOING IN HERE???"),
                say("Jake", "IT'S NOT WHAT IT LOOKS LIKE!"),
                say("Beth", "WE KNOW WHAT YOU ARE DOING IN HERE, YOU PSYCHO! YOU WILL PAY FOR WHAT YOU ARE DOING TO THESE <b>ANIMALS<f>!"),
                say("Jake", "<b>ANIMALS<f>? I CAN EXPLAIN, I SWEAR!"),
                say("Beth", "WELL GO ON THEN, EXPLAIN YOURSELF!"),
                say("Jake", "I don't know what you guys think I am doing here, but this is just my <2>workshop<f>!"),
                say("Jake", "I am just making <b>toys <f>for <b>animals<f>..."),
                say("Beth", "Oh really? Do you really think we are that naive?"),
                say("Beth", "We know that you are experimenting with <b>animals <f>down here, how do you explain this tank?"),
                say("Jake", "Those are my <b>plushies<f>! I preserve them in Jelly so they stay fresh!"),
                say("Jake", "They look super realistic, but no <b>animal <f>was harmed when making them!"),
                say("Jake", "I can prove it! Here, take this. This is one of the <b>plushies <f>I made, you can have it!", LabScenes::givePlushie),
                say("Jake", "So really, I am not harming them at all!"),
                say("Beth", "Everything makes sense now...I'm sorry I yelled at you Jake. I'm so sorry."),
                say("Jake", "It's alright now. I was wrong to lie about it and I should have told you sooner."),
                say("Beth", "<7>{player}<f>, thank you so much for telling me what you saw here. I'll be forever grateful."),
                say("Beth", "I have to go now, I hope to see you guys soon."),
                say("Jake", "That was a close one, thank you <7>{player} <f>for having my back. Anyway, I need to go back to my business.")
        ), () -> ChapterTriggers.fire(player, LabQuest.LAB1_DONE));
    }

    private static void givePlushie(SkyBlockPlayer player) {
        if (ChapterTriggers.done(player, LabQuest.PLUSHIE)) return;
        ChapterTriggers.fire(player, LabQuest.PLUSHIE);
        player.addAndUpdateItem(ItemType.JAKE_PLUSHIE, 1);
        player.sendMessage("<a>Jake gave you <a>Jake's Plushie<a>!");
    }

    public static void secondVisitStart(SkyBlockPlayer player) {
        if (LabQuest.stage(player) != LabQuest.Stage.LAB2_PENDING) return;
        LabScene.play(player, List.of(
                say("Beth", "This place still gives me chills... but I'm sure there's more to uncover."),
                say("Beth", "We know Jake's been hiding things, and I think we're getting closer to the truth."),
                say("Beth", "Hmm, this corner... I don't think I've noticed this before."),
                say("Beth", "There's something strange about this wall. It looks out of place..."),
                say("Beth", "Wait, there's a <d>lever <f>here!"),
                say("Beth", "What is this? A hidden passage? I knew Jake was hiding something from me, but this... this is something different."),
                say("Beth", "I don't know if we should trust this, but we've come this far... Let's see what's behind it."),
                say("Beth", "See if you can pull down this lever, it's a bit stuck.")
        ), () -> ChapterTriggers.fire(player, LabQuest.LAB2_STARTED));
    }

    public static void leverHint(SkyBlockPlayer player) {
        player.sendMessage("<e>[NPC] Beth<f>: See if you can pull down this lever, it's a bit stuck.");
    }

    public static void pullLever(SkyBlockPlayer player) {
        LabQuest.Stage stage = LabQuest.stage(player);
        if (stage == LabQuest.Stage.LAB2_STARTED) {
            ChapterTriggers.fire(player, LabQuest.LAB2_LEVER);
            player.sendMessage("<7>You pull down the lever. A hidden part of the lab opens up!");
            return;
        }
        player.sendMessage("<7>The lever is stuck.");
    }

    public static void revealCropAnalyzer(SkyBlockPlayer player) {
        if (LabQuest.stage(player) != LabQuest.Stage.LAB2_LEVER) return;
        LabScene.play(player, List.of(
                say("Beth", "I... I had no idea this was here. What could Jake be hiding in this secret part of the lab?"),
                say("Beth", "It's very dark in here, where's the light-switch in this place?"),
                say("Beth", "Oh... my... what is all of this?"),
                say("Beth", "This is... this isn't just about plushies anymore. Jake's been working on something much bigger."),
                say("Beth", "Hybridization? <c>Mutation<f>? What is this madness? He's experimenting on something..."),
                say("Beth", "Resilient livestock... enhanced growth rates... Jake, what have you done?"),
                say("Beth", "These aren't normal <a>crops<f>. These are <c>mutated<f>... just like the ones we've been finding in the Garden. But why hide all of this down here?"),
                narrate("A door opens from within the lab, and Jake shows up"),
                say("Jake", "Beth... I told you to stay out of this."),
                say("Beth", "Jake! You have a lot of explaining to do, once again... What is all of this? What are you doing down here?"),
                say("Beth", "Please tell me the truth this time Jake, I can't take all these lies anymore!"),
                say("Jake", "It's... it's not what it looks like. This lab... it's for research. To improve farming techniques, to help both <a>crops <f>and <e>animals <f>adapt to new environments."),
                say("Beth", "<b>Research<f>? This doesn't look like research, Jake! This looks like you are playing with life itself. Those tanks, those animals, what are you actually doing to them?"),
                say("Jake", "You don't understand, Beth! Farming as we know is evolving. If we don't adapt, we'll be left behind. The world's demands are growing, and so are the challenges."),
                say("Beth", "That doesn't give you the right to hide this from everyone. What about the risks? The ethics?"),
                say("Beth", "You're creating something entirely unnatural."),
                say("Jake", "<o>sigh <f>I know it seems... wrong. But these experiments could change everything."),
                say("Jake", "Imagine crops that can survive the harshest conditions, or animals that never get sick."),
                say("Jake", "It's not just about survival, it's about thriving."),
                say("Beth", "And what about the things you are not telling me? What else are you hiding down here, Jake?"),
                say("Jake", "There are some things you're better off not knowing. Not yet at least."),
                say("Beth", "No, Jake. I've come this far, and I won't stop now. If you won't tell me the truth, I'll find it myself."),
                say("Jake", "Beth, you don't understand, some things aren't ready to be uncovered. It's not about keeping secrets, it's about protecting what I've been working towards."),
                say("Beth", "Protecting what? Your pride? Your experiments? This isn't just about you, Jake. We have a responsibility to use this lab for good, not to bury it under layers of secrecy."),
                say("Beth", "What's this?"),
                say("Jake", "That... is the <a>Crop Analyzer<f>."),
                say("Beth", "The <a>Crop Analyzer<f>? What does it do?"),
                say("Jake", "It's a tool I've been developing. It analyzes <c>mutations <f>in crops, helps identify traits that can improve <6>yield<f>, <9>resistance <f>or even <d>flavor<f>."),
                say("Jake", "But it's not finished. It's delicate work, Beth. Work that requires precision, and data."),
                say("Beth", "Work that shouldn't be done in isolation. Jake, this could change everything for farmers everywhere. Why are you hiding something so revolutionary?"),
                say("Jake", "Because it's not perfect yet. A mistake could have disastrous consequences. I didn't want to involve anyone until I was sure it was ready."),
                say("Beth", "Jake, you've done incredible work here. But you don't have to do it alone anymore. Let me and <7>{player} <f>help you."),
                say("Beth", "Together, we can finish this and make sure it's used responsibly."),
                say("Jake", "Alright. You're right, Beth. I've been trying to carry this burden alone for too long. If we're going to make this work, we'll do it together."),
                say("Jake", "And you, <7>{player}<f>, you've been instrumental in getting us to this point. You should have access to the <a>Crop Analyzer<f>. You've earned my trust.")
        ), () -> {
            ChapterTriggers.fire(player, LabQuest.LAB2_DONE);
            player.sendMessage("<a>You can now use the <a>Crop Analyzer<a>!");
        });
    }
}
