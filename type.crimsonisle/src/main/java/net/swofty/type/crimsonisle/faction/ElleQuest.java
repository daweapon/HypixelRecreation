package net.swofty.type.crimsonisle.faction;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.gui.faction.GUIQuestClaim;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonAbiphone;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestMobs;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.crimson.questline.UniversalStep;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import static net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene.CAPTAIN_AHONE;
import static net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene.ELLE;
import static net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene.UGO;

/**
 * Elle: the arrival quest, the faction choice, the parts of both faction lines she leads, the
 * Forgotten Skull meeting, and free faction switching (wiki: Elle, Crimson Isle Main Questline).
 */
public final class ElleQuest {
    public static final Pos STRONGHOLD = new Pos(-364.5, 80, -476.5, 180, 0);
    public static final Pos STRONGHOLD_CENTRE = new Pos(-360.5, 86, -517.5);
    public static final Pos SCARLETON_CC = new Pos(-125.5, 92, -761.5);
    public static final Pos DRAGONTAIL = new Pos(-595.5, 108, -671.5);
    public static final Pos BASTION_CLIFF = new Pos(-612.5, 154, -894.5);
    public static final Pos BURNING_DESERT = new Pos(-487.5, 95, -652.5);
    public static final Pos UGO_CAVE = new Pos(-506.5, 98, -630.5);
    public static final Pos CHIEFS_HUT = new Pos(-587.5, 115, -686.5);
    public static final Pos CATHEDRAL = new Pos(-102.5, 130, -917.5);
    public static final Pos WASTELAND = new Pos(-371.5, 117, -979.5);
    public static final Pos CORRUPTED_SPIDER_SPAWN = new Pos(-492.5, 96, -660.5);

    private static final String ID = "elle";

    private ElleQuest() {
    }

    public static Pos position(SkyBlockPlayer player) {
        UniversalStep universal = CrimsonQuestlines.universal(player);
        if (universal == UniversalStep.BRING_ITEMS) return STRONGHOLD_CENTRE;
        if (universal.ordinal() >= UniversalStep.EXPLORE.ordinal() && universal != UniversalStep.COMPLETE) return SCARLETON_CC;
        if (universal != UniversalStep.COMPLETE) return STRONGHOLD;
        if (ActiveLine.atForgottenSkull(player)) return WASTELAND;
        CrimsonFaction line = ActiveLine.of(player);
        if (line == CrimsonFaction.MAGE) {
            return switch (CrimsonQuestlines.mage(player)) {
                case MEET_ELLE_AT_BASTION, RECOVER_RESEARCH -> BASTION_CLIFF;
                default -> STRONGHOLD;
            };
        }
        if (line == CrimsonFaction.BARBARIAN) {
            return switch (CrimsonQuestlines.barbarian(player)) {
                case FOLLOW_ELLE, KILL_CORRUPTED_MONSTER -> BURNING_DESERT;
                case FIND_THE_NOISE, FEED_THE_PIG -> UGO_CAVE;
                case ESCORT_UGO -> CHIEFS_HUT;
                case MEET_ELLE_AT_CATHEDRAL, STEAL_RESEARCH -> CATHEDRAL;
                default -> STRONGHOLD;
            };
        }
        return STRONGHOLD;
    }

    public static boolean showsInDragontail(SkyBlockPlayer player) {
        UniversalStep universal = CrimsonQuestlines.universal(player);
        return universal.ordinal() >= UniversalStep.EXPLORE.ordinal() && universal != UniversalStep.COMPLETE;
    }

