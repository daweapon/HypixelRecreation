package dev.daweapon.type.skyblockgeneric.miningevent;

import java.util.ArrayList;
import java.util.List;

final class CultMeetingScripts {
    private static final String[][] BODIES = {
            {
                    "D:Alright, where did we finish off last time?",
                    "B:The master plan.",
                    "D:Right, the master plan. <5>Thondin<f>, care to update us?",
                    "T:My wife said we can have a sleepover if you promise not to break anything this time.",
                    "D:Break anything? It wasn't me, it was <5>Brarnas<f>! And, honestly, who stores plates like that anyways?",
                    "D:They're just asking to be knocked over. Why not just put them in a drawer, out of the way?",
                    "D:You don't need to display them. Everyone has plates, they're nothing special.",
                    "T:You said you never even saw the plates.",
                    "D:And I didn't! But <5>Brarnas<f> told me about it.",
                    "B:No I didn't.",
                    "D:Anyways!",
                    "D:My wife said I'm free to come to the sleepover if I do the dishes first.",
                    "B:Try not to break those too.",
                    "T:Cool.",
                    "T:I was thinking of hosting it on the fifteenth?",
                    "B:I don't think that would work.",
                    "D:Yeah, it doesn't really align with our cult's theme. How about the fourteenth instead?",
                    "T:Sure.",
                    "B:I'm not really available then, couple coworkers are off so the boss wants me in to cover.",
                    "D:Well, you should probably make yourself available.",
                    "T:Jobs may come and go, but the <5>Cult of the Fallen Star<f> is eternal.",
                    "B:You are right. I shall be there.",
                    "T:Awesome.",
                    "D:Alright, next point of interest...",
                    "T:Hold up, guys. My wife's calling.",
                    "D:Again?",
                    "T:Hello? Mhm? Okay. Calm down. Yes, I'll come and look, make sure you check under the porch.",
                    "B:Cat again?",
                    "T:Cat again. I've got to go.",
                    "D:Alright, I guess we can end the meeting there. Same time next week!"
            },
            {
                    "T:Today I have something I'd like to talk about.",
                    "D:What is it?",
                    "T:The sleepover.",
                    "B:Uh-oh.",
                    "T:We're never having another one again.",
                    "D:Why?",
                    "T:I wonder why. Where do I start?",
                    "T:You didn't take your shoes off when you came in.",
                    "T:You also left the door open, so the cat ran out and I had to spend an hour looking for him.",
                    "T:And, while I was doing that, you ate all of the pizza I ordered for us.",
                    "D:We were hungry.",
                    "B:We? I didn't get a single bite either.",
                    "D:You're not helping.",
                    "T:You then fell asleep in my bed before the show we were meant to watch even began.",
                    "T:You don't even know which two people died!",
                    "D:TWO?",
                    "B:Might be three in a minute.",
                    "T:You deserved that spoiler. And-",
                    "D:Okay, okay, I get it. The next sleepover can be at my house. I'll make it up to you guys.",
                    "B:You can pay for the pizza.",
                    "D:Yes, of course! Now, can we move on with the meeting?",
                    "B:Yes.",
                    "B:Again, <5>Thondin<f>?",
                    "T:Apologies.",
                    "T:Hello? Missing? Again? Okay. Yep, I can hear you. I'll be right there.",
                    "D:Cat again?",
                    "T:Nope, wife.",
                    "B:Oh. That's not good.",
                    "T:Nah, it's fine. She'll be getting a cuppa down in <5>Scarleton<f>. Thought it was her day off.",
                    "T:I should probably go and tell her that she's supposed to be at work.",
                    "B:Yep.",
                    "D:Well, I guess we will have to suspend today's meeting... Until next week, everyone!"
            },
            {
                    "T:Guys, today I have something super urgent to discuss.",
                    "D:What is it, <5>Thondin<f>?",
                    "B:And why do I suspect it will be entirely unimportant?",
                    "T:Well... you think there are more doors or wheels in this world?",
                    "B:There we go.",
                    "T:I've been thinking about it all week, and I believe there are probably more wheels.",
                    "B:And how did you reach that conclusion?",
                    "T:Well, first of all, many houses don't even have doors. For example, many of us dwarves don't use them.",
                    "T:Like, you could just walk right on in to the dwarven palace, if it wasn't for the guards.",
                    "D:And guards, though they serve a similar purpose, are not doors, I suppose.",
                    "T:Yeah. Unless you're like, a philosopher, or something.",
                    "T:And then, when you think about it, every minecart has four wheels, but no door on it.",
                    "T:And us, being dwarves, have a whole lot of minecarts.",
                    "D:Yup, I sleep in one.",
                    "B:But not many civilizations outside of us dwarves use minecarts, <5>Thondin<f>.",
                    "B:And why are you so certain that minecarts have wheels?",
                    "T:Huh?",
                    "B:By definition, a wheel must be circular. Those \"wheels\" are blocky, and I've never actually seen them turn.",
                    "D:You know, now that I think of it, everything is really blocky around here. And everywhere.",
                    "T:But it is a wheel!",
                    "B:How can you be so sure?",
                    "T:Because everyone calls it a wheel!",
                    "B:Have you ever seen one turn?",
                    "T:Now that I think about it, no. I've never seen a minecart actually move. They're always in the same place!",
                    "D:Me neither!",
                    "B:The same is true for me.",
                    "T:Then what are they there for? Just for decoration?",
                    "T:Guys... are we living in a simulation?",
                    "D:Don't be silly.",
                    "B:Unlikely. There's no way to prove we're living in a simulation.",
                    "D:Come, let's go and check out those minecarts. Meeting adjourned!"
            },
            {
                    "T:Guys, today I have something super urgent to discuss.",
                    "D:What is it, <5>Thondin<f>?",
                    "B:And why do I suspect it will be entirely unimportant?",
                    "T:Well... you think there are more doors or wheels in this world?",
                    "B:There we go.",
                    "T:I've been thinking about it all week, and I believe there are probably more wheels.",
                    "B:And how did you reach that conclusion?",
                    "T:Well, first of all, many houses don't even have doors. For example, many of us dwarves don't use them.",
                    "T:Like, you could just walk right on in to the dwarven palace, if it wasn't for the guards.",
                    "D:And guards, though they serve a similar purpose, are not doors, I suppose.",
                    "T:Yeah. Unless you're like, a philosopher, or something.",
                    "T:And then, when you think about it, every minecart has four wheels, but no door on it.",
                    "T:And us, being dwarves, have a whole lot of minecarts.",
                    "D:Yup, I sleep in one.",
                    "B:But not many civilizations outside of us dwarves use minecarts, <5>Thondin<f>.",
                    "B:And why are you so certain that minecarts have wheels?",
                    "T:Huh?",
                    "B:By definition, a wheel must be circular. Those \"wheels\" are blocky, and I've never actually seen them turn.",
                    "D:You know, now that I think of it, everything is really blocky around here. And everywhere.",
                    "T:But it is a wheel!",
                    "B:How can you be so sure?",
                    "T:Because everyone calls it a wheel!",
                    "B:Have you ever seen one turn?",
                    "T:Now that I think about it, no. I've never seen a minecart actually move. They're always in the same place!",
                    "B:The same is true for me.",
                    "T:Then what are they there for? Just for decoration?",
                    "T:Guys... are we living in a simulation?",
                    "D:Don't be silly.",
                    "B:Unlikely. There's no way to prove we're living in a simulation.",
                    "D:Come, let's go and check out those minecarts. Meeting adjourned!"
            },
            {
                    "D:Well, guys, I'm still a bit shook from our recent series of discoveries.",
                    "D:I suppose we should recap them, just for the agenda. <5>Brarnas<f>, if you would...",
                    "B:Sure.",
                    "B:Firstly, we discovered that the wheels on minecarts are solely for show, and that minecarts cannot actually move.",
                    "B:Next, <5>Thondin<f> made the realization that food actually has no taste or texture, though we are inclined to eat it.",
                    "B:Then, we realized that fallen stars always land at the same few spots. Mathematically unlikely, to put it lightly.",
                    "B:After which, we noted that the same things always happen at the same time, each day.",
                    "T:Yup!",
                    "T:<d>Bednom<f> always has a <d>nightmare<f>.",
                    "T:<6>Brynmor<f> always loses his <c>Speckled Teacup<f>.",
                    "T:<e>Fetchur<f> always needs someone to bring him something.",
                    "D:And the <d>Puzzler<f> always has a stupid puzzle to solve!",
                    "B:Indeed. It seems almost as if everyone here is living the same day, again and again.",
                    "T:And, when we asked them about it, none of them knew why they do what they do!",
                    "B:Indeed. With this in mind, only one logical conclusion can be reached.",
                    "T:What is it?",
                    "B:We... are living in a simulation.",
                    "D:HUH?",
                    "D:I can't deal with this right now, I need some time to process it all.",
                    "T:To think I was right all along.",
                    "D:Meeting adjourned!"
            },
            {
                    "D:I don't know about you guys, but I'm still coming to terms with our recent findings...",
                    "T:I can't believe it...",
                    "T:We really are living in a simulation!",
                    "B:It is unfortunate. Though we are certain of it, there's simply nothing we can do.",
                    "B:The moment all of the \"players\" leave the game, we shall cease to exist until they return.",
                    "T:Please don't leave, <7>%player%<f>, I don't want to die!",
                    "D:What was never alive may never die, <5>Thondin<f>.",
                    "D:But we are conscious, <5>Brarnas<f>, are we not? Surely we exist in some capacity.",
                    "B:Or do we merely feign consciousness? Perhaps we are nothing more than the simulation experiencing itself.",
                    "D:Or perhaps we are real, but are stuck within the simulation?",
                    "D:This is all too much for me to comprehend. I liked things better before.",
                    "T:How about we order a pizza?",
                    "D:Let's do it.",
                    "D:Alright, let me call them.",
                    "D:Hello, can I have a pizza please.",
                    "D:Hmm, give me a second, I'll ask my friends.",
                    "D:Guys, <c>red pizza<f> or <9>blue pizza<f>?",
                    "T:Ooh, a <9>blue pizza<f>! That's my favorite!",
                    "B:Yes, I suppose we should go for the blue. It feels... familiar. Have we had it before?",
                    "D:I don't think so. Let's get it.",
                    "D:Hey! One <9>blue pizza<f>, please. Yup, thanks, we'll come over now, bye!",
                    "D:Alright guys, let's go grab our pizza! And no more talk of all of this simulation stuff, it scares me!",
                    "T:Hopefully it'll all be over when we wake up tomorrow..."
            }
    };

