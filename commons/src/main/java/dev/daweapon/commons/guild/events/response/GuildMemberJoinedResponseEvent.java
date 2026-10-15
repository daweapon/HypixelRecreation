package dev.daweapon.commons.guild.events.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.guild.GuildResponseEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system response event: notifies the guild that a new member joined.
public class GuildMemberJoinedResponseEvent extends GuildResponseEvent {
    private UUID joiner;

    public GuildMemberJoinedResponseEvent(GuildData guild, UUID joiner) {
        super(guild);
        this.joiner = joiner;
    }

}
