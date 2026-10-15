package dev.daweapon.type.skyblockgeneric.dungeons.queue;

import java.util.ArrayList;
import java.util.List;

/** Whether a party may queue a Catacombs floor: leader only, 1 to 5 players, everyone on this server and qualified. */
public final class CatacombsPartyCheck {
    public static final int MIN_PARTY_SIZE = 1;
    public static final int MAX_PARTY_SIZE = 5;

    private CatacombsPartyCheck() {
    }

    public record Member(String name, boolean self, boolean onServer, boolean meetsRequirements) {
    }

    public static List<String> problems(boolean leader, List<Member> members, String floorName) {
        if (!leader) return List.of("<c>Only your party leader can queue for The Catacombs!");
        if (members.size() < MIN_PARTY_SIZE) return List.of("<c>There is nobody to queue!");
        if (members.size() > MAX_PARTY_SIZE) {
            return List.of("<c>Catacombs parties can have at most " + MAX_PARTY_SIZE + " players! Yours has "
                    + members.size() + ".");
        }
        List<String> problems = new ArrayList<>();
        for (Member member : members) {
            if (!member.onServer()) {
                problems.add("<c>" + member.name() + " must be on your server to queue! <7>Use <e>/p warp <7>first.");
            } else if (!member.meetsRequirements()) {
                problems.add(member.self()
                        ? "<c>You don't meet the requirements for " + floorName + "!"
                        : "<c>" + member.name() + " doesn't meet the requirements for " + floorName + "!");
            }
        }
        return problems;
    }
}
