package net.swofty.type.skyblockgeneric.hotm;

import java.util.List;

public record HotmTierReward(int tier, int tokens, int forgeSlots, int skyBlockXp, List<String> rewards) {
}
