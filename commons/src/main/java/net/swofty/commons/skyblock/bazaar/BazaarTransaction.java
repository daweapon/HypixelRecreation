package net.swofty.commons.skyblock.bazaar;

import org.json.JSONObject;

// Common JSON (de)serialization contract for bazaar transaction log entries.
public interface BazaarTransaction {
    JSONObject toJSON();
    BazaarTransaction fromJSON(JSONObject json);
}
