package dev.daweapon.service.api.endpoints;

import dev.daweapon.commons.protocol.objects.api.APIAuthenticateCodeProtocol;
import dev.daweapon.service.api.APIAdminDatabase;
import dev.daweapon.service.api.APIAdminDatabaseObject;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.jetbrains.annotations.Nullable;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that links an admin-panel auth code to the authenticating player, completing panel login.
public class EndpointAuthenticateCode implements RedisMessageHandler<
        APIAuthenticateCodeProtocol.AuthenticateCodeMessage,
        APIAuthenticateCodeProtocol.AuthenticateCodeResponse> {

    @Override
    public APIAuthenticateCodeProtocol protocol() {
        return new APIAuthenticateCodeProtocol();
    }

    @Override
    public APIAuthenticateCodeProtocol.AuthenticateCodeResponse handle(APIAuthenticateCodeProtocol.AuthenticateCodeMessage messageObject, RedisMessageContext context) {
        @Nullable APIAdminDatabaseObject document = APIAdminDatabase.getFromCode(messageObject.authCode());

        if (document == null) {
            return new APIAuthenticateCodeProtocol.AuthenticateCodeResponse(false, "Authentication failed");
        }

        document.setAuthenticatorName(messageObject.playerName());
        document.setAuthenticatorUUID(messageObject.playerUUID());

        APIAdminDatabase.replaceOrInsert(document);

        return new APIAuthenticateCodeProtocol.AuthenticateCodeResponse(true, null);
    }
}
