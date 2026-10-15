package dev.daweapon.commons.guild.events.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.guild.GuildResponseEvent;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system response event: notifies the guild and the invitee that an invite was sent.
public class GuildInviteSentResponseEvent extends GuildResponseEvent {
    private UUID inviter;
    private UUID invitee;

    public GuildInviteSentResponseEvent(GuildData guild, UUID inviter, UUID invitee) {
        super(guild);
        this.inviter = inviter;
        this.invitee = invitee;
    }

    @Override
    public List<UUID> getParticipants() {
        List<UUID> participants = new java.util.ArrayList<>(getGuild().getAllMemberUuids());
        if (!participants.contains(invitee)) participants.add(invitee);
        return participants;
    }

}
