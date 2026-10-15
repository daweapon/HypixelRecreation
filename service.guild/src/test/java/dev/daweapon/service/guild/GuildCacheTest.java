package dev.daweapon.service.guild;

import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.guild.GuildEvent;
import dev.daweapon.commons.guild.GuildMember;
import dev.daweapon.commons.guild.events.*;
import dev.daweapon.commons.guild.events.response.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GuildCacheTest {
    private static final class MemoryStore implements GuildCache.GuildStore {
        final Map<UUID, GuildData> guilds = new HashMap<>();
        final Map<UUID, UUID> playerGuilds = new HashMap<>();

        public GuildData getGuild(UUID guildId) { return guilds.get(guildId); }
        public void saveGuild(GuildData guild) { guilds.put(guild.getGuildId(), guild); }
        public void deleteGuild(UUID guildId) { guilds.remove(guildId); }
        public void mapPlayerToGuild(UUID playerUuid, UUID guildId) { playerGuilds.put(playerUuid, guildId); }
        public void removePlayerMapping(UUID playerUuid) { playerGuilds.remove(playerUuid); }
        public UUID getPlayerGuildId(UUID playerUuid) { return playerGuilds.get(playerUuid); }
        public boolean guildNameExists(String name) {
            return guilds.values().stream().anyMatch(g -> g.getName().equalsIgnoreCase(name));
        }
        public GuildData getGuildByName(String name) {
            return guilds.values().stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }

    private final MemoryStore memory = new MemoryStore();
    private final List<GuildEvent> events = new ArrayList<>();
    private final List<UUID> errorTargets = new ArrayList<>();
    private final UUID master = UUID.randomUUID();
    private final UUID officer = UUID.randomUUID();
    private final UUID member = UUID.randomUUID();
    private final UUID outsider = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        GuildCache.resetState();
        GuildCache.store = memory;
        GuildCache.eventSink = events::add;
        GuildCache.errorSender = (uuid, markup, args) -> errorTargets.add(uuid);
    }

    @AfterEach
    void tearDown() {
        GuildCache.resetState();
    }

    private GuildData createGuild() {
        GuildCache.handleCreateRequest(new GuildCreateRequestEvent(master, "Testers"));
        GuildData guild = GuildCache.getGuildFromPlayer(master);
        addMember(guild, officer, "Officer");
        addMember(guild, member, "Member");
        events.clear();
        return guild;
    }

    private void addMember(GuildData guild, UUID uuid, String rank) {
        guild.getMembers().add(new GuildMember(uuid, rank, System.currentTimeMillis()));
        memory.mapPlayerToGuild(uuid, guild.getGuildId());
    }

    private boolean lastEventIs(Class<?> type) {
        return !events.isEmpty() && type.isInstance(events.getLast());
    }

    @Test
    void createRegistersGuildWithMasterAndMapsPlayer() {
        GuildCache.handleCreateRequest(new GuildCreateRequestEvent(master, "Testers"));
        GuildData guild = GuildCache.getGuildFromPlayer(master);
        assertNotNull(guild);
        assertEquals(master, guild.getMasterUuid());
        assertTrue(GuildCache.isInGuild(master));
        assertTrue(lastEventIs(GuildCreatedResponseEvent.class));
    }

    @Test
    void createRejectsBadNamesDuplicatesAndExistingMembers() {
        GuildCache.handleCreateRequest(new GuildCreateRequestEvent(master, "ab"));
        GuildCache.handleCreateRequest(new GuildCreateRequestEvent(master, "x".repeat(31)));
        assertEquals(2, errorTargets.size());
        assertFalse(GuildCache.isInGuild(master));

        GuildCache.handleCreateRequest(new GuildCreateRequestEvent(master, "Testers"));
        GuildCache.handleCreateRequest(new GuildCreateRequestEvent(master, "Another"));
        GuildCache.handleCreateRequest(new GuildCreateRequestEvent(outsider, "testers"));
        assertEquals(4, errorTargets.size());
        assertEquals(1, memory.guilds.size());
    }

    @Test
    void inviteRequiresPermissionAndFreeInvitee() {
        createGuild();
        GuildCache.handleInviteRequest(new GuildInviteRequestEvent(member, outsider));
        assertEquals(List.of(member), errorTargets);
        assertFalse(GuildCache.hasPendingInvite(outsider));

        GuildCache.handleInviteRequest(new GuildInviteRequestEvent(officer, member));
        assertEquals(2, errorTargets.size());

        GuildCache.handleInviteRequest(new GuildInviteRequestEvent(officer, outsider));
        assertTrue(GuildCache.hasPendingInvite(outsider));
        assertTrue(lastEventIs(GuildInviteSentResponseEvent.class));

        GuildCache.handleInviteRequest(new GuildInviteRequestEvent(master, outsider));
        assertEquals(3, errorTargets.size());
    }

    @Test
    void acceptingInviteAddsMemberAndClearsInvite() {
        GuildData guild = createGuild();
        GuildCache.handleInviteRequest(new GuildInviteRequestEvent(master, outsider));
        GuildCache.handleAcceptInvite(new GuildAcceptInviteRequestEvent(outsider, master));

        assertEquals("Member", guild.getMember(outsider).getRankName());
        assertEquals(guild.getGuildId(), memory.getPlayerGuildId(outsider));
        assertFalse(GuildCache.hasPendingInvite(outsider));
        assertTrue(lastEventIs(GuildMemberJoinedResponseEvent.class));
    }

    @Test
    void acceptingFromWrongInviterOrWithoutInviteFails() {
        GuildData guild = createGuild();
        GuildCache.handleAcceptInvite(new GuildAcceptInviteRequestEvent(outsider, master));
        GuildCache.handleInviteRequest(new GuildInviteRequestEvent(master, outsider));
        GuildCache.handleAcceptInvite(new GuildAcceptInviteRequestEvent(outsider, officer));
        assertEquals(List.of(outsider, outsider), errorTargets);
        assertNull(guild.getMember(outsider));
    }

    @Test
    void joinRequiresOpenGuildWithRoom() {
        GuildData guild = createGuild();
        GuildCache.handleJoinRequest(new GuildJoinRequestEvent(outsider, "Testers"));
        GuildCache.handleJoinRequest(new GuildJoinRequestEvent(outsider, "Missing"));
        assertEquals(2, errorTargets.size());

        guild.setOpenJoin(true);
        GuildCache.handleJoinRequest(new GuildJoinRequestEvent(outsider, "Testers"));
        assertNotNull(guild.getMember(outsider));
        assertTrue(GuildCache.isInGuild(outsider));
    }

    @Test
    void leaveRemovesMemberButMasterCannotLeave() {
        GuildData guild = createGuild();
        GuildCache.handleLeaveRequest(new GuildLeaveRequestEvent(master));
        assertEquals(List.of(master), errorTargets);
        assertTrue(GuildCache.isInGuild(master));

        GuildCache.handleLeaveRequest(new GuildLeaveRequestEvent(member));
        assertNull(guild.getMember(member));
        assertFalse(GuildCache.isInGuild(member));

        GuildCache.handleLeaveRequest(new GuildLeaveRequestEvent(outsider));
        assertEquals(List.of(master, outsider), errorTargets);
    }

    @Test
    void kickEnforcesPermissionAndRankHierarchy() {
        GuildData guild = createGuild();
        GuildCache.handleKickRequest(new GuildKickRequestEvent(member, officer, ""));
        GuildCache.handleKickRequest(new GuildKickRequestEvent(officer, master, ""));
        GuildCache.handleKickRequest(new GuildKickRequestEvent(officer, outsider, ""));
        assertEquals(3, errorTargets.size());
        assertNotNull(guild.getMember(officer));

        GuildCache.handleKickRequest(new GuildKickRequestEvent(officer, member, "afk"));
        assertNull(guild.getMember(member));
        assertFalse(GuildCache.isInGuild(member));
        assertTrue(lastEventIs(GuildMemberKickedResponseEvent.class));

        GuildCache.handleKickRequest(new GuildKickRequestEvent(master, officer, ""));
        assertNull(guild.getMember(officer));
    }

    @Test
    void promoteMovesMemberToOfficerButNotBeyond() {
        GuildData guild = createGuild();
        GuildCache.handlePromoteRequest(new GuildPromoteRequestEvent(officer, member));
        assertEquals(List.of(officer), errorTargets);

        GuildCache.handlePromoteRequest(new GuildPromoteRequestEvent(master, member));
        assertEquals("Officer", guild.getMember(member).getRankName());
        assertTrue(lastEventIs(GuildRankChangedResponseEvent.class));

        GuildCache.handlePromoteRequest(new GuildPromoteRequestEvent(master, member));
        assertEquals("Officer", guild.getMember(member).getRankName());
        assertEquals(2, errorTargets.size());
    }

    @Test
    void demoteMovesOfficerDownAndStopsAtLowestRank() {
        GuildData guild = createGuild();
        GuildCache.handleDemoteRequest(new GuildDemoteRequestEvent(master, officer));
        assertEquals("Member", guild.getMember(officer).getRankName());

        GuildCache.handleDemoteRequest(new GuildDemoteRequestEvent(master, officer));
        assertEquals("Member", guild.getMember(officer).getRankName());
        assertEquals(List.of(master), errorTargets);
    }

    @Test
    void transferSwapsMasterAndOfficerRanks() {
        GuildData guild = createGuild();
        GuildCache.handleTransferRequest(new GuildTransferRequestEvent(officer, master));
        assertEquals(List.of(officer), errorTargets);

        GuildCache.handleTransferRequest(new GuildTransferRequestEvent(master, member));
        assertEquals(member, guild.getMasterUuid());
        assertEquals("Officer", guild.getMember(master).getRankName());
    }

    @Test
    void onlyMasterCanDisbandAndStoreIsCleaned() {
        GuildData guild = createGuild();
        GuildCache.handleDisbandRequest(new GuildDisbandRequestEvent(officer));
        assertEquals(List.of(officer), errorTargets);
        assertTrue(memory.guilds.containsKey(guild.getGuildId()));

        GuildCache.handleDisbandRequest(new GuildDisbandRequestEvent(master));
        assertFalse(memory.guilds.containsKey(guild.getGuildId()));
        assertTrue(lastEventIs(GuildDisbandedResponseEvent.class));
    }

    @Test
    void officerChatNeedsPermission() {
        createGuild();
        GuildCache.handleChatRequest(new GuildChatRequestEvent(member, "hi", true));
        assertEquals(List.of(member), errorTargets);
        assertTrue(events.isEmpty());

        GuildCache.handleChatRequest(new GuildChatRequestEvent(officer, "hi", true));
        assertTrue(lastEventIs(GuildChatResponseEvent.class));
    }

    @Test
    void mutedMemberCannotChat() {
        GuildData guild = createGuild();
        guild.getMember(member).setMutedUntil(System.currentTimeMillis() + 60_000);
        GuildCache.handleChatRequest(new GuildChatRequestEvent(member, "hi", false));
        assertEquals(List.of(member), errorTargets);
        assertTrue(events.isEmpty());
    }

    @Test
    void guildWideMuteBlocksMembersButNotOfficers() {
        GuildData guild = createGuild();
        guild.setEveryoneMuted(true);
        guild.setEveryoneMutedExpiry(System.currentTimeMillis() + 60_000);

        GuildCache.handleChatRequest(new GuildChatRequestEvent(member, "hi", false));
        assertEquals(List.of(member), errorTargets);

        GuildCache.handleChatRequest(new GuildChatRequestEvent(officer, "hi", false));
        assertTrue(lastEventIs(GuildChatResponseEvent.class));
    }

    @Test
    void expiredGuildWideMuteNoLongerApplies() {
        GuildData guild = createGuild();
        guild.setEveryoneMuted(true);
        guild.setEveryoneMutedExpiry(System.currentTimeMillis() - 1);
        GuildCache.handleChatRequest(new GuildChatRequestEvent(member, "hi", false));
        assertTrue(errorTargets.isEmpty());
        assertTrue(lastEventIs(GuildChatResponseEvent.class));
    }

    @Test
    void slowModeBlocksSecondMessageFromSameSender() {
        GuildData guild = createGuild();
        guild.setSlowChat(true);
        GuildCache.handleChatRequest(new GuildChatRequestEvent(member, "one", false));
        GuildCache.handleChatRequest(new GuildChatRequestEvent(member, "two", false));
        assertEquals(List.of(member), errorTargets);
        assertEquals(1, events.size());

        GuildCache.handleChatRequest(new GuildChatRequestEvent(officer, "three", false));
        assertEquals(2, events.size());
    }

    @Test
    void progressAccumulatesGexpDailyStatsAndLevel() {
        GuildData guild = createGuild();
        GuildCache.handleProgressRequest(new GuildProgressRequestEvent(member, 100_000, true));

        assertEquals(100_000, guild.getTotalGexp());
        assertEquals(100_000, guild.getDailyGexp());
        assertEquals(1, guild.getDailyWins());
        assertEquals(100_000, guild.getMember(member).getWeeklyGexp());
        assertEquals(100_000, guild.getMember(member).getTotalGexp());
        assertEquals(1, guild.getLevel());
    }

    @Test
    void progressIgnoresNonPositiveGexpAndUnguildedPlayers() {
        GuildData guild = createGuild();
        GuildCache.handleProgressRequest(new GuildProgressRequestEvent(member, 0, false));
        GuildCache.handleProgressRequest(new GuildProgressRequestEvent(outsider, 500, true));
        assertEquals(0, guild.getTotalGexp());
        assertEquals(0, guild.getDailyWins());
    }
}
