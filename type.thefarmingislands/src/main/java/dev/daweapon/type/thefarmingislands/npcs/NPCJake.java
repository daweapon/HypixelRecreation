package dev.daweapon.type.thefarmingislands.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.farming.jake.JakeDelivery;
import dev.daweapon.type.skyblockgeneric.farming.jake.JakeShop;
import dev.daweapon.type.skyblockgeneric.farming.jake.JakeShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.List;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;

public class NPCJake extends HypixelNPC {
    private static final String MET = "npc.jake";
    private static final Pos POSITION = new Pos(261.5, 184, -565.5, 90, 0);

    public NPCJake() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Jake", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "pMvZ+g1p1HCldbVKd1w05q4HyluUjAeEJipZQe7omlFQdiF5PIoHo6sY7k1/dwPQwXgUGsytvxnFfIiWAflryAC5FrxLpTLhrghbuf0zgbNtrjhA6s67txvbp1jpAs7k4ydufiQ39Ob/Y7+Ojx8ZiPMb6UK5OrwPmUmyq1KxloEl6N2qzA1odf7sSXuVxrFlbPwT8p9rEsV8ogCUmzmTiPqkIAGUCWJ3ZbRDOFjOKbrD8Ff0/7qQ6g0BDrOLNPWeHl/eJHT8puCOktNIQs1lwYyNF9gllpkicFOQ9XqCJH8END6jyyF0wgfubFAFB1eoQKGRenmuY4ktJQT+D3yZlOzV20zbtGzUvVvkC7ooYyWFLY3L6m5NCEUNwU5XJbc1hK9PQH1zajY+v1Lp09DsVmiib4NjDsUvb6nF6BISH6w3J9ZNLlwWcoawXs5EqLQbpQxDXuNkD1q3FszvuCoRYOgmXSZqverQ+v5L18BnUjDMM1bUm5r/gH/hPp06Xc5jZmzhYsajqKwhCnsyYY8NS3PD3BIM96a3XhheEVoFZRvg7OHDQtQgzAydJE9bnJmFTFyThUR5ZH11OlSPmTMToKdOUaj+TbhEPGUXPolQ6gpHbcpRVD93RUbKqfPYIaQvgnPMiF6rWJlOHaYetFEmgIeAQGHzEG8KzTvKQChZQ14=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5NjA3ODI4NjQ3MywKICAicHJvZmlsZUlkIiA6ICJkODAwZDI4MDlmNTE0ZjkxODk4YTU4MWYzODE0Yzc5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVCTFJ4eCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNTFjYzdjMWQxMWFhMGEwOGE3M2NiYzE4NDU0NzA1MWFlYmQwN2IzYmI1MGQ4NmYxMTg4ZmQ3YTdkYWQyZWQzIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return POSITION;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        if (!ChapterTriggers.done(player, MET)) {
            setDialogue(player, "first").thenRun(() -> ChapterTriggers.fire(player, MET));
            return;
        }
        JakeDelivery.Delivery delivery = JakeDelivery.find(player, POSITION);
        if (delivery == null) {
            if (JakeShop.anyOpen()) {
                setDialogue(player, "other-brought").thenRun(() -> player.openView(new JakeShopView()));
            } else {
                setDialogue(player, "none");
            }
            return;
        }
        boolean alreadyOpen = JakeShop.isOpen(delivery.animal().unlocks());
        delivery.consume();
        JakeShop.open(delivery.animal().unlocks());
        String key = alreadyOpen ? "same-open" : delivery.animal().dialogueKey();
        setDialogue(player, key).thenRun(() -> player.openView(new JakeShopView()));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first").lines(
                        "Hi I'm Jake. I've been living on this island for 25 years.",
                        "I used to study animals from all over the land!",
                        "Unfortunately, I suffered an injury which restricts me from leaving my house.",
                        "I really enjoyed studying animals.",
                        "Could you bring me an animal? You can find them all over the island!").build(),
                DialogueSet.builder().key("none").lines(
                        "Could you bring me an animal? You can find them all over the island!").build(),
                DialogueSet.builder().key("other-brought").lines(
                        "Someone already brought me an animal!",
                        "I'm feeling generous, I'll show you my store.").build(),
                DialogueSet.builder().key("same-open").lines(
                        "Someone already brought me this animal recently, but since you put in all the effort I'll open my store for you.",
                        "I'm feeling generous, I'll show you my store.").build(),
                DialogueSet.builder().key("cow").lines(
                        "Wow a cow!",
                        "How did you get this over the void? Incredible!",
                        "To show you my gratitude, I have a special item to sell you!").build(),
                DialogueSet.builder().key("mooshroom").lines(
                        "Wow a mushroom Cow! How did you get it out of the Gorge?",
                        "To show you my gratitude, I have a special item to sell you!").build(),
                DialogueSet.builder().key("chicken").lines(
                        "Wow a chicken!",
                        "Did it fly over here?",
                        "To show you my gratitude, I have a special item to sell you!").build(),
                DialogueSet.builder().key("pig").lines(
                        "Wow a pig!",
                        "How did you get this over the void? You must be really strong.",
                        "To show you my gratitude, I have a special item to sell you!").build(),
                DialogueSet.builder().key("rabbit").lines(
                        "Thank you for bringing me this rabbit!",
                        "To show my gratitude, I have a special item to sell you!").build(),
                DialogueSet.builder().key("sheep").lines(
                        "Thank you for bringing me this sheep!",
                        "To show my gratitude, I have a special item to sell you!").build(),
                DialogueSet.builder().key("oasis-rabbit").lines(
                        "This rabbit looks a little funny, did you fish it out of the Oasis?",
                        "To show you my gratitude, I have a special item to sell you!").build(),
                DialogueSet.builder().key("oasis-sheep").lines(
                        "This sheep looks a little funny, did you fish it out of the Oasis?",
                        "To show you my gratitude, I have a special item to sell you!").build(),
                DialogueSet.builder().key("loch-emperor").lines(
                        "WOW!",
                        "The legendary Skeleton Emperor!",
                        "I searched all my life to find this creature but was never able to find it!",
                        "To show my gratitude, I have a special item to sell you!").build()
        ).toArray(DialogueSet[]::new);
    }
}
