package dev.daweapon.type.skyblockgeneric.hotf;

import java.util.List;

public record HotfTierReward(int tier, long experience, int tokens, int skyBlockXp, List<String> rewards) {
}