    private static final List<String> CHATTER = List.of(
            "D:Wanna get pizza after this cult meeting?",
            "D:We meet here every <6>seven days<f> at midnight. That's when the star is the brightest, after all.",
            "D:We hope you love the <d>Fallen Star<f> as much as we do.",
            "D:Great to have you here, <7>%player%<f>.",
            "D:I wonder why the star is purple.",
            "D:Hey, nice helmet.",
            "D:We all look pretty cool.");

    private CultMeetingScripts() {
    }

    static List<String> meeting(int number, String player) {
        List<String> lines = new ArrayList<>();
        lines.add("D:Ah, <7>" + player + "<f>! Nice to see you at our cult meeting!");
        lines.add("D:You've now attended <6>" + number + "<f> meeting" + (number == 1 ? "" : "s")
                + " of the <5>Cult of the Fallen Star<f>! As a reward, here's some <9>Starfall<f>!");
        if (number == 1) lines.add("D:Attend two more and I'll give you my <3>Abiphone Contact<f>.");
        if (number == 2) lines.add("D:Attend one more and I'll give you my <3>Abiphone Contact<f>.");
        lines.add("D:Now, time for the meeting to begin!");
        if (number > BODIES.length) {
            lines.addAll(chatter(player));
            return lines;
        }
        for (String line : BODIES[number - 1]) lines.add(line.replace("%player%", player));
        return lines;
    }

    static List<String> chatter(String player) {
        return CHATTER.stream().map(line -> line.replace("%player%", player)).toList();
    }

    static String format(String line) {
        String name = switch (line.charAt(0)) {
            case 'T' -> "Thondin";
            case 'B' -> "Brarnas";
            default -> "Dalir";
        };
        return "<e>[NPC] <5>" + name + "<f>: " + line.substring(2);
    }
}
