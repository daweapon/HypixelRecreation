package dev.daweapon.type.skyblockgeneric.user.island;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Real Hypixel co-op has no owner/rank hierarchy - adding or removing a member is instead decided
// by a majority vote of the other online co-op members, cast with /coopvote accept|deny. Only one
// vote can be active per co-op at a time.
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoopVoteManager {
    private static final int VOTE_TIMEOUT_TICKS = 2400; // 2 minutes

    public enum Type { ADD, KICK }

    public static final class ActiveVote {
        final Type type;
        final UUID coopId;
        final UUID target;
        final String targetName;
        final Set<UUID> eligibleVoters;
        final Set<UUID> yesVotes = ConcurrentHashMap.newKeySet();
        final Set<UUID> noVotes = ConcurrentHashMap.newKeySet();
        final Runnable onPassed;
        final Runnable onFailed;

        private ActiveVote(Type type, UUID coopId, UUID initiator, UUID target, String targetName,
                            Set<UUID> eligibleVoters, Runnable onPassed, Runnable onFailed) {
            this.type = type;
            this.coopId = coopId;
            this.target = target;
            this.targetName = targetName;
            this.eligibleVoters = eligibleVoters;
            this.onPassed = onPassed;
            this.onFailed = onFailed;
            this.yesVotes.add(initiator);
        }

        int majorityThreshold() {
            return (eligibleVoters.size() / 2) + 1;
        }
    }

    private static final Map<UUID, ActiveVote> activeVotes = new ConcurrentHashMap<>();

    public static String startKickVote(SkyBlockPlayer initiator, CoopDatabase.Coop coop, UUID target, String targetName,
                                        Runnable onPassed, Runnable onFailed) {
        if (activeVotes.containsKey(coop.coopUUID())) return "A co-op vote is already in progress!";

        Set<UUID> eligibleVoters = new HashSet<>(coop.members());
        eligibleVoters.remove(target);

        Set<UUID> onlineEligible = new HashSet<>();
        for (SkyBlockPlayer online : coop.getOnlineMembers()) {
            if (eligibleVoters.contains(online.getUuid())) onlineEligible.add(online.getUuid());
        }

        if (eligibleVoters.size() <= 1) {
            // Only the initiator and the target exist in this co-op - a real vote is impossible,
            // so real Hypixel allows kicking without one only once the target's been gone 35+ days.
            long lastSeen = CoopDatabase.getLastSeen(coop, target);
            long daysInactive = (System.currentTimeMillis() - lastSeen) / (1000L * 60 * 60 * 24);
            if (lastSeen > 0 && daysInactive >= 35) {
                onPassed.run();
                return null;
            }
            return "Your co-op only has 2 members - you can only kick them without a vote after 35+ days of inactivity!";
        }

        if (onlineEligible.size() < 2) {
            return "Not enough other co-op members are online to hold a kick vote!";
        }

        ActiveVote vote = new ActiveVote(Type.KICK, coop.coopUUID(), initiator.getUuid(), target, targetName, onlineEligible, onPassed, onFailed);
        activeVotes.put(coop.coopUUID(), vote);
        broadcastAndSchedule(coop, vote, initiator, "kick " + targetName + " from the co-op");
        resolveIfDecided(coop.coopUUID());
        return null;
    }

    public static String startAddVote(SkyBlockPlayer initiator, CoopDatabase.Coop coop, UUID target, String targetName,
                                       Runnable onPassed, Runnable onFailed) {
        if (activeVotes.containsKey(coop.coopUUID())) return "A co-op vote is already in progress!";

        Set<UUID> onlineEligible = new HashSet<>();
        for (SkyBlockPlayer online : coop.getOnlineMembers()) {
            onlineEligible.add(online.getUuid());
        }

        if (onlineEligible.size() <= 1) {
            // No other members exist yet (first /coopadd on a solo profile) or none are online to vote.
            onPassed.run();
            return null;
        }

        ActiveVote vote = new ActiveVote(Type.ADD, coop.coopUUID(), initiator.getUuid(), target, targetName, onlineEligible, onPassed, onFailed);
        activeVotes.put(coop.coopUUID(), vote);
        broadcastAndSchedule(coop, vote, initiator, "add " + targetName + " to the co-op");
        resolveIfDecided(coop.coopUUID());
        return null;
    }

    public static String castVote(SkyBlockPlayer voter, boolean accept) {
        CoopDatabase.Coop coop = CoopDatabase.getFromMember(voter.getUuid());
        if (coop == null) return "You are not part of a co-op!";

        ActiveVote vote = activeVotes.get(coop.coopUUID());
        if (vote == null) return "There is no active co-op vote right now!";

        if (!vote.eligibleVoters.contains(voter.getUuid())) return "You're not eligible to vote on this!";
        if (vote.yesVotes.contains(voter.getUuid()) || vote.noVotes.contains(voter.getUuid())) return "You've already voted!";

        (accept ? vote.yesVotes : vote.noVotes).add(voter.getUuid());
        resolveIfDecided(coop.coopUUID());
        return null;
    }

    private static void broadcastAndSchedule(CoopDatabase.Coop coop, ActiveVote vote, SkyBlockPlayer initiator, String actionDescription) {
        initiator.sendMessage("<b>[Co-op] <e>Started a vote to {} - waiting for other members to confirm.", actionDescription);
        for (SkyBlockPlayer online : coop.getOnlineMembers()) {
            if (online.getUuid().equals(initiator.getUuid())) continue;
            if (!vote.eligibleVoters.contains(online.getUuid())) continue;
            online.sendMessage("<b>[Co-op] <e>{} <e>wants to {}. Run <6>/coopvote accept <e>or <6>/coopvote deny <e>within 2 minutes!",
                    initiator.getUsername(), actionDescription);
        }
        ScheduleUtility.delay(() -> expireVote(vote.coopId), VOTE_TIMEOUT_TICKS);
    }

    private static void expireVote(UUID coopId) {
        ActiveVote vote = activeVotes.get(coopId);
        if (vote == null) return;
        activeVotes.remove(coopId);
        vote.onFailed.run();
    }

    private static void resolveIfDecided(UUID coopId) {
        ActiveVote vote = activeVotes.get(coopId);
        if (vote == null) return;

        int majority = vote.majorityThreshold();
        if (vote.yesVotes.size() >= majority) {
            activeVotes.remove(coopId);
            vote.onPassed.run();
            return;
        }

        int remainingPossibleYes = vote.eligibleVoters.size() - vote.noVotes.size();
        if (remainingPossibleYes < majority) {
            activeVotes.remove(coopId);
            vote.onFailed.run();
        }
    }
}
