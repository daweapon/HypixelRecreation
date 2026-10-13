package net.swofty.type.skyblockgeneric.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.ItemType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.pest.PestData;
import net.swofty.type.skyblockgeneric.garden.pest.PestRules;
import net.swofty.type.skyblockgeneric.garden.pest.PestService;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIPesthunter;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;

import java.util.List;
import java.util.stream.Stream;

public class PesthunterNpc extends HypixelNPC {
    public enum Kind {
        PHILLIP("Pesthunter Phillip", "PESTHUNTER_PHILLIP", new Pos(-25.5, 71, -13.5, 0, 0)),
        PAMELA("Pesthunter Pamela", "PESTHUNTER_PAMELA", new Pos(-23.5, 71, -13.5, 0, 0));

        private final String displayName;
        private final String skinKey;
        private final Pos position;

        Kind(String displayName, String skinKey, Pos position) {
            this.displayName = displayName;
            this.skinKey = skinKey;
            this.position = position;
        }
    }

    private final Kind kind;

    protected PesthunterNpc(Kind kind) {
        super(configuration(kind));
        this.kind = kind;
    }

    private static HumanConfiguration configuration(Kind kind) {
        return new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{kind.displayName, "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return GardenNpcSkins.of(kind.skinKey).signature();
            }

            @Override
            public String texture(HypixelPlayer player) {
                return GardenNpcSkins.of(kind.skinKey).texture();
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return kind.position;
            }

            @Override
            public Instance instance(HypixelPlayer player) {
                return player.getInstance();
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                return player.getInstance() != null
                        && player.getInstance() == SkyBlockGarden.of(skyBlockPlayer).getGardenInstance()
                        && PestService.unlocked(Gardens.profile(skyBlockPlayer));
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<b>" + kind.displayName;
            }
        };
    }

    public static PesthunterNpc of(Kind kind) {
        return kind == Kind.PHILLIP ? new PhillipNpc() : new PesthunterNpc(kind);
    }

    public static final class PhillipNpc extends PesthunterNpc implements NPCAbiphoneTrait {
        private PhillipNpc() {
            super(Kind.PHILLIP);
        }

        @Override
        public String getAbiphoneKey() {
            return "phillip";
        }
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        PestData data = Gardens.profile(player).pests();
        if (kind == Kind.PHILLIP) {
            clickPhillip(player, data);
        } else {
            clickPamela(player, data);
        }
    }

    private void clickPhillip(SkyBlockPlayer player, PestData data) {
        if (!data.isMetPhillip()) {
            setDialogue(player, "first").thenRun(() -> choose(player, "What's a pest?", () ->
                    setDialogue(player, "pest").thenRun(() -> choose(player, "What am I supposed to do about this?", () ->
                            setDialogue(player, "vacuum").thenRun(() -> {
                                PestData fresh = Gardens.profile(player).pests();
                                fresh.setMetPhillip(true);
                                Gardens.save(player, Gardens.profile(player));
                                giveOnce(player, ItemType.SKYMART_VACUUM);
                            })))));
            return;
        }
        if (data.getTotalKilled() == 0) {
            setDialogue(player, "before");
            return;
        }
        if (!data.isPhillipDebriefed()) {
            setDialogue(player, "after").thenRun(() -> choose(player, "It was okay.", () ->
                    setDialogue(player, "okay").thenRun(() -> {
                        Gardens.profile(player).pests().setPhillipDebriefed(true);
                        Gardens.save(player, Gardens.profile(player));
                    })));
            return;
        }
        player.openView(new GUIPesthunter());
    }

    private void clickPamela(SkyBlockPlayer player, PestData data) {
        if (!data.isMetPhillip()) {
            setDialogue(player, "early");
            return;
        }
        if (!data.isMetPamela()) {
            setDialogue(player, "first").thenRun(() -> {
                Gardens.profile(player).pests().setMetPamela(true);
                Gardens.save(player, Gardens.profile(player));
                giveOnce(player, ItemType.SPRAYONATOR);
                player.addAndUpdateItem(new SkyBlockItem(ItemType.TASTY_CHEESE));
            });
            return;
        }
        giveOnce(player, ItemType.SPRAYONATOR);
        setDialogue(player, "again");
    }

    private void choose(SkyBlockPlayer player, String label, Runnable next) {
        NPCOption.sendOption(player, kind.skinKey, true, List.of(
                new NPCOption.Option("choice", NamedTextColor.GREEN, false, label, _ -> next.run())));
    }

    private static void giveOnce(SkyBlockPlayer player, ItemType type) {
        if (player.getAmountInInventory(type) == 0) player.addAndUpdateItem(new SkyBlockItem(type));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        if (kind == Kind.PHILLIP) {
            return Stream.of(
                    DialogueSet.builder().key("first").lines(
                            "Howdy, " + player.getUsername() + "!",
                            "Sorry for the intrusion, but I got a report of a <6>Pest <f>outbreak around here?"
                    ).build(),
                    DialogueSet.builder().key("pest").lines(
                            "Err...vermin? Swine? Buggers? Y'know...<6>Pests<f>!",
                            "They seem to pop up when you're <e>breaking crops <f>on your <b>Garden<f>!"
                    ).build(),
                    DialogueSet.builder().key("vacuum").lines(
                            "Okay, feller. Take this <a>SkyMart Vacuum<f>!",
                            "There's a <6>Pest <f>out there somewhere, I'm certain of it.",
                            "When you find one, simply aim the <a>vacuum<f>, hold down the button, and you can suck them buggers up real quick!",
                            "When you've done that, come back and tell me all about how much fun you had!"
                    ).build(),
                    DialogueSet.builder().key("before").lines(
                            "Go out there and <a>vacuum <f>a Pest!",
                            "It's not hard. Just aim, hold down the button, and don't stop until the bug's been squashed!"
                    ).build(),
                    DialogueSet.builder().key("after").lines(
                            "Wow! Look at that ugly thing. Bet you had a blast trying to wrangle it into your vacuum!"
                    ).build(),
                    DialogueSet.builder().key("okay").lines(
                            "To each their own. I'm fascinated by these little fellers!",
                            "Bring some back for me to, uh...study...and I'll reward you with <6>☘ Farming Fortune for your troubles!"
                    ).build()).toArray(DialogueSet[]::new);
        }
        return Stream.of(
                DialogueSet.builder().key("early").lines(
                        "Let <6>Phillip <f>finish teaching you about <6>Pests<f>, then come chat with me!"
                ).build(),
                DialogueSet.builder().key("first").lines(
                        "You know, having dealt with <6>Pests <f>for so long, I'm starting to find them kinda <d>cute<f>!",
                        "I've even developed a way to attract certain Pests to my plots.",
                        "BEHOLD! The <a>Sprayonator<f>!",
                        "Its patented technology transforms <a>organic matter <f>into aromas that only Pests can smell.",
                        "Go see for yourself. Take this <a>Tasty Cheese <f>and use the <a>Sprayonator <f>to spray a plot.",
                        "Then use your vacuum to catch a <6>Rat Pest <f>in that plot!"
                ).build(),
                DialogueSet.builder().key("again").lines(
                        "Take the <a>Sprayonator <f>and get to spraying!"
                ).build()).toArray(DialogueSet[]::new);
    }

    public static String tradeLine() {
        return "<7>Trade <2>Pests <7>for <6>" + PestRules.FORTUNE_PER_TRADED_PEST + " Farming Fortune <7>each.";
    }
}
