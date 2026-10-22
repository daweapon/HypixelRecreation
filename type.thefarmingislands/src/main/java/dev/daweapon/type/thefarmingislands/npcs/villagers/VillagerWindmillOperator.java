package dev.daweapon.type.thefarmingislands.npcs.villagers;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.gui.GUIShopFarmMerchant;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;

import java.util.List;

public class VillagerWindmillOperator extends HypixelNPC {
    private static final String PULLEY = "npc.windmill_operator.pulley";
    private static final String BASE = "npc.windmill_operator.base";
    private static final int REQUIRED = 64;

    public VillagerWindmillOperator() {
        super(new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Windmill Operator", "<e><l>CLICK"};
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<e>Windmill Operator";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(102.5, 82, -282.5, 90, 0);
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
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("pulley").lines(
                        "This windmill has been out of order for a long time.",
                        "The pulley snapped. Bring me <a>64 String <f>and I can fix it, hold it in your hand when you click me."
                ).build(),
                DialogueSet.builder().key("base").lines(
                        "That will do nicely, the pulley works again!",
                        "The base is crumbling too. Bring me <a>64 Cobblestone <f>to patch it up."
                ).build(),
                DialogueSet.builder().key("done").lines(
                        "The windmill turns again, thanks to you!",
                        "I have plenty of farming supplies stored here, take a look."
                ).build()
        ).toArray(DialogueSet[]::new);
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        if (ChapterTriggers.done(player, BASE)) {
            player.openView(new GUIShopFarmMerchant());
            return;
        }
        boolean pulleyFixed = ChapterTriggers.done(player, PULLEY);
        ItemType needed = pulleyFixed ? ItemType.COBBLESTONE : ItemType.STRING;
        ItemType held = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType();

        if (held == null || held == ItemType.AIR) {
            setDialogue(player, pulleyFixed ? "base" : "pulley");
            return;
        }
        if (held != needed || player.getAmountAnywhere(needed) < REQUIRED) {
            sendNPCMessage(player, "<f>This isn't what I need!");
            return;
        }
        if (!player.removeItemFromPlayer(needed, REQUIRED)) return;

        if (pulleyFixed) {
            setDialogue(player, "done").thenRun(() -> {
                ChapterTriggers.fire(player, BASE);
                player.openView(new GUIShopFarmMerchant());
            });
        } else {
            ChapterTriggers.fire(player, PULLEY);
            setDialogue(player, "base");
        }
    }
}
