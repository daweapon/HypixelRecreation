package net.swofty.type.skyblockgeneric.mythological;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * How each Mythological creature looks on Hypixel. Player-model creatures carry Hypixel's own skin
 * values (from the Bestiary heads in the NotEnoughUpdates repo), sent unsigned to a mannequin.
 */
final class MythologicalAppearance {
    private static final byte ALL_SKIN_PARTS = 127;

    private static final Map<MythologicalCreature, String> SKINS = Map.of(
            MythologicalCreature.STRANDED_NYMPH, "ewogICJ0aW1lc3RhbXAiIDogMTc1NTY0NTU0ODAxMSwKICAicHJvZmlsZUlkIiA6ICI1ZjU5NmViY2JlOTQ0NmQxYmI0M2JlNGYzZjRiOGJlNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUZWlsMHNzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU1OTZiMDVhMTZiMzQzMTBhNDQ4NTI2MjM4YjJmYzY1NWM4OTU1Yzg1NjY5OGM3ZDZiMWRiOWFkOWU0MjI3ZmIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
            MythologicalCreature.HARPY, "ewogICJ0aW1lc3RhbXAiIDogMTc1NTY0NDM0MTE4NywKICAicHJvZmlsZUlkIiA6ICI2MTU1NTMyOTY2OWM0ZDA5YmFiOGJlNDNkYWUwYTRjMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJmaTAxNSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80NzBlZTkxZWUxYTAwNTkzMzFkN2VjOTI0YTA5Yjg2NDgwYmQ5NmRhZTE5MTRmZjg1YzJjZDNiNzEzNGFiNjgzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
            MythologicalCreature.MINOTAUR, "ewogICJ0aW1lc3RhbXAiIDogMTU5ODQ0MDI0Njk0NCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Y5MmQ4MTRiOTk3ZmI3YTY3NmZjOGNkMTVkZjIzYjdkNDg1ZDYxMTEwNjBiM2YxZGUzZGQxYjljZmE4MTExYyIKICAgIH0KICB9Cn0=",
            MythologicalCreature.MINOS_CHAMPION, "ewogICJ0aW1lc3RhbXAiIDogMTU5ODQ0MDQzNzk0NCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWVmY2QxM2Q1ODc3ZWU0ZWRkODNhMzQyMDg4NTIwYTljZjIyNjU0ODRiZmI5M2FhMTM1Y2FjZDY1MWI0ZmU4NSIKICAgIH0KICB9Cn0=",
            MythologicalCreature.SPHINX, "ewogICJ0aW1lc3RhbXAiIDogMTc1MjUyMjYwMzQwMiwKICAicHJvZmlsZUlkIiA6ICI3YjE3NjBiYmVkNTk0Mjc1YmU3ZjhjMGFlYmQzMmRiNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdmVuTmlqaHVpczQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2JiYTNmM2FlNWUzNTc3ZTBhZTM3YzMwMmU4YmY4MzhkMzQ0MzU5YTdjNzY5OGMyMzY2ODdiNzRiYzU3M2ExYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            MythologicalCreature.MINOS_INQUISITOR, "ewogICJ0aW1lc3RhbXAiIDogMTU5ODQ0MDY4MzMxOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTlhYWMzNTZkYmJlZjNlYTdlZTM0NDBkN2IzYWI5ZWZjMjk2NWUyNTlmYWRjYjliMDg1MWIwYjI0NDM0N2I3ZCIKICAgIH0KICB9Cn0=",
            MythologicalCreature.MANTICORE, "ewogICJ0aW1lc3RhbXAiIDogMTc1MjUxNDEzODYzMSwKICAicHJvZmlsZUlkIiA6ICJiOWIzY2RlZmIyZmQ0YWY1ODQxMGViZWZjY2ZmYTBhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJpbnRlcnNlY2F0byIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85ZTA1N2U4MTk5MDdhNjM4OTc1NzYwYjljZDkzNWNiOGE1N2RiZjI4MTY5NTRhYzhkOTQ0MTk2YTRlOGIwMmNhIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
            MythologicalCreature.KING_MINOS, "ewogICJ0aW1lc3RhbXAiIDogMTc1NTY0NDI1NDYxOSwKICAicHJvZmlsZUlkIiA6ICI0MDU4NDhjMmJjNTE0ZDhkOThkOTJkMGIwYzhiZDQ0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJMaWFtX1NhZ2UiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg0Y2Q4NWEwNDlmYTliMzI1NmJhYTM0ODk0ZWMwN2Q4NThjNGYzMjRjMzBlYmQ4MDQzNWYyYzRjYTk4NjlkNSIKICAgIH0KICB9Cn0="
    );

    private static final Map<MythologicalCreature, Material> MAIN_HAND = Map.of(
            MythologicalCreature.MINOS_HUNTER, Material.WOODEN_HOE,
            MythologicalCreature.HARPY, Material.BOW,
            MythologicalCreature.MINOTAUR, Material.IRON_AXE,
            MythologicalCreature.MINOS_CHAMPION, Material.DIAMOND_SWORD
    );

    private static final Color HUNTER_COAT = new Color(0xC81E1E);
    private static final Color HUNTER_TROUSERS = new Color(0xB4143C);
    private static final Color HUNTER_BOOTS = new Color(0x283C78);

    private MythologicalAppearance() {
    }

    static @Nullable String skin(MythologicalCreature creature) {
        return SKINS.get(creature);
    }

    static void dress(MythologicalCreatureMob mob) {
        MythologicalCreature creature = mob.getCreature();
        String skin = skin(creature);
        if (skin != null) {
            mob.editEntityMeta(MannequinMeta.class, meta -> {
                meta.setProfile(new ResolvableProfile(new PlayerSkin(skin, null)));
                meta.setDescription(null);
                meta.setDisplayedSkinParts(ALL_SKIN_PARTS);
            });
        }
        if (creature == MythologicalCreature.MINOS_HUNTER) {
            mob.setHelmet(ItemStacks.head(creature.getIconTexture(), "").build());
            mob.setChestplate(dyed(Material.LEATHER_CHESTPLATE, HUNTER_COAT));
            mob.setLeggings(dyed(Material.LEATHER_LEGGINGS, HUNTER_TROUSERS));
            mob.setBoots(dyed(Material.LEATHER_BOOTS, HUNTER_BOOTS));
        }
        if (creature == MythologicalCreature.SIAMESE_LYNXES) {
            mob.editEntityMeta(CatMeta.class, meta -> meta.setVariant(CatVariant.BLACK));
        }
        Material held = MAIN_HAND.get(creature);
        if (held != null) mob.setItemInMainHand(ItemStack.of(held));
    }

    static void dressAsAzrael(MythologicalCreatureMob lynx) {
        lynx.editEntityMeta(CatMeta.class, meta -> meta.setVariant(CatVariant.SIAMESE));
    }

    private static ItemStack dyed(Material material, Color color) {
        return ItemStack.builder(material).set(DataComponents.DYED_COLOR, color).build();
    }
}
