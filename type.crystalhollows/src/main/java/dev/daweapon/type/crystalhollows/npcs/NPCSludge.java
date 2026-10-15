package dev.daweapon.type.crystalhollows.npcs;

import dev.daweapon.type.crystalhollows.quest.CrystalHollowsProgress;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.commission.CommissionProgress;
import dev.daweapon.type.skyblockgeneric.crystalhollows.TreasureLoot;
import dev.daweapon.type.skyblockgeneric.crystalhollows.TreasurePool;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class NPCSludge extends AbstractStructureNPC {
    public static final long GEMSTONE_POWDER_REWARD = 2_500;
    private static final String[] THANKS = {
            "Thank you so much!",
            "Maybe I feel such an intense connection with my other half because, it was literally my other half?"
    };

    public NPCSludge() {
        super("Sludge", "<e>", SLUDGE_TEXTURE, "sludge_cage", "sludge");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        UUID id = player.getUuid();
        if (CrystalHollowsProgress.has(id, CrystalHollowsProgress.SLUDGE_FREED)) {
            sendNPCMessage(player, THANKS[ThreadLocalRandom.current().nextInt(THANKS.length)]);
            return;
        }
        int hits = CrystalHollowsProgress.hitSludge(id);
        if (hits == 1) {
            setDialogue(player, "stuck");
            return;
        }
        if (hits < CrystalHollowsProgress.SLUDGE_HITS_TO_FREE) {
            return;
        }
        CrystalHollowsProgress.mark(id, CrystalHollowsProgress.SLUDGE_FREED);
        setDialogue(player, "freed").thenRun(() -> reward(player));
    }

    private void reward(SkyBlockPlayer player) {
        long powder = HotmService.gainPowder(player, HotmPowder.GEMSTONE, GEMSTONE_POWDER_REWARD);
        player.sendMessage("<d>You received <a>" + powder + " <d>Gemstone Powder!");
        TreasureLoot.open(player, TreasurePool.SLUDGE, ThreadLocalRandom.current(), ThreadLocalRandom.current());
        CommissionProgress.recordChestOpened(player);
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("stuck").lines(
                        "Hey you! Help me I'm stuck!",
                        "I fell asleep and when I woke up I was too large to fit the entrance!",
                        "Maybe you can punch me out."
                ).build(),
                DialogueSet.builder().key("freed").lines(
                        "Wow I wasn't expecting that!",
                        "Thank you so much, I stayed there for weeks!",
                        "You freed me, and helped me find the love of my life!",
                        "Take this offering in return!"
                ).build()
        };
    }
}
