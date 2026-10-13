package net.swofty.type.skyblockgeneric.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.quest.GardenQuests;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.garden.*;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;

import java.util.stream.Stream;

public class NPCSam extends HypixelNPC {
    public static final int WHEAT_FOR_COMPOST = 25;
    public static final int BREAD_WHEAT = 100;
    private static final Pos HOUSE = new Pos(-2.5, 71, -12.5, 0, 0);
    private static final Pos STAND = new Pos(4.5, 71, -9.5, 0, 0);
    private static final Pos COMPOSTER = new Pos(-9.5, 71, -26.5, 90, 0);
    private static NPCSam instance;

    private NPCSam() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Sam", "<e><l>CLICK"};
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
                return GardenGround.onGround(player.getInstance(), standsAtComposter((SkyBlockPlayer) player) ? COMPOSTER
                        : standsAtStand((SkyBlockPlayer) player) ? STAND : HOUSE);
            }

            @Override
            public Instance instance(HypixelPlayer player) {
                return player.getInstance();
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                return player.getInstance() != null
                        && player.getInstance() == SkyBlockGarden.of(skyBlockPlayer).getGardenInstance();
            }

            @Override
            public boolean dynamicPosition() {
                return true;
            }

            @Override
            public boolean walksToNewPosition() {
                return true;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    public static synchronized NPCSam get() {
        if (instance == null) instance = new NPCSam();
        return instance;
    }

    private static boolean standsAtStand(SkyBlockPlayer player) {
        MissionData data = player.getMissionData();
        return data.isCurrentlyActive(MissionSetUpStand.class) || data.isCurrentlyActive(MissionServeFirstCustomer.class);
    }

    private static boolean standsAtComposter(SkyBlockPlayer player) {
        MissionData data = player.getMissionData();
        return data.isCurrentlyActive(MissionTalkToSamAtComposter.class)
                || data.isCurrentlyActive(MissionPurchaseFuel.class)
                || data.isCurrentlyActive(MissionRefuelComposter.class)
                || data.isCurrentlyActive(MissionTakeCompost.class);
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        MissionData data = player.getMissionData();
        data.setSkyBlockPlayer(player);

        if (data.isCurrentlyActive(MissionTalkToSam.class)) {
            setDialogue(player, "welcome").thenRun(() -> GardenQuests.end(player, MissionTalkToSam.class));
        } else if (data.isCurrentlyActive(MissionBringWheatToSam.class)) {
            if (player.getAmountInInventory(ItemType.WHEAT) < WHEAT_FOR_COMPOST) {
                player.sendMessage("<c>Sam needs " + WHEAT_FOR_COMPOST + " Wheat. Come back when you have it!");
                return;
            }
            player.removeItemFromPlayer(ItemType.WHEAT, WHEAT_FOR_COMPOST);
            setDialogue(player, "wheat_delivered").thenRun(() -> GardenQuests.end(player, MissionBringWheatToSam.class));
        } else if (data.isCurrentlyActive(MissionTalkToSamAtComposter.class)) {
            setDialogue(player, "composter_intro").thenRun(() -> GardenQuests.end(player, MissionTalkToSamAtComposter.class));
        } else if (data.isCurrentlyActive(MissionPurchaseFuel.class)) {
            setDialogue(player, "buy_fuel");
        } else if (data.isCurrentlyActive(MissionRefuelComposter.class)) {
            setDialogue(player, "add_fuel");
        } else if (data.isCurrentlyActive(MissionTakeCompost.class)) {
            setDialogue(player, "take_compost");
        } else if (data.isCurrentlyActive(MissionPurchasePlot.class)) {
            setDialogue(player, "buy_plot");
        } else if (data.isCurrentlyActive(MissionCleanPlot.class)) {
            setDialogue(player, "clean_plot");
        } else if (data.isCurrentlyActive(MissionTalkToSamPlotCleaned.class)) {
            setDialogue(player, "plot_cleaned").thenRun(() -> GardenQuests.end(player, MissionTalkToSamPlotCleaned.class));
        } else if (data.isCurrentlyActive(MissionBringBreadWheat.class)) {
            if (player.getAmountInInventory(ItemType.WHEAT) < BREAD_WHEAT) {
                setDialogue(player, "bread_ask");
                return;
            }
            player.removeItemFromPlayer(ItemType.WHEAT, BREAD_WHEAT);
            setDialogue(player, "bread_done").thenRun(() -> GardenQuests.end(player, MissionBringBreadWheat.class));
        } else if (data.isCurrentlyActive(MissionSetUpStand.class)) {
            setDialogue(player, "stand_here").thenRun(() -> {
                GardenQuests.end(player, MissionSetUpStand.class);
                GardenQuests.firstCustomer(player);
            });
        } else if (data.isCurrentlyActive(MissionServeFirstCustomer.class)) {
            setDialogue(player, "first_customer");
        } else {
            setDialogue(player, "idle");
        }
    }

    public void firstCustomerServed(SkyBlockPlayer player) {
        setDialogue(player, "first_customer_done");
    }

    public void plotUnlocked(SkyBlockPlayer player) {
        setDialogue(player, "plot_unlocked").thenRun(() -> {
            if (player.getAmountInInventory(ItemType.SAM_SCYTHE) == 0) {
                player.addAndUpdateItem(new SkyBlockItem(ItemType.SAM_SCYTHE));
            }
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder().key("welcome").lines(
                        "I am so excited to have you here!",
                        "You can come to me anytime you need help, but first let's clear out a plot to grow your crops.",
                        "For that however... I need some <a>Compost<f>... If you can bring me some <e>Wheat<f>, I can make some Compost out of it!"
                ).build(),
                DialogueSet.builder().key("wheat_delivered").lines(
                        "Wow, you did it!",
                        "Now I can finally produce some Compost... let's head inside!"
                ).build(),
                DialogueSet.builder().key("composter_intro").lines(
                        "You see this machine here...",
                        "It produces <a>Compost<f> when you put crops inside of it... somehow... but that's what I need to increase the <b>amount of plots<f> we can have! So exciting!",
                        "It needs machine fuel to operate... luckily we can order fuel online!",
                        "Head over to the desk, there you can browse <a>SkyMart<f> to order some fuel."
                ).build(),
                DialogueSet.builder().key("buy_fuel").lines(
                        "Head over to the desk, there you can browse <a>SkyMart<f> to order some fuel."
                ).build(),
                DialogueSet.builder().key("add_fuel").lines(
                        "Great! Now, you can go open the composter and put the fuel in there, and it'll start doing... whatever it does!"
                ).build(),
                DialogueSet.builder().key("take_compost").lines(
                        "Oh the Composter is done!",
                        "We can finally free up a plot! Take out the compost when you are ready!"
                ).build(),
                DialogueSet.builder().key("buy_plot").lines(
                        "Use the compost at the desk to unlock a new plot!"
                ).build(),
                DialogueSet.builder().key("plot_unlocked").lines(
                        "Amazing!!! We got ourselves a brand new plot!",
                        "Now to the dirty part... the plot has to be cleaned up before it can be used...",
                        "Head over to your unlocked plot and try to clean it up a bit and then come back to me once you've done so!",
                        "Here, take my scythe. It will help you clean your plot even faster!"
                ).build(),
                DialogueSet.builder().key("clean_plot").lines(
                        "Head over to your unlocked plot and try to clean it up a bit and then come back to me once you've done so!"
                ).build(),
                DialogueSet.builder().key("plot_cleaned").lines(
                        "Wow! It's squeaky clean over there, you did so well!",
                        "Now you can grow crops you've unlocked there!",
                        "To save you a bit of time, you can also use a preset farm at your desk!"
                ).build(),
                DialogueSet.builder().key("bread_ask").lines(
                        "I have an idea! Why don't you collect me some <e>Wheat<f> and I'll bake us some lovely bread for dinner!",
                        "To bake us some bread, I think I need about <e>100 Wheat<f>.",
                        "That shouldn't be a problem for someone like you, right?"
                ).build(),
                DialogueSet.builder().key("bread_done").lines(
                        "Wow, that was quick! Here, take some fresh bread! *nom* *nom*",
                        "It's pretty tasty. But it seems like we still have some dozen Wheat left over and our Composter is already full...",
                        "Oh, I have an idea! We could open a small stand and sell our Wheat to other people!",
                        "We need to set up a stand somewhere..."
                ).build(),
                DialogueSet.builder().key("stand_here").lines(
                        "I think this spot would work perfectly!",
                        "Oh, we already have our first customer! How exciting, I wonder what they want! Can you check on them?"
                ).build(),
                DialogueSet.builder().key("first_customer").lines(
                        "Your first customer is right in front of you!",
                        "Go ahead and talk to them to see what they need!"
                ).build(),
                DialogueSet.builder().key("first_customer_done").lines(
                        "Wow we did it! We served our first customer!",
                        "Keep checking back on them, I am sure more people will visit our farm soon.",
                        "Besides that, you can always go check the desk to find out what to do next!"
                ).build(),
                DialogueSet.builder().key("idle").lines(
                        "You can always go check the desk to find out what to do next!"
                ).build()
        ).toArray(DialogueSet[]::new);
    }
}
