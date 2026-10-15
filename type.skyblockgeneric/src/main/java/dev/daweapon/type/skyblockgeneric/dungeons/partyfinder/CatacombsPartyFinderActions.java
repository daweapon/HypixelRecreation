package dev.daweapon.type.skyblockgeneric.dungeons.partyfinder;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.party.FullParty;
import dev.daweapon.dungeons.partyfinder.PartyFinderGroup;
import dev.daweapon.dungeons.partyfinder.PartyFinderMember;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.party.PartyManager;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsAvailability;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsRequirements;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** What a player does in the Catacombs Party Finder: list their party, join a group, leave or de-list. */
public final class CatacombsPartyFinderActions {
    public static final String PREFIX = "<d>Party Finder <f>> ";

    private CatacombsPartyFinderActions() {
    }

    public static int catacombsLevel(SkyBlockPlayer player) {
        return CatacombsProgress.level(player);
    }

    public static PartyFinderMember<Void> asMember(SkyBlockPlayer player) {
        return PartyFinderMember.member(player.getUuid(), player.getUsername(), null, catacombsLevel(player));
    }

    public static boolean blocked(SkyBlockPlayer player) {
        if (CatacombsAvailability.canUsePartyFinder(player)) return false;
        player.sendMessage(CatacombsAvailability.UNAVAILABLE_CHAT);
        return true;
    }

    public static void list(SkyBlockPlayer leader, CatacombsFloor floor, String note, int minimumLevel, Runnable after) {
        if (blocked(leader)) return;
        Thread.startVirtualThread(() -> {
            try {
                if (!CatacombsAvailability.isPlayable(floor)) {
                    leader.sendMessage(CatacombsAvailability.FLOOR_LOCKED_CHAT);
                    return;
                }
                if (!CatacombsRequirements.meets(leader, floor)) {
                    leader.sendMessage("<c>You don't meet the requirements for " + floor.displayName() + "!");
                    return;
                }
                FullParty party = PartyManager.isInParty(leader) ? PartyManager.getPartyFromPlayer(leader) : null;
                if (party != null && !party.getLeader().getUuid().equals(leader.getUuid())) {
                    leader.sendMessage("<c>Only your party leader can list your party in the Party Finder!");
                    return;
                }
                if (party != null && party.getMembers().size() >= CatacombsPartyFinder.CAPACITY) {
                    leader.sendMessage("<c>Your party is already full!");
                    return;
                }
                List<PartyFinderMember<Void>> others = new ArrayList<>();
                if (party != null) {
                    for (FullParty.Member member : party.getMembers()) {
                        if (!member.getUuid().equals(leader.getUuid())) others.add(remoteMember(member.getUuid()));
                    }
                }
                PartyFinderMember<Void> self = PartyFinderMember.leader(leader.getUuid(), leader.getUsername(), null, catacombsLevel(leader));
                Optional<PartyFinderGroup<CatacombsFloor, Void>> listed = CatacombsPartyFinder.list(
                        new CatacombsPartyFinder.Listing(self, others, floor, note, minimumLevel));
                if (listed.isEmpty()) {
                    leader.sendMessage("<c>Your party is already listed in the Party Finder!");
                    return;
                }
                leader.sendMessage(PREFIX + "<e>Your party has been queued in the party finder!");
                MinecraftServer.getSchedulerManager().scheduleNextTick(after);
            } catch (Exception exception) {
                Logger.error(exception, "Catacombs Party Finder listing failed for {}", leader.getUsername());
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
                Logger.error(exception, "Catacombs Party Finder join failed for {}", player.getUsername());
                player.sendMessage("<c>Something went wrong with the Party Finder. Please try again.");
            }
        });
    }

