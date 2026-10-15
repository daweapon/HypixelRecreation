package dev.daweapon.commons.guild.events.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.guild.GuildResponseEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system response event: confirms guild ownership was transferred to a new guild master.
public class GuildTransferredResponseEvent extends GuildResponseEvent {
    private UUID oldOwner;
    private UUID newOwner;

    public GuildTransferredResponseEvent(GuildData guild, UUID oldOwner, UUID newOwner) {
        super(guild);
        this.oldOwner = oldOwner;
        this.newOwner = newOwner;
    }

}
