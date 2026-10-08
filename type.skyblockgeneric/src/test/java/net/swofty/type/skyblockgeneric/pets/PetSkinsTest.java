package net.swofty.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.protocol.serializers.UnderstandableSkyBlockItemSerializer;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.text.TextParser;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.components.PetSkinComponent;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.components.LoreUpdateComponent;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import net.swofty.type.skyblockgeneric.user.pets.PetSkins;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class PetSkinsTest {
    private static final Path SKINS = Path.of("../configuration/skyblock/items/pets/skins.yml");
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Set<String> SKIN_IDS = new TreeSet<>();

    private static final Set<String> PETS_NOT_BUILT_YET = Set.of(
            "GUARDIAN_PET", "HEDGEHOG_PET", "JELLYFISH_PET", "OWL_PET", "PARROT_PET", "RABBIT_PET",
            "ROSE_DRAGON_PET", "SHEEP_PET", "SLUG_PET", "SUBZERO_WISP_PET");

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) {
                    Map<String, Object> item = (Map<String, Object>) entry;
                    ItemConfigParser.parseItem(item);
                    if (file.normalize().equals(SKINS.normalize())) SKIN_IDS.add(String.valueOf(item.get("id")));
                }
            }
        }
    }

    private static SkyBlockItem pet(ItemType type, Rarity rarity) {
        SkyBlockItem pet = new SkyBlockItem(type);
        pet.getAttributeHandler().setRarity(rarity);
        PetLevels.setLevel(pet, 100);
        return pet;
    }

    @Test
    void everySkinLoadsWithATextureAndAKnownOrFlaggedPet() {
        assertEquals(365, SKIN_IDS.size());
        List<String> failures = new ArrayList<>();
        int animated = 0;
        for (String id : SKIN_IDS) {
            ItemType type = ItemType.get(id);
            if (type == null) {
                failures.add(id + ": no ItemType");
                continue;
            }
            SkyBlockItem item = new SkyBlockItem(type);
            if (!item.hasComponent(PetSkinComponent.class)) {
                failures.add(id + ": PET_SKIN component failed to parse");
                continue;
            }
            PetSkinComponent skin = item.getComponent(PetSkinComponent.class);
            if (skin.isAnimated()) animated++;
            if (!skin.getTexture().matches("[0-9a-f]{40,70}")) failures.add(id + ": bad texture " + skin.getTexture());
            if (!skin.getTexture().equals(item.getComponent(SkullHeadComponent.class).getSkullTexture(item))) {
                failures.add(id + ": head does not use the skin texture");
            }
            if (skin.getPets().isEmpty()) failures.add(id + ": no target pet");
            boolean known = !skin.knownPets().isEmpty();
            boolean flagged = skin.getPets().stream().anyMatch(PETS_NOT_BUILT_YET::contains);
            if (!known && !flagged) failures.add(id + ": targets " + skin.getPets() + ", which is neither built nor flagged");
            for (ItemType target : skin.knownPets()) {
                if (!new SkyBlockItem(target).hasComponent(PetComponent.class)) failures.add(id + ": " + target + " is not a pet");
            }
            try {
                LoreConfig lore = item.getComponent(LoreUpdateComponent.class).getHandler();
                for (String line : lore.loreGenerator().apply(item, null)) TextParser.parse(line);
                TextParser.parse(lore.displayNameGenerator().apply(item, null));
            } catch (RuntimeException e) {
                failures.add(id + ": lore " + e.getMessage());
            }
        }
        assertEquals(86, animated);
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void applyAndRemoveRoundTrip() {
        SkyBlockItem elephant = pet(ItemType.ELEPHANT_PET, Rarity.LEGENDARY);
        String original = elephant.getComponent(SkullHeadComponent.class).getSkullTexture(elephant);
        SkyBlockItem skin = new SkyBlockItem(ItemType.PET_SKIN_ELEPHANT_PINK);
        String skinTexture = skin.getComponent(PetSkinComponent.class).getTexture();

        PetSkins.Result result = PetSkins.apply(elephant, skin);
        assertEquals(PetSkins.Outcome.APPLIED, result.outcome());
        assertNull(result.previous());
        assertEquals(ItemType.PET_SKIN_ELEPHANT_PINK, elephant.getAttributeHandler().getPetSkin());
        assertEquals(skinTexture, elephant.getComponent(SkullHeadComponent.class).getSkullTexture(elephant));
        assertNotEquals(original, skinTexture);

        List<String> lore = elephant.getComponent(PetComponent.class).getAbsoluteLore(null, elephant);
        assertTrue(lore.getFirst().contains("Pink Elephant Skin"), lore.getFirst());
        for (String line : lore) TextParser.parse(line);
        assertTrue(elephant.getDisplayName().contains(PetSkins.SKINNED_MARKER));

        assertEquals(PetSkins.Outcome.ALREADY_APPLIED, PetSkins.apply(elephant, skin).outcome());

        SkyBlockItem saved = new SkyBlockItem(new UnderstandableSkyBlockItemSerializer()
                .deserialize(new UnderstandableSkyBlockItemSerializer().serialize(elephant.toUnderstandable())));
        assertEquals(ItemType.PET_SKIN_ELEPHANT_PINK, saved.getAttributeHandler().getPetSkin());

        SkyBlockItem other = new SkyBlockItem(ItemType.PET_SKIN_ELEPHANT_BLUE);
        PetSkins.Result replaced = PetSkins.apply(elephant, other);
        assertEquals(PetSkins.Outcome.APPLIED, replaced.outcome());
        assertEquals(ItemType.PET_SKIN_ELEPHANT_PINK, replaced.previous());

        assertEquals(ItemType.PET_SKIN_ELEPHANT_BLUE, PetSkins.remove(elephant));
        assertNull(elephant.getAttributeHandler().getPetSkin());
        assertEquals(original, elephant.getComponent(SkullHeadComponent.class).getSkullTexture(elephant));
        assertNull(PetSkins.remove(elephant));
    }

    @Test
    void skinsRefuseOtherPets() {
        SkyBlockItem lion = pet(ItemType.LION_PET, Rarity.LEGENDARY);
        assertEquals(PetSkins.Outcome.WRONG_PET,
                PetSkins.apply(lion, new SkyBlockItem(ItemType.PET_SKIN_ELEPHANT_PINK)).outcome());
        assertEquals(PetSkins.Outcome.NOT_A_SKIN, PetSkins.apply(lion, new SkyBlockItem(ItemType.ELEPHANT_PET)).outcome());
        assertNull(lion.getAttributeHandler().getPetSkin());
    }

    @Test
    void animatedSkinsCantBeApplied() {
        SkyBlockItem ammonite = pet(ItemType.AMMONITE_PET, Rarity.LEGENDARY);
        SkyBlockItem magma = new SkyBlockItem(ItemType.PET_SKIN_AMMONITE_MAGMA);
        assertTrue(magma.getComponent(PetSkinComponent.class).isAnimated());
        assertEquals(PetSkins.Outcome.ANIMATED, PetSkins.apply(ammonite, magma).outcome());
        assertNull(ammonite.getAttributeHandler().getPetSkin());
    }

    @Test
    void skinsForUnbuiltPetsFailCleanly() {
        SkyBlockItem orphan = SKIN_IDS.stream()
                .map(id -> new SkyBlockItem(ItemType.valueOf(id)))
                .filter(item -> !item.getComponent(PetSkinComponent.class).isAnimated())
                .filter(item -> item.getComponent(PetSkinComponent.class).knownPets().isEmpty())
                .findFirst().orElse(null);
        assumeTrue(orphan != null, "every skinned pet is built");
        SkyBlockItem lion = pet(ItemType.LION_PET, Rarity.LEGENDARY);
        assertEquals(PetSkins.Outcome.UNKNOWN_PET, PetSkins.apply(lion, orphan).outcome());
    }

    @Test
    void oldSavesWithoutASkinLoadUnchanged() {
        SkyBlockItem elephant = pet(ItemType.ELEPHANT_PET, Rarity.EPIC);
        UnderstandableSkyBlockItem full = elephant.toUnderstandable();
        List<ItemAttribute> withoutSkin = full.attributes().stream()
                .filter(attribute -> !attribute.getKey().equals("pet_skin")).toList();
        assertEquals(full.attributes().size() - 1, withoutSkin.size());
        String json = new UnderstandableSkyBlockItemSerializer().serialize(
                new UnderstandableSkyBlockItem(full.itemKey(), withoutSkin, full.amount(), full.material()));
        assertTrue(!json.contains("pet_skin"), json);

        SkyBlockItem loaded = new SkyBlockItem(new UnderstandableSkyBlockItemSerializer().deserialize(json));
        assertNull(loaded.getAttributeHandler().getPetSkin());
        assertEquals(Rarity.EPIC, loaded.getAttributeHandler().getRarity());
        assertEquals(100, PetLevels.level(loaded));
        assertEquals(elephant.getComponent(SkullHeadComponent.class).getSkullTexture(elephant),
                loaded.getComponent(SkullHeadComponent.class).getSkullTexture(loaded));
    }
}
