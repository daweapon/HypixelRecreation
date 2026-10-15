package dev.daweapon.commons.guild.events.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.guild.GuildResponseEvent;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system response event: notifies inviter and invitee that a pending guild invite expired.
public class GuildInviteExpiredResponseEvent extends GuildResponseEvent {
    private UUID inviter;
    private UUID invitee;

    public GuildInviteExpiredResponseEvent(GuildData guild, UUID inviter, UUID invitee) {
        super(guild);
        this.inviter = inviter;
        this.invitee = invitee;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(inviter, invitee);
    }

}
