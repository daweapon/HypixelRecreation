package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.type.generic.data.datapoints.DatapointDouble;
import dev.daweapon.type.skyblockgeneric.economy.CoinFlow;

public class DatapointCoins extends DatapointDouble {
    public DatapointCoins(String key, Double value) {
        super(key, value);
    }

    public DatapointCoins(String key) {
        super(key);
    }

    @Override
    public void setValue(Double value) {
        Double previous = getValue();
        super.setValue(value);
        if (dataHandler != null && previous != null && value != null) CoinFlow.recordChange(value - previous);
    }
}
