package net.swofty.type.kuudra.run;

import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraPhaseTest {
    private static final KuudraDialogue DIALOGUE = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra")).dialogue();

    @Test
    void everyPhaseLineExistsInTheDialogueFile() {
        for (KuudraPhase phase : KuudraPhase.values()) {
            for (boolean stun : new boolean[]{false, true}) {
                String key = phase.dialogueKey(stun);
                if (key != null) assertDoesNotThrow(() -> DIALOGUE.lines(key), phase + " " + key);
            }
        }
        for (String key : List.of("idle", "start_first_time", "start_veterans", "defeated")) {
            assertDoesNotThrow(() -> DIALOGUE.lines(key), key);
        }
        assertEquals("ballista_ready_stun", KuudraPhase.FUEL.dialogueKey(true));
        assertEquals("ballista_ready", KuudraPhase.FUEL.dialogueKey(false));
    }

    @Test
    void stunAndLairOnlyExistOnTheirTiers() {
        assertFalse(KuudraPhase.STUN.availableIn(false, false));
        assertTrue(KuudraPhase.STUN.availableIn(true, false));
        assertFalse(KuudraPhase.LAIR.availableIn(true, false));
        assertTrue(KuudraPhase.LAIR.availableIn(true, true));
        assertTrue(KuudraPhase.BUILD.availableIn(false, false));
    }

    @Test
    void objectivesUseHypixelsScoreboardWording() {
        assertEquals(List.of("<e>Rescue supplies"), KuudraPhase.SUPPLIES.objective(false));
        assertEquals(List.of("<e>Protect Elle"), KuudraPhase.BUILD.objective(true));
        assertEquals(List.of("<e>Figure out how to", "<e>defeat Kuudra"), KuudraPhase.FUEL.objective(true));
        assertEquals(List.of("<e>Charge Ballista or", "<e>enter Kuudra"), KuudraPhase.STUN.objective(true));
        assertEquals(List.of("<e>Shoot Ballista at", "<e>Kuudra!"), KuudraPhase.DPS.objective(true));
        assertEquals(List.of(), KuudraPhase.LAIR.objective(true));
        assertEquals(List.of("<e>Defeat Kuudra"), KuudraPhase.DPS.objective(false));
    }
}
