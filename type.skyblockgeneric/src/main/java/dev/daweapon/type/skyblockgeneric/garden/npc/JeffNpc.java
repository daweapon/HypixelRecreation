package dev.daweapon.type.skyblockgeneric.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.GardenProgress;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.chip.ChipData;
import dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip;
import dev.daweapon.type.skyblockgeneric.garden.tool.FarmingTools;
import dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUIManageChips;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.garden.SkyBlockGarden;

import java.util.stream.Stream;

public class JeffNpc extends HypixelNPC {
    public static final int MIN_GARDEN_LEVEL = 4;
    private static final Pos POSITION = new Pos(1.5, 72, -28.5, 14, 0);

    public JeffNpc() {
        super(configuration());
    }

    private static HumanConfiguration configuration() {
        return new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Jeff", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return GardenNpcSkins.of("JEFF").signature();
            }

            @Override
            public String texture(HypixelPlayer player) {
                return GardenNpcSkins.of("JEFF").texture();
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
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                return player.getInstance() != null
                        && player.getInstance() == SkyBlockGarden.of(skyBlockPlayer).getGardenInstance()
                        && GardenProgress.level(Gardens.profile(skyBlockPlayer)) >= MIN_GARDEN_LEVEL;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<b>Jeff";
            }
        };
    }

    private static boolean anyChipUnlocked(ChipData chips) {
        for (GardenChip chip : GardenChip.values()) if (chips.unlocked(chip)) return true;
        return false;
    }

    private static boolean holdingFarmingTool(SkyBlockPlayer player) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        return !held.isNA() && FarmingTools.of(held.getAttributeHandler().getTypeAsString()) != null;
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        GardenProfile profile = Gardens.profile(player);
        ChipData chips = profile.chips();
        if (chips.jeffStage() < 2 && anyChipUnlocked(chips)) {
            chips.setJeffStage(3);
            Gardens.save(player, profile);
        }
        switch (chips.jeffStage()) {
            case 0 -> setDialogue(player, "first").thenRun(() -> advance(player, 1));
            case 1 -> {
                if (!holdingFarmingTool(player)) {
                    setDialogue(player, "show");
                    return;
                }
                setDialogue(player, "tool").thenRun(() -> {
                    player.addAndUpdateItem(new SkyBlockItem(ItemType.TUTORIAL_GARDEN_CHIP));
                    advance(player, 2);
                });
            }
            case 2 -> {
                if (!anyChipUnlocked(chips)) {
                    setDialogue(player, "tool");
                    return;
                }
                setDialogue(player, "after").thenRun(() -> advance(player, 3));
            }
            default -> player.openView(new GUIManageChips());
        }
    }

    private static void advance(SkyBlockPlayer player, int stage) {
        GardenProfile profile = Gardens.profile(player);
        if (profile.chips().jeffStage() < stage) profile.chips().setJeffStage(stage);
        Gardens.save(player, profile);
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder().key("first").lines(
                        "Hey there! My name's <b>Jeff<f>...",
                        "Have you picked up a <e>Specialized Farming Tool <f>yet?",
                        "If not, no worries! They each have their own unlock requirements, which you can see inside the <a>SkyMart Shop<f>.",
                        "These <e>Specialized Farming Tools <f>are a game changer! You can level them and upgrade to become even more powerful.",
                        "Farming using one of these tools also gives you <2>Sowdust<f>, which will be essential in upgrading your <9>Chips<f>!",
                        "Once you get your hands on one, swing by and see me and I'll teach you about <9>Chips<f>!"
                ).build(),
                DialogueSet.builder().key("show").lines(
                        "Show me a <e>Specialized Farming Tool <f>and I'll teach you about <9>Chips<f>!",
                        "You can get one from <a>SkyMart<f>."
                ).build(),
                DialogueSet.builder().key("tool").lines(
                        "Well, well! You got yourself a real <e>Farming Tool<f>, huh?",
                        "These don't just sit pretty, they grow stronger the more you use 'em!",
                        "Every crop you harvest gives your tool <b>experience<f>, leveling it up over time.",
                        "But here's the catch - once it hits <a>Level 15<f>, you'll need to upgrade it before it can keep growing. Same goes for <a>Level 30<f>.",
                        "Oh, and you might have noticed something new, <2>Sowdust<f>. You'll start earning it just by using your tool from now on.",
                        "This stuff is important because it lets you upgrade <9>Chips<f>, which will give you permanent buffs.",
                        "Here, take this, your first <9>Chip<f>. Go ahead and use the chip to unlock it."
                ).build(),
                DialogueSet.builder().key("after").lines(
                        "Nice! I've added the <9>Chip Menu <f>to your SkyBlock Menu while you are on the <a>Garden<f>, so you'll have easier access to them from now on.",
                        "You can now have as many <9>Chips <f>as you want, just keep upgrading them with <2>Sowdust<f>!",
                        "<9>Chips <f>can be found all over the place, and once you unlock them, you can start upgrading them.",
                        "And if you come across <c>duplicates <f>of the same <9>Chip<f>, don't worry, you'll be able to upgrade them to <5><l>EPIC <f>and <6><l>LEGENDARY <f>rarity, boosting their maximum level and effectiveness even further!"
                ).build()).toArray(DialogueSet[]::new);
    }
}
