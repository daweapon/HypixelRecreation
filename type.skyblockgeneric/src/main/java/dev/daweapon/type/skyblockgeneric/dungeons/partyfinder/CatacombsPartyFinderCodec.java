package dev.daweapon.type.skyblockgeneric.dungeons.partyfinder;

import dev.daweapon.dungeons.partyfinder.PartyFinderBoard;
import dev.daweapon.dungeons.partyfinder.PartyFinderGroup;
import dev.daweapon.dungeons.partyfinder.PartyFinderMember;
import dev.daweapon.dungeons.partyfinder.PartyFinderRole;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** The Catacombs Party Finder board as JSON, so every server reads and writes the same groups. */
final class CatacombsPartyFinderCodec {
    private CatacombsPartyFinderCodec() {
    }

    static String encode(PartyFinderBoard<CatacombsFloor, Void> board) {
        List<Document> groups = new ArrayList<>();
        for (PartyFinderGroup<CatacombsFloor, Void> group : board.groups()) {
            List<Document> members = new ArrayList<>();
            for (PartyFinderMember<Void> member : group.members()) {
                members.add(new Document("id", member.playerId().toString())
                        .append("name", member.name())
                        .append("level", member.level()));
            }
            groups.add(new Document("id", group.id().toString())
                    .append("leader", group.leaderId().toString())
                    .append("floor", group.key().name())
                    .append("note", group.note())
                    .append("minLevel", group.minimumLevel())
                    .append("createdAt", group.createdAt())
                    .append("members", members));
        }
        return new Document("groups", groups).toJson();
    }

    static PartyFinderBoard<CatacombsFloor, Void> decode(String json) {
        PartyFinderBoard<CatacombsFloor, Void> board = new PartyFinderBoard<>();
        if (json == null || json.isBlank()) return board;
        for (Document stored : Document.parse(json).getList("groups", Document.class, List.of())) {
            try {
                UUID leaderId = UUID.fromString(stored.getString("leader"));
                List<Document> members = stored.getList("members", Document.class, List.of());
                PartyFinderMember<Void> leader = members.stream()
                        .filter(member -> leaderId.toString().equals(member.getString("id")))
                        .findFirst().map(member -> member(member, PartyFinderRole.LEADER))
                        .orElseThrow();
                PartyFinderGroup<CatacombsFloor, Void> group = new PartyFinderGroup<>(UUID.fromString(stored.getString("id")),
                        leader, CatacombsFloor.valueOf(stored.getString("floor")), stored.getString("note"),
                        number(stored, "minLevel"), Set.of(), CatacombsPartyFinder.CAPACITY, 1,
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