    public static void interact(CrimsonNPC elle, SkyBlockPlayer player, boolean inDragontail) {
        switch (CrimsonQuestlines.universal(player)) {
            case KILL_BLAZES -> elle.sayRandom(player, "ONE SECOND I'M PRETTY BUSY RIGHT NOW!",
                    "COULD YOU HELP ME INSTEAD OF JUST SITTING THERE?!");
            case HELP_ELLE -> askForHelp(elle, player);
            case BRING_ITEMS -> collectItems(elle, player);
            case EXPLORE -> explainFactions(player, inDragontail);
            case TALK_TO_VILLAGERS -> elle.sayRandom(player, "The history between the 2 factions is long and complicated.",
                    "Talk to the villagers and come back to me.",
                    "Make sure you also talk to villagers from the other faction before you make your choice.");
            case CHOOSE_FACTION -> offerFactions(elle, player);
            case COMPLETE -> factionLine(elle, player);
        }
    }

    private static void askForHelp(CrimsonNPC elle, SkyBlockPlayer player) {
        boolean asked = CrimsonQuestlines.counter(player, "elle_asked") > 0;
        CrimsonQuestlines.setCounter(player, "elle_asked", 1);
        String[] lines = asked ? new String[]{"Will you help me?"} : new String[]{
                "Wow, that was something!",
                "I'm immune to fire but those monsters are still quite troublesome...",
                "Thanks for your help, I seriously appreciate that.",
                "I'm in a bit of a hurry, though. I need to collect <2>2 Spectre Dust<f>, <2>3 Blaze Ashes<f>, <2>4 Magma Chunks<f> and I'm super late.",
                "Will you help me?"};
        elle.say(player, lines).thenRun(() -> CrimsonChoices.noYes(player, ID,
                () -> elle.say(player, "Okay, that's fine. I just won't be able to buy food today and I won't be able to pay my rent.",
                        "I might also die finding those items and you'll have that on your conscience.",
                        "Have a nice day."),
                () -> {
                    if (!CrimsonQuestlines.advance(player, UniversalStep.HELP_ELLE, UniversalStep.BRING_ITEMS)) return;
                    elle.say(player, "Thanks! You seem pretty strong, all you have to do is exterminate some of the monsters around. Follow me!",
                            "Here, some of the mobs are inside that building and some are on the outside if you follow the path. Piece of cake!",
                            "I need <2>2 Spectre Dust<f>, <2>3 Blaze Ashes<f>, <2>4 Magma Chunks<f>.");
                }));
    }

    private static void collectItems(CrimsonNPC elle, SkyBlockPlayer player) {
        boolean hasAll = player.getAmountInInventory(ItemType.SPECTRE_DUST) >= 2
                && player.getAmountInInventory(ItemType.BLAZE_ASHES) >= 3
                && player.getAmountInInventory(ItemType.MAGMA_CHUNK) >= 4;
        if (!hasAll) {
            elle.sayRandom(player, "Most monsters around drop valuable items, you can make a living killing them.",
                    "I'll be right there, I need to catch my breath.",
                    "I needed <2>2 Spectre Dust<f>, <2>3 Blaze Ashes<f>, <2>4 Magma Chunks<f>.");
            return;
        }
        if (!CrimsonQuestlines.advance(player, UniversalStep.BRING_ITEMS, UniversalStep.EXPLORE)) return;
        player.removeItemFromPlayer(ItemType.BLAZE_ASHES, 3);
        player.removeItemFromPlayer(ItemType.MAGMA_CHUNK, 4);
        player.removeItemFromPlayer(ItemType.SPECTRE_DUST, 2);
        CrimsonScene.create()
                .raw("<c>-3 Blaze Ashes").raw("<c>-4 Magma Chunk").raw("<c>-2 Spectre Dust")
                .say(ELLE, "Oh, you already got everything?", "Yoink!",
                        "Thanks for the items, that's super nice of you!",
                        "You are new around here, right? Never seen you before.",
                        "You are probably wondering what the heck happened to this place?",
                        "This land actually used to be a peaceful forest with farmers, a long long time ago.",
                        "When the <c>Emperor Dragon<f> destroyed the world something awakened deep below the surface.",
                        "The giant volcano emerged and it kept on growing, over the centuries the whole place changed for the worse.",
                        "I've really got to go now but if you are interested I suggest you explore the island a bit more.",
                        "Thanks for your help, see you around!")
                .play(player);
    }

