package dev.daweapon.type.skyblockgeneric.entity.mob;

import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureSpawner;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestService;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreature;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SpawnableMobs {
    public static final int MAX_AMOUNT = 50;

    @FunctionalInterface
    public interface Spawner {
        int spawn(SkyBlockPlayer player, int amount);
    }

    public record Entry(String id, String displayName, Spawner spawner) {
    }

    private SpawnableMobs() {
    }

    public static List<Entry> all() {
        Map<String, Entry> byKey = new LinkedHashMap<>();
        List<Entry> sources = new ArrayList<>();
        sources.addAll(seaCreatures());
        sources.addAll(registeredMobs());
        sources.addAll(mythologicalCreatures());
        sources.addAll(gardenPests());
        sources.addAll(huntingCreatures());
        for (Entry entry : sources) byKey.putIfAbsent(normalize(entry.id()), entry);
        return List.copyOf(byKey.values());
    }

    public static List<Entry> duplicates() {
        Map<String, Entry> seen = new LinkedHashMap<>();
        List<Entry> duplicates = new ArrayList<>();
        List<Entry> sources = new ArrayList<>();
        sources.addAll(seaCreatures());
        sources.addAll(registeredMobs());
        sources.addAll(mythologicalCreatures());
        sources.addAll(gardenPests());
        sources.addAll(huntingCreatures());
        for (Entry entry : sources) {
            if (seen.putIfAbsent(normalize(entry.id()), entry) != null) duplicates.add(entry);
        }
        return duplicates;
    }

    public static Entry find(String input) {
        String key = normalize(input);
        for (Entry entry : all()) {
            if (normalize(entry.id()).equals(key) || normalize(entry.displayName()).equals(key)) return entry;
        }
        return null;
    }

    public static String normalize(String input) {
        return input.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private static List<Entry> seaCreatures() {
        List<Entry> entries = new ArrayList<>();
        for (String id : SeaCreatureSpawner.ids()) {
            SeaCreatureProfile profile = SeaCreatureSpawner.getProfile(id);
            entries.add(new Entry(id, profile.displayName(), (player, amount) -> {
                int spawned = 0;
                for (int i = 0; i < amount; i++) {
                    if (SeaCreatureSpawner.spawn(player, id, player.getPosition()) != null) spawned++;
                }
                if (spawned > 0) SeaCreatureSpawner.announceCatch(player, id, spawned);
                return spawned;
            }));
        }
        return entries;
    }

    private static List<Entry> registeredMobs() {
        List<Entry> entries = new ArrayList<>();
        for (MobRegistry registry : MobRegistry.REGISTERED_MOBS) {
            SkyBlockMob sample = registry.getMobCache();
            String id = sample instanceof BestiaryMob bestiary ? bestiary.getMobID() : registry.getClazz().getSimpleName();
            entries.add(new Entry(id, sample.getDisplayName(), (player, amount) -> {
                int spawned = 0;
                for (int i = 0; i < amount; i++) {
                    SkyBlockMob mob = registry.asMob();
                    if (mob == null) continue;
                    mob.setInstance(player.getInstance(), player.getPosition());
                    spawned++;
                }
                return spawned;
            }));
        }
        return entries;
    }

    private static List<Entry> mythologicalCreatures() {
        List<Entry> entries = new ArrayList<>();
        for (MythologicalCreature creature : MythologicalCreature.values()) {
            if (creature.allVariants().isEmpty()) continue;
            entries.add(new Entry(creature.name(), creature.getDisplayName(), (player, amount) -> {
                for (int i = 0; i < amount; i++) {
                    MythologicalCreatureMob.of(creature, creature.allVariants().getFirst())
                            .setInstance(player.getInstance(), player.getPosition());
                }
                return amount;
            }));
        }
        return entries;
    }

    private static List<Entry> huntingCreatures() {
        List<Entry> entries = new ArrayList<>();
        for (dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec spec
                : dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureCatalog.all()) {
            entries.add(new Entry(spec.id(), spec.name(), (player, amount) -> {
                for (int i = 0; i < amount; i++) {
                    dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpawner.spawnAt(spec, player.getInstance(), player.getPosition());
                }
                return amount;
            }));
        }
        return entries;
    }

    private static List<Entry> gardenPests() {
        List<Entry> entries = new ArrayList<>();
        for (PestCatalogue.Pest pest : PestCatalogue.get().all()) {
            entries.add(new Entry("PEST_" + pest.id(), pest.name() + " Pest",
                    (player, amount) -> PestService.spawnByStaff(player, pest.id(), amount)));
        }
        return entries;
    }
}