    private static void joinNow(SkyBlockPlayer player, UUID groupId) {
        Optional<PartyFinderGroup<CatacombsFloor, Void>> found = CatacombsPartyFinder.group(groupId);
        if (found.isEmpty()) {
            player.sendMessage("<c>This group no longer exists!");
            return;
        }
        PartyFinderGroup<CatacombsFloor, Void> group = found.get();
        if (group.contains(player.getUuid())) {
            player.sendMessage("<c>You are already in this group!");
            return;
        }
        if (PartyManager.isInParty(player)) {
            player.sendMessage("<c>You are already in a party! Leave it to join a Party Finder group.");
            return;
        }
        if (!CatacombsAvailability.isPlayable(group.key())) {
            player.sendMessage(CatacombsAvailability.FLOOR_LOCKED_CHAT);
            return;
        }
        if (!CatacombsRequirements.meets(player, group.key())) {
            player.sendMessage("<c>You don't meet the requirements for " + group.key().displayName() + "!");
            return;
        }
        PartyFinderMember<Void> member = asMember(player);
        PartyFinderGroup.JoinCheck check = CatacombsPartyFinder.join(groupId, member);
        if (check != PartyFinderGroup.JoinCheck.OK) {
            player.sendMessage(refusal(check, group));
            return;
        }
        try {
            PartyManager.addToParty(group.leaderId(), player.getUuid()).join();
        } catch (Exception exception) {
            CatacombsPartyFinder.leave(player.getUuid());
            throw exception;
        }
        String joined = PREFIX + "<b>" + player.getUsername() + " <e>joined the group! (<b>Catacombs Level " + member.level() + "<e>)";
        for (PartyFinderMember<Void> each : group.members()) {
            SkyBlockPlayer online = online(each.playerId());
            if (online != null) online.sendMessage(joined);
        }
        player.sendMessage(joined);
    }

    public static String refusal(PartyFinderGroup.JoinCheck check, PartyFinderGroup<CatacombsFloor, Void> group) {
        return switch (check) {
            case OK -> "";
            case CLOSED -> "<c>This group no longer exists!";
            case FULL -> "<c>This group is full!";
            case ALREADY_MEMBER -> "<c>You are already in this group!";
            case IN_ANOTHER_GROUP -> "<c>You are already in a Party Finder group!";
            case LEVEL_TOO_LOW -> "<c>You need Catacombs Level " + group.minimumLevel() + " to join this group!";
            case CLASS_NOT_ALLOWED -> "<c>You can't join this group!";
        };
    }

    /** Leaves your group, or de-lists it if you lead it. */
    public static void leave(SkyBlockPlayer player, Runnable after) {
        Thread.startVirtualThread(() -> {
            try {
                Optional<PartyFinderGroup<CatacombsFloor, Void>> left = CatacombsPartyFinder.leave(player.getUuid());
                if (left.isEmpty()) {
                    player.sendMessage("<c>You aren't in a Party Finder group!");
                    return;
                }
                player.sendMessage(left.get().leaderId().equals(player.getUuid())
                        ? PREFIX + "<e>Your group has been de-listed!"
                        : PREFIX + "<e>You left the group.");
                MinecraftServer.getSchedulerManager().scheduleNextTick(after);
            } catch (Exception exception) {
                Logger.error(exception, "Catacombs Party Finder leave failed for {}", player.getUsername());
            }
        });
    }

    /** Drops members who have left the leader's party since joining, and groups whose leader no longer leads. */
    public static void sync(PartyFinderGroup<CatacombsFloor, Void> group) {
        SkyBlockPlayer leader = online(group.leaderId());
        if (leader == null) return;
        FullParty party = PartyManager.getPartyFromPlayer(leader);
        if (party == null) {
            if (group.size() > 1) CatacombsPartyFinder.retain(group.id(), List.of(group.leaderId()));
            return;
        }
        if (!party.getLeader().getUuid().equals(group.leaderId())) {
            CatacombsPartyFinder.retain(group.id(), List.of());
            return;
        }
        CatacombsPartyFinder.retain(group.id(), party.getMembers().stream().map(FullParty.Member::getUuid).toList());
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