    /** Elle stops the player at the Throne Room or Chief's Hut and sends them to meet both factions. */
    public static void explainFactions(SkyBlockPlayer player, boolean chiefsHut) {
        if (CrimsonScene.isPlaying(player) || CrimsonQuestlines.universal(player) != UniversalStep.EXPLORE) return;
        CrimsonQuestlines.setCounter(player, "elle_explain", 1);
        String residence = chiefsHut ? "<2>Chief<f>" : "<2>Queen<f>";
        CrimsonScene.create().say(ELLE,
                "Wow careful, you can't simply enter this building, this place is the residence of the " + residence + "!",
                "There are 2 different <c>Factions<f> on this island with incredible tensions between them!",
                "They don't mind travelers but you have to earn their trust before you can do such things.",
                "In fact, you'll have to <2>choose a faction<f>, but it's a little bit early for that.",
                "Joining a faction is an important choice, I suggest you get to know the two factions first.",
                "Perhaps you should start by <2>talking to multiple villagers from each faction<f>.",
                "The other faction is on the other side of the island."
        ).play(player).thenRun(() -> CrimsonQuestlines.advance(player, UniversalStep.EXPLORE, UniversalStep.TALK_TO_VILLAGERS));
    }

    private static void offerFactions(CrimsonNPC elle, SkyBlockPlayer player) {
        elle.say(player, "So you talked to a few villagers from both factions? Nice.",
                "I hope you know which faction you want to join. But don't worry, you'll be able to change factions if you want.",
                "However, changing factions usually means that you become unfriendly with your current faction.",
                "Anyways, which faction did you choose?"
        ).thenRun(() -> CrimsonChoices.pick(player, ID,
                "BARBARIANS", NamedTextColor.RED, () -> choose(elle, player, CrimsonFaction.BARBARIAN),
                "MAGES", NamedTextColor.LIGHT_PURPLE, () -> choose(elle, player, CrimsonFaction.MAGE)));
    }

    private static void choose(CrimsonNPC elle, SkyBlockPlayer player, CrimsonFaction faction) {
        if (!CrimsonQuestlines.advance(player, UniversalStep.CHOOSE_FACTION, UniversalStep.COMPLETE)) return;
        if (faction == CrimsonFaction.MAGE) {
            elle.say(player, "Hum, so you chose the <c>Mage Faction<f> then.",
                    "It's not going to be easy to join them.",
                    "You first have to prove your worth if you want to have a chance at joining their ranks.",
                    "Hum, let me think...", "Oh! I know what you should do!",
                    "The <c>Taika<f> people really like educated individuals.",
                    "If you can bring them back a book or a piece of technology they have never seen before, I'm sure they will take a liking to you.",
                    "I couldn't help but notice that you come from the place we commonly know as The Hub.",
                    "In which case, you must have met <c>Maddox<f>?",
                    "Both <c>Maddox <f>and his brother are in fact from <5>Scarleton<f>, but contrary to the other inhabitants, the 2 brothers were born without any magical aptitudes.",
                    "In fact, they instead showed that they had immense strength, and a taste for violence.",
                    "They got banned from the <7>Academy <f>during their first year, and even got exiled from the island after a terrible incident.",
                    "But I digress. My point is they are both <c>Taika<f>, and I know for a fact that <c>Aatrox <f>is crafting simple <2>Ability Phones <f>for his brother.",
                    "In fact I have one myself. Here take it, I'll just request another one from <c>Maddox<f>. I keep losing them."
            ).thenRun(() -> startMage(elle, player));
        } else {
            elle.say(player, "You want to join the <c>Barbarian Faction<f>, hum.", "What a strange choice.",
                    "In any case, I have no idea how to guarantee your entry in their faction!",
                    "But let's not get discouraged just yet, we'll find something I'm sure.",
                    "<c>Piglins <f>crave <2>Sulphur<f>. Our first goal should be to find some.",
                    "There are only 2 ways to get <2>Sulphur<f>.",
                    "You either wait for the <c>Volcano <f>to erupt, or you find a <2>Corrupted Monster<f>.",
                    "And guess what, I know where to look!", "Follow me!"
            ).thenRun(() -> CrimsonQuestlines.set(player, BarbarianStep.FOLLOW_ELLE));
        }
    }

