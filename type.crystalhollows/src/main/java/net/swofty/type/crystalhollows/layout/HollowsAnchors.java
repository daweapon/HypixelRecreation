package net.swofty.type.crystalhollows.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class HollowsAnchors {
    public record Ref(String structure, String anchor, String user, boolean fallsBack) {
    }

    private static final List<Ref> REFS = List.of(
            new Ref("jungle_temple", "door_guardian", "Kalhuiki Door Guardian NPC", false),
            new Ref("jungle_temple", "entrance", "Jungle Temple entry gate exit and teleport target", false),
            new Ref("jungle_temple", "crystal_claim", "Amethyst crystal claim", false),
            new Ref("goblin_queens_den", "entrance", "Goblin Queen's Den entry gate exit", false),
            new Ref("goblin_queens_den", "crystal_claim", "Amber crystal claim", false),
            new Ref("mines_of_divan", "keeper_lapis", "Keeper of Lapis NPC", false),
            new Ref("mines_of_divan", "keeper_gold", "Keeper of Gold NPC", false),
            new Ref("mines_of_divan", "keeper_diamond", "Keeper of Diamond NPC", false),
            new Ref("mines_of_divan", "keeper_emerald", "Keeper of Emerald NPC", false),
            new Ref("mines_of_divan", "corleone", "Boss Corleone spawn", true),
            new Ref("khazad_dum", "bal", "Bal spawn", true),
            new Ref("lost_precursor_city", "professor_robot", "Professor Robot NPC", false),
            new Ref("king_yolkars_tower", "king_yolkar", "King Yolkar NPC", false),
            new Ref("king_yolkars_tower", "xalx", "Xalx NPC", false),
            new Ref("dragons_lair", "golden_dragon", "Golden Dragon NPC", false),
            new Ref("odawa_hut", "odawa", "Odawa NPC", false),
            new Ref("sludge_cage", "sludge", "Sludge NPC", false),
            new Ref("chunk_stand", "chunk", "Chunk NPC (has a fallback position)", true),
            new Ref("mithril_forger_room", "forger", "Forger NPC", false));

    private HollowsAnchors() {
    }

    public static List<Ref> all() {
        return REFS;
    }

    public static List<String> namesFor(String structure, Iterable<Ref> extra) {
        TreeSet<String> names = new TreeSet<>();
        for (Ref ref : REFS) if (ref.structure().equals(structure)) names.add(ref.anchor());
        for (Ref ref : extra) if (ref.structure().equals(structure)) names.add(ref.anchor());
        return new ArrayList<>(names);
    }
}
