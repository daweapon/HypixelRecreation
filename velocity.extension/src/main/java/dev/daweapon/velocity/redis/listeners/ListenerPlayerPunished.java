package dev.daweapon.velocity.redis.listeners;

import com.google.gson.Gson;
import io.sentry.Sentry;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.to.PunishPlayerProtocol;
import dev.daweapon.commons.punishment.PunishmentReason;
import dev.daweapon.commons.punishment.PunishmentTag;
import dev.daweapon.commons.punishment.ActivePunishment;
import dev.daweapon.commons.punishment.PunishmentMessages;
import dev.daweapon.commons.punishment.PunishmentType;
import dev.daweapon.commons.punishment.template.BanType;
import dev.daweapon.commons.punishment.template.MuteType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.velocity.SkyBlockVelocity;
import dev.daweapon.velocity.text.ProxyText;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;

import java.util.List;
import java.util.UUID;

// Redis handler that applies a punishment (ban/mute/warning) to an online player, disconnecting or messaging them as appropriate.
public class ListenerPlayerPunished implements RedisMessageHandler<
        PunishPlayerProtocol.Request,
        PunishPlayerProtocol.Response> {

    @Override
    public RedisProtocol<PunishPlayerProtocol.Request, PunishPlayerProtocol.Response> protocol() {
        return new PunishPlayerProtocol();
    }

    @Override
    public PunishPlayerProtocol.Response handle(PunishPlayerProtocol.Request message, RedisMessageContext context) {
        UUID target = UUID.fromString(message.target());
        String type = message.type();
        String id = message.id();
        long expiresAt = message.expiresAt();

        PunishmentReason reason;
        try {
            String banString = message.reasonBan();
            String muteString = message.reasonMute();

            if (banString != null) {
                reason = new PunishmentReason(BanType.valueOf(banString));
            } else if (muteString != null) {
                reason = new PunishmentReason(MuteType.valueOf(muteString));
            } else {
                throw new IllegalArgumentException("Missing reason ban or reason_mute");
            }
        } catch (IllegalArgumentException e) {
            Logger.error("Failed to parse punishment reason from message: " + message, e);
            Sentry.captureException(e);
            return new PunishPlayerProtocol.Response();
        }

        List<PunishmentTag> tags = List.of();
        if (message.tags() != null) {
            try {
                tags = List.of(new Gson().fromJson(message.tags(), PunishmentTag[].class));
            } catch (Exception ignored) {
            }
        }

        PunishmentType punishmentType = PunishmentType.valueOf(type);
        PunishmentReason finalReason = reason;
        List<PunishmentTag> finalTags = tags;
        SkyBlockVelocity.getServer().getPlayer(target).ifPresent((player) -> {
            ActivePunishment activePunishment = new ActivePunishment(type, id, finalReason, expiresAt, finalTags);
            switch (punishmentType) {
                case BAN -> ProxyText.disconnect(player, PunishmentMessages.banMessage(activePunishment));
                case MUTE -> player.sendMessage(PunishmentMessages.muteMessage(activePunishment));
                case WARNING -> player.sendMessage(Text.of(
                        "<c>[WARNING] <7>You have received a warning for the following reason: <e>{}",
                        finalReason.getReasonString()));
                default -> {}
            }
        });

        return new PunishPlayerProtocol.Response();
    }
}
