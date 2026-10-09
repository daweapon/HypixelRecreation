package net.swofty.type.skyblockgeneric.crimson.kuudra.partyfinder;

import net.swofty.dungeons.partyfinder.PartyFinderBoard;
import net.swofty.dungeons.partyfinder.PartyFinderGroup;
import net.swofty.dungeons.partyfinder.PartyFinderMember;
import net.swofty.dungeons.partyfinder.PartyFinderRole;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** The Kuudra Party Finder board as JSON, so every server reads and writes the same groups. */
final class KuudraPartyFinderCodec {
    private KuudraPartyFinderCodec() {
    }

    static String encode(PartyFinderBoard<KuudraTier, Void> board) {
        List<Document> groups = new ArrayList<>();
        for (PartyFinderGroup<KuudraTier, Void> group : board.groups()) {
            List<Document> members = new ArrayList<>();
            for (PartyFinderMember<Void> member : group.members()) {
                members.add(new Document("id", member.playerId().toString())
                        .append("name", member.name())
                        .append("level", member.level()));
            }
            groups.add(new Document("id", group.id().toString())
                    .append("leader", group.leaderId().toString())
                    .append("tier", group.key().name())
                    .append("note", group.note())
                    .append("minCombat", group.minimumLevel())
                    .append("createdAt", group.createdAt())
                    .append("members", members));
        }
        return new Document("groups", groups).toJson();
    }

    static PartyFinderBoard<KuudraTier, Void> decode(String json) {
        PartyFinderBoard<KuudraTier, Void> board = new PartyFinderBoard<>();
        if (json == null || json.isBlank()) return board;
        for (Document stored : Document.parse(json).getList("groups", Document.class, List.of())) {
            try {
                UUID leaderId = UUID.fromString(stored.getString("leader"));
                List<Document> members = stored.getList("members", Document.class, List.of());
                PartyFinderMember<Void> leader = members.stream()
                        .filter(member -> leaderId.toString().equals(member.getString("id")))
                        .findFirst().map(member -> member(member, PartyFinderRole.LEADER))
                        .orElseThrow();
                PartyFinderGroup<KuudraTier, Void> group = new PartyFinderGroup<>(UUID.fromString(stored.getString("id")),
                        leader, KuudraTier.valueOf(stored.getString("tier")), stored.getString("note"),
                        number(stored, "minCombat"), Set.of(), KuudraPartyFinder.CAPACITY, 1,
                        ((Number) stored.get("createdAt")).longValue());
                members.forEach(member -> group.restore(member(member, PartyFinderRole.MEMBER)));
                board.add(group);
            } catch (RuntimeException ignored) {
            }
        }
        return board;
    }

    private static PartyFinderMember<Void> member(Document stored, PartyFinderRole role) {
        return new PartyFinderMember<>(UUID.fromString(stored.getString("id")), stored.getString("name"), role, null,
                number(stored, "level"), false);
    }

    private static int number(Document document, String key) {
        Object value = document.get(key);
        return value instanceof Number number ? number.intValue() : 0;
    }
}
