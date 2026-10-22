package dev.daweapon.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.skyblockgeneric.gui.inventories.hunting.GUIAttributeFusion;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class NPCKysha extends HypixelNPC implements NPCAbiphoneTrait {
    public static final String CONTACT = "kysha";
    private static final List<String> IDLE_LINES = List.of("We are still discovering Shards every year!", "Fusions are infinite!");


    public NPCKysha() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Kysha", "<e><l>CLICK"};
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
                return Text.of("<b>Kysha");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-601.5, 115, -12, -180, 0);
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
        if (!HotfService.isUnlocked(player)) {
            player.sendMessage("<e>[NPC] <b>Kysha<f>: <7>{}<f>! Have you talked to <b>Hina <f>at the front of the island to get your hands on a <a>Heart of the Forest<f>?", player.getUsername());
            return;
        }
        if (!ChapterTriggers.done(player, "npc.kysha")) {
            setDialogue(player, "first-interaction").thenRun(() -> ChapterTriggers.fire(player, "npc.kysha"));
            return;
        }
        sendNPCMessage(player, IDLE_LINES.get(ThreadLocalRandom.current().nextInt(IDLE_LINES.size())));
        player.openView(new GUIAttributeFusion());
    }

    @Override
    public String getAbiphoneKey() {
        return CONTACT;
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction")
                        .line("Hi <7>{}<f>!", player.getUsername())
                        .line("Now, it's time to teach you the true purpose of <a>hunting<f>.")
                        .line("Every Shard you collect goes into your <a>Hunting Box<f>, located in the <a>Hunting Skill Menu<f>.")
                        .line("There are two things you can do with those Shards.")
                        .line("<f>You can either <c>syphon <f>or <5>fuse <f>them.")
                        .line("<f>Syphoning is key - it lets you permanently gain the <c>attribute <f>of the creature it came from.")
                        .line("Attributes are very much like <2>Pet <f>abilities in a way.")
                        .line("But hunting lets you harness those powers independently of the creature.")
                        .line("The <5>Precursors <f>built a machine that merges the genetic essence of two creatures.")
                        .line("We call this process <5>fusion<f>.")
                        .line("To fuse, combine <a>5 <f>Shards of one creature with <a>5 <f>Shards of another in the Fusion Machine.")
                        .line("The fusion process can uncover creatures that haven't been discovered yet!")
                        .build()
        ).toArray(DialogueSet[]::new);
    }
}
