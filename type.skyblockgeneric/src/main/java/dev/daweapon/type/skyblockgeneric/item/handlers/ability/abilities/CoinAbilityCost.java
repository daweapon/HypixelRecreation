package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import lombok.NonNull;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

public final class CoinAbilityCost extends RegisteredAbility.AbilityCost {
    private final long coins;

    public CoinAbilityCost(long coins) {
        this.coins = coins;
    }

    @Override
    public boolean canUse(@NonNull SkyBlockPlayer player) {
        return player.getCoins() >= coins;
    }

    @Override
    public void onUse(@NonNull SkyBlockPlayer player, @NonNull RegisteredAbility ability) {
        player.removeCoins(coins);
    }

    @Override
    public void onFail(@NonNull SkyBlockPlayer player) {
        player.sendMessage("<c>You don't have enough coins to use this ability!");
    }

    @Override
    public @Nullable String getLoreDisplay() {
        return "<8>Coin Cost: <6>" + StringUtility.commaify(coins);
    }
}
