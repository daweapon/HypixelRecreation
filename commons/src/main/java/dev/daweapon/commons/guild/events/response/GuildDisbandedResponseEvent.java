package dev.daweapon.commons.guild.events.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.guild.GuildResponseEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system response event: notifies members that the guild was disbanded.
public class GuildDisbandedResponseEvent extends GuildResponseEvent {
    private UUID disbander;

    public GuildDisbandedResponseEvent(GuildData guild, UUID disbander) {
        super(guild);
        this.disbander = disbander;
    }

}
