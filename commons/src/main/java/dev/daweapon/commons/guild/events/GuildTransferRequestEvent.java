package dev.daweapon.commons.guild.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import dev.daweapon.commons.guild.GuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
// Guild-system event: transfers guild ownership from the current guild master to another member.
public class GuildTransferRequestEvent extends GuildEvent {
    @NotNull
    private UUID currentOwner;
    @NotNull
    private UUID newOwner;

    public GuildTransferRequestEvent(@NotNull UUID currentOwner, @NotNull UUID newOwner) {
        super(null);
        this.currentOwner = currentOwner;
        this.newOwner = newOwner;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(currentOwner, newOwner);
    }

}
