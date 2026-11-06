package dev.daweapon.type.catacombs.engine.look;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.dungeons.catacombs.layout.MobSpawnPoint;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplate;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every dungeon mob our floors spawn wears what the captured Hypixel runs showed. */
class MobLooksTest {
    private static final Path DUNGEONS = Path.of("..", "configuration", "skyblock", "dungeons");
    private static MobLooks looks;

    @BeforeAll
    static void load() {
        looks = MobLooks.load(DUNGEONS.resolve("mob_appearance.json"));
    }

    private static String skinHash(Skin skin) {
        String json = new String(Base64.getDecoder().decode(skin.texture()), StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        String url = root.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
        return url.substring(url.lastIndexOf('/') + 1);
    }

    @Test
    void everyMobTheRoomsSpawnHasALookAndKnownMaterials() {
        RoomTemplateLibrary library = RoomTemplateLibrary.fromDirectory(DUNGEONS.resolve("rooms"));
        Set<String> spawned = new HashSet<>();
        for (RoomTemplate template : library.all()) {
            for (MobSpawnPoint spawn : template.mobSpawns()) spawned.add(spawn.mobId());
        }
        spawned.addAll(List.of("CRYPT_DREADLORD", "CRYPT_SOULEATER", "CRYPT_UNDEAD", "UNDEAD_SKELETON", "BONZO", "BONZO_UNDEAD"));
        for (String id : spawned) {
            MobLook look = looks.look(id).orElseThrow(() -> new AssertionError("no look for " + id));
            assertFalse(look.outfits().isEmpty(), id);
            assertNotNull(DungeonMobRegistry.defaults().mob(id), id);
            for (MobLook.Outfit outfit : look.outfits()) {
                if (look.playerModel()) assertNotNull(outfit.skin(), id + " is a player model without a skin");
                outfit.equipment().values().forEach(spec -> assertNotNull(spec.resolvedMaterial(), id + " " + spec));
            }
        }
    }

    @Test
    void cryptDreadlordsAndSouleatersShareTheCapturedSkinWithTheirOwnWeapons() {
        MobLook dreadlord = looks.look("CRYPT_DREADLORD").orElseThrow();
        MobLook souleater = looks.look("CRYPT_SOULEATER").orElseThrow();
        assertEquals(EntityType.MANNEQUIN, dreadlord.entity());
        String hash = "66f349270a3b851896cdad842f5eec6e140bd9119b75c074955c3be7865e27c7";
        assertEquals(hash, skinHash(dreadlord.outfits().getFirst().skin()));
        assertEquals(hash, skinHash(souleater.outfits().getFirst().skin()));
        assertEquals("iron_sword", dreadlord.outfits().getFirst().equipment().get(EquipmentSlot.MAIN_HAND).material());
        assertEquals("bow", souleater.outfits().getFirst().equipment().get(EquipmentSlot.MAIN_HAND).material());
        assertEquals(List.of(MobType.UNDEAD, MobType.WITHER, MobType.SUBTERRANEAN), dreadlord.types());
    }

    @Test
    void theMinibossesAreTheCapturedPlayers() {
        MobLook archaeologist = looks.look("ANGRY_ARCHAEOLOGIST").orElseThrow();
        assertEquals("c48c783458e4cf8518e8ab5863fbc4cb948f90568eeb9a60d16c4fde2b96c033", skinHash(archaeologist.outfits().getFirst().skin()));
        Map<EquipmentSlot, ItemSpec> perfect = archaeologist.outfits().getFirst().equipment();
        assertEquals("PERFECT_HELMET_5", perfect.get(EquipmentSlot.HELMET).item());
        assertEquals("diamond_helmet", perfect.get(EquipmentSlot.HELMET).material());
        assertEquals("<c><l>MINIBOSS", archaeologist.header());

        MobLook adventurer = looks.look("LOST_ADVENTURER").orElseThrow();
        assertEquals(3, adventurer.outfits().size());
        Set<String> helmets = new HashSet<>();
        for (MobLook.Outfit outfit : adventurer.outfits()) {
            ItemSpec helmet = outfit.equipment().get(EquipmentSlot.HELMET);
            assertEquals("player_head", helmet.material());
            assertNotNull(helmet.head());
            helmets.add(helmet.item());
            assertNotNull(outfit.equipment().get(EquipmentSlot.CHESTPLATE).color());
        }
        assertEquals(Set.of("UNSTABLE_DRAGON_HELMET", "HOLY_DRAGON_HELMET", "YOUNG_DRAGON_HELMET"), helmets);
    }

    @Test
    void zombiesAndSkeletonsWearTheirDyedDungeonSets() {
        assertEquals(EntityType.ZOMBIE, looks.look("ZOMBIE_GRUNT").orElseThrow().entity());
        assertEquals(97585, looks.look("ZOMBIE_GRUNT").orElseThrow().outfits().getFirst().equipment().get(EquipmentSlot.CHESTPLATE).color());
        assertEquals(8553090, looks.look("TANK_ZOMBIE").orElseThrow().outfits().getFirst().equipment().get(EquipmentSlot.CHESTPLATE).color());
        assertEquals(14805812, looks.look("SKELETON_GRUNT").orElseThrow().outfits().getFirst().equipment().get(EquipmentSlot.HELMET).color());
        assertEquals(16759819, looks.look("SKELETON_SOLDIER").orElseThrow().outfits().getFirst().equipment().get(EquipmentSlot.BOOTS).color());
        assertEquals(EntityType.SKELETON, looks.look("SCARED_SKELETON").orElseThrow().entity());
        assertEquals("bone", looks.look("CRYPT_LURKER").orElseThrow().outfits().getFirst().equipment().get(EquipmentSlot.MAIN_HAND).material());
        assertEquals(94, looks.look("UNDEAD_SKELETON").orElseThrow().level());
    }

    @Test
    void bonzoChangesSkinAndWeaponForHisSecondLife() {
        MobLook first = looks.look("BONZO").orElseThrow();
        MobLook second = looks.look("BONZO_REVIVED").orElseThrow();
        assertEquals("91e79f7e628f21f427610cc6ced0ff153e21880d61cbac673bf862b1d4e4426b", skinHash(first.outfits().getFirst().skin()));
        assertEquals("12716ecbf5b8da00b05f316ec6af61e8bd02805b21eb8e440151468dc656549c", skinHash(second.outfits().getFirst().skin()));
        assertEquals("BONZO_STAFF", second.outfits().getFirst().equipment().get(EquipmentSlot.MAIN_HAND).item());
        assertEquals(List.of(MobType.HUMANOID, MobType.ARCANE), first.types());
        assertEquals(List.of(MobType.UNDEAD, MobType.ARCANE), second.types());
    }

    @Test
    void watcherSummonsRollACapturedNameAndSkinWithHealthInThousands() {
        MobLooks.WatcherMobs pool = looks.watcherMobs().orElseThrow();
        Map<String, String> skins = Map.of("Reaper", "a89f6303af85877610912dc04b8b1e89724752f0a7eea05ab6547e228179c06f",
                "Leech", "5a79860aca799407c0faa10b1bbcf42998fad4ebcf31d7a214180826b4ac94e1");
        Random random = new Random(9);
        Set<String> names = new HashSet<>();
        for (int roll = 0; roll < 500; roll++) {
            MobLooks.WatcherMobs.Summon summon = pool.roll(random);
            assertTrue(summon.health() >= 15_000 && summon.health() <= 23_000 && summon.health() % 1000 == 0, "health " + summon.health());
            assertEquals("<6>", summon.look().nameColour());
            assertEquals(EntityType.MANNEQUIN, summon.look().entity());
            assertFalse(summon.look().outfits().getFirst().equipment().containsKey(EquipmentSlot.HELMET));
            String name = summon.look().displayName();
            names.add(name);
            if (skins.containsKey(name)) assertEquals(skins.get(name), skinHash(summon.look().outfits().getFirst().skin()));
        }
        assertEquals(11, names.size());
    }
}
