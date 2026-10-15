package dev.daweapon.type.skyblockgeneric.crimson.mob;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.network.player.ResolvableProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;

/**
 * Hypixel's own skins for the player-model Crimson Isle mobs (NEU constants/bestiary.json), sent unsigned to a
 * mannequin.
 */
public final class CrimsonSkins {
    public static final String MATCHO = "ewogICJ0aW1lc3RhbXAiIDogMTY0NDYxMTExNjAxOSwKICAicHJvZmlsZUlkIiA6ICI0ZjU2ZTg2ODk2OGU0ZWEwYmNjM2M2NzRlNzQ3ODdjOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VmMmRhYWJiNzhhMWY3YWExMmQxNDVkODhjMGNhNDZiOWU4NTZmNTUzNGU5Mjg2ZTU1NWZhZjBjMjkxZjRmZDUiCiAgICB9CiAgfQp9";
    public static final String MAGE_OUTLAW = "ewogICJ0aW1lc3RhbXAiIDogMTY0NDA4Mjg3Mzk2NywKICAicHJvZmlsZUlkIiA6ICJiNzQ3OWJhZTI5YzQ0YjIzYmE1NjI4MzM3OGYwZTNjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTeWxlZXgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWJlYzk5YjNhMDUwZmQyNzc1Mjg0MDc2NzYzNjZlMjBiOTIwMDZhZDg4ZDE0NzI3YTRkOTllYjhjYjI3M2I2MiIKICAgIH0KICB9Cn0";
    public static final String BARBARIAN_DUKE_X = "ewogICJ0aW1lc3RhbXAiIDogMTY0MTI0OTYyNTU3NSwKICAicHJvZmlsZUlkIiA6ICIyNzZlMDQ2YjI0MDM0M2VkOTk2NmU0OTRlN2U2Y2IzNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBRFJBTlM3MTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVlOWZjN2MxODFlMmY2MzBmNmIxYWY4NWQ0OTUxMzU5Y2FmY2ZhODJmZjVlYTNiYzI4M2UwZTYwODhjNmU1NCIKICAgIH0KICB9Cn0";
    private static final byte ALL_SKIN_PARTS = 127;

    private CrimsonSkins() {
    }

    public static void dress(SkyBlockMob mob, String skin) {
        mob.editEntityMeta(MannequinMeta.class, meta -> {
            meta.setProfile(new ResolvableProfile(new PlayerSkin(pad(skin), null)));
            meta.setDescription(null);
            meta.setDisplayedSkinParts(ALL_SKIN_PARTS);
        });
    }

    private static String pad(String base64) {
        return base64 + "=".repeat((4 - base64.length() % 4) % 4);
    }
}