    private static void startMage(CrimsonNPC elle, SkyBlockPlayer player) {
        if (CrimsonQuestlines.mage(player) == MageStep.NOT_STARTED) CrimsonQuestlines.set(player, MageStep.SHOW_BATPHONE);
        openBatphoneClaim(elle, player);
    }

    private static void openBatphoneClaim(CrimsonNPC elle, SkyBlockPlayer player) {
        player.openView(new GUIQuestClaim("Maddox Batphone", new SkyBlockItem(ItemType.AATROX_BATPHONE), () -> {
            if (player.getAmountInInventory(ItemType.AATROX_BATPHONE) > 0) {
                player.sendMessage("<c>You already have one in your inventory!");
                return;
            }
            player.addAndUpdateItem(ItemType.AATROX_BATPHONE);
            elle.say(player, "Bring this to the <2>Dean of the Academy <f>in <5>Scarleton<f>, I'm sure he will find it quite interesting.",
                    "The <2>Cathedral <f>got reorganized a while ago, now a large portion of it is used for the <2>Academy<f>.");
        }));
    }

    private static void factionLine(CrimsonNPC elle, SkyBlockPlayer player) {
        if (ActiveLine.atForgottenSkull(player)) {
            forgottenSkull(elle, player);
            return;
        }
        CrimsonFaction line = ActiveLine.of(player);
        if (line == CrimsonFaction.MAGE && mageStep(elle, player)) return;
        if (line == CrimsonFaction.BARBARIAN && barbarianStep(elle, player)) return;
        offerSwitch(elle, player);
    }

    private static boolean mageStep(CrimsonNPC elle, SkyBlockPlayer player) {
        switch (CrimsonQuestlines.mage(player)) {
            case SHOW_BATPHONE -> {
                if (player.getAmountInInventory(ItemType.AATROX_BATPHONE) == 0) openBatphoneClaim(elle, player);
                else elle.say(player, "Bring this to the <2>Dean of the Academy <f>in <5>Scarleton<f>, I'm sure he will find it quite interesting.");
            }
            case TALK_TO_ELLE -> elle.say(player, "Oh wow, it worked! I'm so happy for you!",
                    "And you even got an Abiphone! Take my <a>contact <f>so we can stay in touch."
            ).thenRun(() -> {
                CrimsonAbiphone.grant(player, "elle");
                elle.say(player, "Nice! So what are you up to?",
                        "The Dean told you his research got stolen?!",
                        "Ah, it's not so surprising, maybe you don't know but the Piglins are not so fond of the Taika people.",
                        "I do have an idea where that stolen research might be, follow me."
                ).thenRun(() -> CrimsonQuestlines.advance(player, MageStep.TALK_TO_ELLE, MageStep.MEET_ELLE_AT_BASTION, 0));
            });
            case MEET_ELLE_AT_BASTION -> elle.say(player, "You see this giant Dragon Skull inside the Barbarian Faction?",
                    "That's the skeleton of <c>Jythar the Eternal one <f>- the one who caused the destruction of the world.",
                    "Now you probably understand why the village in front is called <c>Dragontail<f>.",
                    "Anyways, the Barbarians built their <c>Bastion<f> inside the skeleton, and I suspect this is where <2>the stolen object is guarded<f>.",
                    "Piglins fear the growing power of the Taika Kingdom, so they most likely stole it to study the research.",
                    "Maybe you could sneak in and recover the item. Without getting seen and creating a major diplomatic incident, of course.",
                    "I'm not sure exactly where they keep it, but just make sure you don't get seen by the guards!",
                    "I can't help you however, I have way too much to lose sorry. But good luck!"
            ).thenRun(() -> CrimsonQuestlines.advance(player, MageStep.MEET_ELLE_AT_BASTION, MageStep.RECOVER_RESEARCH, 0));
            case RECOVER_RESEARCH -> elle.sayRandom(player, "Piglins can't see very far because of the fog, just don't stand in front of them.",
                    "Do you even know what you are looking for? Maybe a book or a magical item?");
            default -> {
                return false;
            }
        }
        return true;
    }

