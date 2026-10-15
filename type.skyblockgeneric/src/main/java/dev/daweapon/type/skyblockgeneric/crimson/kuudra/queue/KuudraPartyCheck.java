package dev.daweapon.type.skyblockgeneric.crimson.kuudra.queue;

import java.util.ArrayList;
import java.util.List;

/** Whether a party may queue a Kuudra tier: leader only, 1 to 4 players, everyone on this server and qualified. */
public final class KuudraPartyCheck {
    public static final int MAX_PARTY_SIZE = 4;

    private KuudraPartyCheck() {
    }

    public record Member(String name, boolean self, boolean onServer, boolean meetsRequirements) {
    }

    /** The chat lines explaining why the queue is refused, or an empty list when it can go ahead. */
    public static List<String> problems(boolean leader, List<Member> members, String tierName) {
        if (!leader) return List.of("<c>Only your party leader can queue for Kuudra!");
        if (members.isEmpty()) return List.of("<c>There is nobody to queue!");
        if (members.size() > MAX_PARTY_SIZE) {
            return List.of("<c>Kuudra parties can have at most " + MAX_PARTY_SIZE + " players! Yours has "
                    + members.size() + ".");
        }
        List<String> problems = new ArrayList<>();
        for (Member member : members) {
            if (!member.onServer()) {
                problems.add("<c>" + member.name() + " must be on your server to queue! <7>Use <e>/p warp <7>first.");
            } else if (!member.meetsRequirements()) {
                problems.add(member.self()
                        ? "<c>You don't meet the requirements for the " + tierName + " Tier!"
                        : "<c>" + member.name() + " doesn't meet the requirements for the " + tierName + " Tier!");
            }
        }
        return problems;
    }
}
