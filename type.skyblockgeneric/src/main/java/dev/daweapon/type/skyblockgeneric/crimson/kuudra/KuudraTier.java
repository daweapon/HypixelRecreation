package dev.daweapon.type.skyblockgeneric.crimson.kuudra;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * The five Kuudra tiers with their keys, the reputation that gates them (with either faction) and
 * the Emissary key price. Key materials follow the 2024-11 reduction (2/4/16/40/80).
 */
@Getter
public enum KuudraTier {
    BASIC("Basic", "KUUDRA_TIER_KEY", "Kuudra Key", 0, 200_000, 2, 20,
            "bfd3e71838c0e76f890213120b4ce7449577736604338a8d28b4c86db2547e71"),
    HOT("Hot", "KUUDRA_HOT_TIER_KEY", "Hot Kuudra Key", 1_000, 400_000, 4, 40,
            "c0259e8964c3deb95b1233bb2dc82c986177e63ae36c11265cb385180bb91cc0"),
    BURNING("Burning", "KUUDRA_BURNING_TIER_KEY", "Burning Kuudra Key", 3_000, 750_000, 16, 60,
            "330f6f6e63b245f839e3ccdce5a5f22056201d0274411dfe5d94bbe449c4ece"),
    FIERY("Fiery", "KUUDRA_FIERY_TIER_KEY", "Fiery Kuudra Key", 7_000, 1_500_000, 40, 80,
            "bd854393bbf9444542502582d4b5a23cc73896506e2fc739d545bc35bc7b1c06"),
    INFERNAL("Infernal", "KUUDRA_INFERNAL_TIER_KEY", "Infernal Kuudra Key", 12_000, 3_000_000, 80, 100,
            "82ee25414aa7efb4a2b4901c6e33e5eaa705a6ab212ebebfd6a4de984125c7a0"),
    ;

    public static final int KEY_NETHER_STARS = 2;

    private final String displayName;
    private final String keyItemId;
    private final String keyName;
    private final int requiredReputation;
    private final int keyCoins;
    private final int keyMaterials;
    private final int firstClearSkyBlockXP;
    private final String keyTexture;

    KuudraTier(String displayName, String keyItemId, String keyName, int requiredReputation, int keyCoins,
               int keyMaterials, int firstClearSkyBlockXP, String keyTexture) {
        this.displayName = displayName;
        this.keyItemId = keyItemId;
        this.keyName = keyName;
        this.requiredReputation = requiredReputation;
        this.keyCoins = keyCoins;
        this.keyMaterials = keyMaterials;
        this.firstClearSkyBlockXP = firstClearSkyBlockXP;
        this.keyTexture = keyTexture;
    }

    public @Nullable KuudraTier previous() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }
}
