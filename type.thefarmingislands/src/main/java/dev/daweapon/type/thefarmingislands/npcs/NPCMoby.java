package dev.daweapon.type.thefarmingislands.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.farming.chum.MobyShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.List;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;

public class NPCMoby extends HypixelNPC {
    private static final String MET = "npc.moby";
    private static final String TAUGHT = "npc.moby.taught";
    private static final String FULL_SHOWN = "npc.moby.full";
    private static final String BUCKET_EXPLAINED = "npc.moby.bucket";
    private static final int TEACHING_MUSHROOMS = 8;

    public NPCMoby() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Moby", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "O3vkTjT7tKfVpyQwg7q7m0PIqkfl1uvaQoeHMN9pwpOeMZ9kJH45rr4vJV6eb8qjWoOpYl01p8uLHyW3+TSFPuCDBsQSKIqAu7+h6LpZn3dGIfvS+7kml7keIR+L3JPiMz+qT52E2BWzRg78MDIb0OPF5sAB/3XvpiKq4TIQhyrw0m3ovi+7DF4Kk+DK2tTflgpsMb35kiR+hqyIq87vENv8iN9QnE8iI1xMQ3sirpDBhz+dtjMVKZkG9J3lKiiA4LUspphiM8L2NrkF0ru9WET9BngpUKArmBzldfMw0t8SXDUPkiCH2p3R7LkI1bVEaF+Cw/WVlSVMxJrBs9dkRkdDbpMDNRkiEkFfd39NPCaKi5nWzhaLmHTo4nIzYnZ76E4LpaYQflCZdH8ECewbmU/zFXUVcp+iYOkfgAIAFq901KQ/z0aDsC+181hMCfwmaPyz4rWX6efqeZge2qw9bV8SOz7bom0jz2KAoCU1xyi0Ql9A2OXu/YU5pvVUlqBI5pyp+y/EwHeJ3imRzdAH+UHRxdb4nTDl0txFISiZCAG/wvGW/cisdfaFn3PblmbFWtKTN9NrmM/MCnb1w5hnKPhvNFZt1ch8Xi1Xpa5zghSiy7TT2gANlkhvpjYWETmMfwcZ71sf1gM3SngYH6IpJzCeGWN+E0N+SoyMviq9Kec=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYyNjEwNzI5NjMzNCwKICAicHJvZmlsZUlkIiA6ICJiMWMyNWQ0YjMwZDU0N2Y4YTk3NmZlYTllOGU1YzBjMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJvd29FbmRlciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85YjY4ODU0NWUzMjZlNGYxZmQxYzQwNTg3M2Q5YzViOGJiZTY5ZGQyNjg4OWNlM2RlYTEwYjRkOWUzZDdiNGRiIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(206, 43, -500, -130, 0);
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
        if (!ChapterTriggers.done(player, TAUGHT)) {
            if (player.getAmountAnywhere(ItemType.GLOWING_MUSHROOM) < TEACHING_MUSHROOMS
                    || !player.removeItemFromPlayer(ItemType.GLOWING_MUSHROOM, TEACHING_MUSHROOMS)) {
                setDialogue(player, "wrong-items");
                return;
            }
            ChapterTriggers.fire(player, TAUGHT);
            setDialogue(player, "taught");
            return;
        }
        if (!ChapterTriggers.done(player, FULL_SHOWN) && player.getAmountAnywhere(ItemType.FULL_CHUM_BUCKET) > 0) {
            ChapterTriggers.fire(player, FULL_SHOWN);
            setDialogue(player, "full-bucket").thenRun(() -> player.openView(new MobyShopView()));
            return;
        }
        if (ChapterTriggers.done(player, MobyShopView.BOUGHT_BUCKET) && !ChapterTriggers.done(player, BUCKET_EXPLAINED)) {
            ChapterTriggers.fire(player, BUCKET_EXPLAINED);
            setDialogue(player, "after-bucket").thenRun(() -> player.openView(new MobyShopView()));
            return;
        }
        if (!ChapterTriggers.done(player, BUCKET_EXPLAINED)) {
            setDialogue(player, "click-me").thenRun(() -> player.openView(new MobyShopView()));
            return;
        }
        player.openView(new MobyShopView());
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first").lines(
                        "So listen.",
                        "The other day I was chumming with my chums.",
                        "One of them yaps and I say, \"ever tried my tonic\"?",
                        "So yeah, I come back to my crib, right?",
                        "And listen, I've never seen anybody down here, but you seem like a nice fellow.",
                        "So just bring me <a>8 Glowing Mushrooms<f> and I'll show you how to chum, alright buddy?").build(),
                DialogueSet.builder().key("wrong-items").lines("Come back with the right amount of items!").build(),
                DialogueSet.builder().key("taught").lines(
                        "Ok listen now, I practice a secret chumming technique.",
                        "It's reverse-chumming!",
                        "Instead of chumming, it's the sea creatures giving you <a>Chums<f>.",
                        "Listen, it's very easy.",
                        "First we trade a Chum Bucket, ok?",
                        "Just click me!").build(),
                DialogueSet.builder().key("click-me").lines("Just click me!").build(),
                DialogueSet.builder().key("after-bucket").lines(
                        "First you place down the bucket near water, right?",
                        "Then use a fishing rod and throw a line in said water.",
                        "Ok and you wait now. That part is easier with YOUR chums.",
                        "Listen, time is shorter with friends.",
                        "After waiting you kill a sea creature and get a <a>Chum<f>.",
                        "You put it in the bucket and someday it's filled.",
                        "Then listen you bring me the filled bucket, ok?").build(),
                DialogueSet.builder().key("full-bucket").lines(
                        "Wow ok listen that's good.",
                        "You are the chumming expert ok?",
                        "Listen. I also have some good items for your chumming.",
                        "And I have this really good tonic. It's a really good tonic, ok?",
                        "So get mushrooms, fill chum buckets, trade tonics! Easy chums!").build()
        ).toArray(DialogueSet[]::new);
    }
}
