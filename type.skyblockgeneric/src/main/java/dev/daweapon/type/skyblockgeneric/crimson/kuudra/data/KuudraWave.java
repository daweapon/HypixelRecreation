package dev.daweapon.type.skyblockgeneric.crimson.kuudra.data;

import java.util.List;

public record KuudraWave(int number, List<Spawn> spawns) {

    /** One attempted spawn; a count of 0 means the wiki gives no batch size. */
    public record Spawn(String mobId, int count) {
        static Spawn parse(String text) {
            String[] parts = text.trim().split(" x");
            return new Spawn(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 0);
        }
    }

    static KuudraWave parse(int number, List<String> entries) {
        return new KuudraWave(number, entries.stream().map(Spawn::parse).toList());
    }
}
