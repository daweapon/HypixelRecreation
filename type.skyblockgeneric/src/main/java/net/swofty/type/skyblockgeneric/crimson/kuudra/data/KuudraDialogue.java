package net.swofty.type.skyblockgeneric.crimson.kuudra.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

/** Elle's lines in Kuudra's Hollow, by moment, plus the fight's other chat announcements. */
public record KuudraDialogue(String npcPrefix, Set<String> randomOne, Map<String, List<String>> elle,
                             Map<String, String> announcements) {

    static KuudraDialogue parse(Map<String, Object> yaml) {
        Map<String, List<String>> elle = new LinkedHashMap<>();
        Yamls.map(yaml.get("elle")).forEach((key, lines) -> elle.put(key, Yamls.strings(lines)));
        Map<String, String> announcements = new LinkedHashMap<>();
        Yamls.map(yaml.get("announcements")).forEach((key, line) -> announcements.put(key, line.toString()));
        return new KuudraDialogue(Yamls.string(yaml, "npc_prefix"), Set.copyOf(Yamls.strings(yaml.get("random_one"))),
                Map.copyOf(elle), Map.copyOf(announcements));
    }

    public List<String> lines(String key) {
        List<String> lines = elle.get(key);
        if (lines == null) throw new IllegalArgumentException("No Elle dialogue '" + key + "'");
        return lines;
    }

    /** The chat lines for one moment, each with Elle's prefix; a random-one set yields a single line. */
    public List<String> chat(String key, RandomGenerator random) {
        List<String> lines = lines(key);
        if (randomOne.contains(key)) return List.of(npcPrefix + lines.get(random.nextInt(lines.size())));
        return lines.stream().map(line -> npcPrefix + line).toList();
    }

    public String announcement(String key, Map<String, Object> values) {
        String line = announcements.get(key);
        if (line == null) throw new IllegalArgumentException("No Kuudra announcement '" + key + "'");
        for (Map.Entry<String, Object> value : values.entrySet()) {
            line = line.replace("{" + value.getKey() + "}", String.valueOf(value.getValue()));
        }
        return line;
    }
}