    private static boolean barbarianStep(CrimsonNPC elle, SkyBlockPlayer player) {
        switch (CrimsonQuestlines.barbarian(player)) {
            case FOLLOW_ELLE -> elle.say(player, "Look, we found one! A <2>Corrupted Monster<f>!",
                    "It's just ahead of us, the <2>Burning Spider<f>!", "Go fight it, good luck!"
            ).thenRun(() -> {
                if (CrimsonQuestlines.advance(player, BarbarianStep.FOLLOW_ELLE, BarbarianStep.KILL_CORRUPTED_MONSTER, 0)) {
                    QuestMobs.spawn(CrimsonQuestMobs.FLAMING_SPIDERS, CORRUPTED_SPIDER_SPAWN);
                }
            });
            case KILL_CORRUPTED_MONSTER -> {
                elle.say(player, "Go fight it, good luck!");
                if (CrimsonQuestlines.increment(player, "spider_respawns") % 3 == 0) {
                    QuestMobs.spawn(CrimsonQuestMobs.FLAMING_SPIDERS, CORRUPTED_SPIDER_SPAWN);
                }
            }
            case FIND_THE_NOISE -> CrimsonScene.create()
                    .say(ELLE, "You handled that quite well.",
                            "<2>Corrupted Monsters <f>rarely appear on the island, it usually means they consumed too much <2>Sulphur<f>.")
                    .say("<d><k>----", "Oink")
                    .say(ELLE, "Did you hear that?", "I think it was coming from here!", "Let's go.",
                            "This <c>Piglin<f> got lost!",
                            "They probably got cornered by monsters and had to hide here.",
                            "<c>Piglins <f>turn back into pigs if they go too long without <2>Sulphur<f>.",
                            "That <2>Corrupted Monster<f> should have dropped some <2>Sulphur<f>. Quick, give it to them!")
                    .play(player)
                    .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.FIND_THE_NOISE, BarbarianStep.FEED_THE_PIG, 0));
            case FEED_THE_PIG -> elle.say(player, "That <2>Corrupted Monster<f> should have dropped some <2>Sulphur<f>. Quick, give it to them!");
            case ESCORT_UGO -> BarbarianScenes.chiefWelcome(player);
            case MEET_ELLE_AT_CATHEDRAL -> elle.say(player, "This is the <c>Mage Cathedral<f>.",
                    "Your job is to enter the <c>Cathedral<f> and find the <c>Dean's office.",
                    "They are probably hiding the <2>research<f> there.",
                    "There are multiple entrances, most of them have guards but this one down there is often unprotected.",
                    "Good luck, I'll be right there in case you need moral support."
            ).thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.MEET_ELLE_AT_CATHEDRAL, BarbarianStep.STEAL_RESEARCH, 0));
            case STEAL_RESEARCH -> elle.sayRandom(player, "Don't get caught by the guards!",
                    "The Dean's office? I don't know where it is.");
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void forgottenSkull(CrimsonNPC elle, SkyBlockPlayer player) {
        boolean met = CrimsonQuestlines.isAwaitingKuudra(player);
        CrimsonScene scene = CrimsonScene.create();
        if (!met) {
            scene.say(ELLE, "Captain Ahone, Ugo, this is a misunderstanding!", "I saw what happened! I saw everything!",
                            "A monstruous creature was carrying the Sulphur, mountains of Sulphur!",
                            "The monster had giant tentacles, I have never seen anything like it before in my life!")
                    .say(CAPTAIN_AHONE, "Elle of the Nether, do not attempt to trick us.",
                            "Your motives are noble, but time for diplomacy is over.",
                            "Barbarians, return our Sulphur at once, or face the consequences of your actions.")
                    .say(UGO, "WE stole YOUR Sulphur?! You wizards have some nerves!",
                            "We came here following the trails of the Sulphur that YOU robbed!")
                    .say(ELLE, "ENOUGH!", "You were BOTH robbed, isn't it obvious?!",
                            "The culprit is inside, and it is not Human nor Piglin.",
                            "I have no idea what it is, but I have to warn you, that fiend is immensely powerful.",
                            "Now if you wish to retrieve your Sulphur, follow me inside.")
                    .say(CAPTAIN_AHONE, "This giant tentacle monster story is real?",
                            "I have trouble believing it, but in any case we have to get to the bottom of this.")
                    .say(UGO, "I agree, let's get inside.")
                    .say(ELLE, "<player> if you know other strong adventurers, now is the time to bring them.",
                            "This monster is something else, trust me.", "Are you ready to fight?");
        } else {
            scene.say(ELLE, "Are you ready?");
        }
        scene.play(player).thenRun(() -> CrimsonChoices.yesNo(player, ID, () -> {
            CrimsonQuestlines.advance(player, MageStep.MEET_AT_FORGOTTEN_SKULL, MageStep.DEFEAT_KUUDRA, 0);
            CrimsonQuestlines.advance(player, BarbarianStep.MEET_AT_FORGOTTEN_SKULL, BarbarianStep.DEFEAT_KUUDRA, 0);
            elle.say(player, "Perfect! Let's fight!").thenRun(() -> KuudraAvailability.blockIfUnavailable(player));
        }, () -> elle.say(player, "Come back when you are ready, hurry up!")));
    }

    private static void offerSwitch(CrimsonNPC elle, SkyBlockPlayer player) {
        CrimsonFaction current = CrimsonReputationService.faction(player);
        if (current == null) {
            elle.say(player, "Hey!", "Talk to the villagers and come back to me.");
            return;
        }
        CrimsonFaction target = current.opposite();
        elle.say(player, "Hey!", "How do you like your Faction?",
                "If you want to swap Factions let me know, it really isn't that hard.",
                "Do you want to change your Faction?"
        ).thenRun(() -> CrimsonChoices.yesNo(player, ID, () -> switchFaction(elle, player, target),
                () -> elle.say(player, "Great!")));
    }

    /** Switching is free and keeps both reputations; a never-started faction line starts from its first step. */
    private static void switchFaction(CrimsonNPC elle, SkyBlockPlayer player, CrimsonFaction target) {
        CrimsonFaction current = CrimsonReputationService.faction(player);
        if (current == null || current == target) return;
        boolean started = CrimsonQuestlines.isStarted(player, target);
        String opener = target == CrimsonFaction.MAGE ? "So you want to join the Mage Faction then..."
                : "You want to join the <c>Barbarian Faction<f>, hmm.";
        CrimsonReputationService.setFaction(player, target);
        player.sendMessage("<a>You are now a member of the " + target.coloured() + " Faction<a>!");
        if (started) {
            elle.say(player, opener, "Welcome back, I'm sure they missed you.");
            return;
        }
        if (target == CrimsonFaction.MAGE) {
            elle.say(player, opener, "I do have an idea how you could do it.",
                    "The <c>Taika<f> people really like educated individuals. Here, take this <2>Ability Phone<f> to the <2>Dean<f>."
            ).thenRun(() -> startMage(elle, player));
        } else {
            elle.say(player, opener, "I do have an idea how you could do it.",
                    "<c>Piglins <f>crave <2>Sulphur<f>. Our first goal should be to find some.",
                    "There are only 2 ways to get <2>Sulphur<f>.",
                    "You either wait for the <c>Volcano <f>to erupt, or you find a <2>Corrupted Monster<f>.",
                    "And guess what, I know where to look!", "Follow me!"
            ).thenRun(() -> CrimsonQuestlines.set(player, BarbarianStep.FOLLOW_ELLE));
        }
    }
}
