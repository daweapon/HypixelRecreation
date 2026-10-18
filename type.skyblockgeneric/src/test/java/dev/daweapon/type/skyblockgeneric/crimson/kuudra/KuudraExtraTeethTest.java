package dev.daweapon.type.skyblockgeneric.crimson.kuudra;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraReward;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraRewards;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KuudraExtraTeethTest {
    @Test
    void extraTeethAddsOneToTheTeethStack() {
        List<KuudraReward> paid = List.of(KuudraReward.essence(100), KuudraReward.item(ItemType.KUUDRA_TEETH.name(), 3));
        List<KuudraReward> result = KuudraRewards.withExtraTeeth(paid);
        assertEquals(2, result.size());
        assertEquals(4, result.get(1).amount());
    }
}
