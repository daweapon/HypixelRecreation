package dev.daweapon.commons.guild.events.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.guild.GuildResponseEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system response event: confirms a member's rank was changed from one rank to another.
public class GuildRankChangedResponseEvent extends GuildResponseEvent {
    private UUID changer;
    private UUID target;
    private String fromRank;
    private String toRank;

    public GuildRankChangedResponseEvent(GuildData guild, UUID changer, UUID target, String fromRank, String toRank) {
        super(guild);
        this.changer = changer;
        this.target = target;
        this.fromRank = fromRank;
        this.toRank = toRank;
    }

}
