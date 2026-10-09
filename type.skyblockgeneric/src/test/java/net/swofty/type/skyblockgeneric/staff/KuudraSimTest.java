package net.swofty.type.skyblockgeneric.staff;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraChest;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraChestService;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraChestStorage;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraSimTest {

    @BeforeAll
    static void init() throws IOException {
        MaxProfileTest.init();
    }

    @Test
    void aWonRunPaysProgressAndStoresOneOpenableChest() {
        SkyBlockPlayer player = MaxProfileTest.player();
        KuudraRun run = KuudraProgress.completeRun(player, KuudraTier.BASIC);

        assertEquals(1, KuudraProgress.clears(player, KuudraTier.BASIC));
        assertEquals(20, player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.DEFEAT_KUUDRA));
        assertEquals(1, KuudraChestStorage.runs(player).size());

        assertEquals(KuudraChestService.OpenResult.CANNOT_AFFORD, KuudraChestService.open(player, run.id(), KuudraChest.PAID));
        long essence = Essences.get(player, EssenceType.CRIMSON);
        assertEquals(KuudraChestService.OpenResult.OPENED, KuudraChestService.open(player, run.id(), KuudraChest.FREE));
        assertEquals(essence + run.free().getFirst().essence(), Essences.get(player, EssenceType.CRIMSON));
        assertEquals(KuudraChestService.OpenResult.OTHER_OPENED, KuudraChestService.open(player, run.id(), KuudraChest.PAID));
        assertEquals(KuudraChestService.OpenResult.ALREADY_OPENED, KuudraChestService.open(player, run.id(), KuudraChest.FREE));
    }

    @Test
    void thePaidChestTakesTheTiersKeyAndTheCollectionPaysOnce() {
        SkyBlockPlayer player = MaxProfileTest.player();
        KuudraRun run = null;
        for (int i = 0; i < 2; i++) run = KuudraProgress.completeRun(player, KuudraTier.INFERNAL);
        assertEquals(10, KuudraProgress.collectionPoints(player));
        assertEquals(10, player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.BOSS_COLLECTIONS));
        assertEquals(1, player.getAmountInInventory(ItemType.KUUDRA_PET));

        player.addAndUpdateItem(ItemType.KUUDRA_INFERNAL_TIER_KEY);
        assertEquals(KuudraChestService.OpenResult.OPENED, KuudraChestService.open(player, run.id(), KuudraChest.PAID));
        assertEquals(0, player.getAmountInInventory(ItemType.KUUDRA_INFERNAL_TIER_KEY));
        assertEquals(KuudraChest.PAID, KuudraChestStorage.find(player, run.id()).opened());

        KuudraProgress.completeRun(player, KuudraTier.BASIC);
        assertEquals(1, player.getAmountInInventory(ItemType.KUUDRA_PET));
    }
}
