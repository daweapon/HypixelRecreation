package dev.daweapon.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCLoneAdventurer extends AbstractLoneAdventurer {
    public NPCLoneAdventurer() {
        super(new Pos(-524.5, 101, -275.5, 0, 0), LoneAdventurerStage::atFirstStand);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        switch (LoneAdventurerProgress.stage(player)) {
            case NOT_STARTED -> setDialogue(player, "intro")
                    .thenRun(() -> LoneAdventurerProgress.setStage(player, LoneAdventurerStage.KILLING_ENDERMEN));
            case KILLING_ENDERMEN -> {
                int killed = LoneAdventurerProgress.endermenKilled(player);
                if (killed < LoneAdventurerStage.ENDERMEN_REQUIRED) {
                    sendNPCMessage(player, "Come back once you have slain <5>{} <f>Endermen. You are at <a>{}<f>.",
                            LoneAdventurerStage.ENDERMEN_REQUIRED, killed);
                    return;
                }
                setDialogue(player, "offer").thenRun(() -> {
                    LoneAdventurerProgress.setStage(player, LoneAdventurerStage.ENDER_ARMOR);
                    player.openView(new CatalogueShopView("lone_adventurer"));
                });
            }
            default -> { }
        }
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "Few travellers make it out here, and fewer still can fight.",
                        "Prove yourself. Slay <5>5 Endermen<f> and come back to me."
                ).build(),
                DialogueSet.builder().key("offer").lines(
                        "Impressive! You handle yourself well.",
                        "I have a <5>Void Sword <f>I no longer need. I will part with it for <6>200,000 coins<f>.",
                        "If you don't want it, Rusty in the <6>Gold Mine <f>sells lost <5>Void Swords<f>.",
                        "I am heading to the <5>Dragon's Nest<f>. Find me there if you want to hear about the dragons."
                ).build()
        };
    }
}
