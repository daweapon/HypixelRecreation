package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingShardShopView;
import dev.daweapon.type.skyblockgeneric.hunting.npc.SangerSchedule;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class NPCSanger extends HypixelNPC {
    private static final String MET = "npc.sanger";
    private static final String SEEN_DAY = "sanger.seen_day";
    private static final String OPTIONS = "sanger";

    public NPCSanger() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<2>Sanger", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzYwODEyNTI3ZWJiMmQ3MmU2MTE5ZWZmZDBjY2U1ZjFmMjk2NmFkNDVlZGJiNzA1ZWQwNDk0OGFiYTZmMWI3NCIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiIyOTk1YWJlM2FkZTEyNGQ4OGYzN2YzODgyMGNkZTkwNiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg0Nzg3ODQyMTM0fQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<2>Sanger");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                SangerSchedule.Spot spot = SangerSchedule.today();
                return new Pos(spot.x(), spot.y(), spot.z(), spot.yaw(), 0);
            }

            @Override
            public boolean dynamicPosition() {
                return true;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        long today = SangerSchedule.currentDay();
        if (!ChapterTriggers.done(player, MET)) {
            ChapterTriggers.atLeast(player, SEEN_DAY, today);
            setDialogue(player, "first-interaction").thenRun(() -> sendOption(player, List.of(
                    option("what", NamedTextColor.GREEN, "What are Elusive Shards?", this::whatAreElusive))));
            return;
        }
        if (ChapterService.data(player).trigger(SEEN_DAY) < today) {
            ChapterTriggers.atLeast(player, SEEN_DAY, today);
            setDialogue(player, "found-again").thenRun(() -> player.openView(shop()));
            return;
        }
        player.openView(shop());
    }

    private HuntingShardShopView shop() {
        return new HuntingShardShopView("sanger", null, "sanger.buy", player -> {
            sendNPCMessage(player, "Come back anytime!");
            sendNPCMessage(player, "Though, you might have to find me again.");
        });
    }

    private void sendOption(SkyBlockPlayer player, List<NPCOption.Option> options) {
        NPCOption.sendOption(player, OPTIONS, true, options);
    }

    private static NPCOption.Option option(String key, NamedTextColor color, String name, java.util.function.Consumer<HypixelPlayer> action) {
        return new NPCOption.Option(key, color, true, name, action);
    }

    private void step(HypixelPlayer player, String dialogue, List<NPCOption.Option> options) {
        if (isInDialogue(player)) return;
        setDialogue(player, dialogue).thenRun(() -> sendOption((SkyBlockPlayer) player, options));
    }

    private void whatAreElusive(HypixelPlayer player) {
        step(player, "elusive", List.of(
                option("yep", NamedTextColor.GREEN, "Yep!", this::understood),
                option("example-1", NamedTextColor.YELLOW, "Can you give me an example?", this::exampleOne)));
    }

    private void exampleOne(HypixelPlayer player) {
        step(player, "example-1", List.of(
                option("yep", NamedTextColor.GREEN, "Yep!", this::understood),
                option("example-2", NamedTextColor.GOLD, "Can you give me another example", this::exampleTwo)));
    }

    private void exampleTwo(HypixelPlayer player) {
        step(player, "example-2", List.of(
                option("yep", NamedTextColor.GREEN, "Yep!", this::understood),
                option("example-3", NamedTextColor.RED, "Can you give me one last example?", this::exampleThree)));
    }

    private void exampleThree(HypixelPlayer player) {
        step(player, "example-3", List.of(option("yep", NamedTextColor.GREEN, "Yep!", this::understood)));
    }

    private void understood(HypixelPlayer player) {
        if (isInDialogue(player)) return;
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        setDialogue(player, "understood").thenRun(() -> {
            ChapterTriggers.fire(skyBlockPlayer, MET);
            skyBlockPlayer.openView(shop());
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction").lines(
                        "Hello there, my dear fellow!",
                        "It looks like you've found me!",
                        "I'm <2>Sanger <f>- an elusive man who sells <9>Shards <f>gathered from <d>Elusive <f>mobs...").build(),
                DialogueSet.builder().key("elusive").lines(
                        "Good question.",
                        "To put it simply, if you find a mob with the <d>Elusive <f>type...",
                        "And you go ahead and kill that mob...",
                        "It will <c><l>ALWAYS <r><f>drop its <9>Shard<f>!",
                        "No ifs, no buts! No getting unlucky - a <9>Shard <f>is <a><l>GUARANTEED<r><f>!",
                        "Simple as that! Do you understand, my dear fellow?").build(),
                DialogueSet.builder().key("example-1").lines(
                        "Most certainly!",
                        "<d>Special Zealots <f>have the <d>Elusive <f>type...",
                        "That means, whenever you kill a <d>Special Zealot<f>, it will always drop a <5>Special Zealot Shard<f>.",
                        "Understood?").build(),
                DialogueSet.builder().key("example-2").lines(
                        "Of course!",
                        "The <d>Titanoboa <f>has the <d>Elusive <f>type...",
                        "That means, whenever you kill a <d>Titanoboa<f>, it will always drop a <6>Titanoboa Shard<f>.",
                        "Understood?").build(),
                DialogueSet.builder().key("example-3").lines(
                        "I suppose I can...",
                        "The <9>Field Mouse <f>has the <d>Elusive <f>type...",
                        "That means, whenever you kill a <9>Field Mouse<f>, it will always drop a <a>Field Mouse Shard<f>.",
                        "I hope you're beginning to understand now...").build(),
                DialogueSet.builder().key("understood").lines(
                        "Even the best <e>Hunters <f>around struggle to find <d>Elusive <f>mobs...",
                        "However, thankfully for you, I am a particularly celebrated <e>Hunter <f>who has traveled the land far and wide.",
                        "I have collected many <9>Shards <f>in my time, gathered from the <d>Elusive <f>mobs that frequented my homeland.",
                        "Why don't you peruse my collection - perhaps we can strike a deal?").build(),
                DialogueSet.builder().key("found-again").lines(
                        "Oh, how brilliant! You appear to have found me once again!",
                        "There truly is no tricking you, is there?").build()
        ).toArray(DialogueSet[]::new);
    }
}
