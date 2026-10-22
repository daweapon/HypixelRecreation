package dev.daweapon.type.galatea.npcs;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.moonglade.CoralFishFamily;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class NPCCoral extends HypixelNPC {
    private static final String OPTIONS = "coral";
    private static final String MET = "npc.coral";

    public NPCCoral() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Coral", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "VVCsJ2fmOv6SXYbHEIlR0iOr+1O2tKqXtKyVkowX3xRuUWenOxly8M6ooyxON5e6UEbkKCg9qV0UOJCB0pbFSSvKJL17WMey9XG/VUzf1BQzrEcm2KANm1Ij/gZNBz53NkXsip8FDmYyaeutq8fvtZ6jFpsKxIpcjGCt2b9wXTszSjf7/3WRTDz0rBk0kncn3aLgdEi9El0KVhVBzTqxw07VcDMY5SXUyT5ZmB+HhVOODof9HcUKIuOEziteSkq2NdbwQBZlgtjDUVcAlClzfDyw6zZMetRLfRrIT3CXQNMacKsqGT7uOMuJSHp5dyOMLcDVHUFuYVYDQmwk8DFODFuBCZQbl9UTBTj5AcAlbGl4gBWEvJ9FcO0oIIox/PM0oPpIpuYHMyknB3mPsDD2u0Lue87bw4/tG65vGXyIEGUycALejg3+oMUpIeStxoVqkri96S+n9KhbakdnrtcW8S1hSeNuymrM+psh6JEUEbiirSJhDSQWyQUVOqZJNWxhE61bZx2KVbCb00RSu+OusO7SBWwRAQuddXjeGrptABSMPg8r0ztR5uAxl9kkAXVlDXNnXz6OcNH2RjwKXgppwHjNf5SFoT0KKMEm6t1nxWrBK4EMsedpQDJi8oD/S6kNFXFE5lZ15DbGljm6ik9tTLw73FIaZ2GrX4WyyMll5ws=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxOTUyNzQ2MjA3NSwKICAicHJvZmlsZUlkIiA6ICJmODJmNTQ1MDIzZDA0MTFkYmVlYzU4YWI4Y2JlMTNjNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZXNwb25kZW50cyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82MTMzMjhjMGE0NmY5N2YxZTdhZjg2YzQ1OTM0YWNhODcwMDg2MTI3MjFiNTFiM2VmZWFlNzVhNWY0MGM4MGZkIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<d>Coral");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-626.5, 99, -87.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    private record Step(String[] lines, String option) {
    }

    private static final List<Step> INTRO = List.of(
            new Step(new String[]{
                    "Have you ever heard tales of the <a>Fish Family<f>?",
                    "They're the curious group that trails behind <d>Shrimp<f>, you know?"}, "Vaguely."),
            new Step(new String[]{"I see. Would you like to delve deeper into their story?"}, "Yes."),
            new Step(new String[]{
                    "Hmm.",
                    "Just a moment, let me gather my thoughts.",
                    "Are you truly eager to uncover more about them?"}, "Get on with it."),
            new Step(new String[]{
                    "Very well.",
                    "Bring members of the <a>Fish Family <f>to me and I will <d>reward <f>you greatly!",
                    "I just like to look at them, you see."}, null));

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!ChapterTriggers.done(player, MET)) {
            intro(player, 0);
            return;
        }
        ItemType held = heldFish(player);
        if (held == null) {
            sendNPCMessage(player, "Bring members of the <a>Fish Family <f>to me and I will <d>reward <f>you greatly! <7>({} shown)", shown(player));
            return;
        }
        if (player.getHuntingData().counter(CoralFishFamily.shownKey(held.name())) > 0) {
            sendNPCMessage(player, "I already have that one in the collection. Do you have a different Fish?");
            return;
        }
        if (player.getHuntingData().counter(CoralFishFamily.SOULBOUND_KEY) == 0) {
            askSoulbound(player, held);
            return;
        }
        accept(player, held);
    }

    private void intro(SkyBlockPlayer player, int index) {
        if (isInDialogue(player)) return;
        setDialogue(player, "intro-" + index).thenRun(() -> {
            String option = INTRO.get(index).option();
            if (option == null) {
                ChapterTriggers.fire(player, MET);
                return;
            }
            NPCOption.sendOption(player, OPTIONS, true,
                    List.of(new NPCOption.Option("next", NamedTextColor.GREEN, false, option, chosen -> intro((SkyBlockPlayer) chosen, index + 1))));
        });
    }

    private void askSoulbound(SkyBlockPlayer player, ItemType held) {
        setDialogue(player, "soulbound").thenRun(() -> NPCOption.sendOption(player, OPTIONS + ".soulbound", true, List.of(
                new NPCOption.Option("yes", NamedTextColor.GREEN, true, "YES", chosen -> agree((SkyBlockPlayer) chosen, held)),
                new NPCOption.Option("no", NamedTextColor.RED, true, "NO", chosen -> setDialogue(chosen, "declined")))));
    }

    private void agree(SkyBlockPlayer player, ItemType held) {
        player.getHuntingData().setCounter(CoralFishFamily.SOULBOUND_KEY, 1);
        setDialogue(player, "agreed").thenRun(() -> {
            if (heldFish(player) == held) accept(player, held);
        });
    }

    private void accept(SkyBlockPlayer player, ItemType fish) {
        DatapointHunting.HuntingData data = player.getHuntingData();
        data.setCounter(CoralFishFamily.shownKey(fish.name()), 1);
        player.updateItem(PlayerItemOrigin.MAIN_HAND, item -> {
            if (item.getAttributeHandler().getSoulBoundData() == null) item.getAttributeHandler().setSoulBound(true);
        });
        int count = shown(player);
        long xp = CoralFishFamily.fishingXp(count);
        if (xp > 0) player.getSkills().increase(player, SkillCategories.FISHING, (double) xp);
        sendNPCMessage(player, "A new Fish! How delightful.");
        sendNPCMessage(player, "As a token of my appreciation, I've given you <7>{} <3>Fishing XP<f>! <7>({} shown)", String.format("%,d", xp), count);
        ChapterTriggers.fire(player, CoralFishFamily.TRIGGER);
    }

    private static int shown(SkyBlockPlayer player) {
        int count = 0;
        for (ItemType type : ItemType.values()) {
            if (CoralFishFamily.accepts(type) && player.getHuntingData().counter(CoralFishFamily.shownKey(type.name())) > 0) count++;
        }
        return count;
    }

    private static ItemType heldFish(SkyBlockPlayer player) {
        SkyBlockItem item = PlayerItemOrigin.getFromCache(player.getUuid()).get(PlayerItemOrigin.MAIN_HAND);
        if (item == null) return null;
        ItemType type = item.getAttributeHandler().getPotentialType();
        return CoralFishFamily.accepts(type) ? type : null;
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        java.util.List<DialogueSet> sets = new java.util.ArrayList<>();
        for (int index = 0; index < INTRO.size(); index++) {
            sets.add(DialogueSet.builder().key("intro-" + index).lines(INTRO.get(index).lines()).build());
        }
        sets.add(DialogueSet.builder().key("soulbound").lines(
                "You found a new Fish!",
                "Great. Let's add it to the collection! The Fish Family is coming together swimmingly!",
                "But hold on... for that I would need to make your Fish <d>Co-op Soulbound<f>.",
                "Which means you will not be able to trade it anymore.",
                "Are you alright with that?").build());
        sets.add(DialogueSet.builder().key("declined").lines("That's too bad. Come back when you are sure.", "That was a lovely fish...").build());
        sets.add(DialogueSet.builder().key("agreed").lines("Perfect, in that case I won't ask you that question again.").build());
        return sets.toArray(DialogueSet[]::new);
    }
}
