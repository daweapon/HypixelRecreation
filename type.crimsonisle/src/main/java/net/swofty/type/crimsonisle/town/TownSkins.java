package net.swofty.type.crimsonisle.town;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * NEU skin textures for the remaining Crimson Isle NPCs. Stand-ins where NEU has no entry: the
 * Dragontail Auction Agents wear the Hub Auction Agent's skin, Romero his Park skin, Aura Velyna's,
 * Azax and Nuvian other Mage skins (Udel's and Ulyn's), Uvia Seffea's, Eludore Cyndarin's, and Deng
 * and Forast Barbarian skins (Keran's and Jine's).
 */
public final class TownSkins {
    public static final String ARANYA = of("cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1");
    public static final String AVIAR = of("bd0f290e680a57641829a4dafa83948910ce84dc99b62b52460508affa43fb39");
    public static final String BARTER = of("3106127b5d4c25432837c21d8cdf135d18761b5627ae2864cfa30d377804168e");
    public static final String BELANOR = of("5008b6003c607ca8340bd53f4d55dd2d3451be120de7b35406ffd6d7100c90d7");
    public static final String BROMM = of("81713d1852c08750759fb988dd4ff6972cf3d68cf615b995bbef96be4a123b95");
    public static final String BRUTO = of("b9c075ac7d93936a49fb0fb255b38e837ef07f9871830613a421890e4568662d");
    public static final String BRUUH = of("2ff966461b4bd68df3ff7c6adae0cda5f27ac853a16e2a2c7849b3c026101390");
    public static final String CARROLYN = of("405db41a3555560be0d447f1c2edbc57dae5addccafbf90b3d0a802766db01bb");
    public static final String CORM = of("7fb672571623fc427477af584ed1f6b0a6a5714c872e4234119e02b5a34bbc");
    public static final String CYNDARIN = of("d8ed8aea24a58d9e2c0e8d74d70492ff908491631514533ec814d51deb77b7c7");
    public static final String DESPERATE_ENGINEER = of("775ea42050a478f28394884120b42091aa4212154876dc3d72062aea07cac0e9");
    public static final String EDELIS = of("405db41a3555560be0d447f1c2edbc57dae5addccafbf90b3d0a802766db01bb");
    public static final String ELMAR = of("528581630628409f84df20c502c687f6e24fa74c1cb7a9a41d77be17ef63ce2e");
    public static final String GNYL = of("edafdbd699616414d5dfdbe6b8bff9eedfa9efded69fc3475dace4eb028957bd");
    public static final String KERAN = of("17f7bd102631111aaa4223c2a7e6428d2a8a94b0d81408ec35fc3d5add75d0b9");
    public static final String PAM = of("804b2253c24d814bef3199f0a8611eeab54f7ce2ebf71a4bbe0028eb1a7bb08f");
    public static final String PRIE = of("2a633830db45637e0990d746c5409b1ba67e28f56587d783cc2302a03dd5694b");
    public static final String RULENOR = of("b4182c695239748c2cf0b8bf69c5430b98ff061141cb8dbbf8facccbbb45ff27");
    public static final String SELENAR = of("b0dac3131afa83ec50a9d653b84f7c087966ee614d5c50e63eba08b433efbd8");
    public static final String UDEL = of("5d0c25fd9d32e1204a1d32e5d95a2f1cfa09525a8f98e1deef5b2a3c77f98a8c");
    public static final String VESUVIUS = of("11443decbade0555c53938fdf69fb4cfe408a0a600ea8555a69433b3415aa823");
    public static final String ZIRI = of("f226538302f8796dd96b3525df2f060cb65fa09f29c3cb6073ae92e007e0d38f");

    public static final String AUCTION_AGENT = of("18614241b980319c02f5ee3ae1a7fc7ebf8b3fdd5301ed3d4e2159a80dae1d2c");
    public static final String ROMERO = of("7edbde718d74bc6e80c17bf27bffa6d8b2795bf20564fb9b0d5834ca592e3ce6");
    public static final String AURA = of("5b5a3b961823bec47e4e8ac23e7a3b2ae736c9c81347fa2b1be03c68c41faa1e");
    public static final String AZAX = UDEL;
    public static final String NUVIAN = of("54e50795ca428aabc3f38026cdcf744498c9810d922026b8baeba17d4fe26356");
    public static final String UVIA = of("7aeae71e34580887a0b7e38164b4a2159abe219d4cab9454d979b1c603cc0b76");
    public static final String ELUDORE = CYNDARIN;
    public static final String DENG = KERAN;
    public static final String FORAST = of("95fa13cc26539853fea59552b2e5e9016dc414dda2f837b72aa959f7f173cf3");

    private TownSkins() {
    }

    private static String of(String hash) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + hash + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
