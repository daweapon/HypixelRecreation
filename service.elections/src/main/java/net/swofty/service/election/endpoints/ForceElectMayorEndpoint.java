package net.swofty.service.election.endpoints;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.election.ForceElectMayorProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.service.election.ElectionDatabase;
import org.tinylog.Logger;

// Redis endpoint backing /adminbypass skiptospecialmayor - unconditionally overwrites the stored
// election data, bypassing the normal open-election guard that StartElectionEndpoint has.
public class ForceElectMayorEndpoint implements RedisMessageHandler
        <ForceElectMayorProtocol.ForceElectMayorMessage,
                ForceElectMayorProtocol.ForceElectMayorResponse> {

    @Override
    public RedisProtocol<ForceElectMayorProtocol.ForceElectMayorMessage,
            ForceElectMayorProtocol.ForceElectMayorResponse> protocol() {
        return new ForceElectMayorProtocol();
    }

    @Override
    public ForceElectMayorProtocol.ForceElectMayorResponse handle(
            ForceElectMayorProtocol.ForceElectMayorMessage messageObject, RedisMessageContext context) {
        try {
            ElectionDatabase.saveElectionData(messageObject.serializedData());
            return new ForceElectMayorProtocol.ForceElectMayorResponse(true, messageObject.serializedData(), null);
        } catch (Exception e) {
            Logger.error(e, "Failed to force-elect mayor");
            return new ForceElectMayorProtocol.ForceElectMayorResponse(false, null, e.getMessage());
        }
    }
}
