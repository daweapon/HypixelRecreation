package net.swofty.type.skyblockgeneric.experiments;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.slayer.SlayerRequirement;
import net.swofty.type.skyblockgeneric.slayer.SlayerType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

public final class UltraRareBoost {
    public static final int ENDERMAN_SLAYER_LEVEL = 8;
    public static final double ENDERMAN_SLAYER_PERCENT = 15;

    private UltraRareBoost() {
    }

    public static double multiplier(SkyBlockPlayer player) {
        double percent = PetAbilities.value(player, ItemType.get("GUARDIAN_PET"), "Lucky Seven", 0)
                + AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("R15"));
        if (SlayerRequirement.level(player, SlayerType.VOIDGLOOM_SERAPH) >= ENDERMAN_SLAYER_LEVEL) {
            percent += ENDERMAN_SLAYER_PERCENT;
        }
        return 1 + percent / 100D;
    }
}
