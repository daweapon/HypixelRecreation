package dev.daweapon.type.thefarmingislands.npcs.villagers;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.farmhand.FarmhandQuest;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.MissionCraftWheatMinion;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.MissionFarmhandAddPet;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.MissionFarmhandSummonPet;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.MissionReachFarmingFive;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.MissionTalkToFarmHand;
import dev.daweapon.type.skyblockgeneric.mission.missions.barn.MissionTalkToFarmhandAgain;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VillagerFarmHand extends HypixelNPC {
    private static final List<String> HINTS = List.of(
            "Did you know that chickens drop eggs? I never seem to find any of them though.",
            "Cows can be milked, but I prefer to leave them alone.",
            "The farm animals here provide a variety of resources.",
            "Collect wheat, carrots, potatoes, pumpkins and melon slices to add to your Collection and unlock cool rewards!",
            "Those <d>Fairy Souls <f>scare off the animals.",
            "There is a <d>Fairy Soul <f>hiding somewhere around the Barn, if you look carefully."
    );
    private static final List<String> BEFORE_DESERT = List.of(
            "Have you been to the <e>Mushroom Desert<f>? The entrance is on the northern hill, past the terraces.",
            "You will need <a>Farming V <f>before the <e>Mushroom Desert <f>lets you in."
    );
    private static final List<String> AFTER_DESERT = List.of(
            "Farming V already? The <e>Mushroom Desert <f>on the northern hill is open to you, give it a visit!",
            "I hear the <e>Mushroom Desert <f>is the only public island with Sand and Cactus collections."
    );

    public VillagerFarmHand() {
        super(new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Farmhand", "<e><l>CLICK"};
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<e>Farmhand";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(144.5, 73, -240.5, -35f, 0f);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.NONE;
            }
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder()
                        .key("quest-hello").lines(
                                "Welcome to the Barn!",
                                "Feel free to harvest any crops on the island, I'm taking a break.",
                                "Did you craft a <a>Wheat Minion <f>yet? Here's the recipe!"
                        ).build(),
                DialogueSet.builder()
                        .key("spoke-again").lines(
                                "Nice! A <a>Wheat Minion I<f>, that will gather Wheat for you while you do other things.",
                                "Every farmer needs a companion, so take this <a>Chicken Pet<f>.",
                                "Right-click it to add it to your <a>Pets menu<f>."
                        ).build(),
                DialogueSet.builder()
                        .key("pet-added").lines(
                                "It is in your Pets menu now. Open the menu and click the <a>Chicken Pet <f>to summon it.",
                                "A summoned pet earns experience while you do what it loves. Chicken loves <a>Farming<f>!"
                        ).build(),
                DialogueSet.builder()
                        .key("pet-summoned").lines(
                                "Looking good! Your Chicken will level up as you farm.",
                                "Now collect some crops and animal drops here in The Barn to unlock Collections.",
                                "Start with <a>Potatoes<f>, <a>Carrots<f>, <a>Melon Slices <f>and <a>Pumpkins<f>, then <a>Leather<f>, <a>Raw Chicken <f>and <a>Raw Porkchop<f>."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        MissionData data = player.getMissionData();

        if (data.isCurrentlyActive(MissionTalkToFarmHand.class)) {
            setDialogue(player, "quest-hello").thenRun(() -> data.endMission(MissionTalkToFarmHand.class));
            return;
        }
        if (!data.hasCompleted(MissionCraftWheatMinion.class)) {
            sendNPCMessage(player, "<f>Craft a <a>Wheat Minion<f>!");
            return;
        }
        if (data.isCurrentlyActive(MissionTalkToFarmhandAgain.class)) {
            setDialogue(player, "spoke-again").thenRun(() -> data.endMission(MissionTalkToFarmhandAgain.class));
            return;
        }
        if (data.hasCompleted(MissionTalkToFarmhandAgain.class) && !FarmhandQuest.petGiven(player)) {
            FarmhandQuest.giveChickenPet(player);
            FarmhandQuest.startPetLessons(player);
            setDialogue(player, "spoke-again");
            return;
        }
        if (data.isCurrentlyActive(MissionFarmhandAddPet.class)) {
            if (ownsChickenPet(player)) {
                setDialogue(player, "pet-added").thenRun(() -> data.endMission(MissionFarmhandAddPet.class));
            } else {
                sendNPCMessage(player, "<f>Right-click your <a>Chicken Pet <f>to add it to your Pets menu!");
            }
            return;
        }
        if (data.isCurrentlyActive(MissionFarmhandSummonPet.class)) {
            if (chickenSummoned(player)) {
                setDialogue(player, "pet-summoned").thenRun(() -> data.endMission(MissionFarmhandSummonPet.class));
            } else {
                sendNPCMessage(player, "<f>Open your Pets menu and click the <a>Chicken Pet <f>to summon it!");
            }
            return;
        }
        sendNPCMessage(player, "<f>" + randomHint(player));
    }

    private static boolean ownsChickenPet(SkyBlockPlayer player) {
        return player.getPetData().getPet(ItemType.CHICKEN_PET) != null;
    }

    private static boolean chickenSummoned(SkyBlockPlayer player) {
        SkyBlockItem enabled = player.getPetData().getEnabledPet();
        return enabled != null && enabled.getAttributeHandler().getPotentialType() == ItemType.CHICKEN_PET;
    }

    private static String randomHint(SkyBlockPlayer player) {
        List<String> pool = new java.util.ArrayList<>(HINTS);
        pool.addAll(MissionReachFarmingFive.reached(player) ? AFTER_DESERT : BEFORE_DESERT);
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }
}
