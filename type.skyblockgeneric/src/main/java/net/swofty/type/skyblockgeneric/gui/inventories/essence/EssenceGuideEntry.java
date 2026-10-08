package net.swofty.type.skyblockgeneric.gui.inventories.essence;

import net.swofty.type.skyblockgeneric.essence.EssenceType;
import org.jetbrains.annotations.Nullable;

// Icon, colour, flavour text and Essence Guide slot per essence, from the wiki's Essence Guide/UI
// and the NEU repo's ESSENCE_* heads. The guide has no Sun Gecko button or flavour text, so it
// takes the free slot between Forest and Fossil.
record EssenceGuideEntry(EssenceType type, String texture, String colour, @Nullable String flavour, int slot) {

    static EssenceGuideEntry of(EssenceType type) {
        return switch (type) {
            case WITHER -> new EssenceGuideEntry(type, "c4db4adfa9bf48ff5d41707ae34ea78bd2371659fcd8cd8934749af4cce9b",
                    "<8>", "Why are we still here? Just to suffer...", 19);
            case SPIDER -> new EssenceGuideEntry(type, "16617131250e578333a441fdf4a5b8c62163640a9d06cd67db89031d03accf6",
                    "<5>", "Spinning a web of lies...", 20);
            case UNDEAD -> new EssenceGuideEntry(type, "71d7c816fc8c636d7f50a93a0ba7aaeff06c96a561645e9eb1bef391655c531",
                    "<4>", "Is this thing rotten? And the smell, yikes...", 21);
            case DRAGON -> new EssenceGuideEntry(type, "33ff416aa8bec1665b92701fbe68a4effff3d06ed9147454fa77712dd6079b33",
                    "<5>", "Is it glowing? And pulsating?!", 22);
            case GOLD -> new EssenceGuideEntry(type, "8816606260779b23ed15f87c56c932240db745f86f683d1f4deb83a4a125fa7b",
                    "<6>", "There's something dirty about this gold...", 23);
            case DIAMOND -> new EssenceGuideEntry(type, "964f25cfff754f287a9838d8efe03998073c22df7a9d3025c425e3ed7ff52c20",
                    "<b>", "So pristine...so magnificent!", 24);
            case ICE -> new EssenceGuideEntry(type, "ddba642efffa13ec3730eafc5914ab68115c1f998803f74452e2e0cd26af0b8",
                    "<b>", "Ice cold, yet warm to the touch.", 25);
            case CRIMSON -> new EssenceGuideEntry(type, "67c41930f8ff0f2b0430e169ae5f38e984df1244215705c6f173862844543e9d",
                    "<c>", "Smoke is rising from the houses.", 29);
            case FOREST -> new EssenceGuideEntry(type, "e019ece125ec79f95178e1af4da2a8b982de71ed42c31cac4b1d2f665355df5a",
                    "<2>", "Nature's hidden treasure, radiant and precious.", 30);
            case SUN_GECKO -> new EssenceGuideEntry(type, "acb5e9e5c977ee2f9c6de82d83fdd88c1172fa0e72d2889096bdae32e683dcbd",
                    "<6>", null, 31);
            case FOSSIL -> new EssenceGuideEntry(type, "93a1b830399ab432a5178fdaf3939b24bf25c724a66be947296c503352bc380d",
                    "<6>", "The oldest essence of all.", 32);
            case SAFARI -> new EssenceGuideEntry(type, "2d9eb19536a482fdffba3dcd89dcbbd5daa94180d69e156d052061e376373aea",
                    "<2>", "The sweet smell of the safari.", 33);
        };
    }
}
