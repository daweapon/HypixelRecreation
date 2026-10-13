package net.swofty.type.skyblockgeneric.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
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

import java.util.stream.Stream;

public class PesthunterNpc extends HypixelNPC {
    public enum Kind {
        PHILLIP("Pesthunter Phillip", "PESTHUNTER_PHILLIP", new Pos(9.5, 71, -17.5, 90, 0)),
        PAMELA("Pesthunter Pamela", "PESTHUNTER_PAMELA", new Pos(-17.5, 71, -9.5, -90, 0));

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

    public PesthunterNpc(Kind kind) {
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
                return GardenGround.onGround(player.getInstance(), kind.position);
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

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        PestData data = Gardens.profile(player).pests();
        if (kind == Kind.PHILLIP) {
            if (!data.isMetPhillip()) {
                setDialogue(player, "first").thenRun(() -> {
                    data.setMetPhillip(true);
                    Gardens.save(player, Gardens.profile(player));
                    giveOnce(player, ItemType.SKYMART_VACUUM);
                });
                return;
            }
            player.openView(new GUIPesthunter());
        } else {
            if (!data.isMetPamela()) {
                setDialogue(player, "first").thenRun(() -> {
                    data.setMetPamela(true);
                    Gardens.save(player, Gardens.profile(player));
                    giveOnce(player, ItemType.SPRAYONATOR);
                });
                return;
            }
            setDialogue(player, "again");
        }
    }

    private static void giveOnce(SkyBlockPlayer player, ItemType type) {
        if (player.getAmountInInventory(type) == 0) player.addAndUpdateItem(new SkyBlockItem(type));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        if (kind == Kind.PHILLIP) {
            return Stream.of(
                    DialogueSet.builder().key("first").lines(
                            "Well hello there! I'm Phillip, a <2>Pesthunter<f>.",
                            "Whenever you break crops in your Garden, there is a chance that nasty <2>Pests <f>show up and eat away at your <6>Farming Fortune<f>!",
                            "Take this <a>SkyMart Vacuum<f>. <e>Hold right-click <f>to suck them up!",
                            "Bring me the <2>Pests <f>you collect and I'll trade you some temporary <6>Farming Fortune <f>or some of my wares!"
                    ).build()).toArray(DialogueSet[]::new);
        }
        return Stream.of(
                DialogueSet.builder().key("first").lines(
                        "Hello! I'm Pamela, Phillip's sister.",
                        "Some <2>Pests <f>are drawn to certain materials. With this <a>Sprayonator <f>you can spray a plot to attract them!",
                        "<e>Left-click <f>to pick a material and <e>right-click <f>to spray the plot you are standing in. Happy hunting!"
                ).build(),
                DialogueSet.builder().key("again").lines(
                        "Spray a plot with the <a>Sprayonator <f>to make the <2>Pests <f>you want more likely to show up!"
                ).build()).toArray(DialogueSet[]::new);
    }

    public static String tradeLine() {
        return "<7>Trade <2>Pests <7>for <6>" + PestRules.FORTUNE_PER_TRADED_PEST + " Farming Fortune <7>each.";
    }
}
