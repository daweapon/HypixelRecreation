package net.swofty.type.skyblockgeneric.crimson.kuudra.partyfinder;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.party.FullParty;
import net.swofty.dungeons.partyfinder.PartyFinderGroup;
import net.swofty.dungeons.partyfinder.PartyFinderMember;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** What a player does in the Kuudra Party Finder: list their party, join a group, leave or de-list. */
public final class KuudraPartyFinderActions {
    public static final String PREFIX = "<d>Party Finder <f>> ";

    private KuudraPartyFinderActions() {
    }

    public static int combatLevel(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.COMBAT);
    }

    public static PartyFinderMember<Void> asMember(SkyBlockPlayer player) {
        return PartyFinderMember.member(player.getUuid(), player.getUsername(), null, combatLevel(player));
    }

    public static boolean blocked(SkyBlockPlayer player) {
        if (KuudraAvailability.canQueue(player)) return false;
        player.sendMessage(KuudraAvailability.UNAVAILABLE_CHAT);
        return true;
    }

    public static void list(SkyBlockPlayer leader, KuudraTier tier, String note, int minimumCombat, Runnable after) {
        if (blocked(leader)) return;
        Thread.startVirtualThread(() -> {
            try {
                if (!KuudraProgress.meetsRequirements(leader, tier)) {
                    leader.sendMessage("<c>You don't meet the requirements for the " + tier.getDisplayName() + " Tier!");
                    return;
                }
                FullParty party = PartyManager.isInParty(leader) ? PartyManager.getPartyFromPlayer(leader) : null;
                if (party != null && !party.getLeader().getUuid().equals(leader.getUuid())) {
                    leader.sendMessage("<c>Only your party leader can list your party in the Party Finder!");
                    return;
                }
                if (party != null && party.getMembers().size() >= KuudraPartyFinder.CAPACITY) {
                    leader.sendMessage("<c>Your party is already full!");
                    return;
                }
                List<PartyFinderMember<Void>> others = new ArrayList<>();
                if (party != null) {
                    for (FullParty.Member member : party.getMembers()) {
                        if (!member.getUuid().equals(leader.getUuid())) others.add(remoteMember(member.getUuid()));
                    }
                }
                PartyFinderMember<Void> self = PartyFinderMember.leader(leader.getUuid(), leader.getUsername(), null, combatLevel(leader));
                Optional<PartyFinderGroup<KuudraTier, Void>> listed = KuudraPartyFinder.list(
                        new KuudraPartyFinder.Listing(self, others, tier, note, minimumCombat));
                if (listed.isEmpty()) {
                    leader.sendMessage("<c>Your party is already listed in the Party Finder!");
                    return;
                }
                leader.sendMessage(PREFIX + "<e>Your party has been queued in the party finder!");
                MinecraftServer.getSchedulerManager().scheduleNextTick(after);
            } catch (Exception exception) {
                Logger.error(exception, "Kuudra Party Finder listing failed for {}", leader.getUsername());
                leader.sendMessage("<c>Something went wrong with the Party Finder. Please try again.");
            }
        });
    }

    public static void join(SkyBlockPlayer player, UUID groupId) {
        if (blocked(player)) return;
        Thread.startVirtualThread(() -> {
            try {
                joinNow(player, groupId);
            } catch (Exception exception) {
                Logger.error(exception, "Kuudra Party Finder join failed for {}", player.getUsername());
                player.sendMessage("<c>Something went wrong with the Party Finder. Please try again.");
            }
        });
    }

    private static void joinNow(SkyBlockPlayer player, UUID groupId) {
        Optional<PartyFinderGroup<KuudraTier, Void>> found = KuudraPartyFinder.group(groupId);
        if (found.isEmpty()) {
            player.sendMessage("<c>This group no longer exists!");
            return;
        }
        PartyFinderGroup<KuudraTier, Void> group = found.get();
        if (group.contains(player.getUuid())) {
            player.sendMessage("<c>You are already in this group!");
            return;
        }
        if (PartyManager.isInParty(player)) {
            player.sendMessage("<c>You are already in a party! Leave it to join a Party Finder group.");
            return;
        }
        if (!KuudraProgress.meetsRequirements(player, group.key())) {
            player.sendMessage("<c>You don't meet the requirements for the " + group.key().getDisplayName() + " Tier!");
            return;
        }
        PartyFinderMember<Void> member = asMember(player);
        PartyFinderGroup.JoinCheck check = KuudraPartyFinder.join(groupId, member);
        if (check != PartyFinderGroup.JoinCheck.OK) {
            player.sendMessage(refusal(check, group));
            return;
        }
        try {
            PartyManager.addToParty(group.leaderId(), player.getUuid()).join();
        } catch (Exception exception) {
            KuudraPartyFinder.leave(player.getUuid());
            throw exception;
        }
        String joined = PREFIX + "<b>" + player.getUsername() + " <e>joined the group! (<b>Combat Level " + member.level() + "<e>)";
        for (PartyFinderMember<Void> each : group.members()) {
            SkyBlockPlayer online = online(each.playerId());
            if (online != null) online.sendMessage(joined);
        }
        player.sendMessage(joined);
    }

    public static String refusal(PartyFinderGroup.JoinCheck check, PartyFinderGroup<KuudraTier, Void> group) {
        return switch (check) {
            case OK -> "";
            case CLOSED -> "<c>This group no longer exists!";
            case FULL -> "<c>This group is full!";
            case ALREADY_MEMBER -> "<c>You are already in this group!";
            case IN_ANOTHER_GROUP -> "<c>You are already in a Party Finder group!";
            case LEVEL_TOO_LOW -> "<c>You need Combat Level " + group.minimumLevel() + " to join this group!";
            case CLASS_NOT_ALLOWED -> "<c>You can't join this group!";
        };
    }

    /** Leaves your group, or de-lists it if you lead it. */
    public static void leave(SkyBlockPlayer player, Runnable after) {
        Thread.startVirtualThread(() -> {
            try {
                Optional<PartyFinderGroup<KuudraTier, Void>> left = KuudraPartyFinder.leave(player.getUuid());
                if (left.isEmpty()) {
                    player.sendMessage("<c>You aren't in a Party Finder group!");
                    return;
                }
                player.sendMessage(left.get().leaderId().equals(player.getUuid())
                        ? PREFIX + "<e>Your group has been de-listed!"
                        : PREFIX + "<e>You left the group.");
                MinecraftServer.getSchedulerManager().scheduleNextTick(after);
            } catch (Exception exception) {
                Logger.error(exception, "Kuudra Party Finder leave failed for {}", player.getUsername());
            }
        });
    }

    /** Drops members who have left the leader's party since joining, and groups whose leader no longer leads. */
    public static void sync(PartyFinderGroup<KuudraTier, Void> group) {
        SkyBlockPlayer leader = online(group.leaderId());
        if (leader == null) return;
        FullParty party = PartyManager.getPartyFromPlayer(leader);
        if (party == null) {
            if (group.size() > 1) KuudraPartyFinder.retain(group.id(), List.of(group.leaderId()));
            return;
        }
        if (!party.getLeader().getUuid().equals(group.leaderId())) {
            KuudraPartyFinder.retain(group.id(), List.of());
            return;
        }
        KuudraPartyFinder.retain(group.id(), party.getMembers().stream().map(FullParty.Member::getUuid).toList());
    }

    private static PartyFinderMember<Void> remoteMember(UUID uuid) {
        SkyBlockPlayer online = online(uuid);
        if (online != null) return asMember(online);
        String name = HypixelDataHandler.getPotentialIGNFromUUID(uuid);
        return PartyFinderMember.member(uuid, name == null ? "Unknown" : name, null, 0);
    }

    private static @Nullable SkyBlockPlayer online(UUID uuid) {
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid) instanceof SkyBlockPlayer player ? player : null;
    }
}
