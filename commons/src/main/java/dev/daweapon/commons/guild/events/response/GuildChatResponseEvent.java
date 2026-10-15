package dev.daweapon.commons.guild.events.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.guild.GuildResponseEvent;

import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system response event: broadcasts a chat message to the guild (or officer chat).
public class GuildChatResponseEvent extends GuildResponseEvent {
    private UUID sender;
    private String message;
    private boolean officerChat;

    public GuildChatResponseEvent(GuildData guild, UUID sender, String message, boolean officerChat) {
        super(guild);
        this.sender = sender;
        this.message = message;
        this.officerChat = officerChat;
    }

}
