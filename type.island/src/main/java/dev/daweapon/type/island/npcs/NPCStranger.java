package dev.daweapon.type.island.npcs;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.npc.GardenNpcSkin;
import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuests;
import dev.daweapon.type.skyblockgeneric.mission.missions.garden.MissionTalkToSam;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class NPCStranger extends HypixelNPC {
    public static final int REQUIRED_SKYBLOCK_LEVEL = 5;
    public static final Pos POSITION = new Pos(8.5, 100, 41.5, 180, 0);
    private static final Set<UUID> introduced = ConcurrentHashMap.newKeySet();

    public NPCStranger() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Stranger", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return GardenNpcSkin.SIGNATURE;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return GardenNpcSkin.TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return POSITION;
            }

            @Override
            public Instance instance(HypixelPlayer player) {
                return player.getInstance();
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return isEligible((SkyBlockPlayer) player) && inOwnIsland((SkyBlockPlayer) player);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return introduced.contains(player.getUuid()) ? "<b>Sam" : "<b>Stranger";
            }
        });
    }

    public static boolean isEligible(SkyBlockPlayer player) {
        return player.getSkyBlockExperience().getLevel().asInt() >= REQUIRED_SKYBLOCK_LEVEL
                && !Gardens.profile(player).isAccessUnlocked();
    }

    private static boolean inOwnIsland(SkyBlockPlayer player) {
        return player.getSkyBlockIsland() != null
                && player.getInstance() != null
                && player.getInstance() == player.getSkyBlockIsland().getIslandInstance();
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        introduced.remove(player.getUuid());
        setDialogue(player, "greeting").thenRun(() -> {
            introduced.add(player.getUuid());
            setDialogue(player, "request").thenRun(() -> askForHelp(player));
        });
    }

    private void askForHelp(SkyBlockPlayer player) {
        player.sendMessage("<e>Help her out?");
        NPCOption.sendOption(player, "garden_stranger", false, List.of(
                new NPCOption.Option("yes", NamedTextColor.GREEN, true, "YES", _ -> accept(player)),
                new NPCOption.Option("no", NamedTextColor.RED, true, "NO", _ -> declined(player))));
    }

    private void declined(SkyBlockPlayer player) {
        sendNPCMessage(player, "Oh... ok then..");
        introduced.remove(player.getUuid());
    }

    private void accept(SkyBlockPlayer player) {
        if (!isEligible(player)) return;
        setDialogue(player, "accepted").thenRun(() -> {
            GardenProfile profile = Gardens.profile(player);
            profile.setAccessUnlocked(true);
            Gardens.save(player, profile);
            introduced.remove(player.getUuid());
            GardenQuests.start(player, MissionTalkToSam.class);
            player.sendTo(ServerType.SKYBLOCK_THE_GARDEN);
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder().key("greeting").lines(
                        "What a pleasant surprise! I see you have quite an impressive island."
                ).build(),
                DialogueSet.builder().key("request").lines(
                        "Excuse my manners, my name is Sam and I live next door... or should I rather say next \"island\".",
                        "You seem to have quite a talent for growing crops... which is something I couldn't really say about myself...",
                        "You see, I've been having quite the struggle with my farm that I inherited from my father.",
                        "He was a very skillful farmer... not like me... I have two left thumbs when it comes to farming.",
                        "I was wondering if maybe you could... help me out... with my farm."
                ).build(),
                DialogueSet.builder().key("accepted").lines(
                        "Wow!! Thank you so much. I'll bring you right there."
                ).build()
        ).toArray(DialogueSet[]::new);
    }
}
